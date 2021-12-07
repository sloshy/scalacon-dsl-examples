package examples.crudlang

trait Keyed[K, V] {
  def getKey(v: V): K
}

object Keyed {
  def from[K, V](f: V => K): Keyed[K, V] = new Keyed[K, V] {
    def getKey(v: V) = f(v)
  }
}
