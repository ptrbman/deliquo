package deliquo

import Console.{GREEN, RED, RESET, YELLOW, UNDERLINED}
import java.io.File

object Deliquo {

  type Benchmark = String
  type ToolResult = Map[Benchmark, Result]
  type ToolName = (String, String)
  type Results = List[(ToolName, ToolResult)]

  def parseCSV(fileName : String) : (ToolName, ToolResult) = {
    val lines = scala.io.Source.fromFile(fileName).getLines.toList
    val headerR = "(.*),(.*),(.*)".r
    val (tool, timeout, datestring) =
      lines.head match {
        case headerR(a, b, c) => (a, b.toInt, c)
      }

    val thmR = "(.*),Theorem\\((\\d+)\\)".r
    val unkR = "(.*),Unknown\\((.*)\\)".r
    val toR = "(.*),Timeout\\((.*)\\)".r
    val erR = "(.*),Error\\((.*)\\)".r            

    val results = 
      (for (l <- lines.tail) yield {
        l match {
          case thmR(bm, time) => bm -> Theorem(time.toInt)
          case unkR(bm, time) => bm -> Unknown(time.toInt)
          case toR(bm, time) => bm -> Timeout(time.toInt)
          case erR(bm, time) => bm -> Error(time.toInt)
        }
      }).toMap

    println("Tool [" + tool + "] with " + timeout + " seconds timeout.")
    ((tool, datestring), results)
  }


  def printResult(results : Results) = {


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
      (for (b <- benchmarks) yield {
        val str1 =
          String.format("%-"+ longestBenchmark + "s", b) + "  " +
            (for (t <- tools.indices) yield {
              val (str, len, time) = res2str(toolResults(t)(b))
              str + (" "*(CELL_WIDTH-len))
            }).mkString("  ")

        val str2 =
          String.format("%-"+ longestBenchmark + "s", "-") + "  " +
            (for (t <- tools.indices) yield {
              val (_, _, timeStr) = res2str(toolResults(t)(b))              
              timeStr + (" "*(CELL_WIDTH-timeStr.length))
            }).mkString("  ")        

        str1 + "\n" + str2
      })

    val footer =
      ("-"*(longestBenchmark+tools.length*CELL_WIDTH)) + "\n" +
      String.format("%-"+ longestBenchmark + "s", "Total:") + "  " +
        (for (t <- tools.indices) yield {
          val count = toolResults(t).values.count(_.isInstanceOf[Theorem])
          val total = benchmarks.length
          val str = count + "/" + total
          ("%-" + (CELL_WIDTH) + "s").format(str)
        }).mkString("  ")

    Console.println(header1)
    Console.println(header2)    
    for (l <- lines)
      Console.println(l)
    Console.println(footer)
  }

  def parse(args : Array[String]) = {
    val fileNames = 
    args(0) match {
      case "FILES" => args.tail.toList
      case "DIR" => (new File(args(1))).listFiles.filter(_.isFile).filter(_.getName.endsWith(".out")).map(_.toString).toList
    }

    println("Inputs:")
    val files =
      for (f <- fileNames) yield {
        println("\t" + f)
        f
      }

    val results : Results = files.map(parseCSV(_)).toList

    printResult(results)
  }


  def run(args : Array[String]) = {
    if (args.isEmpty) {
      println("Usage:")
      println("\tRUN solver input-directory timeout")
    } else {
      val execs = 
        args(0) match {
          case "leancop" => List(leanCoPExecutor)
          case "bct" => List(bctExecutor)
          case "all" => List(leanCoPExecutor, bctExecutor)
        }

      val inputDir = args(1)
      val timeout = args(2).toInt
      for (ex <- execs)
        ex.run(inputDir, timeout)
    }
  }


  def main(args : Array[String]) = {
    if (args.isEmpty) {
      println("Usage")
      println("\tPARSE")
      println("\tRUN")
    } else {
      args(0) match {
        case "PARSE" => parse(args.tail)
        case "RUN" => run(args.tail)
      }
    }
    Console.println(s"${RESET}${GREEN}Goodbye!${RESET}")
  }
}
