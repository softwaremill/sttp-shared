package sttp.monad

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}

import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FutureMonadTest extends AnyFlatSpec with Matchers with ScalaFutures {
  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(10, Seconds))
  implicit val m: MonadError[Future] = new FutureMonad()

  it should "ensure" in {
    val ran = new AtomicBoolean(false)

    val result = m.ensure2((throw new RuntimeException("boom!")): Future[Int], Future(ran.set(true)))
    result.failed.futureValue shouldBe a[RuntimeException]
    ran.get shouldBe true
  }
}
