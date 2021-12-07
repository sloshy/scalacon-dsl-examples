package examples

import cats.data.Kleisli
import cats.effect.Concurrent
import io.circe.{Decoder, Encoder}
import org.http4s.HttpRoutes
import org.http4s.Response
import cats.syntax.all._
import cats.Functor

package object crudlang {
  type RequestHandler[F[_], Req, Res] = Kleisli[F, Req, Res]

  object RequestHandler {

    /** Just an alias for `Kleisli.apply` to be more domain-friendly. */
    def handle[F[_], Req, Res](f: Req => F[Res]): RequestHandler[F, Req, Res] = Kleisli(f)

    /** Automatically creates a CRUD handler for a given data store. */
    def crudHandler[F[_]: Functor, K, V](dataStore: CRUDStore[F, K, V])(implicit
        keyed: Keyed[K, V]
    ): RequestHandler[F, CRUDRequest[K, V], CRUDResponse[K, V]] = {
      import examples.crudlang.CRUDRequest._
      RequestHandler.handle {
        case Create(v) =>
          dataStore.create(v.getKey, v).map {
            case Left(e)  => e
            case Right(v) => v
          }
        case Read(k) =>
          dataStore.read(k).map {
            case Left(e)  => e
            case Right(v) => v
          }
        case Update(v) =>
          dataStore.update(v.getKey, v).map {
            case Left(e)  => e
            case Right(v) => v
          }
        case Delete(k) =>
          dataStore.delete(k).map {
            case Left(e)  => e
            case Right(v) => v
          }
        case CreateOrUpdate(v) =>
          dataStore.createOrUpdate(v.getKey, v).map {
            case Left(e)  => e
            case Right(v) => v
          }
      }
    }
  }

  implicit class RequestHandlerOps[F[_]: Concurrent, Req: Decoder, Res: Encoder](
      rh: RequestHandler[F, Req, Res]
  ) {

    /** A very simplified and naive http route conversion for our request handler. To handle err
      */
    def toHttpRoutes(implicit s: StatusCodeMapping[Res]): HttpRoutes[F] = {
      val dsl = org.http4s.dsl.Http4sDsl[F]
      import dsl._
      import org.http4s.circe.CirceEntityCodec._

      //Heavily over-simplified, but you get the idea
      HttpRoutes.of[F] { case req @ POST -> Root / "crud" =>
        req.decode[Req] { req =>
          rh(req).map { res =>
            val status = s.getStatusCode(res)
            Response[F](status).withEntity(Encoder[Res].apply(res))
          }
        }
      }
    }

    def handleRequest(req: Req) = rh.run(req)
  }

  implicit class KeyedOps[V](v: V) {
    def getKey[K](implicit keyed: Keyed[K, V]) = keyed.getKey(v)
  }
}
