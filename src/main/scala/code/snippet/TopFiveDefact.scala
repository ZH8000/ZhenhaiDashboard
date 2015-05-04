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

  def getTopFiveReason(machineTypeID: Int) = {
    val topFiveReason = 
      TopReason.findAll("shiftDate", todayString)
               .filter(_.machine_type.get == machineTypeID)
               .sortWith(_.event_qty.get > _.event_qty.get)
               .take(5)

    ".row" #> topFiveReason.map { reason =>
      ".machineID *"  #> reason.mach_id &
      ".defactID *"   #> MachineInfo.getErrorDesc(reason.mach_id.get, reason.defact_id.get) &
      ".eventQty *"   #> reason.event_qty &
      ".date *"       #> reason.date
    }

  }

  def machineType1 = getTopFiveReason(1)
  def machineType2 = getTopFiveReason(2)
  def machineType3 = getTopFiveReason(3)
  def machineType4 = getTopFiveReason(4)
  def machineType5 = getTopFiveReason(5)

  def render = {
    val topFiveReason = TopReason.findAll("shiftDate", todayString).sortWith(_.event_qty.get > _.event_qty.get).take(5)

    ".row" #> topFiveReason.map { reason =>
      ".machineID *"  #> reason.mach_id &
      ".defactID *"   #> MachineInfo.getErrorDesc(reason.mach_id.get, reason.defact_id.get) &
      ".stepTitle *"  #> MachineInfo.getMachineTypeName(reason.mach_id.get) &
      ".eventQty *"   #> reason.event_qty &
      ".date *"       #> reason.date
    }
  }

}
