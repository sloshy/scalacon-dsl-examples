package examples.crudlang

import io.circe.Encoder
import io.circe.Json
import io.circe.Decoder
import org.http4s.Status
import examples.crudlang.CRUDResponse.CRUDUpdateResponse.ItemKeyCannotChange

sealed trait CRUDResponse[K, V] {
  val k: K
}

object CRUDResponse {

  final case class ItemCreated[K, V](k: K, v: V) extends CRUDResponse[K, V]
  final case class ItemAlreadyExists[K, V](k: K) extends CRUDResponse[K, V]
  final case class ItemRead[K, V](k: K, v: V) extends CRUDResponse[K, V]
  final case class ItemDoesNotExist[K, V](k: K) extends CRUDResponse[K, V]

  final case class ItemDeleted[K, V](k: K) extends CRUDResponse[K, V]

  sealed trait CRUDUpdateResponse[K, V] extends CRUDResponse[K, V]

  object CRUDUpdateResponse {
    final case class ItemUpdated[K, V](k: K, v: V) extends CRUDUpdateResponse[K, V]
    final case class ItemKeyCannotChange[K, V](k: K, newKey: K) extends CRUDUpdateResponse[K, V]
  }

  implicit def crudKeyed[K, V]: Keyed[K, CRUDResponse[K, V]] =
    Keyed.from[K, CRUDResponse[K, V]](_.k)

  implicit def crudStatusCode[K, V]: StatusCodeMapping[CRUDResponse[K, V]] =
    new StatusCodeMapping[CRUDResponse[K, V]] {
      def getStatusCode(a: CRUDResponse[K, V]): Status =
        a match {
          case ItemAlreadyExists(_)      => Status.BadRequest
          case ItemDoesNotExist(_)       => Status.NotFound
          case ItemKeyCannotChange(_, _) => Status.BadRequest
          case _                         => Status.Ok
        }
    }

  implicit def encoder[K: Encoder, V: Encoder]: Encoder[CRUDResponse[K, V]] = Encoder.instance {
    case ItemCreated(k, v) =>
      Json.obj(
        "type" -> Json.fromString("ItemCreated"),
        "key" -> Encoder[K].apply(k),
        "payload" -> Encoder[V].apply(v)
      )
    case ItemAlreadyExists(k) =>
      Json.obj(
        "type" -> Json.fromString("ItemAlreadyExists"),
        "key" -> Encoder[K].apply(k)
      )
    case ItemRead(k, v) =>
      Json.obj(
        "type" -> Json.fromString("ItemRead"),
        "key" -> Encoder[K].apply(k),
        "payload" -> Encoder[V].apply(v)
      )
    case ItemDoesNotExist(k) =>
      Json.obj(
        "type" -> Json.fromString("ItemDoesNotExist"),
        "key" -> Encoder[K].apply(k)
      )
    case CRUDUpdateResponse.ItemUpdated(k, v) =>
      Json.obj(
        "type" -> Json.fromString("ItemUpdated"),
        "key" -> Encoder[K].apply(k),
        "payload" -> Encoder[V].apply(v)
      )
    case ItemDeleted(k) =>
      Json.obj(
        "type" -> Json.fromString("ItemDeleted"),
        "key" -> Encoder[K].apply(k)
      )
    case CRUDUpdateResponse.ItemKeyCannotChange(k, newK) =>
      Json.obj(
        "type" -> Json.fromString("ItemKeyCannotChange"),
        "key" -> Encoder[K].apply(k),
        "newKey" -> Encoder[K].apply(newK)
      )
  }

  implicit def decoder[K: Decoder, V: Decoder]: Decoder[CRUDResponse[K, V]] = Decoder.instance {
    hcursor =>
      def getKey: Decoder.Result[K] = hcursor.downField("key").as[K]
      def getKeyAndPayload: Decoder.Result[(K, V)] =
        getKey.flatMap { k => hcursor.downField("payload").as[V].map(v => k -> v) }

      hcursor.downField("type").as[String].flatMap {
        //This is allowed to look a lot more compact in Scala 3 thanks to tuple destructuring
        case "ItemCreated" => getKeyAndPayload.map { case (k, v) => ItemCreated(k, v) }
        case "ItemRead"    => getKeyAndPayload.map { case (k, v) => ItemRead(k, v) }
        case "ItemUpdated" =>
          getKeyAndPayload.map { case (k, v) => CRUDUpdateResponse.ItemUpdated(k, v) }
        case "ItemDeleted"       => getKey.map(ItemDeleted.apply)
        case "ItemAlreadyExists" => getKey.map(ItemAlreadyExists.apply)
        case "ItemDoesNotExist"  => getKey.map(ItemDoesNotExist.apply)
        case "ItemKeyCannotChange" =>
          getKey
            .flatMap(k => hcursor.downField("newKey").as[K].map(k -> _))
            .map { case (k, v) => CRUDUpdateResponse.ItemKeyCannotChange(k, v) }
      }
  }
}
