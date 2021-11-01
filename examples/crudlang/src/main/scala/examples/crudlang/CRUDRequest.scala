package examples.crudlang

import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json

enum CRUDRequest[K, V]:
  case Create[K, V](v: V) extends CRUDRequest[K, V]
  case Read[K, V](k: K) extends CRUDRequest[K, V]
  case Update[K, V](v: V) extends CRUDRequest[K, V]
  case Delete[K, V](k: K) extends CRUDRequest[K, V]
  case CreateOrUpdate[K, V](v: V) extends CRUDRequest[K, V]

object CRUDRequest:
  given [K: Encoder, V: Encoder]: Encoder[CRUDRequest[K, V]] = Encoder.instance {
    case Create(v) =>
      Json.obj(
        "type" -> Json.fromString("Create"),
        "payload" -> Encoder[V].apply(v)
      )
    case Read(k) =>
      Json.obj(
        "type" -> Json.fromString("Read"),
        "key" -> Encoder[K].apply(k)
      )
    case Update(v) =>
      Json.obj(
        "type" -> Json.fromString("Update"),
        "payload" -> Encoder[V].apply(v)
      )
    case Delete(k) =>
      Json.obj(
        "type" -> Json.fromString("Delete"),
        "key" -> Encoder[K].apply(k)
      )
    case CreateOrUpdate(v) =>
      Json.obj(
        "type" -> Json.fromString("CreateOrUpdate"),
        "payload" -> Encoder[V].apply(v)
      )
  }
  given [K: Decoder, V: Decoder]: Decoder[CRUDRequest[K, V]] = Decoder.instance { hcursor =>
    def getKey = hcursor.downField("key").as[K]
    def getPayload = hcursor.downField("payload").as[V]
    hcursor.downField("type").as[String].flatMap {
      case "Create"         => getPayload.map(v => Create[K, V](v))
      case "Read"           => getKey.map(k => Read[K, V](k))
      case "Update"         => getPayload.map(v => Update[K, V](v))
      case "Delete"         => getKey.map(k => Delete[K, V](k))
      case "CreateOrUpdate" => getPayload.map(v => CreateOrUpdate[K, V](v))
    }
  }
