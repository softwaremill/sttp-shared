package sttp.capabilities.akka

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.testkit.scaladsl.TestSink
import akka.util.ByteString
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.capabilities.StreamMaxLengthExceededException

import scala.concurrent.Await
import scala.concurrent.duration._

class AkkaStreamsTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  behavior of "AkkaStreams"
  implicit lazy val system: ActorSystem = ActorSystem()

  override def afterAll(): Unit = {
    val _ = Await.result(system.terminate(), 10.seconds)
  }

  it should "Pass all bytes if limit is not exceeded" in {
    // given
    val inputByteCount = 8192
    val maxBytes = 8192L

    val iterator = Iterator.fill[Byte](inputByteCount)('5'.toByte)
    val chunkSize = 256

    val inputStream: Source[ByteString, Any] =
      Source.fromIterator(() => iterator.grouped(chunkSize).map(group => ByteString(group.toArray)))

    // when
    val stream = AkkaStreams.limitBytes(inputStream, maxBytes)

    // then
    stream
      .fold(0L)((acc, bs) => acc + bs.length)
      .runWith(TestSink[Long]())
      .request(1)
      .expectNext(inputByteCount.toLong)
      .expectComplete()
  }

  it should "Fail stream if limit is exceeded" in {
    // given
    val inputByteCount = 8192
    val maxBytes = 8191L

    val iterator = Iterator.fill[Byte](inputByteCount)('5'.toByte)
    val chunkSize = 256

    val inputStream: Source[ByteString, Any] =
      Source.fromIterator(() => iterator.grouped(chunkSize).map(group => ByteString(group.toArray)))

    // when
    val stream = AkkaStreams.limitBytes(inputStream, maxBytes)
    val probe = stream.runWith(TestSink[ByteString]())
    val _ = for (_ <- 1 to 31) yield probe.requestNext()

    // then
    probe.request(1).expectError(StreamMaxLengthExceededException(maxBytes))
  }
}
