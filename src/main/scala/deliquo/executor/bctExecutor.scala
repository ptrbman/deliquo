package deliquo

object bctExecutor extends Executor {
  val toolName = "BCT"
  val toolCommand = "java"
  override val options = List("-jar", "/home/peter/bin/bct.jar", "file")

  def parseOutput(retVal : Int, stdout : Array[String], stderr : Array[String], time : Long) = {

      for (l <- stdout)
        println("STDOUT: " + l)

      for (l <- stderr)
        println("STDERR: " + l)

      println("RETVAL: " + retVal)

    if (stdout.exists(_ contains "Incomplete search")) {
      Unknown(time)
    } else if (stdout.exists(_ contains "Timeout")) {
      Timeout(time)
    } else {
      throw new Exception("Unhandled bct result")
    }
  }
}
