import java.io.File

fun main(args: Array<String>) {
  val file = File(args[0])
  if (file.isFile) {
    processFile(file)
  } else {
    val listFiles = file.listFiles {
        f -> f.isFile && f.name.endsWith(".cnf")
    }
    listFiles?.forEach {
      processFile(it)
      println()
    }
  }
}

fun processFile(file: File) {
  println("c file: ${file.absoluteFile}")
  val task = parse(file.readText())
  val result = Cdcl(task.clauses).solve()
  
  if (result is CDCLResult.Sat) {
    val assignment = result.assignment
    val failedClauses = task.check(assignment)
    if (failedClauses.isNotEmpty()) {
      println("c assertion failed: $failedClauses")
    } else {
      println("s SATISFIABLE")
      println("v " + assignment.toList().sortedBy(Math::abs).joinToString(separator = " "))
    }
  } else {
    println("s UNSAT")
  }
}