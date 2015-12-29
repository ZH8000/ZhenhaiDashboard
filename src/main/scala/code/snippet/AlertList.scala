package code.snippet

import code.lib._
import code.model._
import net.liftweb.http.S
import net.liftweb.util.Helpers._

import scala.collection.JavaConversions._
import scala.xml.NodeSeq

/**
 *  用來顯示 {{{/alert/}}} 此網中的動態資料的 Snippet
 *
 */
class AlertList {

  /**
   *  當無資料時顯示「查無機台異常」並隱藏其他 HTML 元素的規則
   */
  def showEmptyBox() = {
     S.error("查無機台異常")
     ".dataBlock" #> NodeSeq.Empty
  }

  /**
   *  用來顯示有 alert（從 RaspberryPi 傳來良品數或事件數為 -1） 記錄的日期的列表
   */
  def dateList = {
    val dateList = Alert.useColl(collection => collection.distinct("date")).toList

    ".alertDate" #> dateList.map { date =>
      "a [href]" #> s"/alert/alert/$date" &
      "a *"      #> date.toString
    }
  }

  /**
   *  用來顯示特定日期的 alert 紀錄列表
   */
  def render = {

    val Array(_, _, date) = S.uri.drop(1).split("/")
    val alertList = Alert.findAll("date", date).toList.sortWith(_.timestamp.get < _.timestamp.get)

    alertList.isEmpty match {
      case true => showEmptyBox()
      case false =>
        ".row" #> alertList.map { item =>

          val errorDesc = MachineInfo.getErrorDesc(item.mach_id.get, item.eventID.get)

          ".timestamp *" #> item.timestamp &
          ".machineID *" #> item.mach_id &
          ".defactID *" #> errorDesc
        }
    }
  }
}

