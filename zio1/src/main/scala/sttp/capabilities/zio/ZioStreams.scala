package sttp.capabilities.zio

import sttp.capabilities.Streams
import zio.stream.Stream

trait ZioStreams extends Streams[ZioStreams] {
  override type BinaryStream = Stream[Throwable, Byte]
  override type Pipe[A, B] = Stream[Throwable, A] => Stream[Throwable, B]
}
object ZioStreams extends ZioStreams
