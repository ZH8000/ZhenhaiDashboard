package code.snippet

import net.liftweb.common.{Box, Full, Empty, Failure}
import net.liftweb.common.Box._

import net.liftweb.util._
import net.liftweb.util.Helpers._

import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.SessionVar

import bootstrap.liftweb._
import code.db._
import com.mongodb.casbah.Imports._

object CurrentUser extends SessionVar[Box[User]](Empty)

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
    val currentMonth = calendar.get(Calendar.MONTH + 1)

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
  
}

class Login {

  private var usernameBox: Box[String] = Empty
  private var passwordBox: Box[String] = Empty

  private def setupSessionAndRedirect(user: User) = {
    CurrentUser(Full(user))
    S.redirectTo("/dashboard")
  }

  def login = {

    val loggedInUser = for {
      username <- usernameBox.filter(_.trim.size > 0) ?~ "請輸入帳號"
      password <- passwordBox.filter(_.trim.size > 0) ?~ "請輸入密碼"
      user <- User.loginAs(username, password) ?~ "帳號或密碼錯誤"
    } yield user

    loggedInUser match {
      case Full(user) => setupSessionAndRedirect(user)
      case Failure(msg, _, _) => S.error(msg)
      case Empty => S.error("系統異常無法登入，請連絡管理者")
    }

  }

  def render = {
    "#username" #> SHtml.onSubmit(x => usernameBox = Full(x)) &
    "#password" #> SHtml.onSubmit(x => passwordBox = Full(x)) &
    "type=submit" #> SHtml.onSubmitUnit(login _)
  }
}
