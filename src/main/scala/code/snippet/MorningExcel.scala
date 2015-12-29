package code.snippet

import java.util.{Calendar, GregorianCalendar}

import code.lib._
import code.model._
import net.liftweb.http.{S, SHtml}
import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._

/**
 *  用來顯示「產量統計」－＞「晨間檢討」的頁面的 Snippet
 */
class MorningExcel {

  /**
   *  用來顯示上方的麵包屑和開啟 Excel 的按鈕
   */
  def detail = {
    val Array(_, _, yearString, monthString) = S.uri.drop(1).split("/")
    val year = f"${yearString.toInt}%02d"
    val month = f"${monthString.toInt}%02d"

    "#currentYearMonth *" #> f"$year-$month" &
    "#currentYearMonth [href]" #> s"/excel/morning/$year/$month" &
    "#downloadExcel [href]" #> s"/api/excel/morning/$year/$month.xls"
  }

  /**
   *  用來顯示下方的設定區的編輯表格
   */
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

