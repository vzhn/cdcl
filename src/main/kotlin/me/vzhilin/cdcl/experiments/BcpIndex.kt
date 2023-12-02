package me.vzhilin.cdcl.experiments

import me.vzhilin.cdcl.api.LiteralPolarity
import me.vzhilin.cdcl.impl.simple.DefaultClause

class BcpIndex {
  private val index = mutableMapOf<Pair<UInt, LiteralPolarity>, MutableSet<ClauseAssignments>>()
  private val units = mutableSetOf<ClauseAssignments>()
  private val conflicts = mutableSetOf<ClauseAssignments>()
  
  fun addClauses(clauses: Collection<DefaultClause>) {
    for (clause in clauses) {
      val ca = ClauseAssignments(clause)
      
      for ((lit, polarity) in clause) {
        val key = lit to polarity        
        if (!index.containsKey(key)) {
          index[key] = mutableSetOf(ca)
        } else {
          index[key]!!.add(ca)
        }
      }
    }
  }

  private fun updatePositive(lit: UInt, b: Boolean) {
    val key = lit to LiteralPolarity.fromBoolean(b)
    val clauses = index[key] ?: return
    units.removeAll(clauses)
    for (c in clauses) {
      c.incTruthly(lit)
    }
  }
  
  private fun updateNegative(lit: UInt, b: Boolean) {
    val key = lit to LiteralPolarity.fromBoolean(b).invert()
    val clauses = index[key] ?: return
    
    for (c in clauses) {
      val status = c.incFalsy(lit)
      when  (status) {
        Status.UNIT -> units.add(c)
        Status.CONFLICT -> conflicts.add(c)
        Status.NONE -> Unit
      }
    }
  }
  
  fun assign(lit: UInt, b: Boolean) {    
    updatePositive(lit, b)
    updateNegative(lit, b)
  }
  
  fun unassign(lit: UInt, b: Boolean) {
    val polarity = LiteralPolarity.fromBoolean(b)
    val positive = lit to polarity
    val negative = lit to polarity.invert()
    
    index[positive]?.forEach { 
      it.decFalsy(lit)
      if (it.unit != null) {
        units.add(it)
      } else {
        units.remove(it)
      }
    }
    index[negative]?.forEach { 
      it.decTruthly(lit)
      if (it.unit != null) {
        units.add(it)
      } else {
        units.remove(it)
      }      
    }
  }

  fun backTrack(unassigned: List<Pair<UInt, Boolean>>) {
    for ((lit, polarity) in unassigned) {
      unassign(lit, polarity)
    }
  }
  
  fun getUnit(): Pair<DefaultClause, UInt>? {
    val v = units.firstOrNull() ?: return null
    return v.clause to v.unit!!
  }
  
  fun getConflict(): DefaultClause? {
    return conflicts.firstOrNull()?.clause
  }
}

enum class Status { UNIT, CONFLICT, NONE }

data class ClauseAssignments(val clause: DefaultClause) {
  val values = mutableMapOf<UInt, Boolean>()  
  var truthCount = 0
  var falsyCount = 0
  val unit: UInt? get() = (clause.literals() - values.keys).firstOrNull()
  
  fun incFalsy(lit: UInt): Status {
    values[lit] = false
    falsyCount++
    return if (truthCount > 0) {
      Status.NONE
    } else {
      when (falsyCount) {
        clause.size() -> Status.CONFLICT
        clause.size() - 1 -> Status.UNIT
        else -> Status.NONE
      }
    }
  }

  fun incTruthly(lit: UInt) {
    values[lit] = true
    truthCount++
  }
  
  fun decFalsy(lit: UInt) {
    values.remove(lit)
    falsyCount--
  }
  
  fun decTruthly(lit: UInt) {
    values.remove(lit)
    truthCount--
  }
}