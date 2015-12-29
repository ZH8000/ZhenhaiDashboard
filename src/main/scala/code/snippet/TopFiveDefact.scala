package code.snippet

import java.text.SimpleDateFormat

import code.lib._
import code.model._
import net.liftweb.util.Helpers._

/**
 *  顯示網頁上「錯誤分析」右側的「今日五大不良原因」的列表
 */
class TopFiveDefact {

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  val todayString = dateFormatter.format(now)

  /**
   *  從資料庫中取得特定製程的五大不良原因，並且綁定至 HTML 模板中
   *
   *  @param    machineTypeID     製程代碼（1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳）
   *  @return                     HTML 綁定的設定
   */
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

  /**
   *  用來顯示「加締」的今日五大不良列表
   */
  def machineType1 = getTopFiveReason(1)

  /**
   *  用來顯示「組立」的今日五大不良列表
   */
  def machineType2 = getTopFiveReason(2)

  /**
   *  用來顯示「老化」的今日五大不良列表
   */
  def machineType3 = getTopFiveReason(3)

  /**
   *  用來顯示「選別」的今日五大不良列表
   */
  def machineType4 = getTopFiveReason(4)

  /**
   *  用來顯示「加工切腳」的今日五大不良列表
   */
  def machineType5 = getTopFiveReason(5)

  /**
   *  顯示今日五大不良
   */
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
