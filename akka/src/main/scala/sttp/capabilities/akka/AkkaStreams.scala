package sttp.capabilities.akka

import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.util.ByteString
import sttp.capabilities.StreamMaxLengthExceededException
import sttp.capabilities.Streams

trait AkkaStreams extends Streams[AkkaStreams] {
  override type BinaryStream = Source[ByteString, Any]
  override type Pipe[A, B] = Flow[A, B, Any]
}

object AkkaStreams extends AkkaStreams {

  def limitBytes(stream: Source[ByteString, Any], maxBytes: Long): Source[ByteString, Any] = {
    stream
      .limitWeighted(maxBytes)(_.length.toLong)
      .mapError { case _: akka.stream.StreamLimitReachedException =>
        StreamMaxLengthExceededException(maxBytes)
      }
  }
}
