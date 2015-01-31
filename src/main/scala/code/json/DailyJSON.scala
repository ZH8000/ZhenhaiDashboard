package code.json

import code.lib._
import code.model._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable.HashMap

import com.mongodb.casbah.Imports._

object DailyJSON extends JsonReport {

  def apply(year: Int, month: Int): JValue = {

    val startDate = f"$year-$month%02d"
    val endDate = f"$year-${month+1}%02d"

    val data = MongoDB.zhenhaiDB("daily").find("shiftDate" $gte startDate $lt endDate)
    val dataByStep = data.toList.groupBy(getMachineTypeTitle).mapValues(getSumQty)

    val orderedKey = List("加締卷取", "組立", "老化", "選別", "加工切角")

    val dataSet = orderedKey.map { case step => 
      val countQty = dataByStep.getOrElse(step, 0L)
      ("name"  -> step) ~ 
      ("value" -> countQty) ~ 
      ("link"  -> s"/daily/$year/$month/$step")
    }

    ("dataSet" -> dataSet)
  }

  def apply(year: Int, month: Int, step: String): JValue = {
    val startDate = f"$year-$month%02d"
    val endDate = f"$year-${month+1}%02d"

    val data = MongoDB.zhenhaiDB("daily").find("shiftDate" $gte startDate $lt endDate).filter(x => getMachineTypeTitle(x) == step)
    val dataByDate = data.toList.groupBy(getDate).mapValues(getSumQty)
    val sortedData = dataByDate.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (date, value) =>
      ("name" -> s"$date 日") ~
      ("value" -> value) ~
      ("link" -> s"/daily/$year/$month/$step/$date")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(year: Int, month: Int, step: String, date: Int): JValue = {

    val startDate = f"$year-$month%02d-${date}%02d"
    val endDate = f"$year-$month%02d-${date+1}%02d"

    val data = MongoDB.zhenhaiDB(s"daily").find("shiftDate" $gte startDate $lt endDate).filter(x => getMachineTypeTitle(x) == step)
    val dataByMachine = data.toList.groupBy(getMachineID).mapValues(getSumQty)

    val sortedData = dataByMachine.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (machineID, value) =>
      ("name" -> s"$machineID") ~
      ("value" -> value) ~
      ("link" -> s"/daily/$year/$month/$step/$date/$machineID")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(year: Int, month: Int, step: String, date: Int, machineID: String): JValue = {

    val cacheTableName = f"shift-$year-$month%02d-$date%02d"
    val data = 
      MongoDB.zhenhaiDB(cacheTableName).
              find(MongoDBObject("mach_id" -> machineID)).
              sort(MongoDBObject("timestamp" -> 1))

    val jsonData = data.map { entry => 
      ("timestamp" -> entry("timestamp").toString) ~
      ("defact_id" -> MachineInfo.getErrorDesc(machineID, entry("defact_id").toString.toInt)) ~
      ("count_qty" -> entry("count_qty").toString.toLong) ~
      ("event_qty" -> entry("event_qty").toString.toLong)
    }

    ("dataSet" -> jsonData.toList.sortBy(x => Record(x)))
  }

}

