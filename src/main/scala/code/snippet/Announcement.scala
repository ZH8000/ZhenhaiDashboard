package code.snippet

import code.model._
import code.lib._

import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.http.js.JE._
import code.model.{Announcement => MAnnouncement}

class Announcement {

  private var announcement: String = 
    MAnnouncement.findAll.headOption.flatMap(_.content.get).getOrElse("")

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

  def updateAnnouncementForm = {
    "@announcement" #> SHtml.textarea(announcement, announcement = _) &
    "type=submit" #> SHtml.submit("更新", process _)
  }

  def render = {
    
    val announcementList: List[String] = for {
      announcement <- Announcement.findAll.headOption.toList
      announcementContent <- announcement.content.get.toList
      announcementLine <- announcementContent.split("\n").toList
    } yield announcementLine

    ".text" #> announcementList.map(x => ".text *" #> x)

  }

}

