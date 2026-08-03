package com.red.core.delivery

import android.net.Uri
import android.util.Log
import org.thoughtcrime.securesms.BuildConfig
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * OkHttp WebSocket transport with bounded exponential backoff reconnection.
 *
 * Connects to `${wsUrl}/ws/chat?token=<jwt>` and serializes frames as JSON using [ChatFrame].
 * Supports:
 *  - MESSAGE frames (chat messages)
 *  - TYPING frames (typing indicators)
 *  - READ frames (read receipts)
 *  - ACK frames (server acknowledgments)
 */
@Singleton
class DevelopedWebSocketClientImpl @Inject constructor(
  private val identity: ClientIdentity
) : DevelopedWebSocketClient {

  private var webSocket: WebSocket? = null
  private var listener: DevelopedWebSocketClient.Listener? = null
  private var connectionListener: ((Boolean) -> Unit)? = null
  private val running = AtomicBoolean(false)
  private var attempt = 0

  private val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
  private val frameAdapter: JsonAdapter<ChatFrame> = moshi.adapter(ChatFrame::class.java)
  private val ackAdapter: JsonAdapter<MessageAck> = moshi.adapter(MessageAck::class.java)
  private val typingAdapter: JsonAdapter<TypingFrame> = moshi.adapter(TypingFrame::class.java)
  private val readAdapter: JsonAdapter<ReadFrame> = moshi.adapter(ReadFrame::class.java)

  private val client: OkHttpClient = OkHttpClient.Builder()
    .readTimeout(0, TimeUnit.MILLISECONDS)
    .pingInterval(20, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .build()

  var wsUrl: String = BuildConfig.SIGNAL_URL
    .trimEnd('/')
    .replaceFirst("https://", "wss://")
    .replaceFirst("http://", "ws://")

  override fun connect() {
    if (!running.compareAndSet(false, true)) return
    openSocket()
  }

  private fun openSocket() {
    val request = Request.Builder()
      .url("$wsUrl/ws/chat?token=${Uri.encode(identity.token)}")
      .build()
    webSocket = client.newWebSocket(request, object : WebSocketListener() {
      override fun onOpen(webSocket: WebSocket, response: Response) {
        attempt = 0
        connectionListener?.invoke(true)
        Log.d("RED_WS", "WebSocket connected")
      }

      override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("RED_WS", "Received: ${text.take(100)}")
        listener?.onFrame(text)
      }

      override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        onMessage(webSocket, bytes.utf8())
      }

      override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
      }

      override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        connectionListener?.invoke(false)
        Log.d("RED_WS", "WebSocket closed: $code $reason")
        scheduleReconnect()
      }

      override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        connectionListener?.invoke(false)
        Log.e("RED_WS", "WebSocket failure: ${t.message}")
        scheduleReconnect()
      }
    })
  }

  private fun scheduleReconnect() {
    if (!running.get()) return
    attempt = (attempt + 1).coerceAtMost(MAX_BACKOFF_ATTEMPTS)
    val delayMs = min(BASE_BACKOFF_MS shl attempt, MAX_BACKOFF_MS)
    Thread {
      try {
        Thread.sleep(delayMs)
      } catch (_: InterruptedException) {
        return@Thread
      }
      if (running.get()) openSocket()
    }.apply { isDaemon = true; name = "red-ws-reconnect" }.start()
  }

  override fun send(frame: ChatFrame): Boolean {
    val json = frameAdapter.toJson(frame)
    return webSocket?.send(json) ?: false
  }

  /** Send a typing indicator frame. */
  fun sendTyping(conversationId: String, isTyping: Boolean): Boolean {
    val frame = TypingFrame(
      conversationId = conversationId,
      senderId = identity.userId,
      isTyping = isTyping
    )
    val json = typingAdapter.toJson(frame)
    return webSocket?.send(json) ?: false
  }

  /** Send a read receipt frame. */
  fun sendReadReceipt(conversationId: String, messageIds: List<String>): Boolean {
    val frame = ReadFrame(
      conversationId = conversationId,
      readerId = identity.userId,
      messageIds = messageIds
    )
    val json = readAdapter.toJson(frame)
    return webSocket?.send(json) ?: false
  }

  override fun setListener(listener: DevelopedWebSocketClient.Listener) {
    this.listener = listener
  }

  override fun setConnectionListener(listener: (Boolean) -> Unit) {
    connectionListener = listener
  }

  override fun close() {
    running.set(false)
    connectionListener?.invoke(false)
    webSocket?.close(1000, "client closed")
  }

  companion object {
    private const val BASE_BACKOFF_MS = 500L
    private const val MAX_BACKOFF_MS = 30_000L
    private const val MAX_BACKOFF_ATTEMPTS = 10
  }
}

/** Typing indicator frame. */
@JsonClass(generateAdapter = true)
data class TypingFrame(
  val type: String = "TYPING",
  val conversationId: String,
  val senderId: String,
  val isTyping: Boolean
)

/** Read receipt frame. */
@JsonClass(generateAdapter = true)
data class ReadFrame(
  val type: String = "READ",
  val conversationId: String,
  val readerId: String,
  val messageIds: List<String>
)
