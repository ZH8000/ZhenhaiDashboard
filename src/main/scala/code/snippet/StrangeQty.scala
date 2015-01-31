package code.snippet

import code.model._

import net.liftweb.http.S
import net.liftweb.http.SHtml

import net.liftweb.util.Helpers._
import net.liftweb.util._

import java.text.SimpleDateFormat
import java.util.Date
import scala.xml.NodeSeq
import code.lib._
 
class StrangeQty {

  val strangeQtyList = StrangeQty.findAll.toList.sortWith(_.emb_date.get > _.emb_date.get)
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def showEmptyBox() = {
     S.error("查無機台異常")
     ".dataBlock" #> NodeSeq.Empty
  }


  def render = {
    
    strangeQtyList.isEmpty match {
      case false => showEmptyBox()
      case true =>
        ".row" #> strangeQtyList.map { item =>

          val errorDesc = if (item.count_qty.get > 0) {
            ""
          } else {
            MachineInfo.getErrorDesc(item.mach_id.get, item.defact_id.get)
          }

          ".timestamp *" #> dateFormatter.format(new Date(item.emb_date.get * 1000)) &
          ".machineID *" #> item.mach_id &
          ".countQty *"  #> item.count_qty &
          ".badQty *" #> item.event_qty &
          ".defactID *" #> errorDesc
        }
    }
  }
}

