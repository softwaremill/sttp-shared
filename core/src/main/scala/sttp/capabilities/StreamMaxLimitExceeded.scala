package sttp.capabilities

case class StreamMaxLengthExceeded(maxBytes: Long) extends Exception {
  override def getMessage(): String = s"Stream length limit of $maxBytes bytes exceeded"
}
