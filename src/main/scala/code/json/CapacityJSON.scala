package code.json

import code.model._
import code.lib._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.util.Helpers._

import scala.collection.mutable.HashMap

import com.mongodb.casbah.Imports._

object CapacityJSON extends JsonReport {

  def overview: JValue = {

    val groupedData = MongoDB.zhenhaiDB("product").groupBy(x => x.get("machineType").toString.toInt).mapValues(getSumQty)
    val orderedKey = List(1, 2, 3, 4, 5)

    val dataSet = orderedKey.map { case key => 
      val countQty = groupedData.getOrElse(key, 0L)
      val machineTypeTitle = MachineInfo.machineTypeName.get(key).getOrElse("Unknown")
      ("name" -> machineTypeTitle) ~ ("value" -> countQty) ~ ("link" -> s"/capacity/$key")
    }

    ("dataSet" -> dataSet)
  }

  def apply(step: String): JValue = {

    val data = MongoDB.zhenhaiDB(s"product").find(MongoDBObject("machineType" -> step.toInt)).toList
    val dataByCapacity = data.groupBy(record => record.get("capacityRange").toString).mapValues(getSumQty)
    val sortedData = List("5 - 8", "10 - 12.5", "16 - 18", "Unknown").filter(dataByCapacity contains _)
    val sortedJSON = sortedData.map{ capacity =>
      ("name" -> s"$capacity Φ") ~
      ("value" -> dataByCapacity.getOrElse(capacity, 0L)) ~
      ("link" -> s"/capacity/$step/${urlEncode(capacity)}")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(step: String, capacity: String): JValue = {

    val data = MongoDB.zhenhaiDB(s"daily").find(MongoDBObject("machineType" -> step.toInt, "capacityRange" -> capacity)).toList
    val dataByProduct = data.groupBy(getYearMonth).mapValues(getSumQty)
    val sortedData = dataByProduct.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (yearAndMonth, value) =>
      val Array(year, month) = yearAndMonth.split("-").map(_.toInt)
      ("name" -> yearAndMonth) ~
      ("value" -> value) ~
      ("link" -> s"/capacity/$step/${urlEncode(capacity)}/$year/$month")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(step: String, capacity: String, year: Int, month: Int): JValue = {

    val startDate = f"$year-$month%02d"
    val endDate = f"$year-${month+1}%02d"

    val data = MongoDB.zhenhaiDB(s"daily").find("shiftDate" $gte startDate $lt endDate)
                      .filter(record => record("machineType") == step.toInt && record("capacityRange") == capacity)

    val dataByWeek = data.toList.groupBy(getWeek).mapValues(getSumQty)
    val sortedData = dataByWeek.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (week, value) =>
      ("name" -> s"第 $week 週") ~
      ("value" -> value) ~
      ("link" -> s"/capacity/$step/$capacity/$year/$month/$week")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(step: String, capacity: String, year: Int, month: Int, week: Int): JValue = {

    val startDate = f"$year-$month%02d-01"
    val endDate = f"$year-${month+1}%02d-01"

    val data = MongoDB.zhenhaiDB(s"daily").find("shiftDate" $gte startDate $lt endDate)
                      .filter(record => record("machineType") == step.toInt && record("capacityRange") == capacity)

    val dataInWeek = data.filter { entry => 
      val Array(year, month, date) = entry("shiftDate").toString.split("-").map(_.toInt)
      DateUtils.getWeek(year, month, date) == week
    }

    val dataByDate = dataInWeek.toList.groupBy(getDate).mapValues(getSumQty)
    val sortedData = dataByDate.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (date, value) =>
      ("name" -> s"$date 日") ~
      ("value" -> value) ~
      ("link" -> s"/capacity/$step/$capacity/$year/$month/$week/$date")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(step: String, capacity: String, year: Int, month: Int, week: Int, date: Int): JValue = {

    val startDate = f"$year-$month%02d-${date}%02d"
    val endDate = f"$year-$month%02d-${date+1}%02d"

    val data = MongoDB.zhenhaiDB(s"daily").find("shiftDate" $gte startDate $lt endDate)
                      .filter(record => record("machineType") == step.toInt && record("capacityRange") == capacity)

    val dataByMachine = data.toList.groupBy(getMachineID).mapValues(getSumQty)
    val sortedData = dataByMachine.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (machineID, value) =>
      ("name" -> s"$machineID") ~
      ("value" -> value) ~
      ("link" -> s"/capacity/$step/$capacity/$year/$month/$week/$date/$machineID")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(step: String, capacity: String, year: Int, month: Int, week: Int, date: Int, machineID: String): JValue = {

    val cacheTableName = f"shift-$year-$month%02d-$date%02d"

    val data = 
      MongoDB.zhenhaiDB(cacheTableName).
              find(MongoDBObject("machineType" -> step.toInt, "mach_id" -> machineID, "capacityRange" -> capacity)).
              sort(MongoDBObject("timestamp" -> 1))

    val jsonData = data.map { entry => 
      ("timestamp" -> entry("timestamp").toString) ~
      ("defact_id" -> MachineInfo.getErrorDesc(machineID, entry("defact_id").toString.toInt)) ~
      ("count_qty" -> entry("count_qty").toString.toLong) ~
      ("bad_qty" -> entry("bad_qty").toString.toLong)
    }

    ("dataSet" -> jsonData.toList.sortBy(x => Record(x)))
  }

}

