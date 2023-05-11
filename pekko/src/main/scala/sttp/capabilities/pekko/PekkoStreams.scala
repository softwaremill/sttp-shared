package sttp.capabilities.pekko

import org.apache.pekko.stream.scaladsl.{Flow, Source}
import org.apache.pekko.util.ByteString
import sttp.capabilities.Streams

trait PekkoStreams extends Streams[PekkoStreams] {
  override type BinaryStream = Source[ByteString, Any]
  override type Pipe[A, B] = Flow[A, B, Any]
}
object PekkoStreams extends PekkoStreams
