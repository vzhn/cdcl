import me.vzhilin.cdcl.api.CDCLResult
import me.vzhilin.cdcl.api.LiteralPolarity
import me.vzhilin.cdcl.impl.simple.DefaultClause
import me.vzhilin.cdcl.impl.simple.DefaultCdcl
import java.io.File
import kotlin.math.abs

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
  
  val cdcl = DefaultCdcl()
  val cs = task.clauses.map { it -> 
    val clause = DefaultClause()
    it.forEach { 
      clause.setValue(abs(it).toUInt(), if (it > 0) LiteralPolarity.POSITIVE else LiteralPolarity.NEGATIVE) 
    }
    return@map clause
  }
  cdcl.addClauses(cs)
  val result = cdcl.solve()
  
  if (result is CDCLResult.Sat) {
    val assignment = result.assignment
    val failedClauses = task.check(assignment)
    if (failedClauses.isNotEmpty()) {
      println("c assertion failed: $failedClauses")
    } else {
      println("s SATISFIABLE")
      println("v " + assignment.toList().sortedBy { (k) -> k } .joinToString(separator = " "))
    }
  } else {
    println("s UNSAT")
  }
}