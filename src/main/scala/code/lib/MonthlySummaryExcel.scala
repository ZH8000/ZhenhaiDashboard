package code.lib

import code.model._
import com.mongodb.casbah.Imports._
import java.util.Calendar
import java.util.GregorianCalendar
import java.io.OutputStream

class MonthlySummaryExcel(year: Int, month: Int, capacityRange: String, outputStream: OutputStream) {
  
  val zhenhaiDB = MongoDB.zhenhaiDB
  val maxDate = new GregorianCalendar(year, month-1, 1).getActualMaximum(Calendar.DAY_OF_MONTH)

  lazy val getAllProductPrefix = {
    zhenhaiDB("product").find(DBObject("capacityRange" -> capacityRange))
                        .map(record => record("product").toString.split("x")(0))
                        .toSet.filterNot(_.contains(".")).toList.sortWith(_ < _)
  }

  def getDaily(date: Int, machineType: Int) = {
    val dataList = zhenhaiDB(f"shift-$year-$month%02d-$date%02d").find(DBObject("capacityRange" -> capacityRange, "machineType" -> machineType))
    var productCount: Map[String, Long] = Map.empty

    dataList.foreach { record =>
      val productPrefix = record("product").toString.split("x")(0)
      val newCount = productCount.get(productPrefix).getOrElse(0L) + record("count_qty").toString.toLong
      productCount = productCount.updated(productPrefix, newCount)
    }

    productCount
    
  }

  def outputExcel() {

    import jxl._
    import jxl.write._

    val defaultFont =new WritableFont(WritableFont.ARIAL, 12)
    val centeredTitleFormat = new WritableCellFormat(defaultFont)
    centeredTitleFormat.setAlignment(jxl.format.Alignment.CENTRE)
    centeredTitleFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    centeredTitleFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)

    val centeredNumberFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    centeredNumberFormat.setAlignment(jxl.format.Alignment.CENTRE)
    centeredNumberFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    centeredNumberFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)

    val greyBackgroundFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    greyBackgroundFormat.setBackground(jxl.format.Colour.GRAY_25)
    greyBackgroundFormat.setAlignment(jxl.format.Alignment.CENTRE)
    greyBackgroundFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    greyBackgroundFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)

    val greenBackgroundFormat = new WritableCellFormat(defaultFont, new jxl.write.NumberFormat("#,##0"))
    greenBackgroundFormat.setBackground(jxl.format.Colour.LIGHT_GREEN)
    greenBackgroundFormat.setAlignment(jxl.format.Alignment.CENTRE)
    greenBackgroundFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE)
    greenBackgroundFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN)

    val workbook = Workbook.createWorkbook(outputStream)
    val sheet = workbook.createSheet("abc", 0)
    val sheetSettings = sheet.getSettings
    sheetSettings.setDefaultRowHeight(400)
    sheetSettings.setDefaultColumnWidth(10)
    sheetSettings.setVerticalFreeze(3)
    sheetSettings.setHorizontalFreeze(2)

    val sheetTitleCell = new Label(2, 0, s"$month 月份 $capacityRange 產量表", centeredTitleFormat)
    sheet.addCell(sheetTitleCell)
    sheet.mergeCells(2, 0, maxDate, 0)

    for (date <- 1 to maxDate) {
      val dateTitleCell = new Label(1 + date, 1, date.toString, greyBackgroundFormat)
      val targetCell = new Blank(1 + date, 2, greenBackgroundFormat)
      sheet.addCell(dateTitleCell)
      sheet.addCell(targetCell)
    }
    val sumTitleCell = new Label(1 + maxDate + 1, 1, "總計", greyBackgroundFormat)
    val sumTargetCell = new Formula(1 + maxDate + 1, 2, s"SUM(C3:${CellReferenceHelper.getColumnReference(1+maxDate)}3)", greenBackgroundFormat)
    sheet.addCell(sumTitleCell)
    sheet.addCell(sumTargetCell)

    val capacityRangeTitle = new Label(0, 2, capacityRange + "∮", greenBackgroundFormat)
    val targetTitle = new Label(1, 2, "目標", greenBackgroundFormat)
    sheet.addCell(capacityRangeTitle)
    sheet.addCell(targetTitle)

    var rowCount: Int = 3
    val allProductPrefixWithTotal = getAllProductPrefix ++ List("合計")

    allProductPrefixWithTotal.zipWithIndex.foreach { case(productPrefix, index) =>
      val startRow = (index * 7) + rowCount
      val endRow = startRow + 6
      val productPrefixCell = new Label(0, startRow, s"$productPrefix ∮", centeredTitleFormat)
      sheet.addCell(productPrefixCell)
      sheet.mergeCells(0, startRow, 0, endRow)
    }
 
    for {
      productPrefix   <- allProductPrefixWithTotal
      machineTypeInfo <- List("卷取" -> 1, "含浸" -> -1, "組立" -> 2, "手動老化" -> -1, "自動老化" -> 3, "繳庫" -> -1, "出貨" -> -1)
    } {
      val titleCell = new Label(1, rowCount, machineTypeInfo._1, centeredTitleFormat)
      sheet.addCell(titleCell)
      rowCount += 1
    }

   

    val dateRange = 1 to maxDate
    val machineTypeMapping = Map(
      1 -> dateRange.map(date => (date, getDaily(date, 1))).toMap,
      2 -> dateRange.map(date => (date, getDaily(date, 2))).toMap,
      3 -> dateRange.map(date => (date, getDaily(date, 3))).toMap
    )

    rowCount = 3
    var columnCount = 2

    for {
      productPrefix   <- getAllProductPrefix
      machineTypeInfo <- List("卷取" -> 1, "含浸" -> -1, "組立" -> 2, "手動老化" -> -1, "自動老化" -> 3, "繳庫" -> -1, "出貨" -> -1)
      date            <- dateRange
    } {

      val (title, machineType) = machineTypeInfo
      val countHolder = for {
         dateToProductCount <- machineTypeMapping.get(machineType)
         productCount       <- dateToProductCount.get(date)
         count              <- productCount.get(productPrefix)
      } yield count

      val countCellHolder = countHolder match {
        case None if machineType == -1 => Some(new Blank(columnCount, rowCount, centeredNumberFormat))
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

    for {
      rowOffset <- 0 to 6
      date <- 1 to maxDate
    } {
      
      val currentRow = getAllProductPrefix.size * 7 + 3 + rowOffset
      val dateColumnLabel = CellReferenceHelper.getColumnReference(date + 1)
      val sumCells = (0 until getAllProductPrefix.size).map(i => s"${dateColumnLabel}${i * 7 + 4 + rowOffset}").mkString("+")
      val formula = new Formula(date + 1, currentRow, sumCells, centeredNumberFormat)
      sheet.addCell(formula)
    }

    rowCount = 3
    for {
      productPrefix   <- allProductPrefixWithTotal
      machineTypeInfo <- List("卷取" -> 1, "含浸" -> -1, "組立" -> 2, "手動老化" -> -1, "自動老化" -> 3, "繳庫" -> -1, "出貨" -> -1)
    } {

      val startCell = s"${CellReferenceHelper.getColumnReference(2)}${rowCount+1}"
      val endCell = s"${CellReferenceHelper.getColumnReference(maxDate + 1)}${rowCount+1}"
      val formula = new Formula(maxDate + 2, rowCount, s"SUM($startCell:$endCell)", centeredNumberFormat)
      sheet.addCell(formula)
      rowCount += 1
    }


    workbook.write()
    workbook.close()

  }

}
