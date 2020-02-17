package deliquo

import scala.xml.XML
import java.io._
import sys.process._
import scala.Console
import scala.Console._

object Tool {

  def loadTool(xml_ : scala.xml.Node) : Tool = {
    val toolName = (xml_ \ "name").text
    val toolPath = (xml_ \ "path").text
    val toolCommand  = (xml_ \ "command").text
    val arguments =
      for (arg <- xml_ \ "arguments" \ "argument") yield arg.text


    new Tool(toolName, toolPath + "/" + toolCommand, arguments.toList) {

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
        stderr : Array[String],
        time : Long) : Map[String, String] = {

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

        if (retVal == 124)
          result =  "Timeout"

        if (result == "") {
          println("<<STDOUT>>")
          println(stdout.mkString("\n"))
          println("<<STDERR>>")
          println(stderr.mkString("\n"))
          println("RETVAL: " + retVal)
          throw new Exception("Unhandled " + name + " result")
        }

        val extra : List[(String, String)] =
          (for ((ex, i) <- (extras zipWithIndex).toList) yield {
            if (extras(i) == "") {
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

abstract class Tool(val name : String, command : String, arguments : List[String] = List()) {

  override def toString() : String = {
    return name
  }
  // val specials : List[String]
  // val xml = None : Option[scala.xml.Node]
  def parseOutput(retVal : Int, stdout : Array[String], stderr : Array[String], time : Long) : Map[String, String]

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

  def execute(input : String, timeout : Int, extraOptions : List[String] = List(), output : String = "") : String = {
    D.dboxprintln(name + " " + extraOptions.mkString("&") + "(" + output + ")", "YELLOW")
    val inputFile = new File(input)


    import java.text.SimpleDateFormat
    import java.util.Calendar

    val dateString = new SimpleDateFormat("MMdd-hhmm").format(Calendar.getInstance.getTime)

    // val directory = new File("logs/");
    // if (!directory.exists())
    //   directory.mkdir();

    // val outFileName =
    //   if (output != "")
    //     "logs/" + output + ".out"
    //   else
    //     "logs/" + name + "-" + dateString + extraOptions.mkString("&") + ".out"

    // println("Writing to: \"" + outFileName + "\"")
    // val pw = new PrintWriter(new File(outFileName))

    // if (output == "")
    //   pw.write(List(name+extraOptions.mkString("&"), timeout, dateString).mkString(",") + "\n")
    // else
    //   pw.write(List(output, timeout, dateString).mkString(",") + "\n")
    // pw.write((List("benchmark","result") ++ specials).mkString(",") + "\n")
    // pw.flush()
    // val resultMap = 
    //   for (f <- files) yield {
    val START_TIME = System.currentTimeMillis
    val (exitVal, stdout, stderr) =
      executeCommand(command :: (arguments ++ extraOptions ++ List(inputFile.getPath)), timeout)
    val END_TIME = System.currentTimeMillis

    val time = END_TIME - START_TIME
    val result = parseOutput(exitVal, stdout, stderr, time)
    // val Instance(name, result, extraData) = parseOutput(exitVal, stdout, stderr, time)
    // val line = (List(inputFile.getName, result) ++ specials.map(extraData.getOrElse(_, "n/a"))).mkString(",") + "\n"
    // val line = (List(inputFile.getName, result)).mkString(",") + "\n"    
    // println(line)
    // pw.write(line)
    // pw.flush()
    // f.getName -> result
    //   }

    println(result)
    // pw.close()
    result("result")
  }

  // def executeAllConfigs(inputDir : String, timeout : Int, output : String = "") = {

  //   def allConfigs(list : List[List[String]]) : List[List[String]] = {
  //     list match {
  //       case Nil => List(List())
  //       case param :: tail => {
  //         val tailConfigs = allConfigs(tail)
  //         (param.map{ p => tailConfigs.map(p :: _) }).flatten
  //       }
  //     }
  //   }

  //   val params = xml.get \ "parameters" \ "parameter"
  //   val commands = 
  //     (for (p <- params) yield {
  //         (for (c <- p \ "commands" \ "command") yield {
  //         c.text
  //       }).toList
  //     }).toList
  //   for (cfg <- allConfigs(commands)) {
  //     execute(inputDir, timeout, cfg, output)
  //   }
  // }
}
