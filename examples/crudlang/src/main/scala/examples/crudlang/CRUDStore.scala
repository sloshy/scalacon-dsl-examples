package examples.crudlang

import cats.effect.Concurrent
import cats.syntax.all._
import CRUDResponse._

trait CRUDStore[F[_], K, V]:
  /** Attempts to create a key/value pair. */
  def create(k: K, v: V): F[ItemCreated[K, V] | ItemDoesNotExist[K, V]]

  /** Tries to update an existing value if it exists. */
  def update(k: K, v: V): F[ItemUpdated[K, V] | ItemDoesNotExist[K, V]]

  /** An 'upsert' type operation that always succeeds. */
  def createOrUpdate(k: K, v: V): F[ItemCreated[K, V] | ItemUpdated[K, V]]

  /** Deletes a given key if it exists. */
  def delete(k: K): F[ItemDeleted[K, V] | ItemDoesNotExist[K, V]]

  /** Tries to get the value from the data store, if it exists. */
  def read(k: K): F[ItemRead[K, V] | ItemDoesNotExist[K, V]]

object CRUDStore:
  def inMemory[F[_]: Concurrent, K, V] = Concurrent[F].ref(Map.empty[K, V]).map { ref =>
    new CRUDStore[F, K, V]:
      def create(k: K, v: V): F[ItemCreated[K, V] | ItemDoesNotExist[K, V]] = ref.modify { map =>
        map.get(k) match {
          case Some(_) => map -> ItemDoesNotExist(k)
          case None    => (map + (k -> v)) -> ItemCreated(k, v)
        }
      }
      def update(k: K, v: V): F[ItemUpdated[K, V] | ItemDoesNotExist[K, V]] = ref.modify { map =>
        map.get(k) match {
          case Some(_) => (map + (k -> v)) -> ItemUpdated(k, v)
          case None    => map -> ItemDoesNotExist(k)
        }
      }
      def createOrUpdate(k: K, v: V): F[ItemCreated[K, V] | ItemUpdated[K, V]] = ref.modify { map =>
        //false: was updated in-place. true: was created.
        val newMap = map + (k -> v)
        map.get(k) match {
          case Some(_) => newMap -> ItemUpdated(k, v)
          case None    => newMap -> ItemCreated(k, v)
        }
      }
      def delete(k: K): F[ItemDeleted[K, V] | ItemDoesNotExist[K, V]] = ref.modify { map =>
        //true: was deleted successfully. false: item does not exist and cannot be deleted
        map.get(k) match {
          case Some(_) => (map - k) -> ItemDeleted(k)
          case None    => map -> ItemDoesNotExist(k)
        }
      }
      def read(k: K): F[ItemRead[K, V] | ItemDoesNotExist[K, V]] = ref.get.map(_.get(k) match {
        case Some(v) => ItemRead(k, v)
        case None    => ItemDoesNotExist(k)
      })
  }
