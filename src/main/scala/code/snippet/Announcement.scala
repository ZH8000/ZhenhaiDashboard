package code.snippet

import code.model._
import code.lib._

import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.http.js.JE._
import code.model.{Announcement => MAnnouncement}

/**
 *  用來顯示與更新跑馬燈公告的 Snippet
 */
class Announcement {

  /**
   *  目前系統內的跑馬燈內容
   */
  private var announcement: String = MAnnouncement.findAll.headOption.flatMap(_.content.get).getOrElse("")

  /**
   *  當使用者按下表單上的「更新」按鈕時要執行的動作
   */
  def process() = {

    MAnnouncement.drop

    if (announcement.trim.isEmpty) {
      S.notice("已刪除公告")
    } else {
      val newRecord = MAnnouncement.createRecord.content(announcement.trim).saveTheRecord 

      if (newRecord.isDefined) {
        S.notice("已更新公告")
      } else {
        S.error("無無儲存公告至資料庫，請稍候再試")
      }
    }
  }

  /**
   *  用來綁定「網頁設定」－＞「跑馬燈公告」表單上的預設值，以及相對應的表單動作。
   */
  def updateAnnouncementForm = {
    "@announcement" #> SHtml.textarea(announcement, announcement = _) &
    "type=submit" #> SHtml.submit("更新", process _)
  }

  /**
   *  用來顯示在網頁上方的跑馬燈公告
   *
   *  此函式會將資料表內的跑馬燈內容用換行符號做分隔，
   *  每一行在 HTML 中都會以一個 div  元素呈現，以便配
   *  合前端的 cycle2 JavaScript  函式庫來實現跑馬燈的
   *  效果。
   *
   */
  def render = {
    
    val announcementList: List[String] = for {
      announcement <- Announcement.findAll.headOption.toList
      announcementContent <- announcement.content.get.toList
      announcementLine <- announcementContent.split("\n").toList
    } yield announcementLine

    ".text" #> announcementList.map(x => ".text *" #> x)

  }

}

