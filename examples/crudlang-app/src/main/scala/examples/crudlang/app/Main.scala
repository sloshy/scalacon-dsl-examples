package examples.crudlang.app

import cats.effect.IOApp
import cats.effect.IO
import cats.effect.ExitCode
import examples.crudlang.CRUDStore
import examples.crudlang._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import com.comcast.ip4s._

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    for
      store <- CRUDStore.inMemory[IO, String, Movie]
      _ <- Init.initCrudApp(store)
      _ <- EmberServerBuilder
        .default[IO]
        .withPort(port"8081")
        .withHost(host"0.0.0.0")
        .withHttpApp(RequestHandler.crudHandler(store).toHttpRoutes.orNotFound)
        .build
        .use { _ => IO.println("Server is running!") >> IO.never }
    yield ExitCode.Error
