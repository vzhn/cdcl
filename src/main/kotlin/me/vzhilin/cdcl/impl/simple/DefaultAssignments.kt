package me.vzhilin.cdcl.impl.simple

import me.vzhilin.cdcl.api.Assignments
import me.vzhilin.cdcl.api.Clause
import me.vzhilin.cdcl.api.LiteralPolarity

class DefaultAssignments: Assignments<Clause<UInt>, UInt> {
  private val levelToLit = mutableMapOf<UInt, MutableSet<UInt>>()
  private val litToLevel = mutableMapOf<UInt, UInt>()
  private val litToValue = mutableMapOf<UInt, Boolean>()
  
  override val values: Map<UInt, Boolean> get() = litToValue
  
  override fun assign(level: UInt, literal: UInt, value: Boolean) {
    if (litToValue.contains(literal)) {
      throw AssertionError("already have literal: $literal")
    }
    
    if (!levelToLit.contains(level)) {
      levelToLit[level] = mutableSetOf(literal)
    } else {
      val literals = levelToLit[level]!!
      literals.add(literal)
    }
    litToLevel[literal] = level
    litToValue[literal] = value
  }

  override fun unassign(literal: UInt) {
    litToValue.remove(literal)
  }

  override fun literals(): Set<UInt> {
    return litToValue.keys
  }

  override fun backTrack(level: UInt): List<Pair<UInt, Boolean>> {
    val unassigned = mutableListOf<Pair<UInt, Boolean>>()
    
    for (lv in level + 1u..levelToLit.keys.max()) {
      levelToLit[lv]?.forEach { lit -> 
        litToLevel.remove(lit)
        val value = litToValue.remove(lit)!!
        unassigned.add(lit to value)
      }
      
      levelToLit.remove(lv)
    }
    
    return unassigned
  }

  override fun level(it: UInt): UInt {    
    return litToLevel[it]!!
  }

  override fun value(literal: UInt, polarity: LiteralPolarity): Boolean? {
    val b = litToValue[literal] ?: return null
    return when (polarity) {
      LiteralPolarity.POSITIVE -> b
      LiteralPolarity.NEGATIVE -> b.not()
    }
  }

  override fun evaluate(clause: Clause<UInt>, literal: UInt): Boolean? {
    val assignedValue = litToValue[literal] ?: return null
    val type = clause.polarity(literal)
    return when (type) {
      LiteralPolarity.POSITIVE -> assignedValue
      LiteralPolarity.NEGATIVE -> !assignedValue
    }
  }
  
  override fun evaluate(clause: Clause<UInt>): Boolean? {
    for ((lit) in clause) {
      val v = evaluate(clause, lit) ?: return null
      if (v) {
        return true
      }
    }
    return false
  }
}