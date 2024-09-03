package sttp.capabilities.fs2

import cats.effect.IO
import cats.effect.unsafe
import fs2._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.capabilities.StreamMaxLengthExceededException

class Fs2StreamsTest extends AsyncFlatSpec with Matchers {

  implicit val runtime: unsafe.IORuntime = unsafe.IORuntime(
    executionContext,
    executionContext,
    unsafe.IORuntime.global.scheduler,
    unsafe.IORuntime.global.shutdown,
    unsafe.IORuntime.global.config
  )

  behavior of "Fs2Streams"

  it should "Pass all bytes if limit is not exceeded" in {
    // given
    val inputByteCount = 8192
    val maxBytes = 8192L
    val inputStream = Stream.fromIterator[IO](Iterator.fill[Byte](inputByteCount)('5'.toByte), chunkSize = 1024)

    // when
    val stream = Fs2Streams.limitBytes(inputStream, maxBytes)

    // then
    stream.fold(0L)((acc, _) => acc + 1).compile.lastOrError.unsafeToFuture().map { count =>
      count shouldBe inputByteCount
    }
  }

  it should "Fail stream if limit is exceeded" in {
    // given
    val inputByteCount = 8192
    val maxBytes = 8191L
    val inputStream = Stream.fromIterator[IO](Iterator.fill[Byte](inputByteCount)('5'.toByte), chunkSize = 1024)

    // when
    val stream = Fs2Streams.limitBytes(inputStream, maxBytes)

    // then
    stream.compile.drain
      .map(_ => fail("Unexpected end of stream."))
      .handleErrorWith {
        case StreamMaxLengthExceededException(limit) =>
          IO(limit shouldBe maxBytes)
        case other =>
          IO(fail(s"Unexpected failure cause: $other"))
      }
      .unsafeToFuture()
  }
}
