package deliquo

import scala.xml.XML
import java.io.{ByteArrayOutputStream, File, PrintWriter}
import sys.process._
import scala.Console.{RESET, YELLOW}
import scala.language.postfixOps

object Tool {

  // A tool is loaded from an XML-file
  def loadTool(xml_ : scala.xml.Node) : Tool = {
    val toolName = (xml_ \ "name").text
    val toolPath = (xml_ \ "path").text
    val toolCommand  = (xml_ \ "command").text
    val arguments =
      for (arg <- xml_ \ "arguments" \ "argument") yield arg.text

    val options =
        for (opt <- xml_ \ "options" \ "option") yield {
          val name = (opt \ "name").text
          val values = 
            for (value <- opt \ "values" \ "value") yield {
              val name = (value \ "name").text
              val arg = (value \ "argument").text
              (name, arg)
            }

          (name, values.toList)
      }

    println(options.mkString("\n"))
    
    new Tool(toolName, toolPath + "/" + toolCommand, arguments.toList, options.toList) {

      val resultMap =
        for (r <- xml_ \"parser" \ "results" \ "result") yield {
          ((r \ "regex").text).r -> (r \ "value").text
        }


      val extraMap =
        for (r <- xml_ \ "parser" \ "extras" \ "extra") yield {
          ((r \ "regex").text.r) -> ((r \ "name").text)
        }

      override def parseOutput(
        retVal : Int,
        stdout : Array[String],
        stderr : Array[String]) : Map[String, String] = {

        var result = ""
        var extras = Array.ofDim[String](extraMap.length)

        for (l <- stdout) {
          if (result == "")
            for ((regex, value) <- resultMap)
              if (regex.findFirstIn(l).isDefined)
                result = value

          for (i <- 0 until extraMap.length; if extras(i) == null) {
            val (regex, value) = extraMap(i)
            val find = regex.findFirstMatchIn(l)
            if (find.isDefined)
              extras(i) = find.get.group(1)
          }
        }

        var terminated = false

        if (retVal == 1) {
          result = "Error"
          terminated = true
        }

        if (retVal == 124) {
          result =  "Timeout"
          terminated = true
        }


        if (!terminated && result == "") {
          println("<<STDOUT>>")
          println(stdout.mkString("\n"))
          println("<<STDERR>>")
          println(stderr.mkString("\n"))
          println("RETVAL: " + retVal)
          throw new Exception("Unhandled " + name + " result")
        }

        val extra : List[(String, String)] =
          (for ((ex, i) <- (extras zipWithIndex).toList) yield {
            if (!terminated && extras(i) == "") {
              println("<<STDOUT>>")
              println(stdout.mkString("\n"))
              println("<<STDERR>>")
              println(stderr.mkString("\n"))
              println("RETVAL: " + retVal)
              throw new Exception("Unhandled " + extraMap(i)._2 + " extra")
            }

            extraMap(i)._2 -> ex
          })

        (("result",result) :: extra).toMap
      }
    }
  }

  def fromXML(fileName : String) : Tool = {
    println("Loading: " + fileName)
    val xml = XML.loadFile(fileName)
    loadTool(xml)
  }
}



abstract class Tool(val name : String, command : String, arguments : List[String], val options : List[(String, List[(String, String)])]) {

  override def toString() : String = {
    return name
  }

  def parseOutput(retVal : Int, stdout : Array[String], stderr : Array[String]) : Map[String, String]

  def executeCommand(cmd: Seq[String], timeout : Int): (Int, Array[String], Array[String]) = {
    val stdoutStream = new ByteArrayOutputStream
    val stderrStream = new ByteArrayOutputStream
    val stdoutWriter = new PrintWriter(stdoutStream)
    val stderrWriter = new PrintWriter(stderrStream)

    val toCmd = List("timeout", timeout + "s") ++ cmd
    Console.println(s"${YELLOW}" + toCmd.mkString(" ") + s"${RESET}")
    val exitValue = toCmd.!(ProcessLogger(stdoutWriter.println, stderrWriter.println))
    stdoutWriter.close()
    stderrWriter.close()
    (exitValue, stdoutStream.toString.split("\n"), stderrStream.toString.split("\n"))
  }

  def execute(input : String, timeout : Int, extraOptions : List[String] = List(), output : String = "") : Instance = {
    D.dboxprintln(name + " " + extraOptions.mkString("&") + "(" + output + ")", "YELLOW")
    val inputFile = new File(input)

    val START_TIME = System.currentTimeMillis
    val (exitVal, stdout, stderr) =
      executeCommand(command :: (arguments ++ extraOptions ++ List(inputFile.getPath)), timeout)
    val END_TIME = System.currentTimeMillis

    val time = END_TIME - START_TIME
    val result = parseOutput(exitVal, stdout, stderr)

    println(result)
    Instance(name, input, (Map(("time",time.toString)) ++ result))
  }
}
