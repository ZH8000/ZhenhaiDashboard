package code.snippet

import code.model._
import code.lib._

import com.mongodb.casbah.Imports._
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmd
import net.liftweb.common._

import java.util.Date
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Calendar

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
    val maxDate = new GregorianCalendar(year, month-1, 1).getActualMaximum(Calendar.DAY_OF_MONTH)
    val allProducts = DailyMorningExcel.getAllProducts

    def updateValue(fullDate: String, product: String, name: String)(value: String): JsCmd = {
      asLong(value).foreach { valueInLong =>
        DailySummaryExcelSaved.updateValue(fullDate, product, name, valueInLong)
      }
    }


    val onlyMonth = f"$year-$month%02d"

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
    ".row" #> (1 to maxDate).map { case date =>

      val fullDate = f"$year-$month%02d-$date%02d"
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
