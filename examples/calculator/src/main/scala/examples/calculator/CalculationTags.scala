// package examples.calculator

// object CalculationTags:
//   sealed trait Calculation
//   object Calculation:
//     final case class Value(value: Any, tag: String) extends Calculation
//     sealed trait CalcOp extends Calculation:
//       val value: Value

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
