package examples.calculator

import munit.FunSuite
import Calculator._
import cats.syntax.all._
import cats.Monad

class TaglessSpec extends FunSuite {
  def testProgram[F[_]: Monad](implicit calc: TaglessCalculator[F]) =
    calc.add(5) >> calc.add(5) >> calc.divide(5)

  test("WrappedCalculator") {
    val result = testProgram[WrappedCalculator].run
    assertEquals(result, 2d)
  }
}
