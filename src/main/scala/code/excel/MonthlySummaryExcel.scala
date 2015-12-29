package code.excel

import java.io.OutputStream
import java.util.{Calendar, GregorianCalendar}

import code.model._
import com.mongodb.casbah.Imports._
import jxl._
import jxl.write._

/**
 *  「產量統計」－＞「重點統計」的 Excel 表的產生程式需要用到的公
 *  用函式。
 */
object MonthlySummaryExcel {

  /**
   *  取得某個範圍內的所有產品尺吋的φ別的直徑（10x12 中的 10）
   *
   *  @param    capacityRange       要取得的電容容量的範圍
   *  @return                       該電容容量範圍內
   */
  def getAllProductPrefix(capacityRange: String) = {

    MongoDB.zhenhaiDB("product")
           .find(DBObject("capacityRange" -> capacityRange))
           .map(record => record("product").toString.split("x")(0))
           .toSet
           .filterNot(_.contains(".")).toList.sortWith(_ < _)

  }
}

/**
 *  輸出「重點統計」的 Excel 報表
 */
class MonthlySummaryExcel(year: Int, month: Int, capacityRange: String, outputStream: OutputStream) {
  
  val zhenhaiDB = MongoDB.zhenhaiDB
  val maxDate = new GregorianCalendar(year, month-1, 1).getActualMaximum(Calendar.DAY_OF_MONTH)

  /**
   *  全部φ別的標頭（只有尺吋的直徑，例如 5x11 中的 5），用來做為 column 的標頭
   */
  lazy val allProductPrefix = MonthlySummaryExcel.getAllProductPrefix(capacityRange)

  /**
   *  全部φ別的標頭加上「合計」
   */
  lazy val allProductPrefixWithTotal = allProductPrefix ++ List("合計")

  private lazy val defaultFont = new WritableFont(WritableFont.ARIAL, 12)

  /**
   *  字串置中的格式
   */
  private lazy val centeredTitleFormat = {
    val centeredTitleFormat = new WritableCellFormat(defaultFont)
    centeredTitleFormat.setAlignment(jxl.format.Alignment.CENTRE)
    centeredTitleFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    centeredTitleFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    centeredTitleFormat
  }

  /**
   *  數字置中的格式
   */
  private lazy val centeredNumberFormat = {
    val centeredNumberFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    centeredNumberFormat.setAlignment(jxl.format.Alignment.CENTRE)
    centeredNumberFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    centeredNumberFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    centeredNumberFormat
  }

  /**
   *  數字置中且為灰色背景的格式
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
   *  數字置中且為綠色背景的格式
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
   *  取得特定某日期中某程製程的總良品數
   *
   *  @param    date            要取得哪個日期的良品數
   *  @param    machineType     要取得哪個製程的良品數（1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳）
   *  @return                   在 date 這一天中，machineType 這個製程的總良品數
   */
  def getDaily(date: Int, machineType: Int) = {
    val dataList = zhenhaiDB(f"shift-$year-$month%02d-$date%02d")
                       .find(DBObject("capacityRange" -> capacityRange, "machineType" -> machineType))
    var productCount: Map[String, Long] = Map.empty

    dataList.foreach { record =>
      val productPrefix = record("product").toString.split("x")(0)
      val newCount = productCount.get(productPrefix).getOrElse(0L) + record("count_qty").toString.toLong
      productCount = productCount.updated(productPrefix, newCount)
    }

    productCount
    
  }

  /**
   *  建立 Excel 報表的標頭
   *
   *  @param    sheet     寫到 Excel 中的哪個 Sheet
   *
   */
  def createDocumentTitleRow(sheet: WritableSheet) {
    val sheetTitleCell = new Label(2, 0, s"$month 月份 $capacityRange 產量表", centeredTitleFormat)
    sheet.addCell(sheetTitleCell)
    sheet.mergeCells(2, 0, maxDate, 0)
  }

