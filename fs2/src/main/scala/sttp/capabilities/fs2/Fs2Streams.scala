package sttp.capabilities.fs2

import fs2.Stream
import sttp.capabilities.Streams
import cats.MonadThrow
import fs2.Pull
import sttp.capabilities.StreamMaxLengthExceeded

trait Fs2Streams[F[_]] extends Streams[Fs2Streams[F]] {
  override type BinaryStream = Stream[F, Byte]
  override type Pipe[A, B] = fs2.Pipe[F, A, B]

  def limitBytes(stream: Stream[F, Byte], maxBytes: Long)(implicit mErr: MonadThrow[F]): Stream[F, Byte] = {
    def go(s: Stream[F, Byte], remaining: Long): Pull[F, Byte, Unit] = {
      if (remaining < 0) Pull.raiseError(new StreamMaxLengthExceeded(maxBytes))
      else
        s.pull.uncons.flatMap {
          case Some((chunk, tail)) =>
            val chunkSize = chunk.size.toLong
            if (chunkSize <= remaining)
              Pull.output(chunk) >> go(tail, remaining - chunkSize)
            else
              Pull.raiseError(new StreamMaxLengthExceeded(maxBytes))
          case None => Pull.done
        }
    }
    go(stream, maxBytes).stream
  }
}

object Fs2Streams {
  def apply[F[_]]: Fs2Streams[F] = new Fs2Streams[F] {}
}
