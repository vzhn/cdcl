package me.vzhilin.cdcl.impl.simple

import me.vzhilin.cdcl.api.Clause
import me.vzhilin.cdcl.api.LiteralPolarity

class DefaultClause(vs: List<Pair<UInt, LiteralPolarity>> = emptyList()) : Clause<UInt> {
  private val m = mutableMapOf<UInt, LiteralPolarity>()

  init {
    for ((k, v) in vs) {
      setValue(k, v)
    }
  }

  fun setValue(l: UInt, type: LiteralPolarity) {
    m[l] = type
  }
  
  override fun polarity(l: UInt): LiteralPolarity {
    return m.getValue(l)
  }

  override fun resolve(c: Clause<UInt>, l: UInt): DefaultClause {
    val lhs = filter { (k, _) -> k != l }
    val rhs = c.filter { (k, _) -> k != l }
    return DefaultClause(lhs + rhs)
  }

  override fun literals(): Set<UInt> {
    return m.keys
  }

  override fun hasLiteral(lit: UInt): Boolean {
    return m.containsKey(lit)
  }

  override fun iterator(): Iterator<Pair<UInt, LiteralPolarity>> {
    return m.entries.map { (k, v) -> k to v }.iterator()
  }

  override fun toString(): String {
    return m.entries.joinToString(separator = " ") { (k, v) -> "${if (v == LiteralPolarity.POSITIVE) k else -k.toInt()}" }
  }
}