package code.lib

import java.net.InetAddress
import scala.io.Source

/**
 *  用來代表一台機台的資訊
 *
 *  @param    ip            此機台的 IP 位址
 *  @param    machineID     此機台的機台編號
 *  @param    machineType   此機台的型號
 *  @param    model         此機台的型號
 *  @param    note          備註
 */
case class MachineInfo(ip: String, machineID: String, machineType: Int, model: String, note: Option[String])

/**
 *  機台資訊
 */
object MachineInfo {

  /**
   *  根據伺服器的 Hostname 取得機台設定檔的前綴
   *
   *  @return   若是謝蘇州廠則為 sz，若為謝崗廠則為 xg
   */
  val csvPrefix = {

    val hostname = InetAddress.getLocalHost().getHostName()

    hostname match {
      case "ZhenhaiServerSZ" => "/sz"
      case _ => "/xg"
    }
  }

  /**
   *  系統中可用的維修項目編號
   */
  val maintenanceCodes = List("1", "2", "3", "4", "5", "6", "7", "8")

  /**
   *  從統一錯誤代碼轉到錯誤項目描述的 HashMap
   */
  val defactDescription = Map(
    0 -> "短路不良(計數)",
    1 -> "素子卷取不良(計數)",
    2 -> "胶帯贴付不良(計數)",
    3 -> "素子导线棒不良(計數)",
    4 -> "负导线棒不良",
    5 -> "正导线棒不良",
    6 -> "卷针定位不良",
    7 -> "负电解纸无",
    8 -> "正电解纸无",
    9 -> "正导线棒测试（計數，非錯誤）",
    10 -> "负导线棒测试（計數，非錯誤）",
    11 -> "无胶带",
    12 -> "TP轮无定位",
    13 -> "TP纸带无",
    14 -> "TP胶带无",
    15 -> "无正箔",
    16 -> "无负箔",
    17 -> "无正导线棒",
    18 -> "无负导线棒",
    19 -> "正加締前斷箔",
    20 -> "正加缔后断箔",
    21 -> "負加締前斷箔",
    22 -> "负加缔后断箔",
    23 -> "正铝箔供给",
    24 -> "负铝箔供给断箔",
    25 -> "负铝箔供给",
    26 -> "正铝箔供给断箔",
    27 -> "素子卷取不良",
    28 -> "短路不良",
    29 -> "胶带贴付不良",
    30 -> "素子過大",
    31 -> "素子排料",
    32 -> "墊紙無料",
    33 -> "預沖孔",
    34 -> "膠帶座未退",
    101 -> "不良品A",
    102 -> "不良品B",
    103 -> "不良品C",
    104 -> "不良品D",
    105 -> "真空A",
    106 -> "真空B",
    107 -> "液面A",
    108 -> "液面B",
    109 -> "液面  B2",
    110 -> "外殼有無",
    111 -> "素子有無",
    112 -> "橡皮有無",
    113 -> "含浸槽上下",
    114 -> "素子殘留",
    115 -> "素子位置",
    116 -> "TP位置不良",
    117 -> "前段差斷帶",
    118 -> "後段差故障",
    119 -> "露白測試",
    120 -> "外殼測試",
    121 -> "橡皮測試",
    122 -> "套管打折",
    123 -> "套管有無",
    124 -> "插入",
    125 -> "封口",
    126 -> "檢出",
    201 -> "開路不良計數",
    202 -> "短路不良計數",
    203 -> "LC不良計數",
    204 -> "LC2不良計數",
    205 -> "容量不良計數",
    206 -> "損失不良計數",
    207 -> "重測不良計數",
    208 -> "極性不良"
  )

  /**
   *  機台列表，列出現存的所有機台
   */
  lazy val machineInfoList: List[MachineInfo] = {

    val resourceStream = getClass.getResource(csvPrefix + "MachineList.csv").openStream()
    val csvFile = Source.fromInputStream(resourceStream)("UTF-8")

    csvFile.getLines.toList.map { line =>
      val cols = line.split("\\|")
      val ip = cols(0)
      val machineID = cols(1)
      val machineType = cols(2).toInt
      val model = cols(3)
      val note = if (cols.length == 5) Some(cols(4)) else None

      MachineInfo(ip, machineID, machineType, model, note)
    }
  }

  /**
   *  從機台製程代號轉到製程名稱的 HashMap
   */
  val machineTypeName = Map(
    1 -> "加締卷取",  // 機台編號為 E 開頭的機器
    2 -> "組立",      // 機台編號為 G 開頭的機器
    3 -> "老化",      // 機台編號為 A 開頭的機器
    4 -> "選別",      // 機台編號為 A 開頭的機器的左邊四台
    5 -> "加工切角"   // 機台編號為 T, C 開頭的機器（T 為 Tapping 機，C 為 Cutting 機）
  )

  /**
   *  機台編號的列表
   */
  lazy val machineList = machineInfoList.map(_.machineID)

  /**
   *  從機台的 IP 對應到機台資訊的列表
   */
  lazy val ipTable = machineInfoList.map(machineInfo => machineInfo.ip -> machineInfo).toMap

  /**
   *  從機台的 ID 對應到機台資訊的列表
   */
  lazy val idTable = machineInfoList.map(machineInfo => machineInfo.machineID -> machineInfo).toMap

  /**
   *  從機台編號取得機台的製程的名稱
   *
   *  @param    machineID     機台編號
   *  @return                 機台的製程的名稱（加締／組立……等）
   */
  def getMachineTypeName(machineID: String): Option[String] = {
    for {
      machineType <- idTable.get(machineID).map(_.machineType)
      machineTypeName <- machineTypeName.get(machineType)
    } yield machineTypeName
  }

  /**
   *  從機台編號以及統一錯誤代碼來取得錯誤描述
   *
   *  @param    machineID     機台編號
   *  @param    defactID      統一錯誤代碼
   *  @return                 錯誤描述
   */
  def getErrorDesc(machineID: String, defactID: Int): String = {
    defactDescription.get(defactID).getOrElse(defactID.toString)
  }
}
