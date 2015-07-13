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

class MorningExcel {

  def detail = {
    val Array(_, _, yearString, monthString) = S.uri.drop(1).split("/")
    val year = f"${yearString.toInt}%02d"
    val month = f"${monthString.toInt}%02d"

    "#currentYearMonth *" #> f"$year-$month" &
    "#currentYearMonth [href]" #> s"/excel/morning/$year/$month" &
    "#downloadExcel [href]" #> s"/api/excel/morning/$year/$month"
  }

  def editor = {

    val Array(_, _, yearString, monthString) = S.uri.drop(1).split("/")
    val year = yearString.toInt
    val month = monthString.toInt
    val allProducts = DailyMorningExcel.getAllProducts

    def updateValue(fullDate: String, product: String, name: String)(value: String): JsCmd = {
      asLong(value).foreach { valueInLong =>
        DailySummaryExcelSaved.updateValue(fullDate, product, name, valueInLong)
      }
    }


    val onlyMonth = f"$year-$month"

    val handIn = DailySummaryExcelSaved.get(onlyMonth, "all", "handIn").map(_.toString)
    val handOut = DailySummaryExcelSaved.get(onlyMonth, "all", "handOut").map(_.toString)
    val storage102 = DailySummaryExcelSaved.get(onlyMonth, "all", "storage102").map(_.toString)
    val storage118 = DailySummaryExcelSaved.get(onlyMonth, "all", "storage118").map(_.toString)
    val stock = DailySummaryExcelSaved.get(onlyMonth, "all", "stock").map(_.toString)
    val yieldRate = DailySummaryExcelSaved.get(onlyMonth, "all", "yieldRate").map(_.toString)

    ".handIn" #> SHtml.ajaxText(handIn.getOrElse(""), false, updateValue(onlyMonth, "all", "handIn")_) &
    ".handOut" #> SHtml.ajaxText(handOut.getOrElse(""), false, updateValue(onlyMonth, "all", "handOut")_) &
    ".storage102" #> SHtml.ajaxText(storage102.getOrElse(""), false, updateValue(onlyMonth, "all", "storage102")_) &
    ".storage118" #> SHtml.ajaxText(storage118.getOrElse(""), false, updateValue(onlyMonth, "all", "storage118")_) &
    ".stock" #> SHtml.ajaxText(stock.getOrElse(""), false, updateValue(onlyMonth, "all", "stock")_) &
    ".yieldRate" #> SHtml.ajaxText(yieldRate.getOrElse(""), false, updateValue(onlyMonth, "all", "yieldRate")_) &
    ".productHeader" #> allProducts.map { product =>

      val machineCount = DailySummaryExcelSaved.get(onlyMonth, product, "machineCount")
      val machineCapacity = DailySummaryExcelSaved.get(onlyMonth, product, "machineCapacity")

      ".productName *" #> product &
      ".machineCount" #> SHtml.ajaxText(machineCount.map(_.toString).getOrElse(""), false, updateValue(onlyMonth, product, "machineCount")_) &
      ".machineCapacity" #> SHtml.ajaxText(machineCapacity.map(_.toString).getOrElse(""), false, updateValue(onlyMonth, product, "machineCapacity")_)
    } &
    ".row" #> (1 to 31).map { case date =>

      val fullDate = f"$year-$month-$date%02d"
      val plannedForDate = DailySummaryExcelSaved.get(fullDate, "all", "planned")

      val handIn = DailySummaryExcelSaved.get(fullDate, "all", "handIn").map(_.toString)
      val handOut = DailySummaryExcelSaved.get(fullDate, "all", "handOut").map(_.toString)
      val storage102 = DailySummaryExcelSaved.get(fullDate, "all", "storage102").map(_.toString)
      val storage118 = DailySummaryExcelSaved.get(fullDate, "all", "storage118").map(_.toString)
      val stock = DailySummaryExcelSaved.get(fullDate, "all", "stock").map(_.toString)
      val yieldRate = DailySummaryExcelSaved.get(fullDate, "all", "yieldRate").map(_.toString)

      ".month *" #> monthString &
      ".date *" #> date &
      ".datePlanned" #> SHtml.ajaxText(plannedForDate.map(_.toString).getOrElse(""), false, updateValue(fullDate, "all", "planned")_) &
      ".handIn" #> SHtml.ajaxText(handIn.getOrElse(""), false, updateValue(fullDate, "all", "handIn")_) &
      ".handOut" #> SHtml.ajaxText(handOut.getOrElse(""), false, updateValue(fullDate, "all", "handOut")_) &
      ".storage102" #> SHtml.ajaxText(storage102.getOrElse(""), false, updateValue(fullDate, "all", "storage102")_) &
      ".storage118" #> SHtml.ajaxText(storage118.getOrElse(""), false, updateValue(fullDate, "all", "storage118")_) &
      ".stock" #> SHtml.ajaxText(stock.getOrElse(""), false, updateValue(fullDate, "all", "stock")_) &
      ".yieldRate" #> SHtml.ajaxText(yieldRate.getOrElse(""), false, updateValue(fullDate, "all", "yieldRate")_) &
      ".productPlanned" #> allProducts.map { product =>
        val planned = DailySummaryExcelSaved.get(fullDate, product, "planned").map(_.toString)
        ".productPlannedInput" #> SHtml.ajaxText(planned.getOrElse(""), false, updateValue(fullDate, product, "planned")_)
      }
    }
  }

}

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

