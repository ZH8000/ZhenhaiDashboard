package code.snippet

import code.model._
import code.lib._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import java.text.SimpleDateFormat
import scala.xml.NodeSeq
import scala.collection.JavaConversions._
import net.liftweb.json.JsonDSL._

class OrderStatus {

  def getCustomerName(customerID: String) = Customer.customers.get(customerID).getOrElse("Unknown")
  val orderStatus = OrderStatus.findAll.sortWith(_.lotNo.get < _.lotNo.get)

  def showEmptyBox() = {
    S.error("目前無客戶訂單資料")
    ".dataBlock" #> NodeSeq.Empty
  }

  def customerList = {

    val sortedCustomers = 
      Customer.customers.keys.toList
              .sortWith((customerID1, customerID2) => getCustomerName(customerID1) < getCustomerName(customerID2))

    val existsCustomers = OrderStatus.useColl(collection => collection.distinct("customer")).toList

    ".cardCustomer" #> sortedCustomers.filter(existsCustomers.contains _).map { customerID =>
      val customerName = getCustomerName(customerID)
      "a [href]" #> s"/orderStatus/$customerID" &
      "a *"      #> customerName
    }
  }

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

  def render = {

    val Array(_, customer, date) = S.uri.drop(1).split("/")
    val query = ("customer" -> customer) ~ ("shiftDate" -> date)
    val orderStatus = OrderStatus.findAll(query).sortWith(_.lotNo.get < _.lotNo.get)

    orderStatus.isEmpty match {
      case true  => showEmptyBox()
      case false =>

        val customerName = getCustomerName(customer)

        "#csvURL [href]" #> s"/api/csv/orderStatus/$date" &
        ".stepCustomer *" #> customerName &
        ".stepCustomer [href]" #> s"/orderStatus/$customer" &
        ".stepMonth *" #> date &
        ".stepMonth [href]" #> s"/orderStatus/$customer/$date" &
        ".row" #> orderStatus.map { record =>

          val requireCount = (record.inputCount.get / 1.04).toLong

          val step1Percent = scala.math.min((((record.step1.get.toDouble / record.inputCount.get.toDouble) * 100)).toLong, 100)
          val step2Percent = scala.math.min(((record.step2.get.toDouble / requireCount) * 100).toLong, 100)
          val step3Percent = scala.math.min(((record.step3.get.toDouble / requireCount) * 100).toLong, 100)
          val step4Percent = scala.math.min(((record.step4.get.toDouble / requireCount) * 100).toLong, 100)
          val step5Percent = scala.math.min(((record.step5.get.toDouble / requireCount) * 100).toLong, 100)

          ".lotNo *" #> record.lotNo &
          ".product *" #> record.product &
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
