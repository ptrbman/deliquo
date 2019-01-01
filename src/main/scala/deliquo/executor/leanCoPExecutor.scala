package deliquo

object leanCoPExecutor extends Executor {
  val toolName = "leanCoP"
  val toolCommand = "/home/ptr/Downloads/leancop21/leancop.sh"

  def parseOutput(retVal : Int, stdout : Array[String], stderr : Array[String], time : Long) = {
    if (stderr.head.trim() == "Terminated") {
      Timeout(time)
    } else if (stdout(1) contains "is a Theorem") {
      Theorem(time)
    } else {
      throw new Exception("Unhandled leanCoP result")
    }
  }
}
