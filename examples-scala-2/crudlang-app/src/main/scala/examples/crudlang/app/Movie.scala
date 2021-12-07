package examples.crudlang.app

import java.time.Year
import io.circe.Decoder
import io.circe.Encoder
import examples.crudlang.Keyed

final case class Movie(name: String, runtimeSeconds: Int, releaseYear: Year)

object Movie {
  implicit val movieDecoder: Decoder[Movie] =
    Decoder.forProduct3("name", "runtimeSeconds", "releaseYear")(Movie.apply)
  implicit val movieEncoder: Encoder[Movie] =
    Encoder.forProduct3("name", "runtimeSeconds", "releaseYear")(m =>
      (m.name, m.runtimeSeconds, m.releaseYear)
    )
  implicit val movieKeyed: Keyed[String, Movie] = Keyed.from { m =>
    //Get rid of some problematic characters
    //In a real application you'd pass the string through a proper normalizer
    val sortaNormalized = m.name.toLowerCase
      .replaceAll(" ", "-")
      .replace(":", "-")
      .replace("(", "")
      .replace(")", "")

    val yearString = m.releaseYear.toString

    s"$sortaNormalized-$yearString"
  }
}
