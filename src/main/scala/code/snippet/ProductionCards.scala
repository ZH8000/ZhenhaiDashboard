package code.snippet

import code.model._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import java.text.SimpleDateFormat
import scala.xml.NodeSeq

class ProductionCard {

  val orderStatus = OrderStatus.findAll.sortWith(_.lotNo.get < _.lotNo.get)

  def showEmptyBox() = {
    S.error("目前無生產管理卡資料")
    ".dataBlock" #> NodeSeq.Empty
  }

  def render = {

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
