package code.comet

import java.net._
import java.util.Date
import net.liftweb.actor._
import net.liftweb.http._
import net.liftweb.util._
import code.lib._

case object PushResult
case class Display(deadMachines: List[IPAddress])
case class DeadMachine(ipAddress: IPAddress)
case class LiveMachine(ipAddress: IPAddress)
case class IPAddress(net: String, host: Int) {
  override def toString = s"$net.$host"
  def sortString = f"$net.$host%03d"
}

object PingServer extends LiftActor with ListenerManager {

  private var deadMachines: Set[IPAddress] = Set.empty

  def createUpdate = Display(deadMachines.toList.sortWith(_.sortString < _.sortString))

  def ping(ip: IPAddress): Unit = new Thread {

    override def run() {
      val inet = InetAddress.getByName(ip.net + "." + ip.host)
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
    case DeadMachine(ip) => deadMachines += ip
    case LiveMachine(ip) => deadMachines -= ip
    case PushResult => 
      updateListeners()
      Schedule(() => this ! PushResult, 5000)
  }

  (1 to 254).foreach(i => ping(IPAddress("192.168.10", i)))
  (1 to 76).foreach(i => ping(IPAddress("192.168.20", i)))
 
  this ! PushResult
}

class PingComet extends CometActor with CometListener {
  
  private var deadMachines: List[IPAddress] = Nil

  def registerWith = PingServer

  def render = {
    ".row" #> deadMachines.map { ip =>
      ".ip *" #> ip.toString &
      ".date *" #> (new Date).toString
    }
  }

  def updatePicture(newDeadMachines: List[IPAddress]) {

    val shouldAddColor = newDeadMachines diff deadMachines
    val shouldRemoveColor = deadMachines diff newDeadMachines

    shouldAddColor.foreach { case ip =>
      println("ID:" + MachineInfo.machineID(ip.toString))
    }
    shouldRemoveColor.foreach { case ip =>
      println("ID:" + MachineInfo.machineID(ip.toString))
    }

  }

  override def lowPriority : PartialFunction[Any,Unit] = {
    case Display(newDeadMachines) =>
      //updatePicture(newDeadMachines)
      deadMachines = newDeadMachines
      reRender(false)
  }
}


