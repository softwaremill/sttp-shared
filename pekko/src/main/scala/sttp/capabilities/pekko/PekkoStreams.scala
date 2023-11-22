package sttp.capabilities.pekko

import org.apache.pekko
import sttp.capabilities.StreamMaxLengthExceededException
import sttp.capabilities.Streams

import pekko.stream.scaladsl.{Flow, Source}
import pekko.util.ByteString

trait PekkoStreams extends Streams[PekkoStreams] {
  override type BinaryStream = Source[ByteString, Any]
  override type Pipe[A, B] = Flow[A, B, Any]
}
object PekkoStreams extends PekkoStreams {
  def limitBytes(stream: Source[ByteString, Any], maxBytes: Long): Source[ByteString, Any] = {
    stream
      .limitWeighted(maxBytes)(_.length.toLong)
      .mapError { 
        case _: pekko.stream.StreamLimitReachedException => StreamMaxLengthExceededException(maxBytes) 
      }
  }
}
