package code.snippet

import code.lib._
import code.model._
import net.liftweb.http.S
import net.liftweb.json.JsonDSL._
import net.liftweb.util.Helpers._

import scala.collection.JavaConversions._
import scala.xml.NodeSeq

/**
 *  用來顯示網頁上「訂單狀態」的 Snippet
 *
 */
class OrderStatus {

  /**
   *  從客戶代碼取得客戶名稱
   *
   *  @param      customerID        料號裡的四碼客戶代碼
   *  @return                       客戶名稱                      
   */
  def getCustomerName(customerID: String) = Customer.customers.get(customerID).getOrElse("Unknown")

  /**
   *  訂單狀態
   */
  val orderStatus = OrderStatus.findAll.sortWith(_.lotNo.get < _.lotNo.get)

  /**
   *  顯示「目前無客戶訂單資料」的錯誤訊息並且隱藏 HTML 模板裡 class="dataBlock" 以下的子節點
   */
  def showEmptyBox() = {
    S.error("目前無客戶訂單資料")
    ".dataBlock" #> NodeSeq.Empty
  }

  /**
   *  用來顯示客戶列表
   */
  def customerList = {

    val sortedCustomers = 
      Customer.customers.keys.toList
              .sortWith((customerID1, customerID2) => getCustomerName(customerID1) < getCustomerName(customerID2))

    val existsCustomers = OrderStatus.useColl(collection => collection.distinct("customer")).toList

    ".cardCustomer" #> sortedCustomers.filter(existsCustomers.contains _).map { customerID =>
      val customerName = getCustomerName(customerID)
      "a [href]" #> s"/orderStatus/$customerID" &
      "a *"      #> s"customerName ($customerID)"
    }
  }

  /**
   *  用來顯示特定客戶下，有工單狀態的月份
   */
  def monthList = {
    
    val Array(_, customer) = S.uri.split("/").drop(1)
    val monthList = OrderStatus.findAll("customer", customer).toList.map(_.shiftDate.get).distinct.sortWith(_ < _)

    monthList.isEmpty match {
      case true  => showEmptyBox()
      case false =>
        ".stepCustomer *" #> getCustomerName(customer) &
        ".stepCustomer [href]" #> s"/orderStatus/$customer" &
        ".cardDate" #> monthList.map { date =>
          "a [href]" #> s"/orderStatus/$customer/$date" &
          "a *"      #> date.toString
        }
    }
  }

  /**
   *  用來顯示特定客戶的特定月份的工單狀態
   */
  def render = {

    val Array(_, customer, date) = S.uri.drop(1).split("/")
    val query = ("customer" -> customer) ~ ("shiftDate" -> date)
    val orderStatus = OrderStatus.findAll(query).sortWith(_.lotNo.get < _.lotNo.get)

    orderStatus.isEmpty match {
      case true  => showEmptyBox()
      case false =>

        val customerName = getCustomerName(customer)

        "#csvURL [href]" #> s"/api/csv/orderStatus/$date.csv" &
        ".stepCustomer *" #> customerName &
        ".stepCustomer [href]" #> s"/orderStatus/$customer" &
        ".stepMonth *" #> date &
        ".stepMonth [href]" #> s"/orderStatus/$customer/$date" &
        ".row" #> orderStatus.map { record =>

          val requireCount = (record.inputCount.get / 1.03).toLong

          val step1Percent = scala.math.min((((record.step1.get.toDouble / record.inputCount.get.toDouble) * 100)).toLong, 100)
          val step2Percent = scala.math.min(((record.step2.get.toDouble / requireCount) * 100).toLong, 100)
          val step3Percent = scala.math.min(((record.step3.get.toDouble / requireCount) * 100).toLong, 100)
          val step4Percent = scala.math.min(((record.step4.get.toDouble / requireCount) * 100).toLong, 100)
          val step5Percent = scala.math.min(((record.step5.get.toDouble / requireCount) * 100).toLong, 100)

          val urlEncodedLotNo = urlEncode(record.lotNo.get)
          val productionCardURL = s"/productionCard/$urlEncodedLotNo"

          ".lotNo *" #> record.lotNo &
          ".lotNo [href]" #> productionCardURL &
          ".partNo *" #> record.partNo &
          ".customer *" #> customerName &
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
