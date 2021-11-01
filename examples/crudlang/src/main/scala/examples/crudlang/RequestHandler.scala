package examples.crudlang

import cats.effect.Concurrent
import cats.data.Kleisli
import CRUDRequest._
import CRUDResponse._
import cats.Applicative
import cats.syntax.all._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import io.circe.Encoder
import io.circe.Decoder
import org.http4s._
import org.http4s.implicits._

type RequestHandler[F[_], Req, Res] = Kleisli[F, Req, Res]

extension [F[_]: Concurrent, Req: Decoder, Res: Encoder](rh: RequestHandler[F, Req, Res])
  def toHttpRoutes: HttpRoutes[F] =
    val dsl = org.http4s.dsl.Http4sDsl[F]
    import dsl._

    //Heavily over-simplified, but you get the idea
    HttpRoutes.of[F] { case req @ POST -> Root / "crud" =>
      req.decode[Req] { req =>
        rh(req).flatMap(res => Ok(Encoder[Res].apply(res)))
      }
    }

object RequestHandler:
  /** Just an alias for `Kleisli.apply` to be more domain-friendly. */
  def handle[F[_], Req, Res](f: Req => F[Res]): RequestHandler[F, Req, Res] = Kleisli(f)

  /** Automatically creates a CRUD handler for a given data store. */
  def crudHandler[F[_]: Applicative, K, V](dataStore: CRUDStore[F, K, V])(using
      keyed: Keyed[K, V]
  ): RequestHandler[F, CRUDRequest[K, V], CRUDResponse[K, V]] =
    RequestHandler.handle {
      case Create(v)         => dataStore.create(v.getKey, v).widen
      case Read(k)           => dataStore.read(k).widen
      case Update(v)         => dataStore.update(v.getKey, v).widen
      case Delete(k)         => dataStore.delete(k).widen
      case CreateOrUpdate(v) => dataStore.createOrUpdate(v.getKey, v).widen
    }
