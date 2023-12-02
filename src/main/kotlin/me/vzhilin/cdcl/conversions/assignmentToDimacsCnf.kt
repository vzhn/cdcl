package me.vzhilin.cdcl.conversions

/**
 * Converting assignment to DIMACS CNF
 */
fun Map<UInt, Boolean>.assignmentToDimacsCnf(): List<Int> {
  val result = mutableListOf<Int>()
  for ((lit, value) in this.toList().sortedBy(Pair<UInt, Boolean>::first)) {
    result.add(if (value) lit.toInt() else -lit.toInt())
  }
  return result
}