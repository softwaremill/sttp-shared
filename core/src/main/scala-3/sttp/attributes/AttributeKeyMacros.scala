package sttp.attributes

import scala.quoted.*

trait AttributeKeyMacros {

  /** Create an attribute key which contains the full type name of the given type, as inferred by the macro. */
  inline def apply[T]: AttributeKey[T] = ${ AttributeKeyMacros[T] }
}

private[attributes] object AttributeKeyMacros {
  def apply[T: Type](using q: Quotes): Expr[AttributeKey[T]] = {
    import quotes.reflect.*
    val t = TypeRepr.of[T]

    '{ new AttributeKey[T](${ Expr(t.show) }) }
  }
}
