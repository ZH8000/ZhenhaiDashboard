package code.lib

import code.model._
import com.mongodb.casbah.Imports._
import java.util.Calendar
import java.util.GregorianCalendar
import java.io.OutputStream
import jxl._
import jxl.write._

class DailyMorningExcel(year: Int, month: Int) {
  
  val zhenhaiDB = MongoDB.zhenhaiDB
  val maxDate = new GregorianCalendar(year, month-1, 1).getActualMaximum(Calendar.DAY_OF_MONTH)

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

  lazy val allProducts = {
    val productLists = 
      zhenhaiDB("product").distinct("product")
                          .filter(p => p.toString.contains("x") && !p.toString.contains("."))
                          .filter(p => p.toString.split("x")(0).toDouble <= 18)
                          .map(_.toString)

    productLists.toList.sortWith { case (product1, product2) =>
      val Array(radius1, height1) = product1.split("x").map(_.toInt)
      val Array(radius2, height2) = product2.split("x").map(_.toInt)

      radius1 * 1000 + height1 < radius2 * 1000 + height2
    }
  }

  private lazy val columnAfterAllProducts = {
    ((allProducts.size) * 3) + 4
  }

  def createDocumentTitleRow(sheet: WritableSheet) {
    val sheetTitleCell = new Label(4, 0, s"每日晨間檢討成積表", centeredTitleFormat)
    sheet.addCell(sheetTitleCell)
    sheet.mergeCells(4, 0, 20, 0)
  }

