package code.comet

import java.net._
import java.util.Date
import net.liftweb.actor._
import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw
import code.lib._

case class UpdatePage(liveMachines: List[MachineInfo], deadMachines: List[MachineInfo])
case class DeadMachine(machineInfo: MachineInfo)
case class LiveMachine(machineInfo: MachineInfo)

object PingServer extends LiftActor with ListenerManager {

  private var deadMachines: Set[MachineInfo] = Set.empty
  private var liveMachines: Set[MachineInfo] = Set.empty
  private def ipSorting(ip: String) = ip.split("\\.").map(x => String.format(f"${x.toInt}%03d")).mkString

  def createUpdate = UpdatePage(
    liveMachines.toList.sortWith((x, y) => ipSorting(x.ip) < ipSorting(y.ip)), 
    deadMachines.toList.sortWith((x, y) => ipSorting(x.ip) < ipSorting(y.ip))
  )

  private def updateWebPage() {
     updateListeners()
     Schedule(() => updateWebPage(), 5000)
  }

  def ping(ip: String) {

    val thread = new Thread {
      override def run() {
        val inet = InetAddress.getByName(ip)
        var counter = 0
        var isConnected = inet.isReachable(500)

        while (counter < 9 && !isConnected) {
          isConnected = inet.isReachable(500)
          counter += 1
        }

        MachineInfo.ipTable.get(ip).foreach { machineInfo =>
          val message = if (isConnected) LiveMachine(machineInfo) else DeadMachine(machineInfo)
          PingServer ! message
        }

        Schedule(() => ping(ip), 1000 * 60)
      }
    }

    thread.start()
  }

  override def lowPriority = {
    case DeadMachine(machineInfo) => 
      liveMachines -= machineInfo
      deadMachines += machineInfo
    case LiveMachine(machineInfo) =>
      liveMachines += machineInfo
      deadMachines -= machineInfo
  }

  (1 to 252).foreach(i => ping(s"192.168.10.$i"))
  (1 to 76).foreach(i => ping(s"192.168.20.$i"))
  updateWebPage()
}

class PingComet extends CometActor with CometListener {
  
  case class FillColor(machineInfo: MachineInfo, color: String)

  private var liveMachines: List[MachineInfo] = Nil
  private var deadMachines: List[MachineInfo] = Nil

  def registerWith = PingServer

  def render = {
    ".row" #> deadMachines.map { case machineInfo =>
      ".machineID *" #> machineInfo.machineID &
      ".machineType *" #> machineInfo.model &
      ".machineNote *" #> machineInfo.note &
      ".ip *" #> machineInfo.ip &
      ".date *" #> (new Date).toString
    }
  }

  def fillColor(machineInfo: MachineInfo, color: String) = {
    partialUpdate(JsRaw(s"""jQuery("#${machineInfo.machineID}").css("fill", "$color")"""))
    partialUpdate(JsRaw("""$('.exception').css("fill", "#cccccc");"""))
  }

  def updateTable(liveMachines: List[MachineInfo], deadMachines: List[MachineInfo]) = {
    this.liveMachines = liveMachines
    this.deadMachines = deadMachines
    deadMachines.foreach { machineInfo => this ! FillColor(machineInfo, "#FF0000") }
    liveMachines.foreach { machineInfo => this ! FillColor(machineInfo, "#00FF00") }
    reRender(false)
  }

  override def lowPriority = {
    case UpdatePage(liveMachines, deadMachines) => updateTable(liveMachines, deadMachines)
    case FillColor(ip, color) => fillColor(ip, color)
  }
}
