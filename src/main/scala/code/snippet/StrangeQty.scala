package code.snippet

import java.text.SimpleDateFormat
import java.util.Date

import code.model._
import net.liftweb.http.S
import net.liftweb.util.Helpers._

import scala.xml.NodeSeq
 
/**
 *  用來顯示 /alert/ 裡的「異常數量」的記錄的表格
 */
class StrangeQty {

  /**
   *  系統內所有的異常數量的列表
   */
  val strangeQtyList = StrangeQty.findAll.toList.sortWith(_.emb_date.get > _.emb_date.get)

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  /**
   *  顯示「查無數量異常」的錯誤訊息，並且隱藏 class="dataBlock" 的所有子節點
   */
  def showEmptyBox() = {
     S.error("查無數量異常")
     ".dataBlock" #> NodeSeq.Empty
  }

  /**
   *  顯示異常數量的表格
   */
  def render = {
    
    strangeQtyList.isEmpty match {
      case true => showEmptyBox()
      case false =>
        ".row" #> strangeQtyList.map { item =>

          ".timestamp *" #> dateFormatter.format(new Date(item.emb_date.get * 1000)) &
          ".machineID *" #> item.mach_id &
          ".countQty *"  #> item.count_qty &
          ".badQty *"    #> item.event_qty &
          ".defactID *"  #> item.originEventID
        }
    }
  }
}

