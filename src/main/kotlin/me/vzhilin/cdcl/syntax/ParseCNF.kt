package me.vzhilin.cdcl.syntax

fun parseCnf(input: String): Pair<Map<String, Int>, Set<Set<Int>>> {
  val clauses: MutableSet<Set<Int>> = mutableSetOf()
  
  val s = mutableMapOf<String, Int>()
  input.split('∧').forEach { 
    val clause: MutableSet<Int> = mutableSetOf()
    
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