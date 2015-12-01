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

class Dashboard {


  lazy val (minDate, maxDate) = {

    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    val dateSet = {

      val allDates = for {
        record <- MongoDB.zhenhaiDB("daily")
        date <- record.getAs[String]("timestamp")
      } yield date

      allDates.toSet

    }

    dateSet.isEmpty match {
      case true  => (dateFormatter.format(new Date), dateFormatter.format(new Date))
      case false => (dateSet.min, dateSet.max)
    }
  }

  def aliveLink = {
    if (S.hostAndPath contains "221.4.141.146") {
      "#aliveButton [href]" #> "http://221.4.141.146:8080/pic" 
    } else {
      "#aliveButton [href]" #> "#" &
      "#aliveButton [class+]" #> "disabled"
    }
  }

  def monthPicker = {
    "#maxYear [value]" #> maxDate.substring(0, 4) &
    "#maxMonth [value]" #> maxDate.substring(5, 7) &
    "#minYear [value]" #> minDate.substring(0, 4) &
    "#minMonth [value]" #> minDate.substring(5, 7)
  }

  def reportLink = {
    import java.util.Calendar

    val calendar = Calendar.getInstance
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentDate = calendar.get(Calendar.DATE)

    "#monthlyReportButton [href]" #> s"/monthly/$currentYear" &
    "#dailyReportButton [href]" #> s"/daily/$currentYear/$currentMonth" &
    "#monthlyExcelReportButton [href]" #> s"/excel/monthly/$currentYear/$currentMonth" &
    "#dailyMorningReportButton [href]" #> s"/excel/morning/$currentYear/$currentMonth" &
    "#workerPerformanceReportButton [href]" #> s"/excel/workerPerformance/$currentYear/$currentMonth" &
    "#kadouTableReportButton [href]" #> s"/excel/kadou/$currentYear/$currentMonth" &
    "#machineDefactButton [href]" #> f"/machineDefactSummary/$currentYear/$currentMonth%02d/$currentDate%02d"
  }

  def yearSelector = {

    val range = (minDate.substring(0, 4).toInt to maxDate.substring(0, 4).toInt).reverse

    "option" #> range.map { year =>
      "option *" #> year &
      "option [value]" #> year &
      "option [onclick]" #> s"window.location='/monthly/$year'"
    }
  }

  def alertLink = {

    val alertTable = MongoDB.zhenhaiDB("alert")
    val firstAlert = alertTable.headOption

    firstAlert.isEmpty match {
      case true => "a [class+]" #> "disabled"
      case false => "a [href]" #> "/alert"
    }
  }
  
}


