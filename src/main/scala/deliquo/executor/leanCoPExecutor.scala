package deliquo

object leanCoPExecutor extends Executor {
  val toolName = "leanCoP"
  val toolCommand = "leancop.sh"

  def parseOutput(retVal : Int, stdout : Array[String], stderr : Array[String], time : Long) = {

      for (l <- stdout)
        println("STDOUT: " + l)

      for (l <- stderr)
        println("STDERR: " + l)

      println("RETVAL: " + retVal)

    if (stderr.head.trim() == "Terminated") {
      Timeout(time)
    } else if ((stdout.length > 1) & (stdout(1) contains "is a Theorem")) {
      Theorem(time)
    } else {
      for (l <- stdout)
        println("STDOUT: " + l)

      for (l <- stderr)
        println("STDERR: " + l)

      println("RETVAL: " + retVal)
      throw new Exception("Unhandled leanCoP result")
    }
  }
}
