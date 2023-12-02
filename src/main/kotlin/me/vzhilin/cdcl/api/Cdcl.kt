package me.vzhilin.cdcl.api

interface Cdcl<C: Clause<Literal>, Literal> {
  /**
   * All clauses
   */
  fun clauses(): LearnedClauses<C, Literal>

  /**
   * Add clauses
   * @param cs clauses
   */
  fun addClauses(cs: Collection<C>)

  /**
   * Finds unassigned literal
   * @return literal, if not all literals have their assignment
   */
  fun unassignedLiteral(): Literal?

  /**
   * Pick new literal and value
   * @return false, if all literals was assigned
   */
  fun decide(): Boolean
  
  /**
   * make variable assignment
   */
  fun assign(lit: Literal, b: Boolean)

  /**
   * picking unit clause and corresponding literal
   */
  fun pickUnitClause(): Pair<Clause<Literal>, Literal>?

  /**
   * First conflicting clause in current decision level
   */
  fun hasConflict(): Clause<Literal>?

  /**
   * Evaluates clause literal in current assignment
   * @return null if assignment has no value for literal
   */
  fun evaluate(clause: Clause<Literal>, lit: Literal): Boolean?

  /**
   * Boolean clause propagation
   */
  fun bcp(): BCPResult
}

enum class BCPResult { OK, CONFLICT }

enum class LiteralPolarity { POSITIVE, NEGATIVE;
  fun invert(): LiteralPolarity {
    return when (this) {
      POSITIVE -> NEGATIVE
      NEGATIVE -> POSITIVE
    }
  }

  fun toBoolean() = when(this) {
    POSITIVE -> true
    NEGATIVE -> false
  }

  companion object {
    fun fromBoolean(v: Boolean) = when(v) {
      true -> POSITIVE
      false -> NEGATIVE
    }
  }
}

sealed class CDCLResult<C: Clause<Literal>, Literal> {
  class Unsat<C: Clause<Literal>, Literal>: CDCLResult<C, Literal>()
  data class Sat<C: Clause<Literal>, Literal>(val assignment: Map<UInt, Boolean>): CDCLResult<C, Literal>()
}

sealed class AnalyzeConflictResult<C: Clause<Literal>, Literal> {
  class Unsatisfiable<C: Clause<Literal>, Literal>: AnalyzeConflictResult<C, Literal>()
  data class Level<C: Clause<Literal>, Literal>(
    val level: UInt,
    val clause: Clause<Literal>
  ): AnalyzeConflictResult<C, Literal>()
}
