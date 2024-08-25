package sttp.capabilities.zio

import sttp.capabilities.StreamMaxLengthExceededException
import zio._
import zio.stream.ZStream
import zio.test._

object ZioStreamsTest extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment, Any] = suite("ZioStreams")(
    test("should Pass all bytes if limit is not exceeded") {
      // given
      val inputByteCount = 8192
      val maxBytes = 8192L
      val inputStream = ZStream.fromIterator(Iterator.fill[Byte](inputByteCount)('5'.toByte))

      // when
      val stream = ZioStreams.limitBytes(inputStream, maxBytes)

      // then
      for {
        count <- stream.runFold(0L)((acc, _) => acc + 1)
      } yield assertTrue(count == inputByteCount)
    },
    test("should Fail stream if limit is exceeded") {
      val inputByteCount = 8192
      val maxBytes = 8191L
      val inputStream = ZStream.fromIterator(Iterator.fill[Byte](inputByteCount)('5'.toByte))

      // when
      val stream = ZioStreams.limitBytes(inputStream, maxBytes)

      // then
      for {
        limit <- stream.runLast.flip
          .flatMap {
            case StreamMaxLengthExceededException(limit) =>
              ZIO.succeed(limit)
            case other =>
              ZIO.fail(s"Unexpected failure cause: $other")
          }
      } yield assertTrue(limit == maxBytes)
    }
  )
}
