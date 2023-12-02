package me.vzhilin.cdcl.api

interface Assignments<C: Clause<Literal>, Literal> {
  fun literals(): Set<UInt>
  fun assign(level: UInt, literal: Literal, value: Boolean)
  fun unassign(literal: Literal)
  fun value(literal: Literal, polarity: LiteralPolarity): Boolean?
  fun evaluate(clause: C, literal: Literal): Boolean?
  fun evaluate(clause: C): Boolean?
  fun level(it: Literal): UInt
  fun backTrack(level: UInt): List<Pair<UInt, Boolean>>
  
  val values: Map<Literal, Boolean>
}