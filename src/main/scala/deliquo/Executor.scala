package deliquo

import scala.xml.XML
import java.io._
import sys.process._
import scala.Console
import scala.Console._



object Executor {

  def createSolver(xml_ : scala.xml.Node) : (String, Executor) = {
    val name = (xml_ \ "toolName").text
    (name, new Executor {
      override val xml = Some(xml_)      
      override val toolName = (xml_ \ "toolName").text
      override val toolCommand = (xml_ \ "toolCommand").text

      val px = xml_ \ "parser"
      val (theoremStr, invalidStr, unknownStr, errorStr, timeoutStr) =
        ((px \ "theorem").text,
          (px \ "invalid").text,
          (px \ "unknown").text,
          (px \ "error").text,
          (px \ "timeout").text)

      val specials_ = 
        (for (sp <- xml_ \ "parser" \ "specials" \ "special") yield {
          val name = (sp \ "name").text
          val regex = (sp \ "regex").text.r
          name -> regex
        }).toMap

      override val specials = specials_.keys.toList

      override val options = (xml_ \ "options" \ "option").map(_.text).toList

      override def parseOutput(
        retVal : Int,
        stdout : Array[String],
        stderr : Array[String],
        time : Long) : Instance = {

        import scala.collection.mutable.{Map => MMap}
        val extraData = MMap() : MMap[String, String]
        for (l <- stdout) {
          for ((name, regex) <- specials_) {
            val m = regex.findFirstMatchIn(l)
            if (m.isDefined)
              extraData += name -> m.get.group(1)
          }
        }

        val result = 
          if (retVal == 124) {
            Timeout(time)
          } else if (theoremStr != "" && stdout.exists(_ contains theoremStr)) {
            Theorem(time)
          } else if (invalidStr != "" && stdout.exists(_ contains invalidStr)) {
            Invalid(time)
          } else if (unknownStr != "" && stdout.exists(_ contains unknownStr)) {
            Unknown(time)
          } else if (errorStr != "" && stdout.exists(_ contains errorStr)) {
            Error(time)
          } else if (timeoutStr != "" && stdout.exists(_ contains timeoutStr)) {
            Timeout(time)
          } else {
            for (l <- stdout)
              println("STDOUT: " + l)
            for (l <- stderr)
              println("STDERR: " + l)
            println("RETVAL: " + retVal)
            throw new Exception("Unhandled " + name + " result")
          }

        println("\t" + result)
        Instance(toolName, result, extraData.toMap)
      }
    })
  }

  def fromXML(fileName : String) : Map[String, Executor] = {
    println("Loading: " + fileName)
    val xml = XML.loadFile(fileName)
    (for (solver <- xml \ "solver") yield {
      createSolver(solver)
    }).toMap
  }
}

abstract class Executor {
  val toolName : String
  val toolCommand : String
  val options : List[String] = List()
  val specials : List[String]
  val xml = None : Option[scala.xml.Node]
  def parseOutput(retVal : Int, stdout : Array[String], stderr : Array[String], time : Long) : Instance

  def runCommand(cmd: Seq[String], timeout : Int): (Int, Array[String], Array[String]) = {
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

  def run(inputDir : String, timeout : Int, extraOptions : List[String] = List(), tag : String = "") = {
    D.dboxprintln(toolName + " " + tag, "YELLOW")
    val files = (new File(inputDir)).listFiles.filter(_.isFile).toList

    import java.text.SimpleDateFormat
    import java.util.Calendar

    val dateString = new SimpleDateFormat("MMdd-hhmm").format(Calendar.getInstance.getTime)

    val directory = new File("logs/");
    if (!directory.exists())
      directory.mkdir();

    val outFileName = "logs/" + toolName + "-" + dateString + tag + ".out"
    println("Writing to: \"" + outFileName + "\"")
    val pw = new PrintWriter(new File(outFileName))

    pw.write(List(toolName+tag, timeout, dateString).mkString(",") + "\n")
    pw.write((List("benchmark","result") ++ specials).mkString(",") + "\n")
    pw.flush()
    val resultMap = 
      for (f <- files) yield {

        val START_TIME = System.currentTimeMillis
        val (exitVal, stdout, stderr) =
          runCommand(toolCommand :: (options ++ extraOptions ++ List(f.getPath)), timeout)
        val END_TIME = System.currentTimeMillis

        val time = END_TIME - START_TIME
        val Instance(toolName, result, extraData) = parseOutput(exitVal, stdout, stderr, time)
        val line = (List(f.getName, result) ++ specials.map(extraData.getOrElse(_, "n/a"))).mkString(",") + "\n"
        pw.write(line)
        pw.flush()
        f.getName -> result
      }

    pw.close()
  }

  def runAllConfigs(inputDir : String, timeout : Int) = {

    def allConfigs(list : List[List[String]]) : List[List[String]] = {
      list match {
        case Nil => List(List())
        case param :: tail => {
          val tailConfigs = allConfigs(tail)
          (param.map{ p => tailConfigs.map(p :: _) }).flatten
        }
      }
    }

    val params = xml.get \ "parameters" \ "parameter"
    val commands = 
      (for (p <- params) yield {
          (for (c <- p \ "commands" \ "command") yield {
          c.text
        }).toList
      }).toList
    for (cfg <- allConfigs(commands)) {
      run(inputDir, timeout, cfg, cfg.mkString("&"))
    }
  }
}
