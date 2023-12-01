package me.vzhilin.cdcl.api

interface LearnedClauses<C: Clause<Literal>, Literal> : Iterable<C> {
  fun include(c: C)
  fun include(cs: Collection<C>)
  fun literals(): Collection<Literal>
}