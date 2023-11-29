import me.vzhilin.cdcl.cdcl
import me.vzhilin.cdcl.syntax.parseCnf
import kotlin.test.Test

class ParserTest {
  @Test
  fun testParsing() {
    val cnf = "(a∨¬b)∧(a∨c∨¬d)∧(¬c∨¬e)∧(¬c∨e)∧(c∨d)"
    val cnf2 = "(¬x1∨¬x2)∧(¬x1∨x3)∧(¬x3∨¬x4)∧(x2∨x4∨x5)∧(¬x5∨x6∨¬x7)∧(x2∨x7∨x8)∧(¬x8∨¬x9)∧(¬x8∨x10)∧(x9∨¬x10∨x11)∧(¬x10∨¬x12)∧(¬x11∨x12)"
    val cnf3 = "(a∨b∨c)∧(a∨b∨¬c)∧(¬b∨d)∧(a∨¬b∨¬d)∧(¬a∨e∨f)∧(¬a∨e∨¬f)∧(¬e∨¬f)∧(¬a∨¬e∨f)"
    val (mapping, clauses) = parseCnf(cnf2)
    val mc = clauses.toMutableSet()

    println(cdcl(mc))
  }
}