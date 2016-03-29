package code.lib

object MachineStatusMapping {

  /**
   *  機台狀態對應到機台說明
   */
  private val statusToDescription = Map(
    "01" -> "生產中",
    "02" -> "維修中",
    "03" -> "維修完成",
    "04" -> "生產完成",
    "05" -> "鎖機",
    "06" -> "解鎖",
    "07" -> "結單鈕（已達目標數）",
    "08" -> "結單鈕（未達目標數）",
    "09" -> "刷入條碼",
    "10" -> "STANDBY",
    "11" -> "斷電",
    "12" -> "機台開機"
  )

  def getDescription(status: String) = statusToDescription.get(status).getOrElse("Unknown")
}
