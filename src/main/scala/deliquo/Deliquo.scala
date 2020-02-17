package deliquo

import java.io.File

object Deliquo {

  def printTools(tools : List[Tool]) = {
    println("< --- Tools --- >")
    for (t <- tools)
      println(t)
  }

  def loadTools() : List[Tool] = {
    val toolFiles = new File("tools/").listFiles.filter(_.isFile).filter(_.getName.endsWith(".tool")).toList
    (for (tf <- toolFiles) yield {
      Tool.fromXML(tf.getPath)
    }).toList
  }


  def main(args : Array[String]) = {
    val tools = loadTools()

    Configuration.parse(args) match {
      case Some(config) =>
        if (config.showTools) {
          printTools(tools)
        } else if (config.experiment != "") {
          println("Running experiment!")
          val exp = Experiment(config.experiment)
          println(exp)
          println(exp.writeXML("test.xml"))
          // val results = exp.run(tools)
          // println(results.mkString("\n"))
          // CSV.writeInstances(results.toList, exp.output)
        } else {
          println("Input files: ")
          for (file <- config.inputFiles)
            println("\t" + file)

          val usedTools = 
            for (tool <- config.tools) yield tools.find(_.name == tool).get

          println("Used Tools: ")
          for (t <- usedTools)
            println("\t" + t)


          for (t <- usedTools) {
            val instances = 
            for (f <- config.inputFiles) yield {
              t.execute(f, config.timeout)
            }
            println(instances.mkString("\n"))

            CSV.writeInstances(instances.toList, "test.csv")
          }
        }
      case _ => println("Unrecognized command")
    }

  }
}
