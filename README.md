# Your Program Is a Language - Examples
Demo examples given during [Your Program Is A Language](https://slides.rpeters.dev/scalacon-dsl/) at ScalaCon Fall 2021.

Examples are written in Scala 3 and Scala 2.
Scala 2 examples are found in the `examples-scala-2` folder and are identical to Scala 3 where possible except for syntax.
Two major differences:

1. Scala 3 allows us to write Union types `A | B` and this is useful for scenarios of multiple return types across arbitrary lines. This is used heavily in the `CRUDStore.scala` file for Scala 3 but this code had to be converted to `Either` in Scala 2.
2. Scala 3 has `opaque type` modifiers for types that wrap existing types with no runtime overhead. Code using this style was converted to the much more verbose "wrapper class" style for Scala 2. You can see this in `TaglessCalculator.scala`.

## Calculator
Found in [`examples/calculator`](examples/calculator/src)

This example shows a kind of DSL that is relatively simple to implement in as both an ADT as well as a "tagless final" style algebra.
Both approaches allow for the same code to be written, but they are encoded in slightly different ways.

The ADT approach requires some kind of data structure to work, so we have a compiler for a `List[CalculationOp]` that runs our sequence of operations.
The tagless approach, on the other hand, contains a given interpreter for the `State` type in Cats.
To make this a little easier for end-users, an opaque type is used to hide the implementation details and allow us to write programs in this embedded language without much thought.

For examples, see the [tests](examples/calculator/src/test/scala/examples/calculator)

## CRUDLang
Found in [`examples/crudlang`](examples/crudlang/src/main/scala/examples/crudlang)

This is a more involved example showing the way we can use an embedded DSL solution to model how we want our programs to compose together.
Inside, we have one main algebra, `CRUDStore`, that acts as an interface for storing key/value pairs.
We also have two type classes:

* `Keyed` - Describes data that has an extractable "key" value
* `StatusCodeMapping` - Describes data that can map directly to an http4s `Status`

Two ADTs:

* `CRUDRequest` - Boilerplate ADT for types of requests that could create/read/update/delete from `CRUDStore`
* `CRUDResponse` - The counterpart of `CRUDRequest` and the response type of any action that touches `CRUDStore`

And one opaque wrapper around `Kleisli`, called `RequestHandler`, that allows us to define functions with a type signature `Request => F[Response]`.
We also have some special syntax to convert a `RequestHandler` with a `StatusCodeMapping` for its response type into http4s `HttpRoutes` to plug right into a web server, and a constructor method `RequestHandler.crudHandler` that creates a `RequestHandler` specially tailored to handle CRUD requests/responses.

### CRUDLang App
Found in [`examples/crudlang-app`](examples/crudlang-app/src/main/scala/examples/crudlang/app).

An application that uses `CRUDLang` to define a very basic CRUD server for movies.
Defines a `Movie` type as well as a simple `Init` script that we write using the `CRUDStore` algebra, plus `Applicative` so we can sequence operations.

To talk to the server, you can use any REST client or use the `post.sh` script in this repository along with any properly-formatted JSON as-needed.
