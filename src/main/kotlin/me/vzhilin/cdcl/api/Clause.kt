package me.vzhilin.cdcl.api

interface Clause<Literal> : Iterable<Pair<Literal, LiteralPolarity>>  {
  fun resolve(c: Clause<Literal>, l: Literal): Clause<Literal>
  fun literals(): Set<Literal>
  fun polarity(l: Literal): LiteralPolarity
  fun hasLiteral(lit: Literal): Boolean
  fun size(): Int
  
  fun isEmpty() = !iterator().hasNext()
}