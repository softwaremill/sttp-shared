package sttp.capabilities.fs2

import cats.MonadThrow
import fs2.Pull
import fs2.Stream
import sttp.capabilities.StreamMaxLengthExceededException
import sttp.capabilities.Streams

trait Fs2Streams[F[_]] extends Streams[Fs2Streams[F]] {
  override type BinaryStream = Stream[F, Byte]
  override type Pipe[A, B] = fs2.Pipe[F, A, B]
}

object Fs2Streams {
  def apply[F[_]]: Fs2Streams[F] = new Fs2Streams[F] {}

  def limitBytes[F[_]](stream: Stream[F, Byte], maxBytes: Long)(implicit mErr: MonadThrow[F]): Stream[F, Byte] = {
    def go(s: Stream[F, Byte], remaining: Long): Pull[F, Byte, Unit] = {
      if (remaining < 0) Pull.raiseError(new StreamMaxLengthExceededException(maxBytes))
      else
        s.pull.uncons.flatMap {
          case Some((chunk, tail)) =>
            val chunkSize = chunk.size.toLong
            if (chunkSize <= remaining)
              Pull.output(chunk) >> go(tail, remaining - chunkSize)
            else
              Pull.raiseError(new StreamMaxLengthExceededException(maxBytes))
          case None => Pull.done
        }
    }
    go(stream, maxBytes).stream
  }
}
