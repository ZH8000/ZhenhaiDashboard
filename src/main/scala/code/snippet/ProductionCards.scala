package code.snippet

import code.model._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import java.text.SimpleDateFormat
import scala.xml.NodeSeq
import net.liftweb.http.SHtml

class ProductionCard {

  private var searchBox: String = _

  def showEmptyBox() = {
    S.error("目前無生產管理卡資料")
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

  def process(value: String) {
    println(searchBox)
    S.redirectTo(s"/productionCard/$searchBox")
  }

  def search = {
    "#lotNo" #> SHtml.onSubmit(searchBox = _) &
    "type=submit" #> SHtml.onSubmit(process _)
  }

  def render = {

    val Array(_, date) = S.uri.drop(1).split("/")
    val orderStatus = OrderStatus.findAll("shiftDate", date).sortWith(_.lotNo.get < _.lotNo.get)

    orderStatus.isEmpty match {
      case true  => showEmptyBox()
      case false =>
        ".row" #> orderStatus.map { record =>

          val requireCount = (record.inputCount.get / 1.04).toLong

          ".partNo *" #> record.partNo &
          ".lotNo *" #> record.lotNo &
          ".product *" #> record.product &
          ".inputCount *" #> record.inputCount &
          ".requireCount *" #> requireCount.toLong &
          ".export [href]" #> s"/api/csv/productionCard/${record.lotNo}"
        }
    }
  }
}
