package me.vzhilin.cdcl.impl.simple

import me.vzhilin.cdcl.api.*

class DefaultLearnedClauses: LearnedClauses<DefaultClause, UInt> {
  private val clauses = mutableSetOf<DefaultClause>()

  override fun include(cs: Collection<DefaultClause>) {
    cs.forEach(clauses::add)
  }

  override fun literals(): Set<UInt> {
    val res = mutableSetOf<UInt>()
    for (c in clauses) {
      res.addAll(c.literals())
    }
    return res
  }

  override fun iterator(): Iterator<DefaultClause> {
    return clauses.iterator()
  }
}