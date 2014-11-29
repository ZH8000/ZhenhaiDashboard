package code.comet

import java.net._
import java.util.Date
import net.liftweb.actor._
import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw
import code.lib._

case object PushResult
case class Display(deadMachines: List[(IPAddress, Boolean)])
case class DeadMachine(ipAddress: IPAddress)
case class LiveMachine(ipAddress: IPAddress)
case class IPAddress(net: String, host: Int) {
  override def toString = s"$net.$host"
  def sortString = f"$net.$host%03d"
}

object PingServer extends LiftActor with ListenerManager {

  private var machineStatus: Map[IPAddress, Boolean] = Map.empty

  def createUpdate = Display(machineStatus.toList.sortWith(_._1.sortString < _._1.sortString))

  def ping(ip: IPAddress): Unit = new Thread {

    override def run() {
      val inet = InetAddress.getByName(ip.toString)
      var counter = 0
      var isConnected = inet.isReachable(500)

      while (counter < 9 && !isConnected) {
        isConnected = inet.isReachable(500)
        counter += 1
      }

      val message = if (isConnected) LiveMachine(ip) else DeadMachine(ip)
      PingServer ! message
      Schedule(() => ping(ip), 5000)
    }

  }.start()

  override def lowPriority : PartialFunction[Any,Unit] = {
    case DeadMachine(ip) => machineStatus = machineStatus.updated(ip, false)
    case LiveMachine(ip) => machineStatus = machineStatus.updated(ip, true)
    case PushResult => 
      updateListeners()
      Schedule(() => this ! PushResult, 5000)
  }

  (1 to 254).foreach(i => ping(IPAddress("192.168.10", i)))
  (1 to 76).foreach(i => ping(IPAddress("192.168.20", i)))
 
  this ! PushResult
}

class PingComet extends CometActor with CometListener {
  
  case class FillColor(ip: IPAddress, color: String)

  private var deadMachines: List[(IPAddress, Boolean)] = Nil

  def registerWith = PingServer

  def render = {
    ".row" #> deadMachines.map { case(ip, status) =>
      ".ip *" #> ip.toString &
      ".date *" #> (new Date).toString
    }
  }

  def fillColor(ip: IPAddress, color: String) = {
    val machineID = MachineInfo.machineID(ip.toString)
    partialUpdate(JsRaw(s"""jQuery("#$machineID").css("fill", "$color")"""))
    partialUpdate(JsRaw("""$('.exception').css("fill", "#cccccc");"""))
  }

  def updateTable(newDeadMachines: List[(IPAddress, Boolean)]) {
    deadMachines = newDeadMachines
    deadMachines.foreach { case(ip, isConnected) =>
      val color = if (isConnected) "#00FF00" else "#FF0000"
      this ! FillColor(ip, color)
    }
    reRender(false)
  }

  override def lowPriority : PartialFunction[Any,Unit] = {
    case Display(newDeadMachines) => updateTable(newDeadMachines)
    case FillColor(ip, color) => fillColor(ip, color)
  }
}
