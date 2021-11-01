package examples.calculator

sealed trait CalculationADT
object CalculationADT:
  sealed trait CalcOp extends CalculationADT:
    val value: Double

  final case class Add(value: Double) extends CalcOp
  object Add:
    def apply[A: Numeric](value: A) = new Add(Numeric[A].toDouble(value))
  final case class Subtract(value: Double) extends CalcOp
  object Subtract:
    def apply[A: Numeric](value: A) = new Subtract(Numeric[A].toDouble(value))
  final case class Multiply(value: Double) extends CalcOp
  object Multiply:
    def apply[A: Numeric](value: A) = new Multiply(Numeric[A].toDouble(value))
  final case class Divide(value: Double) extends CalcOp
  object Divide:
    def apply[A: Numeric](value: A) = new Divide(Numeric[A].toDouble(value))

  def compiler(a: List[CalcOp]): Double =
    a.foldLeft(0d) { (s, next) =>
      val value = next.value

      next match
        case Add(value)      => s + value
        case Subtract(value) => s - value
        case Multiply(value) => s * value
        case Divide(value)   => s / value
    }
