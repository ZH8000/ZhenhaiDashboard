package code.snippet

import code.model._
import code.lib._

import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.http.js.JE._

import scala.xml.NodeSeq
import scala.collection.JavaConversions._
import java.text.SimpleDateFormat

class UserList {

  def onDelete(userID: String)(value: String) = {
    User.find(userID).foreach(_.delete_!)
    JsRaw(s"""jQuery('#row-$userID').remove()""").cmd
  }

  def render = {
    ".row" #> User.findAll.map { user =>

      ".row [id]" #> s"row-${user.id}" &
      ".username *" #> user.username &
      ".workID *" #> user.employeeID &
      ".email *" #> user.email &
      ".group *" #> user.role &
      ".deleteLink [onclick]" #> SHtml.onEventIf(s"確定要刪除【${user.username}】這個帳號嗎？", onDelete(user.id.get.toString)_)
    }
  }

}

class AlertList {

  def showEmptyBox() = {
     S.error("查無機台異常")
     ".dataBlock" #> NodeSeq.Empty
  }

  def dateList = {
    val dateList = Alert.useColl(collection => collection.distinct("date")).toList

    ".alertDate" #> dateList.map { date =>
      "a [href]" #> s"/alert/alert/$date" &
      "a *"      #> date.toString
    }
  }

  def render = {

    val Array(_, _, date) = S.uri.drop(1).split("/")
    val alertList = Alert.findAll("date", date).toList.sortWith(_.timestamp.get < _.timestamp.get)

    alertList.isEmpty match {
      case true => showEmptyBox()
      case false =>
        ".row" #> alertList.map { item =>

          val errorDesc = MachineInfo.getErrorDesc(item.mach_id.get, item.defact_id.get)

          ".timestamp *" #> item.timestamp &
          ".machineID *" #> item.mach_id &
          ".defactID *" #> errorDesc
        }
    }
  }
}

