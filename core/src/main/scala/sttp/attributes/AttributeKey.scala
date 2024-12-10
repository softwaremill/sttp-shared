package sttp.attributes

/** A key to be used in an [[AttributeMap]]. The key typically stores the full type name of the value of the attribute.
  * Keys can be conveniently created using the `AttributeKey[T]` macro in the companion object.
  *
  * @param typeName
  *   The fully qualified name of `T`.
  * @tparam T
  *   Type of the value of the attribute.
  */
class AttributeKey[T](val typeName: String) {
  override def equals(other: Any): Boolean = other match {
    case that: AttributeKey[_] => typeName == that.typeName
    case _                     => false
  }

  override def hashCode(): Int = typeName.hashCode
}

object AttributeKey extends AttributeKeyMacros
