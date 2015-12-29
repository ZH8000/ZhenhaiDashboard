package code.excel

import java.io.OutputStream

import code.model._
import com.mongodb.casbah.Imports._
import jxl._
import jxl.write._

/**
 *  用來產生「產量統計」－＞「機台狀況」中的 Excel 報表。
 *
 *  @param    year            要產生哪一年的報表
 *  @param    month           要產生哪一月的報表
 *  @param    date            要產生哪一天的報表
 *  @param    shitTag         若為 "M" 則為早班的資料，若為 "N" 則為晚班的資料
 *  @param    sortTag         排序方式（model / size / area）
 *  @param    outputStream    要將 Excel 檔輸出到哪個 OutputStream 中
 *
 */
class MachineDefactSummaryExcel(year: Int, month: Int, date: Int, shiftTag: String, sortTag: String, outputStream: OutputStream) {

  val dataTable = MongoDB.zhenhaiDB(f"defactSummary-$year%02d-$month%02d")
  val shiftDate = f"$year-$month%02d-$date%02d"

  private lazy val defaultFont = new WritableFont(WritableFont.ARIAL, 12)

  /**
   *  文字置中的格式設定
   */
  private lazy val centeredTitleFormat = {
    val centeredTitleFormat = new WritableCellFormat(defaultFont)
    centeredTitleFormat.setAlignment(jxl.format.Alignment.CENTRE)
    centeredTitleFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    centeredTitleFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    centeredTitleFormat
  }

