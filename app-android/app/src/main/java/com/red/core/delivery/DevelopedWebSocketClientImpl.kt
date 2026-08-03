package com.red.core.delivery

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
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
 */
@Singleton
class DevelopedWebSocketClientImpl @Inject constructor(
  private val identity: ClientIdentity
) : DevelopedWebSocketClient {

  private var webSocket: WebSocket? = null
  private var listener: DevelopedWebSocketClient.Listener? = null
  private val running = AtomicBoolean(false)
  private var attempt = 0

  private val moshi: Moshi = Moshi.Builder().build()
  private val frameAdapter: JsonAdapter<ChatFrame> = moshi.adapter(ChatFrame::class.java)
  private val ackAdapter: JsonAdapter<MessageAck> = moshi.adapter(MessageAck::class.java)

  private val client: OkHttpClient = OkHttpClient.Builder()
    .readTimeout(0, TimeUnit.MILLISECONDS)
    .pingInterval(20, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .build()

  var wsUrl: String = "ws://192.168.1.50:8080"

  override fun connect() {
    if (!running.compareAndSet(false, true)) return
    openSocket()
  }

  private fun openSocket() {
    val request = Request.Builder()
      .url("$wsUrl/ws/chat?token=${identity.token}")
      .build()
    webSocket = client.newWebSocket(request, object : WebSocketListener() {
      override fun onOpen(webSocket: WebSocket, response: Response) {
        attempt = 0
      }

      override fun onMessage(webSocket: WebSocket, text: String) {
        runCatching { ackAdapter.fromJson(text) }
          .getOrNull()
          ?.let { /* bridge to manager via listener */ }
        listener?.onFrame(text)
      }

      override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        onMessage(webSocket, bytes.utf8())
      }

      override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
      }

      override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        scheduleReconnect()
      }

      override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
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

  override fun setListener(listener: DevelopedWebSocketClient.Listener) {
    this.listener = listener
  }

  override fun close() {
    running.set(false)
    webSocket?.close(1000, "client closed")
  }

  companion object {
    private const val BASE_BACKOFF_MS = 500L
    private const val MAX_BACKOFF_MS = 30_000L
    private const val MAX_BACKOFF_ATTEMPTS = 10
  }
}
