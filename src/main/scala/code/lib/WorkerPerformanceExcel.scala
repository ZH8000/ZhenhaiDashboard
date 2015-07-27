package code.lib

import code.model._
import com.mongodb.casbah.Imports._
import java.util.Calendar
import java.util.GregorianCalendar
import java.io.OutputStream
import jxl._
import jxl.write._
import java.text.SimpleDateFormat
import java.util.Date

object WorkerPerformanceExcel {
  private lazy val zhenhaiDB = MongoDB.zhenhaiDB
}

class WorkerPerformanceExcel(year: Int, month: Int, outputStream: OutputStream) {
  
  val zhenhaiDB = MongoDB.zhenhaiDB

  lazy val workers = {
    val workerMongoIDs = zhenhaiDB("workerPerformance").distinct("workerMongoID")
    workerMongoIDs.flatMap(x => Worker.findByMongoID(x.toString)).sortWith(_.workerID.get < _.workerID.get)
  }

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

  case class Performance(standard: Long, standardPerformance: Long, countQty: Long)

  def getWorkerPerformance(workerMongoID: String) = {
    var dateToCount: Map[String, Long] = Map.empty
    var dateToPerformance: Map[String, Performance] = Map.empty
    val data = zhenhaiDB("workerPerformance").find(MongoDBObject("month" -> "2015-07", "workerMongoID" -> workerMongoID))

    data.foreach { data =>
      val shiftDate = data.get("shiftDate").toString
      val machineID = data.get("machineID").toString
      val productCode = data.get("productCode").toString
      val countQty = data.get("countQty").toString.toLong

      val machinePerformance = MachinePerformance.find(machineID, productCode)
      val managementCount = machinePerformance.map(_.managementCount.get).getOrElse(0L)
      val performanceCount = machinePerformance.map(_.performanceCount.get).getOrElse(0L)

      val newData = dateToPerformance.get(shiftDate) match {
        case None => Performance(managementCount, performanceCount, countQty)
        case Some(oldData) => 
          Performance(
            oldData.standard + managementCount, 
            oldData.standardPerformance + performanceCount, 
            oldData.countQty + countQty
          )
      }

      dateToPerformance = dateToPerformance.updated(shiftDate, newData)

      val oldCount = dateToCount.get(shiftDate).getOrElse(0L)
      val newCount = oldCount + data.get("countQty").toString.toLong
      dateToCount = dateToCount.updated(shiftDate, newCount)
    }

    dateToPerformance.toList.sortWith(_._1 < _._1)
  }

  def getAveragePeformance(worker: Worker, date: String) = {

    var average: List[Double] = Nil
    val dataList = WorkerPerformance.findAll(MongoDBObject("workerMongoID" -> worker.id.get.toString, "shiftDate" -> date))
    
    for {
      record <- dataList
      performance <- MachinePerformance.find(record.machineID.get, record.productCode.get).map(_.managementCount.get).filterNot(_ == 0)
    } {
      average ::= record.countQty.get / performance.toDouble
    }

    average.sum / dataList.size
  }

