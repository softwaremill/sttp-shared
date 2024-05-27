package sttp

package object shared {

  /** The `Identity` type constructor can be used where an "effect" or wrapper (usually called `F[_]`) is expected. It
    * represents direct-style / synchronous programming, and allows passing in code written in this style.
    */
  type Identity[X] = X
}
