import me.vzhilin.cdcl.Clauses
import me.vzhilin.cdcl.syntax.parseCnf
import kotlin.math.abs
import kotlin.test.Test

class Demo {
  @Test
  fun testParsing() {
    val cnf = "(a∨¬b)∧(a∨c∨¬d)∧(¬c∨¬e)∧(¬c∨e)∧(c∨d)"
    val cnf2 = "(¬x1∨¬x2)∧(¬x1∨x3)∧(¬x3∨¬x4)∧(x2∨x4∨x5)∧(¬x5∨x6∨¬x7)∧(x2∨x7∨x8)∧(¬x8∨¬x9)∧(¬x8∨x10)∧(x9∨¬x10∨x11)∧(¬x10∨¬x12)∧(¬x11∨x12)"
    val cnf3 = "(a∨b∨c)∧(a∨b∨¬c)∧(¬b∨d)∧(a∨¬b∨¬d)∧(¬a∨e∨f)∧(¬a∨e∨¬f)∧(¬e∨¬f)∧(¬a∨¬e∨f)"
    val cnf4 = "(¬a∨¬b∨c)∧(¬a∨b∨¬c)∧(a∨¬b∨¬c)"
    val (_, clauses) = parseCnf(cnf2)

    val result = Cdcl(clauses).solve()
    if (result is CDCLResult.Sat) {
      when (val tr = testAssignment(clauses, result.assignment)) {
        is CheckResult.Falsy -> println("check: error: " + tr.clauses)
        CheckResult.Ok -> Unit
      }
      println("check: OK ")
      println(result.assignment.sortedBy(::abs))
    } else {
      println("UNSAT")
    }
  }
}

sealed class CheckResult {
  data object Ok: CheckResult()
  data class Falsy(val clauses: Clauses): CheckResult()
}

fun testAssignment(cs: Clauses, assignment: Set<Int>): CheckResult {
  val falsy = cs.filter { s -> s.none(assignment::contains) }.toSet()
  return if (falsy.isEmpty()) CheckResult.Ok else CheckResult.Falsy(falsy)
}