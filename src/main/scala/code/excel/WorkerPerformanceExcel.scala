package code.excel

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import code.model._
import com.mongodb.casbah.Imports._
import jxl._
import jxl.write._
import net.liftweb.common._

/**
 *  用來輸出員工效率的報表的主程式
 *
 *  由於此份員工效率報表需要比較大的運算資源，所以在系統上
 *  是使用 crontab  固每每十五分鐘生成一次 cache  檔，然後
 *  當使用者點選連結時，丟出的是此 cache 檔。
 *
 *  此主程式就是用來產生員工效率 Excel 報表的程式。
 *
 */
object WorkerPerformanceExcel {
  private lazy val zhenhaiDB = MongoDB.zhenhaiDB

  def main(args: Array[String]) {
    val year = args(1).toInt
    val month = args(2).toInt
    val filepath = args(3)
    val boot = new bootstrap.liftweb.Boot
    boot.boot

    val exporter = new WorkerPerformanceExcel(year, month, filepath)
    exporter.outputExcel()
  }
}

/**
 *  輸出「員工效率」的 Excel 報表用的程式
 *
 *  @param    year      年份
 *  @param    month     月份
 *  @param    filepath  要輸出到哪個檔案
 */
class WorkerPerformanceExcel(year: Int, month: Int, filepath: String) {
  
  /**
   *  用來表示員工效率的物件類別
   *
   *  @param    shiftDate   工班日期
   *  @param    countQty    良品數
   */
  case class Performance(shiftDate: String, countQty: Long)

  /**
   *  代表一張工單的記錄
   *
   *  @param    shiftDate   該工單的工班日期
   *  @param    lotNo       該工單的工單號
   *  @param    partNo      該工單的料號
   *  @param    productCode 該工單料號中的產品尺吋
   *  @param    machineID   該工單在哪台機器上生產
   */
  case class OrderCount(shiftDate: String, lotNo: String, partNo: String, productCode: String, machineID: String)

  val zhenhaiDB = MongoDB.zhenhaiDB

  /**
   *  記錄員工效率（生產良品數）的資料表
   */
  val workerPerformanceTable = zhenhaiDB(f"workerPerformance-$year%4d-$month%02d")

  /**
   *  記錄員工工作時間的資料表
   */
  val operationTimeTable = zhenhaiDB(f"operationTime-$year%4d-$month%02d")

  /**
   *  記錄員工鎖機次數的資料表
   */
  val lockTable = zhenhaiDB(f"lock-$year%4d-$month%02d")

  /**
   *  未被刪除的員工列表
   */
  lazy val workers = {
    val workerMongoIDs = workerPerformanceTable.distinct("workerMongoID")
    workerMongoIDs.flatMap(x => Worker.findByMongoID(x.toString)).sortWith(_.workerID.get < _.workerID.get)
  }

  private lazy val defaultFont = new WritableFont(WritableFont.ARIAL, 12)

  /**
   *  字串置中的格式設定
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
   *  數字置中且背景為灰色的格式設定
   */
  private lazy val greyBackgroundFormat = {
    val greyBackgroundFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    greyBackgroundFormat.setBackground(jxl.format.Colour.GRAY_25)
    greyBackgroundFormat.setAlignment(jxl.format.Alignment.CENTRE)
    greyBackgroundFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    greyBackgroundFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    greyBackgroundFormat
  }

  /**
   *  數字置中且背景為綠色的格式設定
   */
  private lazy val greenBackgroundFormat = {
    val greenBackgroundFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    greenBackgroundFormat.setBackground(jxl.format.Colour.LIGHT_GREEN)
    greenBackgroundFormat.setAlignment(jxl.format.Alignment.CENTRE)
    greenBackgroundFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    greenBackgroundFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    greenBackgroundFormat
  }

  /**
   *  取得某個員工的該月份的效能記錄
   *
   *  @param    workerMongoID   員工在 MongoDB 中的 Primary Key 的值
   *  @return                   該員工
   */
  def getWorkerPerformance(workerMongoID: String): List[Performance] = {
    val records = workerPerformanceTable.find(DBObject("workerMongoID" -> workerMongoID)).map { record =>
      Performance(record.get("shiftDate").toString, record.get("countQty").toString.toLong)
    }
    records.toList.sortWith(_.shiftDate < _.shiftDate)
  }

  /**
   *  取得 operationTime 資料表中，某一筆記錄的工作時間（秒）
   *
   *  @param    record      operationTime 資料表中的某筆資料
   *  @return               該筆資料的工作時間（結束時間－開始時間）
   */
  def getOperationTime(record: DBObject) = {
    record.get("currentTimestamp").toString.toLong - record.get("startTimestamp").toString.toLong
  }

