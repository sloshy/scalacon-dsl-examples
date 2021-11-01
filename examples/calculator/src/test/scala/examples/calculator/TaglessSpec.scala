package examples.calculator

import munit.FunSuite
import cats.data.State
import Calculator.*
import cats.FlatMap
import cats.syntax.all._
import cats.Applicative

class TaglessSpec extends FunSuite:
  def testProgram[F[_]: Applicative](using calc: TaglessCalculator[F]) =
    calc.add(5) *> calc.add(5) *> calc.divide(5)

  test("OpaqueCalculator") {
    val result = testProgram[OpaqueCalculator].run
    assertEquals(result, 2d)
  }
