package examples.crudlang

import io.circe.Encoder
import io.circe.Json
import io.circe.Decoder

sealed trait CRUDResponse[K, V]:
  val k: K

object CRUDResponse:

  final case class ItemCreated[K, V](k: K, v: V) extends CRUDResponse[K, V]
  final case class ItemAlreadyExists[K, V](k: K) extends CRUDResponse[K, V]
  final case class ItemRead[K, V](k: K, v: V) extends CRUDResponse[K, V]
  final case class ItemDoesNotExist[K, V](k: K) extends CRUDResponse[K, V]

  final case class ItemDeleted[K, V](k: K) extends CRUDResponse[K, V]

  sealed trait CRUDUpdateResponse[K, V] extends CRUDResponse[K, V]

  object CRUDUpdateResponse:
    final case class ItemUpdated[K, V](k: K, v: V) extends CRUDUpdateResponse[K, V]
    final case class ItemKeyCannotChange[K, V](k: K, newKey: K) extends CRUDUpdateResponse[K, V]

  given [K, V]: Keyed[K, CRUDResponse[K, V]] = new:
    def getKey(v: CRUDResponse[K, V]) = v.k

  given [K: Encoder, V: Encoder]: Encoder[CRUDResponse[K, V]] = Encoder.instance {
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

  given [K: Decoder, V: Decoder]: Decoder[CRUDResponse[K, V]] = Decoder.instance { hcursor =>
    def getKey: Decoder.Result[K] = hcursor.downField("key").as[K]
    def getKeyAndPayload: Decoder.Result[(K, V)] =
      getKey.flatMap { k => hcursor.downField("payload").as[V].map(v => k -> v) }

    hcursor.downField("type").as[String].flatMap {
      //Fancy Scala 3 tuple support in action
      case "ItemCreated"       => getKeyAndPayload.map(ItemCreated.apply)
      case "ItemRead"          => getKeyAndPayload.map(ItemRead.apply)
      case "ItemUpdated"       => getKeyAndPayload.map(CRUDUpdateResponse.ItemUpdated.apply)
      case "ItemDeleted"       => getKey.map(ItemDeleted.apply)
      case "ItemAlreadyExists" => getKey.map(ItemAlreadyExists.apply)
      case "ItemDoesNotExist"  => getKey.map(ItemDoesNotExist.apply)
      case "ItemKeyCannotChange" =>
        getKey
          .flatMap(k => hcursor.downField("newKey").as[K].map(k -> _))
          .map(CRUDUpdateResponse.ItemKeyCannotChange.apply)
    }
  }
