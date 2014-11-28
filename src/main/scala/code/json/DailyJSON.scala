package code.json

import code.lib._
import code.model._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable.HashMap

import com.mongodb.casbah.Imports._

object DailyJSON extends JsonReport {

  def apply(year: Int, month: Int): JValue = {

    val startDate = f"$year-$month"
    val endDate = f"$year-${month+1}"

    val data = MongoDB.zhenhaiDB("daily").find("timestamp" $gte startDate $lt endDate)
    val dataByDate = data.toList.groupBy(getDate).mapValues(getSumQty)

    val sortedData = dataByDate.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (date, value) =>
      ("name" -> s"$date 日") ~
      ("value" -> value) ~
      ("link" -> s"/daily/$year/$month/$date")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(year: Int, month: Int, date: Int): JValue = {

    val startDate = f"$year-$month%02d-${date}%02d"
    val endDate = f"$year-$month%02d-${date+1}%02d"

    val data = MongoDB.zhenhaiDB(s"daily").find("timestamp" $gte startDate $lt endDate)
    val dataByMachine = data.toList.groupBy(getMachineID).mapValues(getSumQty)

    val sortedData = dataByMachine.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (machineID, value) =>
      ("name" -> s"$machineID") ~
      ("value" -> value) ~
      ("link" -> s"/daily/$year/$month/$date/$machineID")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(year: Int, month: Int, date: Int, machineID: String): JValue = {

    val cacheTableName = f"$year-$month%02d-$date%02d"
    val data = 
      MongoDB.zhenhaiDB(cacheTableName).
              find(MongoDBObject("mach_id" -> machineID)).
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

