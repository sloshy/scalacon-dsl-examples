package examples.crudlang

import cats.effect.Concurrent
import cats.syntax.all._
import CRUDResponse._

trait CRUDStore[F[_], K, V] {

  /** Attempts to create a key/value pair. */
  def create(k: K, v: V): F[Either[ItemDoesNotExist[K, V], ItemCreated[K, V]]]

  /** Tries to update an existing value if it exists. */
  def update(k: K, v: V)(implicit
      keyed: Keyed[K, V]
  ): F[Either[ItemDoesNotExist[K, V], CRUDUpdateResponse[K, V]]]

  /** An 'upsert' type operation that always succeeds. */
  def createOrUpdate(
      k: K,
      v: V
  )(implicit keyed: Keyed[K, V]): F[Either[CRUDUpdateResponse[K, V], ItemCreated[K, V]]]

  /** Deletes a given key if it exists. */
  def delete(k: K): F[Either[ItemDoesNotExist[K, V], ItemDeleted[K, V]]]

  /** Tries to get the value from the data store, if it exists. */
  def read(k: K): F[Either[ItemDoesNotExist[K, V], ItemRead[K, V]]]
}

object CRUDStore {
  def inMemory[F[_]: Concurrent, K, V] = Concurrent[F].ref(Map.empty[K, V]).map { ref =>
    new CRUDStore[F, K, V] {
      def create(k: K, v: V): F[Either[ItemDoesNotExist[K, V], ItemCreated[K, V]]] =
        ref.modify { map =>
          map.get(k) match {
            case Some(_) => map -> Left(ItemDoesNotExist(k))
            case None    => (map + (k -> v)) -> Right(ItemCreated(k, v))
          }
        }
      def update(
          k: K,
          v: V
      )(implicit
          keyed: Keyed[K, V]
      ): F[Either[ItemDoesNotExist[K, V], CRUDUpdateResponse[K, V]]] =
        ref.modify { map =>
          map.get(k) match {
            case Some(_) =>
              val newKey = v.getKey[K]
              if (k != v.getKey[K]) map -> Right(CRUDUpdateResponse.ItemKeyCannotChange(k, newKey))
              else (map + (k -> v)) -> Right(CRUDUpdateResponse.ItemUpdated(k, v))
            case None => map -> Left(ItemDoesNotExist(k))
          }
        }
      def createOrUpdate(k: K, v: V)(implicit
          keyed: Keyed[K, V]
      ): F[Either[CRUDUpdateResponse[K, V], ItemCreated[K, V]]] =
        ref.modify { map =>
          //false: was updated in-place. true: was created.
          val newMap = map + (k -> v)
          map.get(k) match {
            case Some(_) =>
              val newKey = v.getKey[K]
              if (k != v.getKey[K]) map -> Left(CRUDUpdateResponse.ItemKeyCannotChange(k, newKey))
              else newMap -> Left(CRUDUpdateResponse.ItemUpdated(k, v))
            case None => newMap -> Right(ItemCreated(k, v))
          }
        }
      def delete(k: K): F[Either[ItemDoesNotExist[K, V], ItemDeleted[K, V]]] = ref.modify { map =>
        //true: was deleted successfully. false: item does not exist and cannot be deleted
        map.get(k) match {
          case Some(_) => (map - k) -> Right(ItemDeleted(k))
          case None    => map -> Left(ItemDoesNotExist(k))
        }
      }
      def read(k: K): F[Either[ItemDoesNotExist[K, V], ItemRead[K, V]]] =
        ref.get.map(_.get(k) match {
          case Some(v) => Right(ItemRead(k, v))
          case None    => Left(ItemDoesNotExist(k))
        })
    }
  }
}
