package sttp.capabilities.zio

import sttp.capabilities.Streams
import zio.blocking.Blocking
import zio.stream.{Stream, ZStream}

trait ZioStreams extends Streams[ZioStreams] {
  override type BinaryStream = Stream[Throwable, Byte]
  override type Pipe[A, B] = Stream[Throwable, A] => Stream[Throwable, B]
}
object ZioStreams extends ZioStreams

trait BlockingZioStreams extends Streams[BlockingZioStreams] {
  override type BinaryStream = ZStream[Blocking, Throwable, Byte]
  override type Pipe[A, B] = ZStream[Blocking, Throwable, A] => ZStream[Blocking, Throwable, B]
}
object BlockingZioStreams extends BlockingZioStreams
