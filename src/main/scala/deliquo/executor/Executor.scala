package deliquo

import java.io._
import sys.process._

abstract class Executor {
  val toolName : String
  val toolCommand : String
  val options : List[String] = List()
  def parseOutput(retVal : Int, stdout : Array[String], stderr : Array[String], time : Long) : Result

  def runCommand(cmd: Seq[String], timeout : Int): (Int, Array[String], Array[String]) = {
    val stdoutStream = new ByteArrayOutputStream
    val stderrStream = new ByteArrayOutputStream
    val stdoutWriter = new PrintWriter(stdoutStream)
    val stderrWriter = new PrintWriter(stderrStream)

    val toCmd = List("timeout", timeout + "s") ++ cmd

    val exitValue = toCmd.!(ProcessLogger(stdoutWriter.println, stderrWriter.println))
    stdoutWriter.close()
    stderrWriter.close()
    (exitValue, stdoutStream.toString.split("\n"), stderrStream.toString.split("\n"))
  }

  def run(inputDir : String, timeout : Int) = {
    D.dboxprintln(toolName, "YELLOW")
    val files = (new File(inputDir)).listFiles.filter(_.isFile).toList

    import java.text.SimpleDateFormat
    import java.util.Calendar

    val dateString = new SimpleDateFormat("MMdd-hhmm").format(Calendar.getInstance.getTime)
    val outFileName = "logs/" + toolName + "-" + dateString + ".out"
    println("Writing to: \"" + outFileName + "\"")
    val pw = new PrintWriter(new File(outFileName))

    pw.write(List(toolName, timeout, dateString).mkString(",") + "\n")
    val resultMap = 
      for (f <- files) yield {
        println("\t" + f)

        val START_TIME = System.currentTimeMillis
        val (exitVal, stdout, stderr) = runCommand(toolCommand :: (options ++ List(f.toString)), timeout)
        val END_TIME = System.currentTimeMillis

        val time = END_TIME - START_TIME
        val result =
          if (exitVal == 124) {
            Timeout(time)
          } else {
            parseOutput(exitVal, stdout, stderr, time)
          }
        val line = List(f.getName, result).mkString(",") + "\n"
        pw.write(line)
        f.getName -> result
      }

    pw.close()
  }
}
