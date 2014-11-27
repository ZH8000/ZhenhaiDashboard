package code.json

import code.util._
import code.model._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable.HashMap

import com.mongodb.casbah.Imports._

object MonthlyJSON extends JsonReport {

  def apply(year: Int): JValue = {

    val startDate = f"$year-"
    val endDate = f"${year+1}-"

    val data = MongoDB.zhenhaiDB("daily").find("timestamp" $gte startDate $lt endDate)
    val dataByMonth = data.toList.groupBy(getYearMonth).mapValues(getSumQty)

    val sortedData = dataByMonth.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (yearMonth, value) =>
      val Array(year, month) = yearMonth.split("-")
      ("name" -> s"$month 月") ~
      ("value" -> value) ~
      ("link" -> s"/monthly/$year/$month")
    }

    ("dataSet" -> sortedJSON)
  }

  def apply(year: Int, month: Int): JValue = {

    val startDate = f"$year-$month%02d"
    val endDate = f"$year-${month+1}%02d"

    val data = MongoDB.zhenhaiDB("daily").find("timestamp" $gte startDate $lt endDate)
    val dataByWeek = data.toList.groupBy(getWeek).mapValues(getSumQty)
    val sortedData = dataByWeek.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (week, value) =>
      ("name" -> s"第 $week 週") ~
      ("value" -> value) ~
      ("link" -> s"/monthly/$year/$month/$week")
    }

    ("steps" -> List(f"$month%02d 月")) ~
    ("dataSet" -> sortedJSON)
  }

  def apply(year: Int, month: Int, week: Int): JValue = {

    val startDate = f"$year-$month%02d-01"
    val endDate = f"$year-${month+1}%02d-01"

    val data = MongoDB.zhenhaiDB(s"daily").find("timestamp" $gte startDate $lt endDate)
    val dataInWeek = data.filter { entry => 
      val Array(year, month, date) = entry("timestamp").toString.split("-").map(_.toInt)
      DateUtils.getWeek(year, month, date) == week
    }

    val dataByDate = dataInWeek.toList.groupBy(getDate).mapValues(getSumQty)
    val sortedData = dataByDate.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (date, value) =>
      ("name" -> s"$date 日") ~
      ("value" -> value) ~
      ("link" -> s"/monthly/$year/$month/$week/$date")
    }

    ("steps" -> List(f"$month%02d 月", f"第 $week 週")) ~
    ("dataSet" -> sortedJSON)
  }

  def apply(year: Int, month: Int, week: Int, date: Int): JValue = {

    val startDate = f"$year-$month%02d-${date}%02d"
    val endDate = f"$year-$month%02d-${date+1}%02d"

    val data = MongoDB.zhenhaiDB(s"daily").find("timestamp" $gte startDate $lt endDate)
    val dataByMachine = data.toList.groupBy(getMachineID).mapValues(getSumQty)

    val sortedData = dataByMachine.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (machineID, value) =>
      ("name" -> s"$machineID") ~
      ("value" -> value) ~
      ("link" -> s"/monthly/$year/$month/$week/$date/$machineID")
    }

    ("steps" -> List(f"$month%02d 月", f"第 $week 週", f"$date 日")) ~
    ("dataSet" -> sortedJSON)
  }

  def apply(year: Int, month: Int, week: Int, date: Int, machineID: String): JValue = {

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

    ("steps" -> List(f"$year-$month%02d", f"第 $week 週", f"$date 日", machineID)) ~
    ("dataSet" -> jsonData.toList.sortBy(x => Record(x)))
  }

}


