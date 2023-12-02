
data class Task(val vars: Int, val clauses: Set<Set<Int>>) {
  fun check(assignment: Map<UInt, Boolean>): Set<Set<Int>> {
    val result = assignment.map { (k, v)  -> 
      when (v) {
        true -> k.toInt()
        false -> -k.toInt()
      }
    }
    return clauses.filterNot { it.any(result::contains) }.toSet()
  }
}

fun parse(input: String): Task {
  var vars = 0
  val formulas = mutableSetOf<Set<Int>>()

  for (line in input.lines()) {
    if (line.isBlank()) {
      continue
    }

    if (line.startsWith("c") || line.startsWith("%") || line.startsWith("0")) {
      continue
    }

    if (line.startsWith("p")) {
      val (_, type, nv, _) = line.split(' ')
      if (type != "cnf") {
        throw IllegalArgumentException("$type is not supported")
      }
      vars = nv.toInt()
      continue
    }

    formulas.add(line.trim().split(' ').map(String::toInt).filterNot { it == 0 }.toSet())
  }
  return Task(vars, formulas)
}