  def createMatrix(sheet: WritableSheet) {
    var rowCount = 3

    workers.foreach { worker =>

      val performanceOfDates = getWorkerPerformance(worker.id.get.toString)
      val startRow = rowCount

      performanceOfDates.foreach { case(date, performance) =>

        
        val workerIDCell = new Label(0, rowCount, worker.workerID.get, centeredTitleFormat)
        val workerNameCell = new Label(1, rowCount, worker.name.get, centeredTitleFormat)
        val dateCell = new Label(2, rowCount, date, centeredTitleFormat)
        val standardCell = new Number(3, rowCount, performance.standard, centeredNumberFormat)
        val standardPerformanceCell = new Number(4, rowCount, performance.standardPerformance, centeredNumberFormat)
        val countQtyCell = new Number(5, rowCount, performance.countQty, centeredNumberFormat)
        val lockCount = 
            zhenhaiDB("lock").find(MongoDBObject("shiftDate" -> date, "workerMongoID" -> worker.id.toString)).count
        val lockTimeCell = new Number(7, rowCount, lockCount * 10, centeredNumberFormat)

        val standardLoc = CellReferenceHelper.getCellReference(3, rowCount)
        val standardPerformanceLoc = CellReferenceHelper.getCellReference(4, rowCount)
        val countQtyLoc = CellReferenceHelper.getCellReference(5, rowCount)
        val kadou = new Formula(8, rowCount, s"$countQtyLoc / $standardLoc", centeredPercentFormat)
        val performancePercent = new Formula(9, rowCount, s"$countQtyLoc / $standardPerformanceLoc", centeredPercentFormat)
        val averagePerformance = new Number(10, rowCount, getAveragePeformance(worker, date), centeredPercentFormat)

        sheet.addCell(workerIDCell)
        sheet.addCell(workerNameCell)
        sheet.addCell(dateCell)
        sheet.addCell(standardCell)
        sheet.addCell(standardPerformanceCell)
        sheet.addCell(countQtyCell)
        sheet.addCell(lockTimeCell)

        sheet.addCell(kadou)
        sheet.addCell(performancePercent)
        sheet.addCell(averagePerformance)

        rowCount += 1
      }

      val titleCell = new Label(0, rowCount, "小計:", centeredTitleFormat)
      val standardFormula = s"SUM(D${startRow+1}:D${rowCount})"
      val standardCell = new Formula(3, rowCount, standardFormula, centeredNumberFormat)
      val standardPerformanceFormula = s"SUM(E${startRow+1}:E${rowCount})"
      val standardPerformanceCell = new Formula(4, rowCount, standardPerformanceFormula, centeredNumberFormat)
      val countQtyFormula = s"SUM(F${startRow+1}:F${rowCount})"
      val countQtyCell = new Formula(5, rowCount, countQtyFormula, centeredNumberFormat)

      sheet.addCell(titleCell)
      sheet.addCell(new Blank(1, rowCount, centeredTitleFormat))
      sheet.addCell(new Blank(2, rowCount, centeredTitleFormat))
      sheet.addCell(standardCell)
      sheet.addCell(standardPerformanceCell)
      sheet.addCell(countQtyCell)
      sheet.addCell(new Blank(6, rowCount, centeredTitleFormat))
      sheet.addCell(new Blank(7, rowCount, centeredTitleFormat))
      sheet.addCell(new Blank(8, rowCount, centeredTitleFormat))
      sheet.addCell(new Blank(9, rowCount, centeredTitleFormat))

      rowCount += 1

    }
    
  }

  def createDocumentTitleRow(sheet: WritableSheet) {
    val dateFormatter = new SimpleDateFormat("yyyy/mm/dd")
    val sheetTitleCell = new Label(5, 0, s"個人效率期間表", centeredTitleFormat)
    val createDate = new Label(0, 1, dateFormatter.format(new Date), centeredTitleFormat)

    sheet.addCell(sheetTitleCell)
    sheet.addCell(createDate)
    sheet.addCell(new Label(0, 2, "工號", centeredTitleFormat))
    sheet.addCell(new Label(1, 2, "姓名", centeredTitleFormat))
    sheet.addCell(new Label(2, 2, "日期", centeredTitleFormat))
    sheet.addCell(new Label(3, 2, "標準量", centeredTitleFormat))
    sheet.addCell(new Label(4, 2, "效率標準量", centeredTitleFormat))
    sheet.addCell(new Label(5, 2, "實質生產量", centeredTitleFormat))
    sheet.addCell(new Label(6, 2, "總稼動時間", centeredTitleFormat))
    sheet.addCell(new Label(7, 2, "總停機時間", centeredTitleFormat))
    sheet.addCell(new Label(8, 2, "稼動 %", centeredTitleFormat))
    sheet.addCell(new Label(9, 2, "數量效率 %", centeredTitleFormat))
    sheet.addCell(new Label(10, 2, "平均效率 %", centeredTitleFormat))

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
