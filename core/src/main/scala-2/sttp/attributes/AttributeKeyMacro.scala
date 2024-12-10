package sttp.attributes

import scala.reflect.macros.blackbox

private[attributes] object AttributeKeyMacro {
  def apply[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[AttributeKey[T]] = {
    import c.universe._
    c.Expr[AttributeKey[T]](
      q"new _root_.sttp.attributes.AttributeKey(${c.universe.show(implicitly[c.WeakTypeTag[T]].tpe)})"
    )
  }
}
