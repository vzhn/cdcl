package me.vzhilin.cdcl.api

interface Trail<C: Clause<Literal>, Literal> {
  fun addDecision(lit: Literal, v: Boolean)
  fun addBcp(lit: Literal, v: Boolean, antecedent: C)
  fun addConflict(lit: Literal, v: Boolean, antecedent: C)
  
  fun currentDecisionLevel(): UInt
  fun currentLevelAssignments(): Collection<Literal>
  fun currentConflictingClause(): C
  fun lastAssignedLiteral(cl: Clause<Literal>): Literal
  fun antecedent(lit: Literal): C
  fun firstUip(): Pair<Literal, LiteralPolarity>
  
  fun backTrack(level: UInt)
}