package sttp.monad

import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FutureMonadTest extends AnyFlatSpec with Matchers {
  implicit val m: MonadError[Future] = new FutureMonad()

  it should "ensure" in {
    val ran = new AtomicBoolean(false)

    intercept[RuntimeException] {
      m.ensure2((throw new RuntimeException("boom!")): Future[Int], Future(ran.set(true))).futureValue
    }

    ran.get shouldBe true
  }
}
