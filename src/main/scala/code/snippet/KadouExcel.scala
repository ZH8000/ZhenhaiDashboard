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
 *  用來顯示「產量統計」－＞「稼動率」的頁面
 *
 */
class KadouExcel {

  /**
   *  用來該頁顯示上方的麵包屑和開啟 Excel 的按鈕
   */
  def detail = {
    val Array(_, _, yearString, monthString) = S.uri.drop(1).split("/")
    val year = f"${yearString.toInt}%02d"
    val month = f"${monthString.toInt}%02d"

    "#currentYearMonth *" #> f"$year-$month" &
    "#currentYearMonth [href]" #> s"/excel/kadou/$year/$month" &
    "#downloadExcel [href]" #> s"/api/excel/kadou/$year/$month"
  }

  /**
   *  用來該頁顯示下方的目標值的設定表格
   */
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
      ".step5"  #> SHtml.ajaxText(step5Value.map(_.toString).getOrElse(""), false, updateValue(fullDate, 5)_)
    }
  }
}

