import me.vzhilin.cdcl.Sat
import me.vzhilin.cdcl.cdcl
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
  val result = cdcl(task.clauses)
  if (result is Sat) {
    val assignment = result.a
    val failedClauses = task.check(assignment.vars)
    if (failedClauses.isNotEmpty()) {
      println("c assertion failed: $failedClauses")
    } else {
      println("s SATISFIABLE")
      println("v " + assignment.vars.toList().sortedBy(Math::abs).joinToString(separator = " "))
    }
  } else {
    println("s UNSAT")
  }
}