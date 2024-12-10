package sttp.attributes

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class AttributeKeyTest extends AnyFlatSpec with Matchers {
  it should "create an attribute key" in {
    val key = AttributeKey[SomeAttribute]
    key.typeName shouldBe "sttp.attributes.SomeAttribute"
  }
}

case class SomeAttribute(v: String)
