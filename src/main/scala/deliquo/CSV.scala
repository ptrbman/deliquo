package deliquo

import java.io.{File, PrintWriter}

object CSV {

  def writeInstances(instances : List[Instance], outfile : String) = {
    val fields = "solver" :: "benchmark" :: "result" :: "time" :: instances(0).fields

    println("Writing to: \"" + outfile + "\"")
    val pw = new PrintWriter(new File(outfile))

    pw.write(fields.mkString(",") + "\n")
    for (i <- instances) pw.write(i.CSV + "\n")
    pw.flush()
    pw.close()    
  }

}
