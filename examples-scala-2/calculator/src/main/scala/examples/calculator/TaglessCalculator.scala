package examples.calculator

import cats.data.State
import cats.Monad

/** A tagless-final approach at implementing a simple, linear calculator DSL */
trait TaglessCalculator[F[_]] {
  def add[A: Numeric](a: A): F[Unit]
  def subtract[A: Numeric](a: A): F[Unit]
  def multiply[A: Numeric](a: A): F[Unit]
  def divide[A: Numeric](a: A): F[Unit]
}

object Calculator {

  //Our baseline interpreter for the underlying type
  implicit val stateCalculator: TaglessCalculator[State[Double, *]] =
    new TaglessCalculator[State[Double, *]] {
      def add[A: Numeric](a: A): State[Double, Unit] =
        State.modify(s => Numeric[A].toDouble(a) + s)
      def subtract[A: Numeric](a: A): State[Double, Unit] =
        State.modify(s => s - Numeric[A].toDouble(a))
      def multiply[A: Numeric](a: A): State[Double, Unit] =
        State.modify(s => Numeric[A].toDouble(a) * s)
      def divide[A: Numeric](a: A): State[Double, Unit] =
        State.modify(s => s / Numeric[A].toDouble(a))
    }

  //We don't want users to have to type that all in, so lets make a wrapped effect
  //In Scala 3 we can use Opaque Types so this is a bit more ergonomic there.
  class WrappedCalculator[A](underlying: State[Double, A]) {
    private[calculator] val internalState = underlying
    //We only care about the state value when ran
    def run = underlying.run(0).value._1
  }

  //Must be assigned to the exact instance. Cannot use `summon` or the `Monad.apply` summoner
  implicit val wrappedCalcMonad: Monad[WrappedCalculator] = new Monad[WrappedCalculator] {
    def flatMap[A, B](fa: WrappedCalculator[A])(
        f: A => WrappedCalculator[B]
    ): WrappedCalculator[B] = new WrappedCalculator(
      fa.internalState.flatMap(s => f(s).internalState)
    )

    def tailRecM[A, B](a: A)(f: A => WrappedCalculator[Either[A, B]]): WrappedCalculator[B] =
      new WrappedCalculator(Monad[State[Double, *]].tailRecM(a)(f.andThen(_.internalState)))

    def pure[A](x: A): WrappedCalculator[A] = new WrappedCalculator(State.pure(x))

  }
  cats.data.IndexedStateT.catsDataMonadForIndexedStateT[cats.Eval, Double]

  implicit val taglessWrappedCalc: TaglessCalculator[WrappedCalculator] =
    new TaglessCalculator[WrappedCalculator] {
      def add[A: Numeric](a: A): WrappedCalculator[Unit] = new WrappedCalculator(
        stateCalculator.add(a)
      )

      def subtract[A: Numeric](a: A): WrappedCalculator[Unit] = new WrappedCalculator(
        stateCalculator.subtract(a)
      )

      def multiply[A: Numeric](a: A): WrappedCalculator[Unit] = new WrappedCalculator(
        stateCalculator.multiply(a)
      )

      def divide[A: Numeric](a: A): WrappedCalculator[Unit] = new WrappedCalculator(
        stateCalculator.divide(a)
      )

    }
}
