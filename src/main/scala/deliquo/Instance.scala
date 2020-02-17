package deliquo

object Instance {
  def apply(tool : String, benchmark : String, data : Map[String, String]) = {
    val result = data("result")
    val time = data("time").toLong
    val extra = (for ((k, v) <- data; if k != "result" && k != "time") yield (k, v)).toMap
    new Instance(tool, benchmark, result, time, extra)
  }
}

case class Instance(tool : String, benchmark : String, result : String, time : Long, extra : Map[String, String]) {

  val fields = extra.keys.toList.sorted

  val CSV = {
    (tool ::
      benchmark ::
      result :: 
      time.toString ::
    (for (f <- fields) yield extra(f)).toList).mkString(",")
  }

}
