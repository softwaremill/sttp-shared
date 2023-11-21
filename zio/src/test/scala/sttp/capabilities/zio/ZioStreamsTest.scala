package sttp.capabilities.zio

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.capabilities.StreamMaxLengthExceededException
import zio._
import zio.stream.ZStream

class ZioStreamsTest extends AsyncFlatSpec with Matchers {
  behavior of "ZioStreams"

  implicit val r: Runtime[Any] = Runtime.default

  it should "Pass all bytes if limit is not exceeded" in {
    // given
    val inputByteCount = 8192
    val maxBytes = 8192L
    val inputStream = ZStream.fromIterator(Iterator.fill[Byte](inputByteCount)('5'.toByte))

    // when
    val stream = ZioStreams.limitBytes(inputStream, maxBytes)

    // then
    Unsafe.unsafe(implicit u =>
      r.unsafe.runToFuture(stream.runFold(0L)((acc, _) => acc + 1).map { count =>
        count shouldBe inputByteCount
      })
    )
  }

  it should "Fail stream if limit is exceeded" in {
    // given
    val inputByteCount = 8192
    val maxBytes = 8191L
    val inputStream = ZStream.fromIterator(Iterator.fill[Byte](inputByteCount)('5'.toByte))

    // when
    val stream = ZioStreams.limitBytes(inputStream, maxBytes)

    // then
    Unsafe.unsafe(implicit u =>
      r.unsafe.runToFuture(
        stream.runLast
          .flatMap(_ => ZIO.succeed(fail("Unexpected end of stream")))
          .catchSome {
            case StreamMaxLengthExceededException(limit) =>
              ZIO.succeed(limit shouldBe maxBytes)
            case other =>
              ZIO.succeed(fail(s"Unexpected failure cause: $other"))
          }
      )
    )
  }
}
