package examples.crudlang

trait Keyed[K, V]:
  def getKey(v: V): K

object Keyed:
  def from[K, V](f: V => K): Keyed[K, V] = new:
    def getKey(v: V) = f(v)

extension [V](v: V) def getKey[K](using keyed: Keyed[K, V]) = keyed.getKey(v)
