package code.snippet

import code.lib._
import code.model._
import net.liftweb.common._
import net.liftweb.http.{S, SHtml}
import net.liftweb.util.Helpers._

import scala.xml.NodeSeq

/**
 *  用來處理網頁上「生產管理卡」的 Snippet
 *  
 */
class ProductionCard {

  private var searchBox: String = _     // 用來記錄網頁上工單號搜尋框的內容

  /**
   *  用來顯示「查無此製令編號」的錯誤訊息，並隱藏 HTML 中 class="dataBlock" 元素下的所有子節點
   */
  def showEmptyBox() = {
    S.error("查無此製令編號")
    ".dataBlock" #> NodeSeq.Empty
  }

  /**
   *  當使用者輸入工單號後用來轉址的函式
   */
  def process() {
    val searchBox = urlEncode(S.param("lotNo").getOrElse(""))
    S.redirectTo(s"/productionCard/$searchBox")
  }

  /**
   *  設定搜尋框的「查詢」按鈕按下去後要執行的動作
   */
  def search = {
    "type=submit" #> SHtml.onSubmitUnit(process)
  }

  /**
   *  設定查詢結果頁面中的麵包屑上的工單號
   */
  def lotNo = {
    val Array(_, lotNoEncoded) = S.uri.split("/").drop(1)
    val lotNo = urlDecode(lotNoEncoded)
    ".lotNo *" #> lotNo
  }

  /**
   *  用來顯示生產管理卡的表格
   */
  def renderTable(orderStatus: code.model.OrderStatus) = {

    val requireCount = (orderStatus.inputCount.get / 1.04).toLong
    val customer = Customer.fromPartNo(orderStatus.partNo.get)

    "#exportCSV [href]" #> s"/api/csv/productionCard/${orderStatus.lotNo}.csv" &
    ".lotNo *" #> orderStatus.lotNo &
    ".partNo *" #> orderStatus.partNo &
    ".product *" #> orderStatus.product &
    ".inputCount *" #> orderStatus.inputCount &
    ".requireCount *" #> requireCount &
    ".customer *" #> customer &
    ".step1Date *" #> orderStatus.step1StartTimeString &
    ".step1Count *" #> orderStatus.step1 &
    ".step1Machine *" #> orderStatus.step1machineID &
    ".step1Worker *" #> orderStatus.step1WorkerName &
    ".step1DoneDate *" #> orderStatus.step1DoneTimeString &
    ".step2Date *" #> orderStatus.step2StartTimeString &
    ".step2Count *" #> orderStatus.step2 &
    ".step2Machine *" #> orderStatus.step2machineID &
    ".step2Worker *" #> orderStatus.step2WorkerName &
    ".step2DoneDate *" #> orderStatus.step2DoneTimeString &
    ".step3Date *" #> orderStatus.step3StartTimeString &
    ".step3Count *" #> orderStatus.step3 &
    ".step3Machine *" #> orderStatus.step3machineID &
    ".step3Worker *" #> orderStatus.step3WorkerName &
    ".step3DoneDate *" #> orderStatus.step3DoneTimeString &
    ".step4Date *" #> orderStatus.step4StartTimeString &
    ".step4Count *" #> orderStatus.step4 &
    ".step4Machine *" #> orderStatus.step4machineID &
    ".step4Worker *" #> orderStatus.step4WorkerName &
    ".step4DoneDate *" #> orderStatus.step4DoneTimeString &
    ".step5Date *" #> orderStatus.step5StartTimeString &
    ".step5Count *" #> orderStatus.step5 &
    ".step5Machine *" #> orderStatus.step5machineID &
    ".step5Worker *" #> orderStatus.step5WorkerName &
    ".step5DoneDate *" #> orderStatus.step5DoneTimeString
  }

  /**
   *  用來從網址取出工單號並顯示生產管理卡表格
   */
  def render = {

    val Array(_, lotNoEncoded) = S.uri.split("/").drop(1)
    val lotNo = urlDecode(lotNoEncoded)
    val orderStatusBox = OrderStatus.find("lotNo", lotNo)

    orderStatusBox match {
      case Full(orderStatus) => renderTable(orderStatus)
      case _ => showEmptyBox()
    }
  }

}
