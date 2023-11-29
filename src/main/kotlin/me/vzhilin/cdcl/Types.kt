package me.vzhilin.cdcl

typealias MutableClauses = MutableSet<Set<Int>>
typealias Clauses = Set<Set<Int>>

typealias MutableClause = MutableSet<Int>
typealias Clause = Set<Int>

data class Assignment(val vars: MutableSet<Int>) {
  fun size() = vars.size

  fun haveLiteral(n: Int) = vars.contains(n) || vars.contains(-n)
  
  fun haveLiterals(c: Clause) = c.all(::haveLiteral)
  
  fun eval(clause: Clause, strict: Boolean = true): Boolean {
    for (n in clause) {
      if (strict && !haveLiteral(n)) throw AssertionError("missing assignment for $n")
      if (vars.contains(n)) {
        return true
      }
    }
    return false
  }
  
  fun eval(n: Int): Boolean {
    if (!haveLiteral(n)) throw AssertionError("missing assignment for $n")
    return vars.contains(n)
  }
  
  fun unitVar(c: Clause): Int? {
    val undefinedLiterals = c.filterNot(::haveLiteral)
    if (undefinedLiterals.size == 1) return undefinedLiterals.first()
    return null
  }
  
  fun isUnit(clause: Clause): Boolean {
    if (clause.size < 2) return false
    val undef = clause.filter { !haveLiteral(it) }
    if (undef.size != 1) return false
    val literal = undef[0]
    return !eval(clause - literal, true)
  }

  fun findUnit(clauses: Set<Set<Int>>): Pair<Clause, Int>? {
    val clause = clauses.firstOrNull(::isUnit) ?: return null
    return clause to unitVar(clause)!!
  }

  fun extend(ns: Collection<Int>) {
    this.vars.addAll(ns)
  }

  fun extend(n: Int) {
    this.vars.add(n)
  }

  fun unassign(n: Int) {
    this.vars.remove(n)
  }

  fun falsifiesClause(clauses: Set<Set<Int>>): Boolean {
    return !clauses.all { !haveLiterals(it) || eval(it, false) }
  }

  fun pickUndefined(f: Clauses): Int {
    val first = f.flatten().toSet().first { !haveLiteral(it) }
    return first
  }


  fun isEmpty(): Boolean {
    return this.vars.isEmpty()
  }
}