  /**
   *  百分比置中的格式設定
   */
  private lazy val centeredPercentFormat = {
    val centeredNumberFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("0.00%"))
    centeredNumberFormat.setAlignment(jxl.format.Alignment.CENTRE)
    centeredNumberFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    centeredNumberFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    centeredNumberFormat
  }

  /**
   *  數字置中的格式設定
   */
  private lazy val centeredNumberFormat = {
    val centeredNumberFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    centeredNumberFormat.setAlignment(jxl.format.Alignment.CENTRE)
    centeredNumberFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    centeredNumberFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    centeredNumberFormat
  }

  /**
   *  依照 sortTag 指定的方法排序 dataRow 的資料
   *
   *  @param    dataRow     要排序過的資料
   *  @param    sortTag     排序的方式（model = 機型 / size = 尺吋 / area = 區域）
   *  @return               排序過後的資料
   */
  def sortData(dataRow: List[DBObject], sortTag: String) = {
    sortTag match {
      case "model" => dataRow.sortWith((x, y) => x.get("machineModel").toString < y.get("machineModel").toString)
      case "size"  => dataRow.sortWith((x, y) => x.get("product").toString < y.get("product").toString)
      case "area"  => dataRow.sortWith { case (x, y) => 
        s"${x.get("floor").toString} 樓 ${x.get("area").toString} 區"  < 
        s"${y.get("floor").toString} 樓 ${y.get("area").toString} 區"
      }
      case _ => dataRow
    }
  }

  /**
   *  在 Excel 的 WorkBook 中建立一個新的 Sheet
   *
   *  @param      workbook      要寫到哪個 WorkBook
   *  @param      title         該 Sheet 的標題
   *  @return                   建立過的 Sheet
   */
  def createSheet(workbook: WritableWorkbook, title: String) = {
    val sheet = workbook.createSheet(title, 0)
    val sheetSettings = sheet.getSettings
    sheetSettings.setDefaultRowHeight(400)
    sheetSettings.setDefaultColumnWidth(20)
    sheet
  }

  /**
   *  從 shiftTag 對應到該工班時段的標題。
   */
  val shiftTagTitle = shiftTag match {
    case "M" => "07:00-19:00"   // 早班為早上七點到晚上七點
    case "N" => "19:00-07:00"   // 晚班為晚上七點到早上七點
  }

  /**
   *  建立加締卷取的 Sheet
   *
   *  @param    sheet     要寫到 Excel 中的哪個 Sheet
   */
  def createStep1SheetMatrix(sheet: WritableSheet) {

    val reportTitle = f"卷取機生產狀況表     $year 年 $month 月 $date 日  $shiftTagTitle"

    sheet.addCell(new Label(0, 0, reportTitle, centeredTitleFormat))
    sheet.mergeCells(0, 0, 15, 0)

    val titles = List(
      "機台號", "機種", "尺寸", "區域", "標準量", "良品數", "稼動率", 
      "良品率", "短路不良率", "素子導線棒不良率", "膠帶貼付不良率", 
      "素子卷取不良率", "正導針損耗率", "負導針損耗率", "改善對策", "負責人"
    )

    titles.zipWithIndex.foreach { case(title, index) => sheet.addCell(new Label(index, 1, title, centeredTitleFormat)) }

    val dataRow = dataTable.find(MongoDBObject("shiftDate" -> shiftDate, "shift" -> shiftTag, "machineType" -> 1)).toList
    val sortedData = sortData(dataRow, sortTag)

    sortedData.zipWithIndex.map { case(record, index) =>
      val machineID = record.get("machineID").toString
      val machineModel = record.get("machineModel").toString
      val product = record.get("product").toString
      val area = s"${record.get("floor").toString} 樓 ${record.get("area").toString} 區"

      sheet.addCell(new Label(0, index + 2, machineID, centeredTitleFormat))
      sheet.addCell(new Label(1, index + 2, machineModel, centeredTitleFormat))
      sheet.addCell(new Label(2, index + 2, product, centeredTitleFormat))
      sheet.addCell(new Label(3, index + 2, area, centeredTitleFormat))

      val standard = MachineLevel.find("machineID", machineID).map(x => x.levelA.get).toOption
      val countQty = Option(record.get("countQty")).map(_.toString.toLong)

      val standardCell = standard match {
        case None => new Label(4, index + 2, "-", centeredTitleFormat)
        case Some(value) => new Number(4, index + 2, value, centeredNumberFormat)
      }


      sheet.addCell(standardCell)
      sheet.addCell(new Number(5, index + 2, countQty.getOrElse(0L).toDouble, centeredNumberFormat))

      val kadouRate = standard match {
        case None => new Label(6, index + 2, "-", centeredTitleFormat)
        case Some(standardValue) => 
          new Number(6, index + 2, countQty.getOrElse(0L) / standard.getOrElse(0L).toDouble, centeredPercentFormat)
      }

      sheet.addCell(kadouRate)

      val short = Option(record.get("short")).map(_.toString.toLong)
      val stick = Option(record.get("stick")).map(_.toString.toLong)
      val tape  = Option(record.get("tape")).map(_.toString.toLong)
      val roll  = Option(record.get("roll")).map(_.toString.toLong)
      val plus  = Option(record.get("plus")).map(_.toString.toLong)
      val minus = Option(record.get("minus")).map(_.toString.toLong)
      val total = countQty.getOrElse(0L) + short.getOrElse(0L) + stick.getOrElse(0L) + tape.getOrElse(0L) + roll.getOrElse(0L)

      val okRate = total match {
        case 0 => new Label(7, index + 2, "總數為 0 無法計算", centeredTitleFormat)
        case x => new Number(7, index + 2, countQty.getOrElse(0L) / total.toDouble, centeredPercentFormat)
      }

      sheet.addCell(okRate)


      val shortRate = total match {
        case 0 => new Label(8, index + 2, "總數為 0 無法計算", centeredTitleFormat)
        case x => short match {
          case None => new Label(8, index + 2, "-", centeredTitleFormat)
          case Some(shortCount) => new Number(8, index + 2, shortCount / total.toDouble, centeredPercentFormat)
        }
      }

      sheet.addCell(shortRate)

      val stickRate = total match {
        case 0 => new Label(9, index + 2, "總數為 0 無法計算", centeredTitleFormat)
        case x => stick match {
          case None => new Label(9, index + 2, "-", centeredTitleFormat)
          case Some(stickCount) => new Number(9, index + 2, stickCount / total.toDouble, centeredPercentFormat)
        }
      }

      sheet.addCell(stickRate)

      val tapeRate = total match {
        case 0 => new Label(10, index + 2, "總數為 0 無法計算", centeredTitleFormat)
        case x => tape match {
          case None => new Label(10, index + 2, "-", centeredTitleFormat)
          case Some(tapeCount) => new Number(10, index + 2, tapeCount / total.toDouble, centeredPercentFormat)
        }
      }

      sheet.addCell(tapeRate)

      val rollRate = total match {
        case 0 => new Label(11, index + 2, "總數為 0 無法計算", centeredTitleFormat)
        case x => roll match {
          case None => new Label(11, index + 2, "-", centeredTitleFormat)
          case Some(rollCount) => new Number(11, index + 2, (rollCount / total.toDouble), centeredPercentFormat)
        }
      }

      sheet.addCell(rollRate)

      val plusRate = countQty.getOrElse(0L) match {
        case 0 => new Label(12, index + 2, "良品數為 0 無法計算", centeredTitleFormat)
        case x => plus match {
          case None => new Label(12, index + 2, "-", centeredTitleFormat)
          case Some(plusCount) => new Number(12, index + 2, (plusCount / countQty.getOrElse(0L).toDouble) - 1, centeredPercentFormat)
        }
      }

      sheet.addCell(plusRate)

      val minusRate = countQty.getOrElse(0L) match {
        case 0 => new Label(13, index + 2, "良品數為 0 無法計算", centeredTitleFormat)
        case x => minus match {
          case None => new Label(13, index + 2, "-", centeredTitleFormat)
          case Some(minusCount) => new Number(13, index + 2, (minusCount / countQty.getOrElse(0L).toDouble) - 1, centeredPercentFormat)
        }
      }

      sheet.addCell(minusRate)

      val policy = Option(record.get("policy")).map(_.toString).getOrElse("")
      val fixer = Option(record.get("fixer")).map(_.toString).getOrElse("")

      sheet.addCell(new Label(14, index + 2, policy, centeredTitleFormat))
      sheet.addCell(new Label(15, index + 2, fixer, centeredTitleFormat))

    }

  }

  /**
   *  建立組立機的 Sheet
   *
   *  @param    sheet     要寫到 Excel 中的哪個 Sheet
   */
  def createStep2SheetMatrix(sheet: WritableSheet) {

    val reportTitle = f"組立機生產狀況表     $year 年 $month 月 $date 日  $shiftTagTitle"

    sheet.addCell(new Label(0, 0, reportTitle, centeredTitleFormat))
    sheet.mergeCells(0, 0, 14, 0)

    val titles = List(
      "機台號", "機種", "尺寸", "區域", "標準量", "良品數", "稼動率", 
      "良品率", "素子插入不良率", "不良品 D 不良率", "露白不良不良率", 
      "柏梗損耗率", "外殼損耗率", "改善對策", "負責人"
    )

    titles.zipWithIndex.foreach { case(title, index) => sheet.addCell(new Label(index, 1, title, centeredTitleFormat)) }

    val dataRow = dataTable.find(
      MongoDBObject(
        "shiftDate" -> shiftDate,
        "shift" -> shiftTag,
        "machineType" -> 2
      )
    ).toList

    val sortedData = sortData(dataRow, sortTag)

    sortedData.zipWithIndex.map { case(record, index) =>
      val machineID = record.get("machineID").toString
      val machineModel = record.get("machineModel").toString
      val product = record.get("product").toString
      val area = s"${record.get("floor").toString} 樓 ${record.get("area").toString} 區"
      val standard = MachineLevel.find("machineID", machineID).map(x => x.levelA.get).toOption
      val countQty = Option(record.get("countQty")).map(_.toString.toLong)

      sheet.addCell(new Label(0, index + 2, machineID, centeredTitleFormat))
      sheet.addCell(new Label(1, index + 2, machineModel, centeredTitleFormat))
      sheet.addCell(new Label(2, index + 2, product, centeredTitleFormat))
      sheet.addCell(new Label(3, index + 2, area, centeredTitleFormat))

      val standardCell = standard match {
        case None => new Label(4, index + 2, "-", centeredTitleFormat)
        case Some(value) => new Number(4, index + 2, value, centeredNumberFormat)
      }

      sheet.addCell(standardCell)
      sheet.addCell(new Number(5, index + 2, countQty.getOrElse(0L).toDouble, centeredNumberFormat))

      val kadouRate = standard match {
        case None => new Label(6, index + 2, "-", centeredTitleFormat)
        case Some(standardValue) => 
          new Number(6, index + 2, countQty.getOrElse(0L) / standard.getOrElse(0L).toDouble, centeredPercentFormat)
      }

      sheet.addCell(kadouRate)

      val defactD = Option(record.get("defactD")).map(_.toString.toLong)
      val white   = Option(record.get("white")).map(_.toString.toLong)
      val rubber  = Option(record.get("rubber")).map(_.toString.toLong)
      val shell   = Option(record.get("shell")).map(_.toString.toLong)
      val inaccurateTotal = Some(countQty.getOrElse(0L) + defactD.getOrElse(0L) + white.getOrElse(0L))
      val total = Option(record.get("total")).map(_.toString.toLong) orElse inaccurateTotal

      val okRate = total match {
        case None => new Label(7, index + 2, "-", centeredTitleFormat)
        case Some(totalValue) => new Number(7, index + 2, countQty.getOrElse(0L) / totalValue.toDouble, centeredPercentFormat)
      }

      sheet.addCell(okRate)

      val insertRate = total match {
        case None => new Label(8, index + 2, "-", centeredTitleFormat)
        case Some(totalValue) =>
          val rate = ((totalValue - defactD.getOrElse(0L) - white.getOrElse(0L) - countQty.getOrElse(0L)) / totalValue.toDouble)
          new Number(8, index + 2, rate, centeredPercentFormat)
      }

      sheet.addCell(insertRate)

      val defactDRateHolder = for {
        totalValue <- total
        defactDValue <- defactD
      } yield (defactDValue / totalValue.toDouble)

      val defactDRate = defactDRateHolder match {
        case None => new Label(9, index + 2, "-", centeredTitleFormat)
        case Some(value) => new Number(9, index + 2, value, centeredPercentFormat)
      }

      sheet.addCell(defactDRate)

      val whiteRateHolder = for {
        totalValue <- total
        whiteValue <- white
      } yield (whiteValue / totalValue.toDouble)

      val whiteRate = whiteRateHolder match {
        case None => new Label(10, index + 2, "-", centeredTitleFormat)
        case Some(value) => new Number(10, index + 2, value, centeredPercentFormat)
      }

      sheet.addCell(whiteRate)

      val rubberRate = rubber match {
        case None => new Label(11, index + 2, "-", centeredTitleFormat)
        case Some(rubberValue) => new Number(11, index + 2, (rubberValue / countQty.getOrElse(0L).toDouble) - 1, centeredPercentFormat)
      }

      sheet.addCell(rubberRate)

      val shellRate = shell match {
        case None => new Label(12, index + 2, "-", centeredTitleFormat)
        case Some(shellValue) => new Number(12, index + 2, (shellValue / countQty.getOrElse(0L).toDouble) - 1, centeredPercentFormat)
      }

      sheet.addCell(shellRate)

      val policy = Option(record.get("policy")).map(_.toString).getOrElse("")
      val fixer = Option(record.get("fixer")).map(_.toString).getOrElse("")

      sheet.addCell(new Label(13, index + 2, policy, centeredTitleFormat))
      sheet.addCell(new Label(14, index + 2, fixer, centeredTitleFormat))

    }

  }

  /**
   *  建立老化機的 Sheet
   *
   *  @param    sheet     要寫到 Excel 中的哪個 Sheet
   */
  def createStep3SheetMatrix(sheet: WritableSheet) {

    val reportTitle = f"老化機生產狀況表     $year 年 $month 月 $date 日  $shiftTagTitle"

    sheet.addCell(new Label(0, 0, reportTitle, centeredTitleFormat))
    sheet.mergeCells(0, 0, 15, 0)

    val titles = List(
      "機台號", "機種", "尺寸", "區域", "標準量", "良品數", "稼動率", 
      "良品率", "短路不良率", "開路不良率", "容量不良率", 
      "損失不良率", "LC 不良率", "重測不良率", "改善對策", "負責人"
 
    )

    titles.zipWithIndex.foreach { case(title, index) => sheet.addCell(new Label(index, 1, title, centeredTitleFormat)) }

    val dataRow = dataTable.find(
      MongoDBObject(
        "shiftDate" -> shiftDate,
        "shift" -> shiftTag,
        "machineType" -> 3
      )
    ).toList

    val sortedData = sortData(dataRow, sortTag)

    sortedData.zipWithIndex.map { case(record, index) =>

      val machineID = record.get("machineID").toString
      val machineModel = record.get("machineModel").toString
      val product = record.get("product").toString
      val area = s"${record.get("floor").toString} 樓 ${record.get("area").toString} 區"

      sheet.addCell(new Label(0, index + 2, machineID, centeredTitleFormat))
      sheet.addCell(new Label(1, index + 2, machineModel, centeredTitleFormat))
      sheet.addCell(new Label(2, index + 2, product, centeredTitleFormat))
      sheet.addCell(new Label(3, index + 2, area, centeredTitleFormat))

      val standard = MachineLevel.find("machineID", machineID).map(x => x.levelA.get).toOption
      val countQty = Option(record.get("countQty")).map(_.toString.toLong)
      val standardCell = standard match {
        case None => new Label(4, index + 2, "-", centeredTitleFormat)
        case Some(value) => new Number(4, index + 2, value, centeredNumberFormat)
      }

      sheet.addCell(standardCell)
      sheet.addCell(new Number(5, index + 2, countQty.getOrElse(0L).toDouble, centeredNumberFormat))

      val kadouRate = standard match {
        case None => new Label(6, index + 2, "-", centeredTitleFormat)
        case Some(standardValue) => 
          new Number(6, index + 2, countQty.getOrElse(0L) / standard.getOrElse(0L).toDouble, centeredPercentFormat)
      }

      sheet.addCell(kadouRate)

      val short     = Option(record.get("short")).map(_.toString.toLong)
      val open      = Option(record.get("open")).map(_.toString.toLong)
      val capacity  = Option(record.get("capacity")).map(_.toString.toLong)
      val lose      = Option(record.get("lose")).map(_.toString.toLong)
      val lc        = Option(record.get("lc")).map(_.toString.toLong)
      val retest    = Option(record.get("retest")).map(_.toString.toLong)
      val policy = Option(record.get("policy")).map(_.toString).getOrElse("")
      val fixer = Option(record.get("fixer")).map(_.toString).getOrElse("")
      val inaccurateTotal = Some(
        countQty.getOrElse(0L) + capacity.getOrElse(0L) + lose.getOrElse(0L) + lc.getOrElse(0L) + retest.getOrElse(0L)
      )
      val total = Option(record.get("total")).map(_.toString.toLong) orElse inaccurateTotal

      val okRate = total match {
        case None => new Label(7, index + 2, "-", centeredTitleFormat)
        case Some(totalValue) => new Number(7, index + 2, countQty.getOrElse(0L) / totalValue.toDouble, centeredPercentFormat)
      }

      sheet.addCell(okRate)

      val shortHolder = for {
        totalValue <- total
        shortValue <- short
      } yield (shortValue / totalValue.toDouble)

      val shortCell = shortHolder match {
        case None => new Label(8, index + 2, "-", centeredTitleFormat)
        case Some(value) => new Number(8, index + 2, value, centeredPercentFormat)
      }

      sheet.addCell(shortCell)

      val openHolder = for {
        totalValue <- total
        openValue <- open
      } yield (openValue / totalValue.toDouble)

      val openCell = openHolder match {
        case None => new Label(9, index + 2, "-", centeredTitleFormat)
        case Some(value) => new Number(9, index + 2, value, centeredPercentFormat)
      }

      sheet.addCell(openCell)


      val capacityHolder = for {
        totalValue <- total
        capacityValue <- capacity
      } yield (capacityValue / totalValue.toDouble)

      val capacityCell = capacityHolder match {
        case None => new Label(10, index + 2, "-", centeredTitleFormat)
        case Some(value) => new Number(10, index + 2, value, centeredPercentFormat)
      }

      sheet.addCell(capacityCell)

      val loseHolder = for {
        totalValue <- total
        loseValue <- lose
      } yield (loseValue / totalValue.toDouble)

      val loseCell = loseHolder match {
        case None => new Label(11, index + 2, "-", centeredTitleFormat)
        case Some(value) => new Number(11, index + 2, value, centeredPercentFormat)
      }

      sheet.addCell(loseCell)

      val lcHolder = for {
        totalValue <- total
        lcValue <- lc
      } yield (lcValue / totalValue.toDouble)

      val lcCell = lcHolder match {
        case None => new Label(12, index + 2, "-", centeredTitleFormat)
        case Some(value) => new Number(12, index + 2, value, centeredPercentFormat)
      }

      sheet.addCell(lcCell)

      val retestHolder = for {
        totalValue <- total
        retestValue <- retest
      } yield (retestValue / totalValue.toDouble)

      val retestCell = retestHolder match {
        case None => new Label(13, index + 2, "-", centeredTitleFormat)
        case Some(value) => new Number(13, index + 2, value, centeredPercentFormat)
      }

      sheet.addCell(retestCell)
      sheet.addCell(new Label(14, index + 2, policy, centeredTitleFormat))
      sheet.addCell(new Label(15, index + 2, fixer, centeredTitleFormat))


    }

  }

  /**
   *  建立 CUTTING / TAPPING 機的 Sheet
   *
   *  @param    sheet     要寫到 Excel 中的哪個 Sheet
   *  @param    prefix    若為 "C" 為 CUTTING 機，若為 "T" 為 Tapping 機
   */
  def createStep5SheetMatrix(sheet: WritableSheet, prefix: String) {

    val machineTitle = if (prefix == "C") "CUT" else "TP"
    val reportTitle = f"$machineTitle 機生產狀況表     $year 年 $month 月 $date 日  $shiftTagTitle"

    sheet.addCell(new Label(0, 0, reportTitle, centeredTitleFormat))
    sheet.mergeCells(0, 0, 9, 0)

    val titles = List(
      "機台號", "機種", "尺寸", "區域", "標準量", "良品數", "稼動率", 
      "良品率", "改善對策", "負責人"
 
    )

    titles.zipWithIndex.foreach { case(title, index) => sheet.addCell(new Label(index, 1, title, centeredTitleFormat)) }

    val dataRow = dataTable.find(
      MongoDBObject(
        "shiftDate" -> shiftDate,
        "shift" -> shiftTag,
        "machineType" -> 5
      )
    ).toList.filter(x => x.get("machineID").toString.startsWith(prefix))

    val sortedData = sortData(dataRow, sortTag)

    sortedData.zipWithIndex.map { case (record, index) =>

      val machineID = record.get("machineID").toString
      val machineModel = record.get("machineModel").toString
      val product = record.get("product").toString
      val area = s"${record.get("floor").toString} 樓 ${record.get("area").toString} 區"

      sheet.addCell(new Label(0, index + 2, machineID, centeredTitleFormat))
      sheet.addCell(new Label(1, index + 2, machineModel, centeredTitleFormat))
      sheet.addCell(new Label(2, index + 2, product, centeredTitleFormat))
      sheet.addCell(new Label(3, index + 2, area, centeredTitleFormat))

      val standard = MachineLevel.find("machineID", machineID).map(x => x.levelA.get).toOption

      val standardCell = standard match {
        case None => new Label(4, index + 2, "-", centeredTitleFormat)
        case Some(value) => new Number(4, index + 2, value, centeredNumberFormat)
      }

      val countQty = Option(record.get("countQty")).map(_.toString.toLong)

      sheet.addCell(standardCell)
      sheet.addCell(new Number(5, index + 2, countQty.getOrElse(0L).toDouble, centeredNumberFormat))


      val kadouRate = standard match {
        case None => new Label(6, index + 2, "-", centeredTitleFormat)
        case Some(standardValue) => 
          new Number(6, index + 2, countQty.getOrElse(0L) / standard.getOrElse(0L).toDouble, centeredPercentFormat)
      }

      sheet.addCell(kadouRate)

      val total   = Option(record.get("total")).map(_.toString.toLong)

      val okRate = total match {
        case None => new Label(7, index + 2, "-", centeredTitleFormat)
        case Some(totalValue) => new Number(7, index + 2, countQty.getOrElse(0L) / totalValue.toDouble, centeredPercentFormat)
      }

      sheet.addCell(okRate)

      val policy = Option(record.get("policy")).map(_.toString).getOrElse("")
      val fixer = Option(record.get("fixer")).map(_.toString).getOrElse("")

      sheet.addCell(new Label(8, index + 2, policy, centeredTitleFormat))
      sheet.addCell(new Label(9, index + 2, fixer, centeredTitleFormat))

    }

  }

  /**
   *  輸出 Excel 檔到建構子中指定的 OutputStream
   */
  def outputExcel() {

    val workbook = Workbook.createWorkbook(outputStream)
    val step52Sheet = createSheet(workbook, "CUT 機生產狀況表")
    val step51Sheet = createSheet(workbook, "TP 機生產狀況表")
    val step3Sheet = createSheet(workbook, "老化機生產狀況表")
    val step2Sheet = createSheet(workbook, "組立機生產狀況表")
    val step1Sheet = createSheet(workbook, "卷取生產狀況表")

    createStep1SheetMatrix(step1Sheet)
    createStep2SheetMatrix(step2Sheet)
    createStep3SheetMatrix(step3Sheet)
    createStep5SheetMatrix(step51Sheet, "T")
    createStep5SheetMatrix(step52Sheet, "C")

    workbook.write()
    workbook.close()
  }

}
