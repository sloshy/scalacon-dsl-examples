package examples.calculator

import munit.FunSuite

class ADTSpec extends FunSuite:
  test("CalculationADT compiler") {
    val program = List(
      CalculationADT.Add(5),
      CalculationADT.Add(5),
      CalculationADT.Divide(5)
    )

    assertEquals(CalculationADT.compiler(program), 2d)
  }
