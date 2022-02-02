package sttp.capabilities.armeria

import com.linecorp.armeria.common.HttpData
import org.reactivestreams.{Processor, Publisher}
import sttp.capabilities.Streams

trait ArmeriaStreams extends Streams[ArmeriaStreams] {
  override type BinaryStream = Publisher[HttpData]
  override type Pipe[A, B] = Processor[A, B]
}

object ArmeriaStreams extends ArmeriaStreams
