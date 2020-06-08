package deliquo

import scala.xml.XML

object ToolConfig {
  def fromXML(xml : scala.xml.Node) = {
    val toolName = (xml \ "name").text
    // TODO: This if is not very nice...
    val optionValues =
      for (x <- (xml \ "options") if (x \ "option").text != "") yield {
        val (o, v) = ((x \ "option").text, (x \ "value").text)
        (o, v)
      }
    val extras = (xml \ "extras").text
    println(xml)
    println(toolName)
    println(optionValues)
    println(extras)
    ToolConfig(toolName, optionValues.toMap, extras)
  }
}


case class ToolConfig(toolName: String, optionValues : Map[String, String], extras : String) {

  val name = toolName + "_" + optionValues.values.mkString("_")
  def toXML =
<toolconfig>
  <name>{ toolName }</name>
  <options>{ optionValues.map{ case (o, v) => <option>{o}</option><value>{v}</value> } }</options>
  <extras>{extras}</extras>
</toolconfig>

}

object Experiment {
  def apply(fileName : String) : Experiment = {
    println("Loading: " + fileName)
    val xml = XML.loadFile(fileName)
    val expName = (xml \ "name").text
    val timeout = (xml \ "timeout").text.toInt
    val output = (xml \ "output").text    
    val inputs = for (i <- xml \ "inputs" \ "input") yield i.text.trim
    // val toolConfigs = for (tc <- xml \ "toolconfigs" \ "toolconfig") yield ToolConfig((tc \ "name").text)
    val toolConfigs = for (tc <- xml \ "toolconfigs" \ "toolconfig") yield ToolConfig.fromXML(tc)

    Experiment(expName, timeout, output, inputs.toList, toolConfigs.toList) 
  }
}




case class Experiment(name : String, timeout : Int, output : String, inputs : List[String], toolConfigs : List[ToolConfig]) {

  def run(tools : List[Tool]) : List[Instance] = {
    println("One tool at a time...")
           (for (tc <- toolConfigs) yield {
              println("Searching for " + tc + " (" + tc.toolName + ")")
              val tool = tools.find(_.name == tc.toolName).get
              for (f <- inputs) yield tool.execute(f, timeout, tc)
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
