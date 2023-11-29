package me.vzhilin.cdcl

import kotlin.math.abs

sealed class TrailItem {
  abstract val level: Int
  abstract val n: Int
}

data class Dec(
  override val level: Int, 
  override val n: Int
): TrailItem()

data class Var(
  override val level: Int, 
  override val n: Int, 
  val clause: Clause
): TrailItem()

/**
 * Unit Propagation for simple single-literal Units
 */
fun unitPropagation(clauses: MutableClauses, assignment: Assignment, trail: MutableList<TrailItem>, level: Int) {  
  while (true) {
    val (unitClause, undef) = assignment.findUnit(clauses) ?: break
    trail.add(Var(level, undef, unitClause))
    println("UP: $undef:$unitClause")
    assignment.extend(undef)
    if (assignment.falsifiesClause(clauses)) break
  }
}

fun analyzeConflict(clauses: Set<Set<Int>>, assignment: Assignment, trail: List<TrailItem>, level: Int): Pair<Clause, Int> {
  val index = trail.indexOfFirst { it.level == level }
  val vCount = mutableMapOf<Int, Int>()
  val posNeg = mutableMapOf<Int, Boolean>()
  trail.subList(index, trail.size).forEach { item -> 
    val an = abs(item.n)
    posNeg[an]= item.n > 0
    vCount[an] = 0
    if (item is Var) {
      item.clause.map(::abs).filter(vCount::containsKey).filter { it != an }.forEach { 
        val v = abs(it)
        vCount[v] = vCount.getValue(v) + 1 
      }
    }
  }
  val (uip, _) = vCount.entries.firstOrNull { (k, v) -> v > 1} ?: vCount.entries.last { (k, v) -> v == 1}
  val result = if (posNeg.getValue(uip)) uip else -uip
  return setOf(-result) to 0
}

sealed class CDCLResult
data object Unsat: CDCLResult()
data class Sat(val a: Assignment): CDCLResult()

fun cdcl(f: Clauses): CDCLResult {
  val vcount = f.flatten().map(::abs).toSet().size
  
  val mc = f.toMutableSet()
  val trail = mutableListOf<TrailItem>()
  var decisionLevel = 0
  val assignment = Assignment(mutableSetOf())
  while (true) {
    unitPropagation(mc, assignment, trail, decisionLevel)
    if (!assignment.isEmpty() && assignment.falsifiesClause(mc)) {
      println("$decisionLevel: false assignment: " + mc.first { assignment.falsifiesClause(setOf(it)) })
      if (decisionLevel == 0) {        
        return Unsat
      } else {
        val (c, lv) = analyzeConflict(mc, assignment, trail, decisionLevel)        
        decisionLevel = lv
        trail.filter { it.level >= decisionLevel }.forEach { item ->
          assignment.unassign(item.n)
        }
        trail.removeIf { it.level >= decisionLevel }
        mc.add(c)
        println("learn: $c")
      }
    } else {
      ++decisionLevel
      if (assignment.size() == vcount) {
        return Sat(assignment)
      }
      val v = assignment.pickUndefined(mc)
      trail.add(Dec(decisionLevel, v))
      println("extend at Decide: $v")
      assignment.extend(v)
    }
  }
}