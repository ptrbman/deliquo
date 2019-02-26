package deliquo

import Console.{GREEN, RED, RESET, YELLOW, UNDERLINED}
import java.io.File
import scala.collection.mutable.{Set => MSet}

object Deliquo {

  type Benchmark = String
  type ToolResult = Map[Benchmark, (Result , Map[String, String])]
  type ToolName = (String, String)
  type Results = List[(ToolName, ToolResult)]

  def parseCSV(fileName : String) : Option[(ToolName, ToolResult, List[String])] = {
    val lines = scala.io.Source.fromFile(fileName).getLines.toList
    val headerR = "(.*),(.*),(.*)".r
    if (lines.length < 1)
      return None

    val (tool, timeout, datestring) =
      lines(0) match {
        case headerR(a, b, c) => (a, b.toInt, c)
        case _ => return None
      }

    val columns = lines(1).split(",").toList
    assert(columns(0) == "benchmark")
    assert(columns(1) == "result")    

    val thmR = "Theorem\\((\\d+)\\)".r
    val unkR = "Unknown\\((.*)\\)".r
    val toR = "Timeout\\((.*)\\)".r
    val erR = "Error\\((.*)\\)".r            

    val results = 
      (for (l <- lines.drop(2)) yield {
        val data = l.split(",")
        val bm : String = data(0)
        val result : Result = 
          data(1) match {
            case thmR(time) => Theorem(time.toInt)
            case unkR(time) => Unknown(time.toInt)
            case toR(time) => Timeout(time.toInt)
            case erR(time) => Error(time.toInt)
            case str => {
              throw new Exception("Match error:" + str)
            }
          }

        val extraData : Map[String, String] = 
          (for ((k, v) <- columns.drop(2) zip data.drop(2)) yield {
            k -> v
          }).toMap

        bm -> (result, extraData)
      }).toMap

    Some((tool, datestring), results, columns.drop(2))
  }


  def printResult(results : Results, columns : List[String]) = {
    val tools = results.map(_._1)
    val toolResults = results.map(_._2)
    val benchmarks = toolResults.map(_.keys.toSet).fold(Set())(_ ++ _).toList.sorted

    val longestTool = tools.map{ case (tn, ds) => tn.length.max(ds.length)}.max
    val longestBenchmark = benchmarks.map(_.length).max
    val CELL_WIDTH = longestTool+4    


    def res2str(r : Result) : (String, Int, String) = {
      r match {
        case Theorem(time) => {
          val t = "%.3f".format((time.toFloat/1000.0.toFloat))
          (s"${GREEN}Thm${RESET}", 3, t)
        }
        case Timeout(time) => {
          val t = "%.3f".format(time.toFloat/1000.0.toFloat)          
          (s"${RESET}T/o${RESET}", 3, t)
        }          
        case Unknown(time) => {
          val t = "%.3f".format(time.toFloat/1000.0.toFloat)                    
          (s"${YELLOW}Unknown${RESET}", 7, t)
        }
        case Error(time) => {
          val t = "%.3f".format(time.toFloat/1000.0.toFloat)          
          (s"${RED}Error${RESET}", 5, t)
        }
      }
    }

    val header1 =
      (" "*longestBenchmark) + "  " +
        (for ((t, _) <- tools) yield String.format("%-"+ CELL_WIDTH+ "s", "<" + t + ">")).mkString("  ")

    val header2 =
      (" "*longestBenchmark) + "  " +
        (for ((_, ds) <- tools) yield String.format("%-"+ CELL_WIDTH+ "s", "<" + ds + ">")).mkString("  ")    

    val lines =
      for (b <- benchmarks) yield {
        val str1 =
          String.format("%-"+ longestBenchmark + "s", b) + "  " +
            (for (t <- tools.indices) yield {
              val (str, len, time) =
                if (toolResults(t) contains b)
                  res2str(toolResults(t)(b)._1)
                else
                  ("", 0, "")

              str + (" "*(CELL_WIDTH-len))
            }).mkString("  ")

        val str2 =
          String.format("%-"+ longestBenchmark + "s", "time") + "  " +
            (for (t <- tools.indices) yield {
              val (str, len, timeStr) =
                if (toolResults(t) contains b)
                  res2str(toolResults(t)(b)._1)
                else
                  ("", 0, "")              
              timeStr + (" "*(CELL_WIDTH-timeStr.length))
            }).mkString("  ")


        val strs =
          for (c <- columns) yield {
            String.format("%-"+ longestBenchmark + "s", c) + "  " +
              (for (t <- tools.indices) yield {
                val sstr = 
                  if (toolResults(t) contains b)
                    toolResults(t)(b)._2.getOrElse(c, "n/a")
                else
                  ""
                sstr  + (" "*(CELL_WIDTH-sstr.length))
              }).mkString("  ")
          }
        
        (List(str1, str2) ++ strs).mkString("\n")
      }

    // val footer =
    //   ("-"*(longestBenchmark+tools.length*CELL_WIDTH)) + "\n" +
    //   String.format("%-"+ longestBenchmark + "s", "Total:") + "  " +
    //     (for (t <- tools.indices) yield {
    //       val count = toolResults(t).values.count(_.isInstanceOf[Theorem])
    //       val total = benchmarks.length
    //       val str = count + "/" + total
    //       ("%-" + (CELL_WIDTH) + "s").format(str)
    //     }).mkString("  ")

    Console.println(header1)
    Console.println(header2)    
    for (l <- lines)
      Console.println(l)
    // Console.println(footer)
  }

  def logs(args : Array[String]) = {
    val dirs = 
      if (args.length > 0)
        args.toList
      else
        List("logs/")

    val fileNames =
      (for (d <- dirs) yield new File(d).listFiles.filter(_.isFile).filter(_.getName.endsWith(".out")).map(_.toString).toList.sorted).flatten

    val columns = MSet() : MSet[String]
    val results =
      for (f <- fileNames; if parseCSV(f).isDefined) yield {
        val (toolName, toolResults, cols) = parseCSV(f).get
        columns ++= cols.toSet
        toolName -> toolResults
      }

    printResult(results, columns.toList.sorted)
  }


  def run(args : Array[String]) = {
    val executors = Executor.fromXML("tools.xml")
    if (args.length < 3) {
      printHelp("run")
    } else {
      val execs = 
        args(0) match {
          case "all" => executors.values
          case solver => List(executors(solver))
        }

      val inputDir = args(1)
      val timeout = args(2).toInt
      for (ex <- execs)
        ex.runAllConfigs(inputDir, timeout)
    }
  }


  def printHelp(cat : String = "") = {
    cat match {
      case "" => {
        println("Usage")
        println("\tLOGS")
        println("\tRUN")
      }

      case "run" => {
        Console.println(s"Usage: ${YELLOW}run ${RESET}solver input-directory timeout(s)")
      }
    }
  }

  def main(args : Array[String]) = {

    if (args.isEmpty) {
      printHelp()
    } else {
      args(0) match {
        case "logs" => logs(args.tail)
        case "run" => run(args.tail)
        case _ => printHelp()
      }
    }
    Console.println(s"${RESET}${GREEN}Goodbye!${RESET}")
  }
}
