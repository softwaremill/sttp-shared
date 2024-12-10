package sttp.attributes

/** A type-safe map from [[AttributeKey]] to values of types determined by the key.
  *
  * An attribute is arbitrary data that is attached to some top-level sttp-defined objects. The data is not interpreted
  * by the libraries in any way, and is passed as-is. However, built-in or user-provided extensions and integrations
  * might use the attributes to provide their functionality.
  *
  * Typically, you'll add attributes using an `.attribute` method on the sttp-defined object. Each attribute should
  * correspond to a type. The type should be defined by the extension, which uses the attribute, and make it available
  * for import.
  */
case class AttributeMap(private val storage: Map[String, Any]) {
  def get[T](k: AttributeKey[T]): Option[T] = storage.get(k.typeName).asInstanceOf[Option[T]]
  def put[T](k: AttributeKey[T], v: T): AttributeMap = copy(storage = storage + (k.typeName -> v))
  def remove[T](k: AttributeKey[T]): AttributeMap = copy(storage = storage - k.typeName)

  def isEmpty: Boolean = storage.isEmpty
  def nonEmpty: Boolean = storage.nonEmpty
}

object AttributeMap {
  val Empty: AttributeMap = AttributeMap(Map.empty)
}
