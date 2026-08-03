package com.red.core.delivery

/** Abstraction over the real-time transport so the delivery manager is testable. */
interface DevelopedWebSocketClient {
  /** Begin the connection (idempotent). */
  fun connect()

  /** Send a raw frame. Returns true if the socket accepted it. */
  fun send(frame: ChatFrame): Boolean

  /** Register a listener for inbound messages and acks. */
  fun setListener(listener: Listener)

  /** Observe transport connectivity without exposing the concrete WebSocket implementation. */
  fun setConnectionListener(listener: (Boolean) -> Unit) {}

  fun close()

  fun interface Listener {
    fun onFrame(frame: String)
  }
}