  def createHandInColumn(sheet: WritableSheet) {
    val titleCell = new Label(columnAfterAllProducts, 1, "繳庫量", centeredTitleFormat)
    val belowCell = new Blank(columnAfterAllProducts, 3, centeredNumberFormat)

    sheet.addCell(titleCell)
    sheet.addCell(belowCell)
    sheet.mergeCells(columnAfterAllProducts, 1, columnAfterAllProducts, 2)
    
    val rowOffset = 4
    (1 to maxDate).foreach { date =>
      val row = date + rowOffset
      val blankCell = new Blank(columnAfterAllProducts, row, centeredNumberFormat)
      sheet.addCell(blankCell)
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
    val belowCell = new Blank(columnIndex, 3, centeredNumberFormat)

    sheet.addCell(titleCell)
    sheet.addCell(belowCell)
    sheet.mergeCells(columnIndex, 1, columnIndex, 2)

    val rowOffset = 4

    (1 to maxDate).foreach { date =>
      val blankCell = new Blank(columnIndex, date + rowOffset, centeredNumberFormat)
      sheet.addCell(blankCell)
    }
  }

  def create102StorageColumn(sheet: WritableSheet) {
    val columnIndex = columnAfterAllProducts + 4
    val titleCell = new Label(columnIndex, 1, "102 倉庫存量", centeredTitleFormat)
    val belowCell = new Blank(columnIndex, 4, centeredNumberFormat)

    sheet.addCell(titleCell)
    sheet.addCell(belowCell)
    sheet.mergeCells(columnIndex, 1, columnIndex, 3)

    val rowOffset = 4

    (1 to maxDate).foreach { date =>
      val blankCell = new Blank(columnIndex, date + rowOffset, centeredNumberFormat)
      sheet.addCell(blankCell)
    }
  }

  def create118StorageColumn(sheet: WritableSheet) {
    val columnIndex = columnAfterAllProducts + 5
    val titleCell = new Label(columnIndex, 1, "118 倉庫存量", centeredTitleFormat)
    val belowCell = new Blank(columnIndex, 4, centeredNumberFormat)

    sheet.addCell(titleCell)
    sheet.addCell(belowCell)
    sheet.mergeCells(columnIndex, 1, columnIndex, 3)

    val rowOffset = 4

    (1 to maxDate).foreach { date =>
      val blankCell = new Blank(columnIndex, date + rowOffset, centeredNumberFormat)
      sheet.addCell(blankCell)
    }
  }

  def createStockColumn(sheet: WritableSheet) {
    val columnIndex = columnAfterAllProducts + 6
    val titleCell = new Label(columnIndex, 1, "現場堆貨量", centeredTitleFormat)
    val belowCell = new Blank(columnIndex, 3, centeredNumberFormat)

    sheet.addCell(titleCell)
    sheet.addCell(belowCell)
    sheet.mergeCells(columnIndex, 1, columnIndex, 2)
    sheet.mergeCells(columnIndex, 3, columnIndex, 4)

    val rowOffset = 4

    (1 to maxDate).foreach { date =>
      val blankCell = new Blank(columnIndex, date + rowOffset, centeredNumberFormat)
      sheet.addCell(blankCell)
    }
  }

  def createYieldRateColumn(sheet: WritableSheet) {
    val columnIndex = columnAfterAllProducts + 7
    val titleCell = new Label(columnIndex, 1, "步留率\n≧98.50％", centeredTitleFormat)
    val belowCell = new Blank(columnIndex, 3, centeredNumberFormat)

    sheet.addCell(titleCell)
    sheet.addCell(belowCell)
    sheet.mergeCells(columnIndex, 1, columnIndex, 2)
    sheet.mergeCells(columnIndex, 3, columnIndex, 4)

    val rowOffset = 4

    (1 to maxDate).foreach { date =>
      val blankCell = new Blank(columnIndex, date + rowOffset, centeredPercentFormat)
      sheet.addCell(blankCell)
    }
  }

  def createLeftPinnedMatrix(sheet: WritableSheet) {

    val rowOffset = 4
    val columnStartList = (0 until allProducts.size).map(_  * 3 + 4)

    (1 to maxDate).foreach { date =>
      val row = date + rowOffset
      val titleCell = new Label(0, row, f"$month%02d 月 $date%02d 日", centeredTitleFormat)
      val plannedCellsLoc = columnStartList.map(columnStart => CellReferenceHelper.getCellReference(columnStart, row))
      val countQtyCellsLoc = columnStartList.map(columnStart => CellReferenceHelper.getCellReference(columnStart + 1, row))
      val diffCellsLoc = columnStartList.map(columnStart => CellReferenceHelper.getCellReference(columnStart + 2, row))

      val plannedFormula = new Formula(1, row, plannedCellsLoc.mkString(" + "), centeredYellowNumberFormat)
      val countQtyFormula = new Formula(2, row, countQtyCellsLoc.mkString(" + "), centeredGreenNumberFormat)
      val diffFormula = new Formula(3, row, diffCellsLoc.mkString(" + "), centeredBlueNumberFormat)

      sheet.addCell(titleCell)
      sheet.addCell(plannedFormula)
      sheet.addCell(countQtyFormula)
      sheet.addCell(diffFormula)

    }

  }

  def createLeftPinnedColumnHeader(sheet: WritableSheet) {
    val planned = new Label(1, 1, "計劃生產量", centeredTitleFormat)
    val produced = new Label(2, 1, "實際組立量", centeredTitleFormat)
    val diff = new Label(3, 1, "實際組立量", centeredTitleFormat)

    sheet.addCell(planned)
    sheet.addCell(produced)
    sheet.addCell(diff)
    sheet.mergeCells(1, 1, 1, 3)
    sheet.mergeCells(2, 1, 2, 3)
    sheet.mergeCells(3, 1, 3, 3)
  }

  def createHeaderForEachProduct(sheet: WritableSheet) {
    
    val offsetForPinnedColumn = 4

    allProducts.zipWithIndex.foreach { case (product, index) =>
      val columnIndex = index * 3 + offsetForPinnedColumn
      val productTitle = new Label(columnIndex, 1, product, leftBorderTitleFormat)
      val machineCountTitle = new Label(columnIndex+1, 1, "機台數量", centeredTitleFormat)
      val machineCount = new Blank(columnIndex+2, 1, rightBorderTitleFormat)

      val capacity = new Blank(columnIndex, 2, bothBorderTitleFormat)

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

  def createMatrix(sheet: WritableSheet) {

    val rowOffset = 4
    val columnOffset = 4

    for {
      (product, index) <- allProducts.zipWithIndex
      dateToCount = getMonthlyCountForProduct(product)
      date <- 1 to maxDate
    } {
      
      val fullDate = f"$year-$month%02d-$date%02d"
      val count = dateToCount.get(fullDate).getOrElse(0L)
      val column = index * 3 + columnOffset
      val countCell = new Number(column + 1, date + rowOffset, count, centeredGreenNumberFormat)
      val plannedCell = new Blank(column, date + rowOffset, centeredYellowNumberFormat)

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
    val row = rowOffset + maxDate
    val titleCell = new Label(0, row, "TOTAL:", centeredTitleFormat)

    sheet.addCell(titleCell)

    for (column <- 1 to (allProducts.size + 1) * 3 + 8) {

      val columnLoc = CellReferenceHelper.getColumnReference(column)

      val formula = s"SUM(${columnLoc}${rowOffset+2}:${columnLoc}${row})"
      val formulaCell = new Formula(column, row, formula, centeredYellowNumberFormat)
      sheet.addCell(formulaCell)
    }

    val columnFor102 = (allProducts.size + 1) * 3 + 5
    val columnFor118 = (allProducts.size + 1) * 3 + 6
    val columnForYieldRate = (allProducts.size + 1) * 3 + 8

    val columnForHandIn = (allProducts.size + 1) * 3 + 1
    val columnForHandOut = (allProducts.size + 1) * 3 + 4

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

    val workbook = Workbook.createWorkbook(new java.io.File("/home/brianhsu/test.xls"))
    val sheet = workbook.createSheet("abc", 0)
    val sheetSettings = sheet.getSettings
    sheetSettings.setDefaultRowHeight(400)
    sheetSettings.setDefaultColumnWidth(15)
    sheetSettings.setVerticalFreeze(5)
    sheetSettings.setHorizontalFreeze(4)

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
