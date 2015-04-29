package code.snippet

import code.model._
import code.lib._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import java.text.SimpleDateFormat
import scala.xml.NodeSeq
import net.liftweb.http.SHtml
import net.liftweb.common._

class ProductionCard {

  private var searchBox: String = _

  def showEmptyBox() = {
    S.error("查無此製令編號")
    ".dataBlock" #> NodeSeq.Empty
  }

  def monthList = {
    val monthList = LotDate.monthList
    monthList.isEmpty match {
      case true  => showEmptyBox()
      case false =>
        ".cardDate" #> monthList.map { date =>
          "a [href]" #> s"/productionCard/$date" &
          "a *"      #> date.toString
        }
    }
  }

  def process() {
    val searchBox = S.param("lotNo").getOrElse("")
    S.redirectTo(s"/productionCard/$searchBox")
  }

  def search = {
    "type=submit" #> SHtml.onSubmitUnit(process)
  }

  def renderTable(orderStatus: code.model.OrderStatus) = {

    val requireCount = (orderStatus.inputCount.get / 1.04).toLong
    val customer = Customer.fromPartNo(orderStatus.partNo.get)

    "#exportCSV [href]" #> s"/api/csv/productionCard/${orderStatus.lotNo}" &
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

  def render = {

    val Array(_, lotNo) = S.uri.split("/").drop(1)
    val orderStatusBox = OrderStatus.find("lotNo", lotNo)

    orderStatusBox match {
      case Full(orderStatus) => renderTable(orderStatus)
      case _ => showEmptyBox()
    }
  }

}
