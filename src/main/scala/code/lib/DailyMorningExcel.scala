package code.lib

import java.io.OutputStream
import java.util.{Calendar, GregorianCalendar}
import java.text.SimpleDateFormat
import code.model._
import com.mongodb.casbah.Imports._
import jxl._
import jxl.write._


object DailyMorningExcel {

  val validSet = Set(
    "4x7", "5x11", "6x7", "6x11", "6x15", "8x11", "8x15",
    "8x20", "10x12", "10x16", "10x20", "10x25", "10x30", "10x40",
    "12x15", "12x20", "12x25", "12x30", "12x35", "12x40", "12x45",
    "12x50", "16x20", "16x25", "16x30", "16x35", "16x40", "18x16",
    "18x20", "18x25", "18x30", "18x35", "18x40", "18x45", "18x50",
    "20x40", "22x40", "8x18", "8x12", "12x31", "8x13", "10x17",
    "12x35", "8x16", "10x18", "8x17", "10x31", "8x18"
  )

  def getAllProducts = {
    val zhenhaiDB = MongoDB.zhenhaiDB

    val productLists = 
      zhenhaiDB("product").distinct("product")
                          .filter(validSet contains _.toString)
                          .filter(p => p.toString.contains("x") && !p.toString.contains("."))
                          .filter(p => p.toString.split("x")(0).toDouble <= 22)
                          .filterNot(p => p.toString.startsWith("16x") || p.toString.startsWith("18x"))
                          .map(_.toString)

    productLists.toList.sortWith { case (product1, product2) =>
      val Array(radius1, height1) = product1.split("x").map(_.toInt)
      val Array(radius2, height2) = product2.split("x").map(_.toInt)

      radius1 * 1000 + height1 < radius2 * 1000 + height2
    } ++ List("16", "18")


  }
}

class DailyMorningExcel(year: Int, month: Int, outputStream: OutputStream) {
  
  val zhenhaiDB = MongoDB.zhenhaiDB
  val maxDate = new GregorianCalendar(year, month-1, 1).getActualMaximum(Calendar.DAY_OF_MONTH)
  val onlyMonth = f"$year-$month%02d"

  private lazy val defaultFont = new WritableFont(WritableFont.ARIAL, 12)
  private lazy val centeredTitleFormat = {
    val format = new WritableCellFormat(defaultFont)
    format.setAlignment(jxl.format.Alignment.CENTRE)
    format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    format
  }

  private lazy val leftBorderTitleFormat = {
    val format = new WritableCellFormat(defaultFont)
    format.setAlignment(jxl.format.Alignment.CENTRE)
    format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    format.setBorder(jxl.format.Border.LEFT, jxl.format.BorderLineStyle.THICK)
    format
  }

  private lazy val rightBorderTitleFormat = {
    val format = new WritableCellFormat(defaultFont)
    format.setAlignment(jxl.format.Alignment.CENTRE)
    format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    format.setBorder(jxl.format.Border.RIGHT, jxl.format.BorderLineStyle.THICK)
    format
  }

  private lazy val bothBorderTitleFormat = {
    val format = new WritableCellFormat(defaultFont)
    format.setAlignment(jxl.format.Alignment.CENTRE)
    format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    format.setBorder(jxl.format.Border.LEFT, jxl.format.BorderLineStyle.THICK)
    format.setBorder(jxl.format.Border.RIGHT, jxl.format.BorderLineStyle.THICK)
    format
  }

  private lazy val centeredNumberFormat = {
    val centeredNumberFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    centeredNumberFormat.setAlignment(jxl.format.Alignment.CENTRE)
    centeredNumberFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    centeredNumberFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    centeredNumberFormat
  }

