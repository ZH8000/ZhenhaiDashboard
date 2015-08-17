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

  def outputExcel() {

    val workbook = Workbook.createWorkbook(outputStream)
    val sheet = workbook.createSheet("abc", 0)
    val sheetSettings = sheet.getSettings
    sheetSettings.setDefaultRowHeight(400)
    sheetSettings.setDefaultColumnWidth(20)
    workbook.write()
    workbook.close()
  }

}
