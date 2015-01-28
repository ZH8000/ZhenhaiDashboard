package code.snippet

import code.model._
import code.lib._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._

import scala.xml.NodeSeq
import scala.collection.JavaConversions._
import java.text.SimpleDateFormat

class TopFiveDefact {

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  val todayString = dateFormatter.format(now)

  def render = {
    println(todayString)
    val topFiveReason = TopReason.findAll("date", todayString).sortWith(_.bad_qty.get > _.bad_qty.get).take(5)

    ".row" #> topFiveReason.map { reason =>
      ".machineID *"  #> reason.mach_id &
      ".defactID *"   #> MachineInfo.getErrorDesc(reason.mach_id.get, reason.defact_id.get) &
      ".stepTitle *"  #> MachineInfo.getMachineTypeName(reason.mach_id.get) &
      ".badQty *"     #> reason.bad_qty &
      ".date *"       #> reason.date
    }
  }

}
