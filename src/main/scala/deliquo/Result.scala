package deliquo

class Result(time : Long)
case class Theorem(time_ : Long) extends Result(time_)
case class Invalid(time_ : Long) extends Result(time_)
case class Timeout(time_ : Long) extends Result(time_)
case class Unknown(time_ : Long) extends Result(time_)
case class Error(time_ : Long) extends Result(time_)
