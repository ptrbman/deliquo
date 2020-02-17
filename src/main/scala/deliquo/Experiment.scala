package deliquo

import scala.xml.XML

case class ToolConfig(tool : String) {
  def toXML =
<toolconfig>
  <name>{ tool }</name>
</toolconfig>

}

object Experiment {
  def apply(fileName : String) : Experiment = {
    println("Loading: " + fileName)
    val xml = XML.loadFile(fileName)
    val expName = (xml \ "name").text
    val timeout = (xml \ "timeout").text.toInt
    val output = (xml \ "output").text    
    val inputs = for (i <- xml \ "inputs" \ "input") yield i.text
    val toolConfigs = for (tc <- xml \ "toolconfigs" \ "toolconfig") yield ToolConfig((tc \ "name").text)

    Experiment(expName, timeout, output, inputs.toList, toolConfigs.toList) 
  }
}




case class Experiment(name : String, timeout : Int, output : String, inputs : List[String], toolConfigs : List[ToolConfig]) {

  def run(tools : List[Tool]) : List[Instance] = {
    println("One tool at a time...")
      (for (tc <- toolConfigs) yield {
        val tool = tools.find(_.name == tc.tool).get
        for (f <- inputs) yield tool.execute(f, timeout)
      }).flatten
  }

  def toXML =
<experiment>
  <name>{ name }</name>
  <timeout>{ timeout }</timeout>
  <inputs>{ inputs.map(i => <input>{ i }</input>) }</inputs>
  <output>{ output }</output>
  <toolconfigs>{ toolConfigs.map(_.toXML) }</toolconfigs>
</experiment>


  def writeXML(outfile : String) = {
    val xml = this.toXML
    val printer = new scala.xml.PrettyPrinter(80, 2)
    XML.save(outfile, XML.loadString(printer.format(xml)), "UTF-8", true, null)
  }

}
