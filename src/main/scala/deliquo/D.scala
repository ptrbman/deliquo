package deliquo

import Console.{GREEN, RED, RESET, YELLOW, UNDERLINED}

object D {
  val MARGIN = 2

  val GREEN = Console.GREEN
  val YELLOW = Console.YELLOW

  def dprintln(str : String) = cPrintln(str)

  def colorString(str : String, color : String) = {
    color match {
      case "YELLOW" => s"${RESET}${YELLOW}" + str + s"${RESET}"
      case "RED" => s"${RESET}${RED}" + str + s"${RESET}"        
      case "" => str
    }
  }

  def dboxprintln(str : String, color : String = "") = {
    val cstr = colorString(str, color)

    dprintln("+" + ("-"*(MARGIN + str.length + MARGIN)) + "+")
    dprintln("|" + (" "*MARGIN) + cstr + (" "*MARGIN) + "|")
    dprintln("+" + ("-"*(MARGIN + str.length + MARGIN)) + "+")    
  }

  def dlargeboxprintln(str : String) = {
    cPrintln("+" + ("-"*(2*MARGIN + str.length + 2*MARGIN)) + "+")
    dprintln("|" + (" "*2*MARGIN) + (" "*str.length) + (" "*2*MARGIN) + "|")    
    dprintln("|" + (" "*2*MARGIN) + str + (" "*2*MARGIN) + "|")
    dprintln("|" + (" "*2*MARGIN) + (" "*str.length) + (" "*2*MARGIN) + "|")
    dprintln("+" + ("-"*(2*MARGIN + str.length + 2*MARGIN)) + "+")    
  }

  def cPrintln(str : String) = {
    Console.println(str)
  }
}
