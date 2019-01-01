package deliquo

object bctExecutor extends Executor {
  val toolName = "BCT"
  val toolCommand = "scala"
  override val options = List("/home/ptr/Projects/bct/bin/bct.jar", "file")

  def parseOutput(retVal : Int, stdout : Array[String], stderr : Array[String], time : Long) = {
    if (stdout.exists(_ contains "Incomplete search")) {
      Unknown(time)
    } else {

      for (l <- stdout)
        println("STDOUT: " + l)

      for (l <- stderr)
        println("STDERR: " + l)

      println("RETVAL: " + retVal)
      throw new Exception("Unhandled bct result")
    }
  }
}
