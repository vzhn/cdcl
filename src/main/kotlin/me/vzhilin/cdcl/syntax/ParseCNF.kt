package me.vzhilin.cdcl.syntax

import me.vzhilin.cdcl.Clauses
import me.vzhilin.cdcl.MutableClause
import me.vzhilin.cdcl.MutableClauses

fun parseCnf(input: String): Pair<Map<String, Int>, Clauses> {
  val clauses: MutableClauses = mutableSetOf()
  
  val s = mutableMapOf<String, Int>()
  input.split('∧').forEach { 
    val clause: MutableClause = mutableSetOf()
    
    it.removePrefix("(").removeSuffix(")").split("∨").map { literal ->
      if (literal.startsWith("¬")) {
        val letter = literal.removePrefix("¬")
        if (!s.containsKey(letter)) {
          s[letter] = s.size + 1
        }
        clause.add(-s[letter]!!)
      } else {
        if (!s.containsKey(literal)) {
          s[literal] = s.size + 1
        }
        clause.add(s[literal]!!)
      }
    }
    clauses.add(clause)
  }
  return s to clauses
}