  /**
   *  取得某員工在某一天的效率記錄
   *
   *  @param    date              工班日期
   *  @param    workerMongoID     該員工在 MongoDB 中的 ID
   *  @return                     該員工的 (日管理標準量, 日效率標準量, 總嫁動時間)
   */
  def getWorkerPerformance(date: String, workerMongoID: String) = {
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

  /**
   *  取得某員工在某天的「平均效率」
   *
   *  @param    date              工班日期
   *  @param    workerMongoID     該員工在 MongoDB 中的 ID
   *  @return                     該員工在該日期的平均效率，若無法計算則為 None
   */
  def getAveragePeformance(workerMongoID: String, date: String): Option[Double] = {
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
        case Full(managementCount) => Some(countQty / managementCount.toDouble)
        case _ => None
      }
    }

    performances.flatten match {
      case Nil => None
      case xs => Some(xs.sum / xs.size)
    }
  }

  /**
   *  建立 Excel 報表中的員工效率矩陣
   *
   *  @param    sheet      要寫到 Excel 中的哪個 Sheet 中
   */
  def createMatrix(sheet: WritableSheet) {

    var rowCount = 3

    // 針對每一個員工
    workers.foreach { worker =>

      
      val performanceOfDates = getWorkerPerformance(worker.id.get.toString)
      val startRow = rowCount

      // 針對該員工每一個有工作記錄的日期
      performanceOfDates.foreach { performance =>

        // 計算該員工的效率並填到相對應的 Excel 欄中
        val (standard, standardPerformance, operationTime) = getWorkerPerformance(performance.shiftDate, worker.id.toString)
        
        val workerIDCell = new Label(0, rowCount, worker.workerID.get, centeredTitleFormat)
        val workerNameCell = new Label(1, rowCount, worker.name.get, centeredTitleFormat)
        val dateCell = new Label(2, rowCount, performance.shiftDate, centeredTitleFormat)
        val standardCell = new Number(3, rowCount, standard, centeredNumberFormat)
        val standardPerformanceCell = new Number(4, rowCount, standardPerformance, centeredNumberFormat)
        val countQtyCell = new Number(5, rowCount, performance.countQty, centeredNumberFormat)
        val operationTimeCell = new Number(6, rowCount, operationTime, centeredNumberFormat)

        val lockCount = lockTable.count(MongoDBObject("shiftDate" -> performance.shiftDate, "workerMongoID" -> worker.id.toString))
        val lockTimeCell = new Number(7, rowCount, lockCount * 10, centeredNumberFormat)

        val standardLoc = CellReferenceHelper.getCellReference(3, rowCount)
        val standardPerformanceLoc = CellReferenceHelper.getCellReference(4, rowCount)
        val countQtyLoc = CellReferenceHelper.getCellReference(5, rowCount)
        val kadou = {
          if (standard == 0) {
            new Label(8, rowCount, "請設定標準量", centeredTitleFormat)
          } else {
            new Formula(8, rowCount, s"$countQtyLoc / $standardLoc", centeredPercentFormat)
          }
        }

        val performancePercent = {
          if (standardPerformance == 0) {
            new Label(9, rowCount, "請設定效率標準量", centeredTitleFormat)
          } else {
            new Formula(9, rowCount, s"$countQtyLoc / $standardPerformanceLoc", centeredPercentFormat)
          }
        }

        val averagePerformance = getAveragePeformance(worker.id.toString, performance.shiftDate) match {
          case None => new Label(10, rowCount, "請設定標準量", centeredTitleFormat)
          case Some(value) => new Number(10, rowCount, value, centeredPercentFormat)
        }

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

  /**
   *  建立 Excel 報表中的員工效的標頭
   *
   *  @param    sheet      要寫到 Excel 中的哪個 Sheet 中
   */
  def createDocumentTitleRow(sheet: WritableSheet) {
    val dateFormatter = new SimpleDateFormat("yyyy/MM/dd")
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

  /**
   *  輸出 Excel 到建構子中指定的 OutputStream
   */
  def outputExcel() {

    val workbook = Workbook.createWorkbook(new File(filepath))
    val sheet = workbook.createSheet("人員效率", 0)
    val sheetSettings = sheet.getSettings
    sheetSettings.setDefaultRowHeight(400)
    sheetSettings.setDefaultColumnWidth(20)

    createDocumentTitleRow(sheet)
    createMatrix(sheet)
    workbook.write()
    workbook.close()
  }
}