  /**
   *  建立 Excel 報表上面該月的日期的標頭
   *
   *  Excel 中第二列中從 C 到 AH 欄的日期的標頭
   *
   *  @param    sheet     寫到 Excel 哪個 sheet
   */
  def createDateAndTargetRow(sheet: WritableSheet) {

    // 1 號到該月最後一天
    for (date <- 1 to maxDate) {
      val fullDate = f"$year-$month%02d-$date%02d"
      val dateTitleCell = new Label(1 + date, 1, date.toString, greyBackgroundFormat)
      val targetCell = MonthlySummaryExcelSaved.get(fullDate, 10, capacityRange) match {
        case None => new Blank(1 + date, 2, greenBackgroundFormat)
        case Some(value) => new Number(1 + date, 2, value, greenBackgroundFormat)
      }
      sheet.addCell(dateTitleCell)
      sheet.addCell(targetCell)
    }

    // 最後一欄的總計
    val sumTitleCell = new Label(1 + maxDate + 1, 1, "總計", greyBackgroundFormat)
    val sumTargetFormula = s"SUM(C3:${CellReferenceHelper.getColumnReference(1+maxDate)}3)"
    val sumTargetCell = new Formula(1 + maxDate + 1, 2, sumTargetFormula, greenBackgroundFormat)
    sheet.addCell(sumTitleCell)
    sheet.addCell(sumTargetCell)
  }

  /**
   *  建立 Excel 左邊鎖定的兩欄
   *
   *  A 欄的 φ 別標頭，以及 B 欄的製程的標頭
   *
   *  @param    sheet       要寫到 Excel 哪個 Sheet 中
   */
  def createLeftPinnedTitleColumn(sheet: WritableSheet) {

    // 左上兩欄
    val capacityRangeTitle = new Label(0, 2, capacityRange + "∮", greenBackgroundFormat)
    val targetTitle = new Label(1, 2, "目標", greenBackgroundFormat)
    sheet.addCell(capacityRangeTitle)
    sheet.addCell(targetTitle)

    // φ 別
    var rowCount: Int = 3

    allProductPrefixWithTotal.zipWithIndex.foreach { case(productPrefix, index) =>
      val startRow = (index * 7) + rowCount
      val endRow = startRow + 6
      val productPrefixCell = new Label(0, startRow, s"$productPrefix ∮", centeredTitleFormat)
      sheet.addCell(productPrefixCell)
      sheet.mergeCells(0, startRow, 0, endRow)
    }
 
    // 製程
    for {
      productPrefix   <- allProductPrefixWithTotal
      machineTypeInfo <- List("卷取", "含浸", "組立", "手動老化", "自動老化", "繳庫", "出貨")
    } {
      val titleCell = new Label(1, rowCount, machineTypeInfo, centeredTitleFormat)
      sheet.addCell(titleCell)
      rowCount += 1
    }

  }

  def createValueMatrix(sheet: WritableSheet) {
    val dateRange = 1 to maxDate
    val machineTypeMapping = Map(
      1 -> dateRange.map(date => (date, getDaily(date, 1))).toMap,
      2 -> dateRange.map(date => (date, getDaily(date, 2))).toMap,
      3 -> dateRange.map(date => (date, getDaily(date, 3))).toMap
    )

    var rowCount = 3
    var columnCount = 2

    def getCountHolder(machineType: Int, date: Int, productPrefix: String): Option[Long] = {
      for {
         dateToProductCount <- machineTypeMapping.get(machineType)
         productCount       <- dateToProductCount.get(date)
         count              <- productCount.get(productPrefix)
      } yield count
    }

    def getCountHolderFromCustomData(machineType: Int, date: Int, productPrefix: String): Option[Long] = {
      MonthlySummaryExcelSaved.get(f"$year-$month%02d-$date%02d", machineType, productPrefix)
    }

    for {
      productPrefix   <- allProductPrefix
      machineTypeInfo <- List("卷取" -> 1, "含浸" -> 6, "組立" -> 2, "手動老化" -> 7, "自動老化" -> 3, "繳庫" -> 8, "出貨" -> 9)
      date            <- dateRange
    } {

      val (title, machineType) = machineTypeInfo

      val countHolder = if (machineType <= 5) {
        getCountHolder(machineType, date, productPrefix) 
      } else {
        getCountHolderFromCustomData(machineType, date, productPrefix)
      }

      val countCellHolder = countHolder match {
        case None if machineType >= 6 => Some(new Blank(columnCount, rowCount, centeredNumberFormat))
        case None => Some(new Number(columnCount, rowCount, 0, centeredNumberFormat))
        case Some(count) => Some(new Number(columnCount, rowCount, count, centeredNumberFormat))
      }

      countCellHolder.foreach(sheet.addCell)

      columnCount += 1

      if (date == dateRange.max) {
        rowCount += 1
        columnCount = 2
      }
    }

  }

