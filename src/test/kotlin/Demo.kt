import me.vzhilin.cdcl.Clauses


sealed class CheckResult {
  data object Ok: CheckResult()
  data class Falsy(val clauses: Clauses): CheckResult()
}

fun testAssignment(cs: Clauses, assignment: Set<Int>): CheckResult {
  val falsy = cs.filter { s -> s.none(assignment::contains) }.toSet()
  return if (falsy.isEmpty()) CheckResult.Ok else CheckResult.Falsy(falsy)
}