package deliquo

import scopt.OParser

case class Config(
  showTools : Boolean = false,
  inputFiles : Array[String] = Array(),
  experiment : String = "",
  tools : Array[String] = Array(),
  timeout : Int = Configuration.DEFAULT_TIMEOUT
)

object Configuration {
  val DEFAULT_TIMEOUT = 5

  def parse(args : Array[String]) = {
    val builder = OParser.builder[Config]
    val parser1 = {
      import builder._
      OParser.sequence(
        programName("Deliquo"),
        head("Deliquo", "0.2"),
        opt[String]("files")
          .action((x, c) => c.copy(inputFiles = x.split(',')))
          .text("Input files <file1>,<file2>,..."),
        opt[String]("tools")
          .action((x, c) => c.copy(tools = x.split(',')))
          .text("Use tool <tool1>,<tool2>,..."),
        opt[Int]('t', "timeout")
          .action((x, c) => c.copy(timeout = x))
          .text("Timeout in seconds (default: " + DEFAULT_TIMEOUT + ")"),
        opt[Unit]("show-tools")
          .action((_, c) => c.copy(showTools = true))
          .text("List all tools and exit"),
        opt[String]("exp")
          .action((x, c) => c.copy(experiment = x))
          .text("Run experiment <exp>"),          
      )
    }
    OParser.parse(parser1, args, Config())
  }
}


