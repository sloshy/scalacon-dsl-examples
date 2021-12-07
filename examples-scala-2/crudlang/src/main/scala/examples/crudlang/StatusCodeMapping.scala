package examples.crudlang

import org.http4s.Status

/** Small type class for getting a status code related to some model, like an ADT. */
trait StatusCodeMapping[A] {
  def getStatusCode(a: A): Status
}
