package me.vzhilin.cdcl

typealias MutableClauses = MutableSet<Set<Int>>
typealias Clauses = Set<Set<Int>>

typealias MutableClause = MutableSet<Int>
typealias Clause = Set<Int>

class Assignment {
  val values get() = assignments.keys
  
  private val assignments = mutableMapOf<Int, UInt>() // assignment -> decision level
  
  fun size() = assignments.size

  fun haveLiteral(n: Int) = assignments.contains(n) || assignments.contains(-n)  
  
  fun eval(clause: Clause, strict: Boolean = true): Boolean {
    for (n in clause) {
      if (strict && !haveLiteral(n)) throw AssertionError("missing assignment for $n")
      if (assignments.contains(n)) {
        return true
      }
    }
    return false
  }
  
  fun unitVar(c: Clause): Int? {
    val undefinedLiterals = c.filterNot(::haveLiteral)
    if (undefinedLiterals.size == 1) return undefinedLiterals.first()
    return null
  }
  
  fun isUnit(clause: Clause): Boolean {
    val undef = clause.filter { !haveLiteral(it) }
    if (undef.size != 1) return false
    val literal = undef[0]
    return !eval(clause - literal, true)
  }

  fun extend(n: Int, decisionLevel: UInt) {
    this.assignments[n] = decisionLevel
  }

  fun falsyClauses(clauses: Collection<Set<Int>>) = clauses.filter { c ->
    c.all { v ->
      assignments.contains(-v)
    }
  }

  fun isEmpty(): Boolean {
    return this.assignments.isEmpty()
  }

  fun remove(n: Int) {
    this.assignments.remove(n)
    this.assignments.remove(-n)
  }

  fun level(it: Int): UInt {
    return (this.assignments[it] ?: this.assignments[-it])!!
  }

  override fun toString(): String {
    return assignments.keys.toString()
  }
}
