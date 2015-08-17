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

object WorkerPerformanceExcel {
  private lazy val zhenhaiDB = MongoDB.zhenhaiDB
}

class WorkerPerformanceExcel(year: Int, month: Int, outputStream: OutputStream) {
  
  val zhenhaiDB = MongoDB.zhenhaiDB
  val workerPerformanceTable = zhenhaiDB(f"workerPerformance-$year%4d-$month%02d")
  val operationTimeTable = zhenhaiDB(f"operationTime-$year%4d-$month%02d")
  val lockTable = zhenhaiDB(f"lock-$year%4d-$month%02d")

  lazy val workers = {
    val workerMongoIDs = workerPerformanceTable.distinct("workerMongoID")
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

  case class Performance(shiftDate: String, countQty: Long)

  def getWorkerPerformance(workerMongoID: String): List[Performance] = {
    val records = workerPerformanceTable.find(DBObject("workerMongoID" -> workerMongoID)).map { record =>
      Performance(record.get("shiftDate").toString, record.get("countQty").toString.toLong)
    }
    records.toList.sortWith(_.shiftDate < _.shiftDate)
  }

  def getOperationTime(record: DBObject) = {
    record.get("currentTimestamp").toString.toLong - record.get("startTimestamp").toString.toLong
  }


  case class OrderCount(shiftDate: String, lotNo: String, partNo: String, productCode: String, machineID: String)

  def getStandardPerformance(date: String, workerMongoID: String) = {
    val data = operationTimeTable.find(DBObject("shiftDate" -> date, "workerID" -> workerMongoID)).toList
    val operationTimeInMinutes = data.map(record => getOperationTime(record)).sum / 60
    val distinctOrders = data.map { record => 
      OrderCount(
        record.get("shiftDate").toString, 
        record.get("lotNo").toString, 
        record.get("partNo").toString, 
        record.get("productCode").toString,
        record.get("machineID").toString
      ) 
    }.toSet

    var managementCount = 0L
    var performanceCount = 0L

    distinctOrders.foreach { order =>
      val machinePerformance = MachinePerformance.find(order.machineID, order.productCode)
      managementCount  += machinePerformance.map(_.managementCount.get).getOrElse(0L)
      performanceCount += machinePerformance.map(_.performanceCount.get).getOrElse(0L)
    }

    (managementCount, performanceCount, operationTimeInMinutes)
  }

  
  def getAveragePeformance(workerMongoID: String, date: String): Double = {
    case class OperationTimeWithCount(machineID: String, lotNo: String, partNo: String, productCode: String)

    var operationToCount: Map[OperationTimeWithCount, Long] = Map.empty
    val data = operationTimeTable.find(DBObject("shiftDate" -> date, "workerID" -> workerMongoID)).toList
    val operationTimes = data.foreach { record =>
      val operation = OperationTimeWithCount(
        record.get("machineID").toString,
        record.get("lotNo").toString,
        record.get("partNo").toString,
        record.get("productCode").toString
      )

      val newValue = operationToCount.get(operation).getOrElse(0L)
      operationToCount = operationToCount.updated(operation, newValue)
    }

    val performances = operationToCount.map { case (operation, countQty) =>
      val machinePerformance = MachinePerformance.find(operation.machineID, operation.productCode)
      val managementCountHolder = machinePerformance.map(_.managementCount.get)

      managementCountHolder match {
        case Full(managementCount) => countQty / managementCount.toDouble
        case _ => 0
      }
    }

    performances match {
      case Nil => 0
      case xs => xs.sum / xs.size
    }
  }

  def createMatrix(sheet: WritableSheet) {
    var rowCount = 3

    workers.foreach { worker =>

      
      val performanceOfDates = getWorkerPerformance(worker.id.get.toString)
      val startRow = rowCount

      performanceOfDates.foreach { performance =>

        val (standard, standardPerformance, operationTime) = getStandardPerformance(performance.shiftDate, worker.id.toString)
        
        val workerIDCell = new Label(0, rowCount, worker.workerID.get, centeredTitleFormat)
        val workerNameCell = new Label(1, rowCount, worker.name.get, centeredTitleFormat)
        val dateCell = new Label(2, rowCount, performance.shiftDate, centeredTitleFormat)
        val standardCell = new Number(3, rowCount, standard, centeredNumberFormat)
        val standardPerformanceCell = new Number(4, rowCount, standardPerformance, centeredNumberFormat)
        val countQtyCell = new Number(5, rowCount, performance.countQty, centeredNumberFormat)
        val operationTimeCell = new Number(6, rowCount, operationTime, centeredNumberFormat)

        getStandardPerformance(performance.shiftDate, worker.id.toString)

        val lockCount = lockTable.count(MongoDBObject("shiftDate" -> performance.shiftDate, "workerMongoID" -> worker.id.toString))
        val lockTimeCell = new Number(7, rowCount, lockCount * 10, centeredNumberFormat)

        val standardLoc = CellReferenceHelper.getCellReference(3, rowCount)
        val standardPerformanceLoc = CellReferenceHelper.getCellReference(4, rowCount)
        val countQtyLoc = CellReferenceHelper.getCellReference(5, rowCount)
        val kadou = new Formula(8, rowCount, s"$countQtyLoc / $standardLoc", centeredPercentFormat)
        val performancePercent = new Formula(9, rowCount, s"$countQtyLoc / $standardPerformanceLoc", centeredPercentFormat)
        val averagePerformance = new Number(10, rowCount, getAveragePeformance(worker.id.toString, performance.shiftDate), centeredPercentFormat)

        sheet.addCell(workerIDCell)
        sheet.addCell(workerNameCell)
        sheet.addCell(dateCell)
        sheet.addCell(standardCell)
        sheet.addCell(standardPerformanceCell)
        sheet.addCell(countQtyCell)
        sheet.addCell(operationTimeCell)
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

      val operationTimeFormula = s"SUM(G${startRow+1}:G${rowCount})"
      val operationTimeCell = new Formula(6, rowCount, operationTimeFormula, centeredNumberFormat)
      val lockTimeFormula = s"SUM(H${startRow+1}:H${rowCount})"
      val lockTimeCell = new Formula(7, rowCount, operationTimeFormula, centeredNumberFormat)

      val kadouFormula = s"F${rowCount+1} / D${rowCount+1}"
      val kadouCell = new Formula(8, rowCount, kadouFormula, centeredPercentFormat)

      val performanceFormula = s"F${rowCount+1} / E${rowCount+1}"
      val performanceCell = new Formula(9, rowCount, performanceFormula, centeredPercentFormat)

      val averageFormula = s"AVERAGE(K${startRow+1}:K${rowCount})"
      val averageCell = new Formula(10, rowCount, averageFormula, centeredPercentFormat)

      sheet.addCell(titleCell)
      sheet.addCell(new Blank(1, rowCount, centeredTitleFormat))
      sheet.addCell(new Blank(2, rowCount, centeredTitleFormat))
      sheet.addCell(standardCell)
      sheet.addCell(standardPerformanceCell)
      sheet.addCell(countQtyCell)
      sheet.addCell(operationTimeCell)
      sheet.addCell(lockTimeCell)
      sheet.addCell(kadouCell)
      sheet.addCell(performanceCell)
      sheet.addCell(averageCell)

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
