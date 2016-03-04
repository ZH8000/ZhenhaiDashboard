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

    val requireCount = (orderStatus.inputCount.get / 1.03).toLong
    val customer = Customer.fromPartNo(orderStatus.partNo.get)
    val step1LossCount = DefactByLotAndPart.getCount(orderStatus.lotNo.get, orderStatus.step1machineID.get).getOrElse(0L)
    val step2LossCount = DefactByLotAndPart.getCount(orderStatus.lotNo.get, orderStatus.step2machineID.get).getOrElse(0L)
    val step3LossCount = DefactByLotAndPart.getCount(orderStatus.lotNo.get, orderStatus.step3machineID.get).getOrElse(0L)
    val step4LossCount = DefactByLotAndPart.getCount(orderStatus.lotNo.get, orderStatus.step4machineID.get).getOrElse(0L)
    val step5LossCount = DefactByLotAndPart.getCount(orderStatus.lotNo.get, orderStatus.step5machineID.get).getOrElse(0L)
    val step1LossRate = if (orderStatus.step1.get > 0) { f"${(step1LossCount.toDouble / orderStatus.step1.get) * 100}%.02f %%" } else {"-"}
    val step2LossRate = if (orderStatus.step2.get > 0) { f"${(step2LossCount.toDouble / orderStatus.step2.get) * 100}%.02f %%" } else {"-"}
    val step3LossRate = if (orderStatus.step3.get > 0) { f"${(step3LossCount.toDouble / orderStatus.step3.get) * 100}%.02f %%" } else {"-"}
    val step4LossRate = if (orderStatus.step4.get > 0) { f"${(step4LossCount.toDouble / orderStatus.step4.get) * 100}%.02f %%" } else {"-"}
    val step5LossRate = if (orderStatus.step5.get > 0) { f"${(step5LossCount.toDouble / orderStatus.step5.get) * 100}%.02f %%" } else {"-"}
    val totalLossCount = if (orderStatus.step5.get > 0) { (orderStatus.inputCount.get - orderStatus.step5.get).toString } else { "-" }
    val totalLossRate = if (orderStatus.step5.get > 0) { 
      val lossCount = (orderStatus.inputCount.get - orderStatus.step5.get).toDouble 
      val lossRate = lossCount / orderStatus.inputCount.get
      f"${lossRate}%.02f %%"
    } else { 
      "-" 
    }
    val unknownReasonLoss = if (orderStatus.step5.get > 0) {
      (orderStatus.inputCount.get - orderStatus.step5.get) - 
      step1LossCount - step2LossCount - step3LossCount - step4LossCount - step5LossCount
    } else {
      "-"
    }

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
    ".step5DoneDate *" #> orderStatus.step5DoneTimeString &
    ".step1LossCount *" #> step1LossCount &
    ".step2LossCount *" #> step2LossCount &
    ".step3LossCount *" #> step3LossCount &
    ".step4LossCount *" #> step4LossCount &
    ".step5LossCount *" #> step5LossCount &
    ".step1LossRate *" #> step1LossRate &
    ".step2LossRate *" #> step2LossRate &
    ".step3LossRate *" #> step3LossRate &
    ".step4LossRate *" #> step4LossRate &
    ".step5LossRate *" #> step5LossRate &
    ".totalLossCount *" #> totalLossCount &
    ".totalLossRate *" #> totalLossRate &
    ".unknownReasonLoss *" #> unknownReasonLoss.toString

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
