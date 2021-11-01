// package examples.calculator

// import cats.data.State
// import cats.syntax.all._

// object CalculationConcreteValues:
//   sealed trait Calculation
//   object Calculation:
//     sealed trait Value extends Calculation
//     sealed trait CalcOp extends Calculation:
//       val value: Value

//     final case class IntValue(value: Int) extends Value
//     final case class LongValue(value: Long) extends Value
//     final case class FloatValue(value: Float) extends Value
//     final case class DoubleValue(value: Double) extends Value
//     final case class Add(value: Value) extends CalcOp
//     final case class Subtract(value: Value) extends CalcOp
//     final case class Multiply(value: Value) extends CalcOp
//     final case class Divide(value: Value) extends CalcOp

//   object CalculatorOp:
//     def compiler(a: List[Calculation.CalcOp]): Double =
//       import Calculation._
//       a.foldLeft(0d) { (s, next) =>
//         val value = next.value match
//           case IntValue(a)    => a.toDouble
//           case LongValue(a)   => a.toDouble
//           case FloatValue(a)  => a.toDouble
//           case DoubleValue(a) => a

//         next match
//           case _: Add      => s + value
//           case _: Subtract => s - value
//           case _: Multiply => s * value
//           case _: Divide   => s / value
//       }
