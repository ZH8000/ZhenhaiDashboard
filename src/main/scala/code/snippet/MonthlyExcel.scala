package code.snippet

import code.model._
import code.lib._

import com.mongodb.casbah.Imports._
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmd

import java.util.Date
import java.text.SimpleDateFormat

import net.liftweb.http.S

class MonthlyExcel {
  import java.net.URLDecoder
  def detail = {
    val Array(_, _, yearString, monthString, capacityRange) = S.uri.drop(1).split("/")
    val year = f"${yearString.toInt}%02d"
    val month = f"${monthString.toInt}%02d"

    "#currentYearMonth *" #> f"$year-$month" &
    "#currentYearMonth [href]" #> s"/excel/monthly/$year/$month" &
    "#capacityRange *" #> f"${URLDecoder.decode(capacityRange, "utf-8")} Φ" &
    "#capacityRange [href]" #> s"/excel/monthly/$year/$month/$capacityRange" &
    "#downloadExcel [href]" #> s"/api/excel/monthly/$year/$month/$capacityRange"
  }

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

  def table = {


    val Array(_, _, yearString, monthString, capacityRangeURL) = S.uri.drop(1).split("/")
    val capacityRange = URLDecoder.decode(capacityRangeURL, "UTF-8")
    val year = f"${yearString.toInt}%02d"
    val month = f"${monthString.toInt}%02d"
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
    ".row" #> (1 to 31).map { date =>
      val fullDate = f"$year-$month-$date%02d"
      val targetSavedExcel = MonthlySummaryExcelSaved.get(fullDate, 10, capacityRange)

      ".date *" #> fullDate &
      ".targetValue" #> SHtml.ajaxText(targetSavedExcel.map(_.toString).getOrElse(""), false, updateValue(fullDate, 10, capacityRange)_) &
      ".entryRow" #> allProductPrefix.map { product => entryBinding(fullDate, product)(entryRow) }
    }
  }
}
