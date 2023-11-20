package sttp.capabilities.zio

import sttp.capabilities.Streams
import zio.stream.{Stream, ZStream}
import zio.stream.ZChannel
import zio.Trace
import zio.Chunk
import scala.util.control.NonFatal
import sttp.capabilities.StreamMaxLengthExceeded

trait ZioStreams extends Streams[ZioStreams] {
  override type BinaryStream = Stream[Throwable, Byte]
  override type Pipe[A, B] = Stream[Throwable, A] => Stream[Throwable, B]
}

object ZioStreams extends ZioStreams {

  def limitBytes(stream: Stream[Throwable, Byte], maxBytes: Long): Stream[Throwable, Byte] =
    scanChunksAccum(stream, initState = 0L) { case (accumulatedBytes, chunk) =>
      val byteCount = accumulatedBytes + chunk.size
      if (byteCount > maxBytes)
        throw new StreamMaxLengthExceeded(maxBytes)
      else
        byteCount
    }

  def scanChunksAccum[S, R, A](inputStream: ZStream[R, Throwable, A], initState: => S)(
      f: (S, Chunk[A]) => S
  )(implicit trace: Trace): ZStream[R, Throwable, A] =
    ZStream.succeed(initState).flatMap { state =>
      def accumulator(currS: S): ZChannel[Any, Throwable, Chunk[A], Any, Throwable, Chunk[A], Unit] =
        ZChannel.readWith(
          (in: Chunk[A]) => {
            try {
              val nextS = f(currS, in)
              ZChannel.write(in) *> accumulator(nextS)
            } catch {
              case NonFatal(err) => ZChannel.fail(err)
            }
          },
          (err: Throwable) => ZChannel.fail(err),
          (_: Any) => ZChannel.unit
        )

      ZStream.fromChannel(inputStream.channel >>> accumulator(state))
    }
}
