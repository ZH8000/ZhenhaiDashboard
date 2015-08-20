package code.lib

import net.liftweb.common._
import code.model._
import com.mongodb.casbah.Imports._
import java.util.Calendar
import java.util.GregorianCalendar
import java.io.OutputStream
import jxl._
import jxl.write._
import java.text.SimpleDateFormat
import java.util.Date

object KadouExcel {
  private lazy val zhenhaiDB = MongoDB.zhenhaiDB
}

class KadouExcel(year: Int, month: Int, outputStream: OutputStream) {

  val maxDate = new GregorianCalendar(year, month-1, 1).getActualMaximum(Calendar.DAY_OF_MONTH)
  val operationTime = KadouExcel.zhenhaiDB(f"operationTime-$year%02d-$month%02d")
  val workQty = KadouExcel.zhenhaiDB(f"workQty-$year%02d-$month%02d")
 
  private lazy val defaultFont = new WritableFont(WritableFont.ARIAL, 12)
  private lazy val centeredTitleFormat = {
    val centeredTitleFormat = new WritableCellFormat(defaultFont)
    centeredTitleFormat.setAlignment(jxl.format.Alignment.CENTRE)
    centeredTitleFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    centeredTitleFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    centeredTitleFormat
  }

  private lazy val centeredPercentFormat = {
    val centeredNumberFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("0.00%"))
    centeredNumberFormat.setAlignment(jxl.format.Alignment.CENTRE)
    centeredNumberFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    centeredNumberFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    centeredNumberFormat.setBackground(jxl.format.Colour.RED)
    centeredNumberFormat
  }

  private lazy val centeredNumberFormat = {
    val centeredNumberFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    centeredNumberFormat.setAlignment(jxl.format.Alignment.CENTRE)
    centeredNumberFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    centeredNumberFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    centeredNumberFormat
  }

  private lazy val greyBackgroundFormat = {
    val greyBackgroundFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    greyBackgroundFormat.setBackground(jxl.format.Colour.GRAY_25)
    greyBackgroundFormat.setAlignment(jxl.format.Alignment.CENTRE)
    greyBackgroundFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    greyBackgroundFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    greyBackgroundFormat
  }

  private lazy val greenBackgroundFormat = {
    val greenBackgroundFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    greenBackgroundFormat.setBackground(jxl.format.Colour.LIGHT_GREEN)
    greenBackgroundFormat.setAlignment(jxl.format.Alignment.CENTRE)
    greenBackgroundFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    greenBackgroundFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    greenBackgroundFormat
  }

  def createDocumentTitleRow(sheet: WritableSheet) {

    val month = (new SimpleDateFormat("M")).format(new Date)
    val sheetTitleCell = new Label(0, 0, s"製 造 部 各 工 程 稼 動 率 一 覽 表", centeredTitleFormat)
    val monthTitleCell = new Label(0, 1, s"月份：$month", centeredTitleFormat)
    val targetTitleCell = new Label(6, 1, "目標", centeredTitleFormat)

    val step1TitleCell = new Label(1, 2, "卷  取(%)", centeredTitleFormat)
    val step2TitleCell = new Label(2, 2, "組  立(%)", centeredTitleFormat)
    val step3TitleCell = new Label(3, 2, "老  化(%)", centeredTitleFormat)
    val step4TitleCell = new Label(4, 2, "TAPPING(%)", centeredTitleFormat)
    val step5TitleCell = new Label(5, 2, "CUTTING(%)", centeredTitleFormat)
    val targetStep1TitleCell = new Label(6, 2, "加  締", centeredTitleFormat)
    val targetStep2TitleCell = new Label(7, 2, "卷  取", centeredTitleFormat)
    val targetStep3TitleCell = new Label(8, 2, "組  立", centeredTitleFormat)
    val targetStep4TitleCell = new Label(9, 2, "老  化", centeredTitleFormat)
    val targetStep5TitleCell = new Label(10, 2, "TAPPING", centeredTitleFormat)
    val targetStep6TitleCell = new Label(11, 2, "CUTTING", centeredTitleFormat)

    sheet.addCell(sheetTitleCell)
    sheet.addCell(monthTitleCell)
    sheet.addCell(targetTitleCell)
    sheet.addCell(step1TitleCell)
    sheet.addCell(step2TitleCell)
    sheet.addCell(step3TitleCell)
    sheet.addCell(step4TitleCell)
    sheet.addCell(step5TitleCell)
    sheet.addCell(targetStep1TitleCell)
    sheet.addCell(targetStep2TitleCell)
    sheet.addCell(targetStep3TitleCell)
    sheet.addCell(targetStep4TitleCell)
    sheet.addCell(targetStep5TitleCell)
    sheet.addCell(targetStep6TitleCell)

    sheet.mergeCells(0, 0, 5, 0)
  }

  def getKadouRate(date: String, step: Int, countQty: Long): Option[Double] = {
    KadouExcelSaved.get(date, step).map { workQty => countQty / workQty.toDouble }
  }

  def getData(sheet: WritableSheet, date: Int) = {
    val shiftDate = f"$year-$month%02d-$date%02d"
    val step1 = operationTime.find(
      MongoDBObject(
        "shiftDate"   -> f"$year-$month%02d-$date%02d",
        "machineType" -> 1
      )
    ).toList

    val step2 = operationTime.find(
      MongoDBObject(
        "shiftDate"   -> f"$year-$month%02d-$date%02d",
        "machineType" -> 2
      )
    ).toList
   
    val step3 = operationTime.find(
      MongoDBObject(
        "shiftDate"   -> f"$year-$month%02d-$date%02d",
        "machineType" -> 3
      )
    ).toList

    val step4 = operationTime.find(
      MongoDBObject(
        "shiftDate"   -> f"$year-$month%02d-$date%02d",
        "machineType" -> 5
      )
    ).toList.filter(x => x.get("machineID").toString.startsWith("T"))

    val step5 = operationTime.find(
      MongoDBObject(
        "shiftDate"   -> f"$year-$month%02d-$date%02d",
        "machineType" -> 5
      )
    ).toList.filter(x => x.get("machineID").toString.startsWith("C"))

    val step1Count = step1.map(x => x.get("countQty").toString.toLong).sum
    val step2Count = step2.map(x => x.get("countQty").toString.toLong).sum
    val step3Count = step3.map(x => x.get("countQty").toString.toLong).sum
    val step4Count = step4.map(x => x.get("countQty").toString.toLong).sum
    val step5Count = step5.map(x => x.get("countQty").toString.toLong).sum

    val step1Kadou = getKadouRate(shiftDate, 1, step1Count)
    val step2Kadou = getKadouRate(shiftDate, 2, step2Count)
    val step3Kadou = getKadouRate(shiftDate, 3, step3Count)
    val step4Kadou = getKadouRate(shiftDate, 4, step4Count)
    val step5Kadou = getKadouRate(shiftDate, 5, step5Count)

    (step1Kadou, step2Kadou, step3Kadou, step4Kadou, step5Kadou)
  }

  def createMatrix(sheet: WritableSheet) {

    val rowOffset = 2

    (1 to maxDate).foreach { date =>

      val dateTitle = new Label(0, rowOffset + date, date.toString, centeredTitleFormat)
      val step1TargetTitle = new Label(6, rowOffset + date, "85.00", centeredTitleFormat)
      val step2TargetTitle = new Label(7, rowOffset + date, "85.00", centeredTitleFormat)
      val step3TargetTitle = new Label(8, rowOffset + date, "85.00", centeredTitleFormat)
      val step4TargetTitle = new Label(9, rowOffset + date, "85.00", centeredTitleFormat)
      val step5TargetTitle = new Label(10, rowOffset + date, "85.00", centeredTitleFormat)
      val step6TargetTitle = new Label(11, rowOffset + date, "85.00", centeredTitleFormat)

      val (step1Kadou, step2Kadou, step3Kadou, step4Kadou, step5Kadou) = getData(sheet, date)

      val step1KadouCell = step1Kadou match {
        case Some(value) => new Number(1, rowOffset + date, value, centeredPercentFormat)
        case None  => new Label(1, rowOffset + date, "請先設定製造應生產數", centeredPercentFormat)
      }

      val step2KadouCell = step2Kadou match {
        case Some(value) => new Number(2, rowOffset + date, value, centeredPercentFormat)
        case None  => new Label(2, rowOffset + date, "請先設定製造應生產數", centeredPercentFormat)
      }

      val step3KadouCell = step3Kadou match {
        case Some(value) => new Number(3, rowOffset + date, value, centeredPercentFormat)
        case None  => new Label(3, rowOffset + date, "請先設定製造應生產數", centeredPercentFormat)
      }

      val step4KadouCell = step4Kadou match {
        case Some(value) => new Number(4, rowOffset + date, value, centeredPercentFormat)
        case None  => new Label(4, rowOffset + date, "請先設定製造應生產數", centeredPercentFormat)
      }

      val step5KadouCell = step5Kadou match {
        case Some(value) => new Number(5, rowOffset + date, value, centeredPercentFormat)
        case None  => new Label(5, rowOffset + date, "請先設定製造應生產數", centeredPercentFormat)
      }

      sheet.addCell(dateTitle)
      sheet.addCell(step1TargetTitle)
      sheet.addCell(step2TargetTitle)
      sheet.addCell(step3TargetTitle)
      sheet.addCell(step4TargetTitle)
      sheet.addCell(step5TargetTitle)
      sheet.addCell(step6TargetTitle)

      sheet.addCell(step1KadouCell)
      sheet.addCell(step2KadouCell)
      sheet.addCell(step3KadouCell)
      sheet.addCell(step4KadouCell)
      sheet.addCell(step5KadouCell)
    }
  }


  def outputExcel() {

    val workbook = Workbook.createWorkbook(outputStream)
    val sheet = workbook.createSheet("abc", 0)
    val sheetSettings = sheet.getSettings
    sheetSettings.setDefaultRowHeight(400)
    sheetSettings.setDefaultColumnWidth(20)

    createDocumentTitleRow(sheet)
    createMatrix(sheet)

    workbook.write()
    workbook.close()
  }

}
