package code.snippet

import code.lib.MachineInfo
import net.liftweb.util.Helpers._

class MachineList {
  def render = {
    "option" #> MachineInfo.machineList.sortWith(_ < _).map { machineID => 
      "option *" #> machineID 
    }
  }
}



