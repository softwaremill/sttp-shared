package sttp.attributes

trait AttributeKeyMacros {

  /** Create an attribute key which contains the full type name of the given type, as inferred by the macro. */
  def apply[T]: AttributeKey[T] = macro AttributeKeyMacro[T]
}
