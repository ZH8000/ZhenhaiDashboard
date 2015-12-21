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

class KadouExcel {

  def detail = {
    val Array(_, _, yearString, monthString) = S.uri.drop(1).split("/")
    val year = f"${yearString.toInt}%02d"
    val month = f"${monthString.toInt}%02d"

    "#currentYearMonth *" #> f"$year-$month" &
    "#currentYearMonth [href]" #> s"/excel/kadou/$year/$month" &
    "#downloadExcel [href]" #> s"/api/excel/kadou/$year/$month"
  }


  def table = {
    val Array(_, _, yearString, monthString) = S.uri.drop(1).split("/")
    val year = f"${yearString.toInt}%02d"
    val month = f"${monthString.toInt}%02d"
    val maxDate = new GregorianCalendar(year.toInt, month.toInt-1, 1).getActualMaximum(Calendar.DAY_OF_MONTH)

    def updateValue(fullDate: String, step: Int)(value: String): JsCmd = {
      println(s"====> updateVlaue($fullDate, $step)($value)")
      asLong(value).foreach { valueInLong =>
        KadouExcelSaved.updateValue(fullDate, step, valueInLong)
      }
    }

    ".row" #> (1 to maxDate).map { case date =>

      val fullDate = f"$year-$month-$date%02d"
      val step1Value = KadouExcelSaved.get(fullDate, 1)
      val step2Value = KadouExcelSaved.get(fullDate, 2)
      val step3Value = KadouExcelSaved.get(fullDate, 3)
      val step4Value = KadouExcelSaved.get(fullDate, 4)
      val step5Value = KadouExcelSaved.get(fullDate, 5)

      ".date *" #> fullDate &
      ".step1"  #> SHtml.ajaxText(step1Value.map(_.toString).getOrElse(""), false, updateValue(fullDate, 1)_) &
      ".step2"  #> SHtml.ajaxText(step2Value.map(_.toString).getOrElse(""), false, updateValue(fullDate, 2)_) &
      ".step3"  #> SHtml.ajaxText(step3Value.map(_.toString).getOrElse(""), false, updateValue(fullDate, 3)_) &
      ".step4"  #> SHtml.ajaxText(step4Value.map(_.toString).getOrElse(""), false, updateValue(fullDate, 4)_) &
      ".step5"  #> SHtml.ajaxText(step4Value.map(_.toString).getOrElse(""), false, updateValue(fullDate, 5)_)
    }
  }
}

class MonthlyExcel {

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