  private lazy val centeredPercentFormat = {
    val centeredNumberFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("0.00%"))
    centeredNumberFormat.setAlignment(jxl.format.Alignment.CENTRE)
    centeredNumberFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    centeredNumberFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    centeredNumberFormat
  }

  private lazy val centeredYellowPercentFormat = {
    val format = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("0.00%"))
    format.setBackground(jxl.format.Colour.YELLOW)
    format.setAlignment(jxl.format.Alignment.CENTRE)
    format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    format.setBorder(jxl.format.Border.LEFT, jxl.format.BorderLineStyle.THICK)
    format
  }

  private lazy val centeredYellowNumberFormat = {
    val format = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    format.setBackground(jxl.format.Colour.YELLOW)
    format.setAlignment(jxl.format.Alignment.CENTRE)
    format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    format.setBorder(jxl.format.Border.LEFT, jxl.format.BorderLineStyle.THICK)
    format
  }

  private lazy val centeredBlueNumberFormat = {
    val format = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0_ ;[RED]-#,##0"))
    format.setBackground(jxl.format.Colour.LIGHT_TURQUOISE2)
    format.setAlignment(jxl.format.Alignment.CENTRE)
    format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    format.setBorder(jxl.format.Border.RIGHT, jxl.format.BorderLineStyle.THICK)
    format
  }
  private lazy val centeredGreenNumberFormat = {
    val format = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    format.setBackground(jxl.format.Colour.LIME)
    format.setAlignment(jxl.format.Alignment.CENTRE)
    format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)
    format
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

  lazy val allProducts = DailyMorningExcel.getAllProducts
  
  private lazy val columnAfterAllProducts = {
    ((allProducts.size) * 3) + 5
  }

  def createDocumentTitleRow(sheet: WritableSheet) {
    val sheetTitleCell = new Label(5, 0, s"每日晨間檢討成績表", centeredTitleFormat)
    sheet.addCell(sheetTitleCell)
    sheet.mergeCells(5, 0, 20, 0)
  }

  def createHandInColumn(sheet: WritableSheet) {
    val titleCell = new Label(columnAfterAllProducts, 1, "繳庫量", centeredTitleFormat)
    val belowCell = DailySummaryExcelSaved.get(onlyMonth, "all", "handIn") match {
      case None => new Blank(columnAfterAllProducts, 3, centeredNumberFormat)
      case Some(value) => new Number(columnAfterAllProducts, 3, value, centeredNumberFormat)
    }

    sheet.addCell(titleCell)
    sheet.addCell(belowCell)
    sheet.mergeCells(columnAfterAllProducts, 1, columnAfterAllProducts, 2)
    
    val rowOffset = 4
    (1 to maxDate).foreach { date =>
      val row = date + rowOffset
      val fullDate = f"$year-$month%02d-$date%02d"
      val valueCell = DailySummaryExcelSaved.get(fullDate, "all", "handIn") match {
        case None => new Blank(columnAfterAllProducts, row, centeredNumberFormat)
        case Some(value) => new Number(columnAfterAllProducts, row, value, centeredNumberFormat)
      }

      sheet.addCell(valueCell)
    }

  }

  def createAgingColumn(sheet: WritableSheet) {
    val titleCell = new Label(columnAfterAllProducts + 1, 1, "老化機產量", centeredTitleFormat)
    val dayCell = new Label(columnAfterAllProducts + 1, 3, "白班", centeredTitleFormat)
    val nightCell = new Label(columnAfterAllProducts + 2, 3, "晚班", centeredTitleFormat)

    sheet.addCell(titleCell)
    sheet.addCell(dayCell)
    sheet.addCell(nightCell)
    sheet.mergeCells(columnAfterAllProducts + 1, 1, columnAfterAllProducts + 2, 2)
    
    val rowOffset = 4
    (1 to maxDate).foreach { date =>
      val row = date + rowOffset
      val (dayCount, nightCount) = getAgingCount(f"$year-$month%02d-$date%02d")

      val dayCell = new Number(columnAfterAllProducts + 1, row, dayCount, centeredNumberFormat)
      val nightCell = new Number(columnAfterAllProducts + 2, row, nightCount, centeredNumberFormat)

      sheet.addCell(dayCell)
      sheet.addCell(nightCell)

    }

  }

  def getAgingCount(date: String) = {
    
    var dayCount: Long = 0
    var nightCount: Long = 0
    val records = zhenhaiDB(s"shift-$date").find(MongoDBObject("machineType" -> 3))

    records.foreach { record =>
      
      val hours = record.get("timestamp").toString.substring(11, 13).toInt

      if (hours >= 19 || hours <= 6) {
        nightCount += record.get("count_qty").toString.toLong
      } else {
        dayCount += record.get("count_qty").toString.toLong
      }
    }

    (dayCount, nightCount)

  }

  def createHandOutColumn(sheet: WritableSheet) {
    val columnIndex = columnAfterAllProducts + 3
    val titleCell = new Label(columnIndex, 1, "每日出庫數", centeredTitleFormat)
    val belowCell = DailySummaryExcelSaved.get(onlyMonth, "all", "handOut") match {
      case None => new Blank(columnIndex, 3, centeredNumberFormat)
      case Some(value) => new Number(columnIndex, 3, value, centeredNumberFormat)
    }

    sheet.addCell(titleCell)
    sheet.addCell(belowCell)
    sheet.mergeCells(columnIndex, 1, columnIndex, 2)

    val rowOffset = 4

    (1 to maxDate).foreach { date =>
      val fullDate = f"$year-$month%02d-$date%02d"
      val valueCell = DailySummaryExcelSaved.get(fullDate, "all", "handOut") match {
        case None => new Blank(columnIndex, date + rowOffset, centeredNumberFormat)
        case Some(value) => new Number(columnIndex, date + rowOffset, value, centeredNumberFormat)
      }

      sheet.addCell(valueCell)
    }
  }

  def create102StorageColumn(sheet: WritableSheet) {
    val columnIndex = columnAfterAllProducts + 4
    val titleCell = new Label(columnIndex, 1, "102 倉庫存量", centeredTitleFormat)
    val belowCell = DailySummaryExcelSaved.get(onlyMonth, "all", "storage102") match {
      case None => new Blank(columnIndex, 4, centeredNumberFormat)
      case Some(value) => new Number(columnIndex, 4, value, centeredNumberFormat)
    }

    sheet.addCell(titleCell)
    sheet.addCell(belowCell)
    sheet.mergeCells(columnIndex, 1, columnIndex, 3)

    val rowOffset = 4

    (1 to maxDate).foreach { date =>
      val fullDate = f"$year-$month%02d-$date%02d"
      val valueCell = DailySummaryExcelSaved.get(fullDate, "all", "storage102") match {
        case None => new Blank(columnIndex, date + rowOffset, centeredNumberFormat)
        case Some(value) => new Number(columnIndex, date + rowOffset, value, centeredNumberFormat)
      }

      sheet.addCell(valueCell)
    }
  }

  def create118StorageColumn(sheet: WritableSheet) {
    val columnIndex = columnAfterAllProducts + 5
    val titleCell = new Label(columnIndex, 1, "118 倉庫存量", centeredTitleFormat)
    val belowCell = DailySummaryExcelSaved.get(onlyMonth, "all", "storage118") match {
      case None => new Blank(columnIndex, 4, centeredNumberFormat)
      case Some(value) => new Number(columnIndex, 4, value, centeredNumberFormat)
    }

    sheet.addCell(titleCell)
    sheet.addCell(belowCell)
    sheet.mergeCells(columnIndex, 1, columnIndex, 3)

    val rowOffset = 4

    (1 to maxDate).foreach { date =>
      val fullDate = f"$year-$month%02d-$date%02d"
      val valueCell = DailySummaryExcelSaved.get(fullDate, "all", "storage118") match {
        case None => new Blank(columnIndex, date + rowOffset, centeredNumberFormat)
        case Some(value) => new Number(columnIndex, date + rowOffset, value, centeredNumberFormat)
      }
      sheet.addCell(valueCell)
    }
  }

  def createStockColumn(sheet: WritableSheet) {
    val columnIndex = columnAfterAllProducts + 6
    val titleCell = new Label(columnIndex, 1, "現場堆貨量", centeredTitleFormat)
    val belowCell = DailySummaryExcelSaved.get(onlyMonth, "all", "stock") match {
      case None => new Blank(columnIndex, 3, centeredNumberFormat)
      case Some(value) => new Number(columnIndex, 3, value, centeredNumberFormat)
    }

    sheet.addCell(titleCell)
    sheet.addCell(belowCell)
    sheet.mergeCells(columnIndex, 1, columnIndex, 2)
    sheet.mergeCells(columnIndex, 3, columnIndex, 4)

    val rowOffset = 4

    (1 to maxDate).foreach { date =>
      val fullDate = f"$year-$month%02d-$date%02d"
      val valueCell = DailySummaryExcelSaved.get(fullDate, "all", "stock") match {
        case None => new Blank(columnIndex, date + rowOffset, centeredNumberFormat)
        case Some(value) => new Number(columnIndex, date + rowOffset, value, centeredNumberFormat)
      }

      sheet.addCell(valueCell)
    }
  }

  def createYieldRateColumn(sheet: WritableSheet) {
    val columnIndex = columnAfterAllProducts + 7
    val titleCell = new Label(columnIndex, 1, "步留率\n≧98.50％", centeredTitleFormat)
    val belowCell = DailySummaryExcelSaved.get(onlyMonth, "all", "yieldRate") match {
      case None => new Blank(columnIndex, 3, centeredNumberFormat)
      case Some(value) => new Number(columnIndex, 3, value / 100.0, centeredPercentFormat)
    }

    sheet.addCell(titleCell)
    sheet.addCell(belowCell)
    sheet.mergeCells(columnIndex, 1, columnIndex, 2)
    sheet.mergeCells(columnIndex, 3, columnIndex, 4)

    val rowOffset = 4

    (1 to maxDate).foreach { date =>
      val fullDate = f"$year-$month%02d-$date%02d"
      val valueCell = DailySummaryExcelSaved.get(fullDate, "all", "yieldRate") match {
        case None => new Blank(columnIndex, date + rowOffset, centeredPercentFormat)
        case Some(value) => new Number(columnIndex, date + rowOffset, value / 100.0, centeredPercentFormat)
      }


      sheet.addCell(valueCell)
    }
  }

  def createLeftPinnedMatrix(sheet: WritableSheet) {

    val rowOffset = 4
    val columnStartList = (0 until allProducts.size).map(_  * 3 + 5)
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    val calendar = Calendar.getInstance

    (1 to maxDate).foreach { date =>
      val row = date + rowOffset
      val dateTime = dateFormatter.parse(f"$year-$month%02d-$date%02d")
      calendar.setTime(dateTime)
      val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) match {
        case Calendar.SUNDAY     => "日"
        case Calendar.MONDAY     => "一"
        case Calendar.TUESDAY    => "二"
        case Calendar.WEDNESDAY  => "三"
        case Calendar.THURSDAY   => "四"
        case Calendar.FRIDAY     => "五"
        case Calendar.SATURDAY   => "六"
      }
      val titleCell = new Label(0, row, f"$year/$month%02d/$date%02d", centeredTitleFormat)
      val dayOfWeekTitle = new Label(1, row, dayOfWeek, centeredTitleFormat)
      val plannedCellsLoc = columnStartList.map(columnStart => CellReferenceHelper.getCellReference(columnStart, row))
      val countQtyCellsLoc = columnStartList.map(columnStart => CellReferenceHelper.getCellReference(columnStart + 1, row))
      val diffCellsLoc = columnStartList.map(columnStart => CellReferenceHelper.getCellReference(columnStart + 2, row))

      val plannedFormula = new Formula(2, row, plannedCellsLoc.mkString(" + "), centeredYellowNumberFormat)
      val countQtyFormula = new Formula(3, row, countQtyCellsLoc.mkString(" + "), centeredGreenNumberFormat)
      val diffFormula = new Formula(4, row, diffCellsLoc.mkString(" + "), centeredBlueNumberFormat)

      sheet.addCell(titleCell)
      sheet.addCell(dayOfWeekTitle)
      sheet.addCell(plannedFormula)
      sheet.addCell(countQtyFormula)
      sheet.addCell(diffFormula)

    }

  }

  def createLeftPinnedColumnHeader(sheet: WritableSheet) {
    val planned = new Label(2, 1, "計劃生產量", centeredTitleFormat)
    val produced = new Label(3, 1, "實際組立量", centeredTitleFormat)
    val diff = new Label(4, 1, "差異量", centeredTitleFormat)

    sheet.addCell(planned)
    sheet.addCell(produced)
    sheet.addCell(diff)
    sheet.mergeCells(2, 1, 2, 3)
    sheet.mergeCells(3, 1, 3, 3)
    sheet.mergeCells(4, 1, 4, 3)
  }

  def createHeaderForEachProduct(sheet: WritableSheet) {
    
    val offsetForPinnedColumn = 5

    allProducts.zipWithIndex.foreach { case (product, index) =>
      val columnIndex = index * 3 + offsetForPinnedColumn
      val productTitle = new Label(columnIndex, 1, product, leftBorderTitleFormat)
      val machineCountTitle = new Label(columnIndex+1, 1, "機台數量", centeredTitleFormat)

      val machineCount = DailySummaryExcelSaved.get(onlyMonth, product, "machineCount") match {
        case Some(value) => new Number(columnIndex + 2, 1, value, rightBorderTitleFormat)
        case None => new Blank(columnIndex + 2, 1, rightBorderTitleFormat)
      }

      val capacity = DailySummaryExcelSaved.get(onlyMonth, product, "machineCapacity") match {
        case Some(value) => new Number(columnIndex, 2, value, bothBorderTitleFormat)
        case None => new Blank(columnIndex, 2, bothBorderTitleFormat)
      }

      val planned = new Label(columnIndex, 3, "排程量", leftBorderTitleFormat)
      val produced = new Label(columnIndex+1, 3, "製造量", centeredTitleFormat)
      val diff = new Label(columnIndex+2, 3, "差異量", rightBorderTitleFormat)

      sheet.addCell(productTitle)
      sheet.addCell(machineCountTitle)
      sheet.addCell(machineCount)
      sheet.addCell(capacity)
      sheet.addCell(planned)
      sheet.addCell(produced)
      sheet.addCell(diff)
      sheet.mergeCells(columnIndex, 2, columnIndex + 2, 2)

    }

  }

  def getMonthlyCountForProduct(product: String) = {

      if (product == "16" || product == "18") {
        var dateToCount: Map[String, Long] = Map.empty

        for (productCode <- DailyMorningExcel.validSet.filter(_.startsWith(product))) {

          println(s"===> productCode in $product: $productCode")
          val dataTable = zhenhaiDB(s"product-$productCode")

          dataTable.find(DBObject("machineType" -> 2)).foreach { case record =>
            val shiftDate = record.get("shiftDate").toString
            val oldValue = dateToCount.get(shiftDate).getOrElse(0L)
            val newValue = record.get("count_qty").toString.toLong + oldValue
            dateToCount = dateToCount.updated(shiftDate, newValue)
          }
        }

        println(dateToCount)

        dateToCount

      } else {
        var dateToCount: Map[String, Long] = Map.empty
        val dataTable = zhenhaiDB(s"product-$product")

        dataTable.find(DBObject("machineType" -> 2)).foreach { case record =>
          val shiftDate = record.get("shiftDate").toString
          val oldValue = dateToCount.get(shiftDate).getOrElse(0L)
          val newValue = record.get("count_qty").toString.toLong + oldValue
          dateToCount = dateToCount.updated(shiftDate, newValue)
        }

        dateToCount
      }
  }

  def createMatrix(sheet: WritableSheet) {

    val rowOffset = 4
    val columnOffset = 5

    for {
      (product, index) <- allProducts.zipWithIndex
      dateToCount = getMonthlyCountForProduct(product)
      date <- 1 to maxDate
    } {
      
      val fullDate = f"$year-$month%02d-$date%02d"
      val count = dateToCount.get(fullDate).getOrElse(0L)
      val column = index * 3 + columnOffset
      val countCell = new Number(column + 1, date + rowOffset, count, centeredGreenNumberFormat)

      val plannedCell = DailySummaryExcelSaved.get(fullDate, product, "planned") match {
        case Some(value) => new Number(column, date + rowOffset, value, centeredYellowNumberFormat)
        case None => new Blank(column, date + rowOffset, centeredYellowNumberFormat)
      }

      val leftCellLoc = CellReferenceHelper.getCellReference(column, date + rowOffset)
      val rightCellLoc = CellReferenceHelper.getCellReference(column + 1, date + rowOffset)
      val formula = s"$rightCellLoc - $leftCellLoc"

      val diffCell = new Formula(column + 2, date + rowOffset, formula, centeredBlueNumberFormat)

      sheet.addCell(countCell)
      sheet.addCell(plannedCell)
      sheet.addCell(diffCell)

    }
  }

  def createSumRow(sheet: WritableSheet) {
    val rowOffset = 4
    val row = rowOffset + maxDate + 1
    val titleCell = new Label(0, row, "TOTAL:", centeredTitleFormat)

    sheet.addCell(titleCell)

    for (column <- 1 to (allProducts.size + 1) * 3 + 8) {

      val columnLoc = CellReferenceHelper.getColumnReference(column)

      val formula = s"SUM(${columnLoc}${rowOffset+2}:${columnLoc}${row})"
      val formulaCell = new Formula(column, row, formula, centeredYellowNumberFormat)
      sheet.addCell(formulaCell)
    }

    val columnFor102 = (allProducts.size + 1) * 3 + 6
    val columnFor118 = (allProducts.size + 1) * 3 + 7
    val columnForYieldRate = (allProducts.size + 1) * 3 + 9

    val columnForHandIn = (allProducts.size + 1) * 3 + 2
    val columnForHandOut = (allProducts.size + 1) * 3 + 5

    val formulaFor102 = 
      s"${CellReferenceHelper.getCellReference(columnFor102, 4)} + " +
      s"${CellReferenceHelper.getCellReference(columnForHandIn, maxDate + rowOffset)} -" +
      s"${CellReferenceHelper.getCellReference(columnForHandOut, maxDate + rowOffset)} "

    val formulaForYield = 
      s"AVERAGE(${CellReferenceHelper.getCellReference(columnForYieldRate, 5)}:" +
      s"${CellReferenceHelper.getCellReference(columnForYieldRate, maxDate + rowOffset - 1)})"

    val formulaCellFor102 = new Formula(columnFor102, maxDate + rowOffset, formulaFor102, centeredYellowNumberFormat)
    val blankCellFor118 = new Blank(columnFor118, maxDate + rowOffset, centeredYellowNumberFormat)
    val formulaCellForYield = new Formula(columnForYieldRate, maxDate + rowOffset, formulaForYield, centeredYellowPercentFormat)

    sheet.addCell(formulaCellFor102)
    sheet.addCell(blankCellFor118)
    sheet.addCell(formulaCellForYield)

  }

  def outputExcel() {

    val workbook = Workbook.createWorkbook(outputStream)
    val sheet = workbook.createSheet("晨間檢討", 0)
    val sheetSettings = sheet.getSettings
    sheetSettings.setDefaultRowHeight(400)
    sheetSettings.setDefaultColumnWidth(15)
    sheetSettings.setVerticalFreeze(5)
    sheetSettings.setHorizontalFreeze(5)

    createDocumentTitleRow(sheet)
    createLeftPinnedColumnHeader(sheet)
    createLeftPinnedMatrix(sheet)
    createHeaderForEachProduct(sheet)
    createMatrix(sheet)
    createHandInColumn(sheet)
    createAgingColumn(sheet)
    createHandOutColumn(sheet)
    create102StorageColumn(sheet)
    create118StorageColumn(sheet)
    createStockColumn(sheet)
    createYieldRateColumn(sheet)
    createSumRow(sheet)

    workbook.write()
    workbook.close()

  }

}
