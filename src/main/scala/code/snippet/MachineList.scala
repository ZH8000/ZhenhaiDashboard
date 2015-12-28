package code.snippet

import code.lib.MachineInfo
import net.liftweb.util.Helpers._

/**
 *  用來在下拉式選單列出所有的機台
 *
 *  主要用在「產量統計」－＞「依φ別／依容量／日報表／月報表」一路點下去的
 *  最後一個頁面中的左側的機台選單。
 *
 */
class MachineList {

  /**
   *  用來將網頁上的下拉式選單填入機台列表
   */
  def render = {
    "option" #> MachineInfo.machineList.sortWith(_ < _).map { machineID => 
      "option *" #> machineID 
    }
  }
}



