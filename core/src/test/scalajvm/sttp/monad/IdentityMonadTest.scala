package sttp.monad

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import sttp.monad.syntax._
import sttp.shared.Identity

class IdentityMonadTest extends AnyFlatSpec with Matchers {
  implicit val m: MonadError[Identity] = IdentityMonad

  it should "map" in {
    m.map(10)(_ + 2) shouldBe 12
  }

  it should "ensure" in {
    var ran = false

    intercept[RuntimeException] {
      m.ensure2(m.error(new RuntimeException("boom!")), { ran = true })
    }

    ran shouldBe true
  }

  it should "ensure using syntax" in {
    var ran = false

    intercept[RuntimeException] {
      m.error[Int](new RuntimeException("boom!")).ensure({ ran = true })
    }

    ran shouldBe true
  }
}
