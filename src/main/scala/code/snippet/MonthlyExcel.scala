package code.snippet

import code.lib._
import code.model._
import code.excel._
import com.mongodb.casbah.Imports._
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import net.liftweb.common._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.util._
import net.liftweb.util.Helpers._

/**
 *  用來顯示網頁上「產量統計」－＞「重點統計」頁面的 Snippet
 *
 */
class MonthlyExcel {

  /**
   *  用來顯示最後一頁中上方的麵包屑和開啟 Excel 按鈕的 Snippet
   */
  def detail = {
    val Array(_, _, yearString, monthString, capacityRange) = S.uri.drop(1).split("/")
    val year = f"${yearString.toInt}%02d"
    val month = f"${monthString.toInt}%02d"

    "#currentYearMonth *" #> f"$year-$month" &
    "#currentYearMonth [href]" #> s"/excel/monthly/$year/$month" &
    "#capacityRange *" #> f"${URLDecoder.decode(capacityRange, "utf-8")} Φ" &
    "#capacityRange [href]" #> s"/excel/monthly/$year/$month/$capacityRange" &
    "#downloadExcel [href]" #> s"/api/excel/monthly/$year/$month/$capacityRange.xls"
  }

  /**
   *  用來顯示第二層裡選擇電容容量大小的按鈕
   */
  def month = {
    val Array(_, _, yearString, monthString) = S.uri.drop(1).split("/")
    val year = f"${yearString.toInt}%02d"
    val month = f"${monthString.toInt}%02d"

    "#currentYearMonth *" #> f"$year-$month" &
    "#currentYearMonth [href]" #> s"/excel/monthly/$year/$month" &
    "#smallCapacityButton [href]" #> s"/excel/monthly/$year/$month/5 - 8" &
    "#middleCapacityButton [href]" #> s"/excel/monthly/$year/$month/10 - 12.5" &
    "#largeCapacityButton [href]" #> s"/excel/monthly/$year/$month/16 - 18"
  }

  /**
   *  用來顯示下方的設定的表格
   */
  def table = {

    val Array(_, _, yearString, monthString, capacityRangeURL) = S.uri.drop(1).split("/")
    val capacityRange = URLDecoder.decode(capacityRangeURL, "UTF-8")
    val year = f"${yearString.toInt}%02d"
    val month = f"${monthString.toInt}%02d"
    val maxDate = new GregorianCalendar(year.toInt, month.toInt-1, 1).getActualMaximum(Calendar.DAY_OF_MONTH)

    lazy val allProductPrefix = MonthlySummaryExcel.getAllProductPrefix(URLDecoder.decode(capacityRange, "UTF-8"))

    val titleRow = <tr class="row">
      <th class="ui center aligned">含浸</th>
      <th class="ui center aligned">手動老化</th>
      <th class="ui center aligned">繳庫</th>
      <th class="ui center aligned">出貨</th>
    </tr>
    val entryRow = <tr class="row">
      <th class="ui center aligned"><input size="5" type="text" class="step6" /></th>
      <th class="ui center aligned"><input size="5" type="text" class="step7" /></th>
      <th class="ui center aligned"><input size="5" type="text" class="step8" /></th>
      <th class="ui center aligned"><input size="5" type="text" class="step9" /></th>
    </tr>

    def updateValue(fullDate: String, step: Int, productPrefix: String)(value: String): JsCmd = {
      asLong(value).foreach { valueInLong =>
        MonthlySummaryExcelSaved.updateValue(fullDate, step, productPrefix, valueInLong)
      }
    }

    def titleBinding(productPrefix: String) = ".row ^*" #> ""
    def entryBinding(fullDate: String, productPrefix: String) = {
      val step6SavedExcel = MonthlySummaryExcelSaved.get(fullDate, 6, productPrefix)
      val step7SavedExcel = MonthlySummaryExcelSaved.get(fullDate, 7, productPrefix)
      val step8SavedExcel = MonthlySummaryExcelSaved.get(fullDate, 8, productPrefix)
      val step9SavedExcel = MonthlySummaryExcelSaved.get(fullDate, 9, productPrefix)

      ".row ^*" #> "" &
      ".step6"  #> SHtml.ajaxText(step6SavedExcel.map(_.toString).getOrElse(""), false, updateValue(fullDate, 6, productPrefix)_) &
      ".step7"  #> SHtml.ajaxText(step7SavedExcel.map(_.toString).getOrElse(""), false, updateValue(fullDate, 7, productPrefix)_) &
      ".step8"  #> SHtml.ajaxText(step8SavedExcel.map(_.toString).getOrElse(""), false, updateValue(fullDate, 8, productPrefix)_) &
      ".step9"  #> SHtml.ajaxText(step9SavedExcel.map(_.toString).getOrElse(""), false, updateValue(fullDate, 9, productPrefix)_)
    }

    ".productTitleRow" #> allProductPrefix.map { product => ".product *" #> product } &
    ".titleRow" #> allProductPrefix.map { product => titleBinding(product)(titleRow) } &
    ".row" #> (1 to maxDate).map { date =>
      val fullDate = f"$year-$month-$date%02d"
      val targetSavedExcel = MonthlySummaryExcelSaved.get(fullDate, 10, capacityRange)

      ".date *" #> fullDate &
      ".targetValue" #> SHtml.ajaxText(targetSavedExcel.map(_.toString).getOrElse(""), false, updateValue(fullDate, 10, capacityRange)_) &
      ".entryRow" #> allProductPrefix.map { product => entryBinding(fullDate, product)(entryRow) }
    }
  }
}

