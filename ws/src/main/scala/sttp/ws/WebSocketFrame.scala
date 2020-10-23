package sttp.ws

sealed trait WebSocketFrame

object WebSocketFrame {
  sealed trait Data[T] extends WebSocketFrame {
    def payload: T
    def finalFragment: Boolean
    def rsv: Option[Int]
  }

  sealed trait Control extends WebSocketFrame

  /** A text frame with fragmentation or extension bits.
    *
    * @param payload a text fragment.
    * @param finalFragment flag indicating whether or not this is the final fragment
    * @param rsv optional extension bits
    */
  case class Text(payload: String, finalFragment: Boolean, rsv: Option[Int]) extends Data[String]

  /** A binary frame with fragmentation or extension bits.
    *
    * @param payload a binary payload
    * @param finalFragment flag indicating whether or not this is the last fragment
    * @param rsv optional extension bits
    */
  case class Binary(payload: Array[Byte], finalFragment: Boolean, rsv: Option[Int]) extends Data[Array[Byte]]

  case class Ping(payload: Array[Byte]) extends Control
  case class Pong(payload: Array[Byte]) extends Control
  case class Close(statusCode: Int, reasonText: String) extends Control

  def text(payload: String): Text = Text(payload, finalFragment = true, rsv = None)
  def binary(payload: Array[Byte]): Binary = Binary(payload, finalFragment = true, rsv = None)
  def ping: Ping = Ping(Array.emptyByteArray)
  def pong: Pong = Pong(Array.emptyByteArray)
  def close: Close = Close(1000, "normal closure")
}
