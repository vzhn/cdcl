package me.vzhilin.cdcl.impl.simple

import me.vzhilin.cdcl.api.*
import java.util.Collections

class DefaultCdcl: Cdcl<DefaultClause, UInt> {
  private var decisionLevel = 0u
  private val clauses = DefaultLearnedClauses()
  private val assignments = DefaultAssignments()
  private val trail = DefaultTrail()

  override fun clauses(): DefaultLearnedClauses {
    return this.clauses
  }

  override fun unassignedLiteral(): UInt? {
    val unassignedLiterals = this.clauses.literals().toSet() - assignments.literals().toSet()
    if (unassignedLiterals.isEmpty()) return null
    return unassignedLiterals.first()
  }

  override fun decide(): Boolean {
    val literal = unassignedLiteral() ?: return false
    val value = true
    ++decisionLevel;
    assign(literal, value)
    trail.addDecision(literal, value)
    return true
  }
  
  override fun pickUnitClause(): Pair<DefaultClause, UInt>? {
    for (c in this.clauses) {
      val literals = c.filterNot { (lit, p) -> assignments.value(lit, p) != null }
      if (literals.size == 1) {
        val (lit) = literals.first()
        if (c.any { (l, p) -> l != lit && assignments.value(l, p) == true}) {
          continue
        }
        return c to lit
      }
    }

    return null
  }

  override fun hasConflict(): DefaultClause? {
    for (c in clauses()) {      
      if (assignments.evaluate(c) == false) {
        return c
      }
    }
    return null
  }

  override fun bcp(): BCPResult {
    while (true) {
      val (clause, lit) = pickUnitClause() ?: return BCPResult.OK      
      val v = clause.polarity(lit).toBoolean()
      
      assign(lit, v)
      trail.addBcp(lit, v, clause)
      
      val conflictClause = hasConflict() ?: continue
      trail.addConflict(lit, v, conflictClause)
      
      return BCPResult.CONFLICT
    }
  }

  override fun evaluate(clause: Clause<UInt>, lit: UInt): Boolean? {
    return assignments.evaluate(clause, lit)
  }

  override fun assign(lit: UInt, b: Boolean) {
    assignments.assign(decisionLevel, lit, b)
  }

  override fun addClauses(cs: Collection<DefaultClause>) {
    clauses.include(cs)
  }

  private fun stopCriterionMet(cl: DefaultClause): Boolean {
    val currentLevelNodes = trail.currentLevelAssignments()
    val filteredClause = cl.filter { (lit) -> currentLevelNodes.contains(lit) }
    if (filteredClause.size != 1) return false
    
    val (lit, polarity) = filteredClause.first()    
    val (uip, uipPolarity) = trail.firstUip()
    
    return lit == uip && polarity == uipPolarity.invert()
  }
  
  private fun assertingLevel(cl: DefaultClause): UInt {
    val ls = cl.map { (lit) -> assignments.level(lit) }.sortedDescending()
    if (ls.size == 1) return 0u    
    return ls[1]
  }
  
  private fun analyzeConflict(): AnalyzeConflictResult<DefaultClause, UInt> {
    if (decisionLevel == 0u) return AnalyzeConflictResult.Unsatisfiable()

    val conflictingClause = trail.currentConflictingClause()
    var cl = conflictingClause

    while (!stopCriterionMet(cl)) {
      val lit = trail.lastAssignedLiteral(cl)
      val ante = trail.antecedent(lit)
      cl = cl.resolve(ante, lit)
      if (cl.isEmpty()) return AnalyzeConflictResult.Unsatisfiable()
    }
    
    addClauses(Collections.singleton(cl))
    
    return AnalyzeConflictResult.Level(assertingLevel(cl), cl)
  }

  private fun backTrack(level: UInt) {
    assignments.backTrack(level)
    trail.backTrack(level)
    this.decisionLevel = level
  }
  
  fun solve(): CDCLResult<DefaultClause, UInt> {
    while (true) {
      while (bcp() == BCPResult.CONFLICT) {
        when(val level = analyzeConflict()) {
          is AnalyzeConflictResult.Level -> backTrack(level.level)
          else -> return CDCLResult.Unsat()
        }
      }

      if (!decide()) {
        return CDCLResult.Sat(assignments.values)
      }
    }
  }
}