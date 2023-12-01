import me.vzhilin.cdcl.Assignment
import me.vzhilin.cdcl.Clause
import me.vzhilin.cdcl.Clauses
import me.vzhilin.cdcl.MutableClauses
import kotlin.math.abs
import kotlin.math.absoluteValue

sealed class TrailItem {
  abstract val level: UInt
  abstract val n: Int
  abstract val clause: Clause
}

data class Dec(
  override val level: UInt,
  override val n: Int
): TrailItem() {
  override val clause get() = setOf(n)
}

data class Var(
  override val level: UInt,
  override val n: Int,
  override val clause: Clause
): TrailItem()

data class Conflict(
  override val n: Int,
  override val level: UInt,
  override val clause: Clause
): TrailItem()

sealed class CDCLResult {
  data object Unsat: CDCLResult()
  data class Sat(val assignment: Set<Int>): CDCLResult()
}

enum class BCPResult { OK, CONFLICT }
sealed class AnalyzeConflictResult
data object Unsatisfiable: AnalyzeConflictResult()
data class Level(
  val level: UInt,
  val clause: Clause
): AnalyzeConflictResult()

class Cdcl(clauses: Clauses) {
  private val clauses: MutableClauses
  private val assignments = Assignment()
  private var decisionLevel: UInt = 0u
  private val trail = mutableListOf<TrailItem>()

  init {
    this.clauses = clauses.toMutableSet()
  }
  
  private fun decide(): Boolean {    
    ++decisionLevel
    val variable = clauses.flatten().firstOrNull { !assignments.haveLiteral(it) } ?: return false
    assignments.extend(variable, decisionLevel)
    trail.add(Dec(decisionLevel, variable))
    return true
  }

  /**
   * Boolean constraint propagation
   */
  private fun bcp(): BCPResult {
    while (true) {
      val clause = clauses.firstOrNull(assignments::isUnit) ?: return BCPResult.OK

      val v = assignments.unitVar(clause) ?: throw AssertionError("no unit: $clause")
      assignments.extend(v, decisionLevel)
      trail.add(Var(decisionLevel, v, clause))

      val conflictingClauses = assignments.falsyClauses(clauses)
      if (conflictingClauses.isNotEmpty()) {
        trail.add(Conflict(v, decisionLevel, conflictingClauses.first()))
        return BCPResult.CONFLICT
      }      
    }    
  }
  
  private fun currentConflictingClause(): Conflict {
    return trail.filterIsInstance<Conflict>().last()
  }
  
  private fun currentLevelTrail(): List<TrailItem> {
    return trail.filter { it.level == decisionLevel }
  }
  
  private fun currentLevelAssignments(): Set<Int> {
    return currentLevelTrail().map(TrailItem::n).map(::abs).toSet()
  }
  
  private fun firstUip(): Int {    
    val trailPart = currentLevelTrail().asReversed()
    val currentAssignments = currentLevelAssignments()
    
    val ns = mutableSetOf<Int>()
    
    for (item in trailPart) {
      if (item !is Conflict) {
        ns.remove(abs(item.n))
        if (ns.isEmpty()) {
          return item.n
        }
      }
      ns.addAll((item.clause.map(::abs) - abs(item.n)).intersect(currentAssignments))
    }
    
    throw AssertionError("could not find UIP")
  }
  
  private fun stopCriterionMet(cl: Clause): Boolean {
    val currentLevelNodes = currentLevelAssignments()
    val filteredClause = cl.filter { currentLevelNodes.contains(it.absoluteValue) }
    return filteredClause.size == 1 && filteredClause.first() == -firstUip() 
  }
  
  private fun lastAssignedLiteral(cl: Clause): Int = assignments.values.last { cl.map(::abs).contains(abs(it)) }

  private fun resolve(cl: Set<Int>, ante: Set<Int>, v: Int): Set<Int> {
    val s = setOf(v, -v)
    return (cl - s) + (ante - s)
  }

  private fun analyzeConflict(): AnalyzeConflictResult {
    if (decisionLevel == 0u) return Unsatisfiable

    val conflictingClause = currentConflictingClause()
    var cl = conflictingClause.clause
    
    while (!stopCriterionMet(cl)) {
      val lit = lastAssignedLiteral(cl)
      val ante = antecedent(lit)
      cl = resolve(cl, ante, lit)
      if (cl.isEmpty()) return Unsatisfiable
    }
    addClause(cl)

    return Level(assertingLevel(cl), cl)
  }

  private fun assertingLevel(cl: Set<Int>): UInt {
    if (cl.size == 1) {
      return 0u // for unary clause backtrack to ground level
    }
    val ls = cl.map { assignments.level(it) }.sortedDescending()
    return ls[1] // second most recent decision level
  }

  private fun addClause(cl: Set<Int>) {
    clauses.add(cl)
  }

  private fun antecedent(lit: Int): Set<Int> {
    return trail.first { abs(it.n) == abs(lit)  }.clause
  }

  private fun backTrack(level: UInt) {
    this.decisionLevel = level
    for (item in this.trail.filter { it.level >= level}) {
      assignments.remove(item.n)
    }
    this.trail.removeAll { it.level >= level }
  }
  
  fun solve(): CDCLResult {
    while (true) {      
      while (bcp() == BCPResult.CONFLICT) {
        when(val level = analyzeConflict()) {
          is Level -> backTrack(level.level)
          else -> return CDCLResult.Unsat
        }
      }      

      if (!decide()) {
        return CDCLResult.Sat(assignments.values)
      }
    }
  }
}

