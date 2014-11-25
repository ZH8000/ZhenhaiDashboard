package code.snippet

import code.model._
import com.mongodb.casbah.Imports._
import net.liftweb.util.Helpers._
import net.liftweb.util._

class Dashboard {

  lazy val (minDate, maxDate) = {

    val dateSet = {

      val allDates = for {
        record <- MongoDB.zhenhaiDB("daily")
        date <- record.getAs[String]("timestamp")
      } yield date

      allDates.toSet

    }

    (dateSet.min, dateSet.max)
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

    "#monthlyReportButton [href]" #> s"/monthly/$currentYear" &
    "#dailyReportButton [href]" #> s"/daily/$currentYear/$currentMonth"
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


