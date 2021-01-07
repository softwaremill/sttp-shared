package sttp.ws

abstract class WebSocketException(msg: String) extends Exception(msg)

/** @param frame The received closing frame, if available.
  */
case class WebSocketClosed(frame: Option[WebSocketFrame.Close]) extends WebSocketException(null)

case class WebSocketBufferFull(capacity: Int) extends WebSocketException(s"Buffered $capacity messages")