  /**
   *  建立 Excel 中「總計」的欄位
   *
   *  每一列中的最後一欄的「總計」欄位，為該製程中的該月一號到最後一天的
   *  良品數的加總，加總的方式為使用 Excel 的 SUM 函式計算。
   *
   *  @param    sheet     要寫到哪個 Excel 的 Sheet 中
   */
  def createColumnSum(sheet: WritableSheet) {
    for {
      rowOffset <- 0 to 6     // 每欄有七種加總（0 = 卷取， 1 = 含浸 …… ，6 = 出貨）
      date <- 1 to maxDate    // 每個月有幾天
    } {
      
      val currentRow = 
        (allProductPrefix.size * 7) + // 每個 φ 別有七項，共有 allProductPrefix.size 
        3                           + // Excel 最上方有固定三行的表頭
        rowOffset                     // 目前計算的是第幾個加總（0 = 卷取， 1 = 含浸 …… ，6 = 出貨）

      val dateColumnLabel = CellReferenceHelper.getColumnReference(date + 1)
      val sumCells = (0 until allProductPrefix.size).map(i => s"${dateColumnLabel}${i * 7 + 4 + rowOffset}").mkString("+")
      val formula = new Formula(date + 1, currentRow, sumCells, centeredNumberFormat)

      sheet.addCell(formula)
    }

  }

  /**
   *  建立 Excel 中每一個 Column 中的加總
   *
   *  這裡計算的是 Excel 往下卷後最後一個大項「合計」的部份，
   *  計算的是該天中各個製程（卷取，含浸……）的加總。
   *
   */
  def createRowSum(sheet: WritableSheet) {

    var rowCount = 3  // Excel 上方固定三行的表頭，所以從第三行（從零開始算）開始計算

    for {
      productPrefix   <- allProductPrefixWithTotal
      machineTypeInfo <- List("卷取", "含浸", "組立", "手動老化", "自動老化", "繳庫", "出貨")
    } {
      val startCell = s"${CellReferenceHelper.getColumnReference(2)}${rowCount+1}"
      val endCell = s"${CellReferenceHelper.getColumnReference(maxDate + 1)}${rowCount+1}"
      val formula = new Formula(maxDate + 2, rowCount, s"SUM($startCell:$endCell)", centeredNumberFormat)
      sheet.addCell(formula)
      rowCount += 1
    }
  }

  /**
   *  輸出 Excel 到建構子指定的 OutputStream
   */
  def outputExcel() {

    val workbook = Workbook.createWorkbook(outputStream)
    val sheet = workbook.createSheet("重點統計", 0)
    val sheetSettings = sheet.getSettings
    sheetSettings.setDefaultRowHeight(400)
    sheetSettings.setDefaultColumnWidth(10)
    sheetSettings.setVerticalFreeze(3)
    sheetSettings.setHorizontalFreeze(2)

    createDocumentTitleRow(sheet)
    createDateAndTargetRow(sheet)
    createLeftPinnedTitleColumn(sheet)
    createValueMatrix(sheet)
    createColumnSum(sheet)
    createRowSum(sheet)

    workbook.write()
    workbook.close()
  }
}
