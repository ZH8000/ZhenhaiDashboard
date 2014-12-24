package code.json

import code.model._
import code.lib._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.util.Helpers._

import scala.collection.mutable.HashMap

import com.mongodb.casbah.Imports._

object TotalJSON extends JsonReport {

  def overview: JValue = {

    val groupedData = MongoDB.zhenhaiDB("product").groupBy(x => x.get("machineTypeTitle")).mapValues(getSumQty)
    val orderedKey = List("加締卷取", "組立", "老化", "選別", "加工切角")

    val dataSet = orderedKey.map { case key => 
      val countQty = groupedData.getOrElse(key, 0).toString
      ("name" -> key) ~ ("value" -> countQty) ~ ("link" -> s"/total/$key")
    }

    ("dataSet" -> dataSet)
  }

  def apply(step: String): JValue = {

    val data = MongoDB.zhenhaiDB(s"product").find(MongoDBObject("machineTypeTitle" -> step)).toList
    val dataByProduct = data.groupBy(record => record.get("product").toString).mapValues(getSumQty)
    val sortedData = dataByProduct.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (product, value) =>
      ("name" -> product) ~
      ("value" -> value) ~
      ("link" -> s"/total/$step/$product")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(step: String, product: String): JValue = {

    val data = MongoDB.zhenhaiDB(s"product-$product").find(MongoDBObject("machineTypeTitle" -> step)).toList
    val dataByProduct = data.groupBy(getYearMonth).mapValues(getSumQty)
    val sortedData = dataByProduct.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (yearAndMonth, value) =>
      val Array(year, month) = yearAndMonth.split("-").map(_.toInt)
      ("name" -> yearAndMonth) ~
      ("value" -> value) ~
      ("link" -> s"/total/$step/$product/$year/$month")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(step: String, productName: String, year: Int, month: Int): JValue = {

    val startDate = f"$year-$month%02d"
    val endDate = f"$year-${month+1}%02d"

    val data = MongoDB.zhenhaiDB(s"product-$productName").find("timestamp" $gte startDate $lt endDate).filter(_("machineTypeTitle") == step)

    val dataByWeek = data.toList.groupBy(getWeek).mapValues(getSumQty)
    val sortedData = dataByWeek.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (week, value) =>
      ("name" -> s"第 $week 週") ~
      ("value" -> value) ~
      ("link" -> s"/total/$step/$productName/$year/$month/$week")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(step: String, productName: String, year: Int, month: Int, week: Int): JValue = {

    val startDate = f"$year-$month%02d-01"
    val endDate = f"$year-${month+1}%02d-01"

    val data = MongoDB.zhenhaiDB(s"product-$productName").find("timestamp" $gte startDate $lt endDate).filter(_("machineTypeTitle") == step)
    val dataInWeek = data.filter { entry => 
      val Array(year, month, date) = entry("timestamp").toString.split("-").map(_.toInt)
      DateUtils.getWeek(year, month, date) == week
    }

    val dataByDate = dataInWeek.toList.groupBy(getDate).mapValues(getSumQty)
    val sortedData = dataByDate.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (date, value) =>
      ("name" -> s"$date 日") ~
      ("value" -> value) ~
      ("link" -> s"/total/$step/$productName/$year/$month/$week/$date")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(step: String, productName: String, year: Int, month: Int, week: Int, date: Int): JValue = {

    val startDate = f"$year-$month%02d-${date}%02d"
    val endDate = f"$year-$month%02d-${date+1}%02d"

    val data = MongoDB.zhenhaiDB(s"product-$productName").find("timestamp" $gte startDate $lt endDate).filter(_("machineTypeTitle") == step)
    val dataByMachine = data.toList.groupBy(getMachineID).mapValues(getSumQty)
    val sortedData = dataByMachine.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (machineID, value) =>
      ("name" -> s"$machineID") ~
      ("value" -> value) ~
      ("link" -> s"/total/$step/$productName/$year/$month/$week/$date/$machineID")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(productName: String, year: Int, month: Int, week: Int, date: Int, machineID: String): JValue = {

    val cacheTableName = f"$year-$month%02d-$date%02d"
    val data = 
      MongoDB.zhenhaiDB(cacheTableName).
              find(MongoDBObject("product" -> productName, "mach_id" -> machineID)).
              sort(MongoDBObject("timestamp" -> 1))

    val jsonData = data.map { entry => 
      ("timestamp" -> entry("timestamp").toString) ~
      ("defact_id" -> entry("defact_id").toString) ~
      ("count_qty" -> entry("count_qty").toString.toLong) ~
      ("bad_qty" -> entry("bad_qty").toString.toLong)
    }

    ("dataSet" -> jsonData.toList.sortBy(x => Record(x)))
  }

}

