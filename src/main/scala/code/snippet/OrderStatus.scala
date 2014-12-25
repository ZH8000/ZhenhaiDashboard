package code.snippet

import code.model._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import java.text.SimpleDateFormat
import scala.xml.NodeSeq

class OrderStatus {

  val orderStatus = OrderStatus.findAll.sortWith(_.customer.get < _.customer.get)

  def showEmptyBox() = {
    S.error("目前無客戶訂單資料")
    ".dataBlock" #> NodeSeq.Empty
  }

  def render = {

    orderStatus.isEmpty match {
      case true  => showEmptyBox()
      case false =>
        ".row" #> orderStatus.map { record =>

          val requireCount = (record.inputCount.get - (record.inputCount.get * 0.04)).toLong

          val step1Percent = scala.math.min((((record.step1.get.toDouble / record.inputCount.get.toDouble) * 100)).toLong, 100)
          val step2Percent = scala.math.min(((record.step2.get.toDouble / requireCount) * 100).toLong, 100)
          val step3Percent = scala.math.min(((record.step3.get.toDouble / requireCount) * 100).toLong, 100)
          val step4Percent = scala.math.min(((record.step4.get.toDouble / requireCount) * 100).toLong, 100)
          val step5Percent = scala.math.min(((record.step5.get.toDouble / requireCount) * 100).toLong, 100)

          ".customer *" #> record.customer &
          ".lotNo *" #> record.lotNo &
          ".product *" #> record.product &
          ".inputCount *" #> record.inputCount &
          ".requireCount *" #> requireCount.toLong &
          ".step1" #> (
            ".inputCount *" #> record.inputCount &
            ".currentCount *" #> record.step1 &
            ".percent [data-percent]" #> step1Percent
          ) &
          ".step2" #> (
            ".requireCount *" #> requireCount &
            ".currentCount *" #> record.step2 &
            ".percent [data-percent]" #> step2Percent
          ) &
          ".step3" #> (
            ".requireCount *" #> requireCount &
            ".currentCount *" #> record.step3 &
            ".percent [data-percent]" #> step3Percent
          ) &
          ".step4" #> (
            ".requireCount *" #> requireCount &
            ".currentCount *" #> record.step4 &
            ".percent [data-percent]" #> step4Percent
          ) &
          ".step5" #> (
            ".requireCount *" #> requireCount &
            ".currentCount *" #> record.step5 &
            ".percent [data-percent]" #> step5Percent
          )

        }
    }
  }
}
