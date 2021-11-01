package examples.crudlang.app

import java.time.Year

import cats.Applicative
import cats.syntax.all._
import examples.crudlang.CRUDStore
import examples.crudlang.*
import cats.effect.IO

object Init:
  def initCrudApp[F[_]: Applicative](store: CRUDStore[F, String, Movie]): F[Unit] =
    val firstMovie = Movie("Scott Pilgrim VS The World", 6720, Year.of(2010))
    val secondMovie = Movie("Ghost in the Shell", 4980, Year.of(1995))
    val thirdMovie = Movie("Birdman", 7200, Year.of(2014))

    store.create(firstMovie.getKey, firstMovie)
      *> store.create(secondMovie.getKey, secondMovie)
      *> store.create(thirdMovie.getKey, thirdMovie).void

//Can also do:
// List(
//   firstMovie,
//   secondMovie,
//   thirdMovie
// ).traverse_ { m => store.create(m.getKey, m) }
