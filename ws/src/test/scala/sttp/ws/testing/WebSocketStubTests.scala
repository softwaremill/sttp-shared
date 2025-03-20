package sttp.ws.testing

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.monad.MonadError
import sttp.ws.{WebSocketClosed, WebSocketFrame}

import scala.util.{Failure, Success}
import sttp.monad.IdentityMonad

class WebSocketStubTests extends AnyFlatSpec with Matchers with ScalaFutures {
  class MyException extends Exception

  "web socket stub" should "return initial Incoming frames on 'receive'" in {
    val frames = List("a", "b", "c").map(WebSocketFrame.text)
    val webSocketStub = WebSocketStub.initialReceive(frames)
    val ws = webSocketStub.build(IdentityMonad)

    ws.receive() shouldBe WebSocketFrame.text("a")
    ws.receive() shouldBe WebSocketFrame.text("b")
    ws.receive() shouldBe WebSocketFrame.text("c")
  }

  it should "return initial responses on 'receive'" in {
    val okFrame = WebSocketFrame.text("abc")
    val exception = new MyException
    val webSocketStub = WebSocketStub.initialReceiveWith(List(Success(okFrame), Failure(exception)))
    val ws = webSocketStub.build(IdentityMonad)

    ws.receive() shouldBe WebSocketFrame.text("abc")
    assertThrows[MyException](ws.receive())
  }

  it should "add next incoming frames as reaction to send" in {
    val firstFrame = WebSocketFrame.text("No. 1")
    val secondFrame = WebSocketFrame.text("Second")
    val thirdFrame = WebSocketFrame.text("3")
    val expectedFrame = WebSocketFrame.text("give me more!")
    val webSocketStub = WebSocketStub
      .initialReceive(List(firstFrame))
      .thenRespond {
        case `expectedFrame` => List(secondFrame, thirdFrame)
        case _               => List.empty
      }
    val ws = webSocketStub.build(IdentityMonad)

    ws.receive() shouldBe WebSocketFrame.text("No. 1")
    assertThrows[IllegalStateException](ws.receive()) // no more stubbed messages
    ws.send(WebSocketFrame.text("not expected"))
    assertThrows[IllegalStateException](ws.receive()) // still, no more stubbed messages
    ws.send(expectedFrame)
    ws.receive() shouldBe WebSocketFrame.text("Second")
    ws.receive() shouldBe WebSocketFrame.text("3")
  }

  it should "add next responses as reaction to send" in {
    val ok = WebSocketFrame.text("ok")
    val exception = new MyException

    val webSocketStub = WebSocketStub.noInitialReceive
      .thenRespondWith(_ => List(Success(ok), Failure(exception)))
    val ws = webSocketStub.build(IdentityMonad)

    ws.send(WebSocketFrame.text("let's add responses"))
    ws.receive() shouldBe WebSocketFrame.text("ok")
    assertThrows[MyException](ws.receive())
  }

  it should "be closed after sending close frame" in {
    val ok = WebSocketFrame.text("ok")
    val closeFrame = WebSocketFrame.Close(500, "internal error")
    val webSocketStub = WebSocketStub.initialReceive(List(closeFrame, ok))
    val ws = webSocketStub.build(IdentityMonad)

    ws.send(WebSocketFrame.text("let's add responses"))
    ws.receive() shouldBe closeFrame
    ws.isOpen() shouldBe false
    assertThrows[WebSocketClosed](ws.receive())
  }

  it should "use state to add next responses" in {
    val webSocketStub = WebSocketStub.noInitialReceive
      .thenRespondS(0) { case (counter, _) =>
        (counter + 1, List(WebSocketFrame.text(s"No. $counter")))
      }
    val ws = webSocketStub.build(IdentityMonad)

    ws.send(WebSocketFrame.text("a"))
    ws.send(WebSocketFrame.text("b"))
    ws.send(WebSocketFrame.text("c"))

    ws.receive() shouldBe WebSocketFrame.text("No. 0")
    ws.receive() shouldBe WebSocketFrame.text("No. 1")
    ws.receive() shouldBe WebSocketFrame.text("No. 2")
  }

  object LazyMonad extends MonadError[() => *] {
    override def unit[T](t: T): () => T = () => t
    override def map[T, T2](fa: () => T)(f: T => T2): () => T2 = () => f(fa())
    override def flatMap[T, T2](fa: () => T)(f: T => () => T2): () => T2 = () => f(fa())()
    override def error[T](t: Throwable): () => T = () => throw t
    override protected def handleWrappedError[T](rt: () => T)(h: PartialFunction[Throwable, () => T]): () => T =
      () =>
        try rt()
        catch {
          case e if h.isDefinedAt(e) => h.apply(e)()
          case e: Exception          => throw e
        }
    override def ensure[T](f: () => T, e: => () => Unit): () => T =
      () =>
        try f()
        finally e()
  }

  "receive" should "be lazy" in {
    // given
    val ws = WebSocketStub.noInitialReceive.thenRespond(_ => List(WebSocketFrame.text("test"))).build(LazyMonad)

    // when
    val doReceive = ws.receive() // should return a lazy receive
    ws.send(WebSocketFrame.text("x"))()

    // then
    doReceive() shouldBe WebSocketFrame.text("test")
  }
}
