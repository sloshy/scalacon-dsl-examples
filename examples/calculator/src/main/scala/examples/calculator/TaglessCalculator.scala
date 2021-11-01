package examples.calculator

import cats.FlatMap
import cats.syntax.all._
import cats.data.State
import cats.Applicative
import cats.Monad

/** A tagless-final approach at implementing a simple, linear calculator DSL */
trait TaglessCalculator[F[_]]:
  def add[A: Numeric](a: A): F[Unit]
  def subtract[A: Numeric](a: A): F[Unit]
  def multiply[A: Numeric](a: A): F[Unit]
  def divide[A: Numeric](a: A): F[Unit]

object Calculator:

  //Our baseline interpreter for the underlying type
  given stateCalculator: TaglessCalculator[State[Double, *]] with
    def add[A: Numeric](a: A): State[Double, Unit] =
      State.modify(s => Numeric[A].toDouble(a) + s)
    def subtract[A: Numeric](a: A): State[Double, Unit] =
      State.modify(s => s - Numeric[A].toDouble(a))
    def multiply[A: Numeric](a: A): State[Double, Unit] =
      State.modify(s => Numeric[A].toDouble(a) * s)
    def divide[A: Numeric](a: A): State[Double, Unit] =
      State.modify(s => s / Numeric[A].toDouble(a))

  //We don't want users to have to type that all in, so lets make an opaque effect
  opaque type OpaqueCalculator[A] = State[Double, A]

  //Must be assigned to the exact instance. Cannot use `summon` or the `Monad.apply` summoner
  given Monad[OpaqueCalculator] =
    cats.data.IndexedStateT.catsDataMonadForIndexedStateT[cats.Eval, Double]

  given TaglessCalculator[OpaqueCalculator] = stateCalculator

  //We only care about the state value when ran
  extension [A](c: OpaqueCalculator[A]) def run = c.run(0).value._1
