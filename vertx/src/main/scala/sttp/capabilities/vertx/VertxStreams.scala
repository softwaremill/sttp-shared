package sttp.capabilities.vertx

import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import sttp.capabilities.Streams

trait VertxStreams extends Streams[VertxStreams] {
  override type BinaryStream = ReadStream[Buffer]
  override type Pipe[A, B] = ReadStream[A] => ReadStream[B]
}

object VertxStreams extends VertxStreams
