package sttp.capabilities.monix

import monix.reactive.Observable
import sttp.capabilities.Streams

trait MonixStreams extends Streams[MonixStreams] {
  override type BinaryStream = Observable[Array[Byte]]
  override type Pipe[A, B] = Observable[A] => Observable[B]
}
object MonixStreams extends MonixStreams
