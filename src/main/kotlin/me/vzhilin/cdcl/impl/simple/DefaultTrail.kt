package me.vzhilin.cdcl.impl.simple

import me.vzhilin.cdcl.api.Clause
import me.vzhilin.cdcl.api.LiteralPolarity
import me.vzhilin.cdcl.api.Trail

class DefaultTrail : Trail<DefaultClause, UInt> {
  private enum class Type { DECISION, BCP, CONFLICT }  
  private data class Node(
    val type: Type,
    val lit: UInt,
    val v: Boolean,
    val antecedent: DefaultClause
  )
  
  private val trail = mutableMapOf<UInt, MutableList<Node>>()
  private var decisionLevel = 0u

  private fun add(type: Type, lit: UInt, v: Boolean, antecedent: DefaultClause) {
    val i = when(v) {
      true -> lit.toInt()
      false -> -lit.toInt()
    }
    val node = Node(type, lit, v, antecedent)
    
    if (!trail.contains(decisionLevel)) {
      trail[decisionLevel] = mutableListOf(node)
    } else {
      trail[decisionLevel]!!.add(node)
    }
  }
  
  override fun addDecision(lit: UInt, v: Boolean) {
    ++decisionLevel
    
    val antecedent = DefaultClause().also {
      it.setValue(lit, 
        if (v)
          LiteralPolarity.POSITIVE 
        else
          LiteralPolarity.NEGATIVE
      ) 
    }
    add(Type.DECISION, lit, v, antecedent)
  }

  override fun addBcp(lit: UInt, v: Boolean, antecedent: DefaultClause) {
    add(Type.BCP, lit, v, antecedent)
  }

  override fun addConflict(lit: UInt, v: Boolean, antecedent: DefaultClause) {
    add(Type.CONFLICT, lit, v, antecedent)
  }

  override fun currentDecisionLevel(): UInt {
    return decisionLevel
  }

  override fun backTrack(level: UInt) {
    for (lv in level + 1u .. decisionLevel) {
      trail.remove(lv)
    }
    decisionLevel = level
  }

  override fun currentLevelAssignments(): Set<UInt> {
    return trail[decisionLevel]!!.map { it.lit }.toSet()
  }

  override fun currentConflictingClause(): DefaultClause {
    val (type, _, _, antecedent) = trail[decisionLevel]!!.last()
    if (type != Type.CONFLICT) throw AssertionError("last trail item is not conflict")
    return antecedent
  }

  override fun antecedent(lit: UInt): DefaultClause {
    return trail.flatMap { it.value }.first { it.lit == lit }.antecedent    
  }

  override fun lastAssignedLiteral(cl: Clause<UInt>): UInt {
    return trail
      .flatMap { it.value }
      .mapNotNull { node -> 
        if (cl.hasLiteral(node.lit)) {
          node.lit
        } else {
          null
        }
    }.last()
  }

  private fun currentLevelTrail(): List<Node> {
    return trail[decisionLevel]!!
  }
  
  override fun firstUip(): Pair<UInt, LiteralPolarity> {
    val trailPart = currentLevelTrail().asReversed()
    val currentAssignments = currentLevelAssignments()

    val ns = mutableSetOf<UInt>()

    for ((type, lit, _, antecedent) in trailPart) {
      if (type != Type.CONFLICT) {
        ns.remove(lit)
        if (ns.isEmpty()) {
          return lit to antecedent.polarity(lit)
        }
      }
      ns.addAll((antecedent.literals() - lit).intersect(currentAssignments))
    }

    throw AssertionError("could not find UIP")
  }
}