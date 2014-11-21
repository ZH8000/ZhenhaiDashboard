package code.json

import code.util._
import code.db._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable.HashMap

import com.mongodb.casbah.Imports._

object ProductJSON {

  def overview: JValue = {

    val sortedProducts = MongoDB.zhenhaiDB("product").toList.sortBy(_("product").toString)
    val dataSet = sortedProducts.map { case item => 
      val productName = item("product").toString
      val countQty = item("count_qty").toString.toInt
      ("name" -> productName) ~ ("value" -> countQty) ~ ("link" -> s"/total/$productName")
    }

    ("dataSet" -> dataSet)
  }

  def product(productName: String): JValue = {

    val data = MongoDB.zhenhaiDB(s"product-$productName")
    val dataByMonth = HashMap.empty[String, Long]

    data.foreach { case entry => 
      val yearMonth = entry("timestamp").toString.substring(0, 7)
      val addValue = entry("count_qty").toString.toLong
      val newValue = dataByMonth.get(yearMonth).getOrElse(0L) + addValue
      dataByMonth(yearMonth) = newValue
    }

    val sortedData = dataByMonth.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (yearMonth, value) =>
      val Array(year, month) = yearMonth.split("-")
      ("name" -> yearMonth) ~
      ("value" -> value) ~
      ("link" -> s"/total/$productName/$year/$month")
    }

    ("steps" -> List(productName)) ~
    ("dataSet" -> sortedJSON)
  }

  def productMonth(productName: String, year: Int, month: Int): JValue = {

    val startDate = f"$year-$month%02d"
    val endDate = f"$year-${month+1}%02d"

    val data = MongoDB.zhenhaiDB(s"product-$productName").find("timestamp" $gte startDate $lt endDate)
    val dataByWeek = HashMap.empty[Int, Long]

    data.foreach { case entry => 
      val Array(year, month, date) = entry("timestamp").toString.split("-")
      val week = DateUtils.getWeek(year.toInt, month.toInt, date.toInt)
      val addValue = entry("count_qty").toString.toLong
      val newValue = dataByWeek.get(week).getOrElse(0L) + addValue
      dataByWeek(week) = newValue
    }

    val sortedData = dataByWeek.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (week, value) =>
      ("name" -> s"第 $week 週") ~
      ("value" -> value) ~
      ("link" -> s"/total/$productName/$year/$month/$week")
    }

    ("steps" -> List(productName, f"$year-$month%02d")) ~
    ("dataSet" -> sortedJSON)
  }

  def productMonthWeek(productName: String, year: Int, month: Int, week: Int): JValue = {

    val startDate = f"$year-$month%02d-01"
    val endDate = f"$year-${month+1}%02d-01"

    val data = MongoDB.zhenhaiDB(s"product-$productName").find("timestamp" $gte startDate $lt endDate)
    val dataInWeek = data.filter { entry => 
      val Array(year, month, date) = entry("timestamp").toString.split("-").map(_.toInt)
      DateUtils.getWeek(year, month, date) == week
    }

    val dataByDate = HashMap.empty[Int, Long]

    dataInWeek.foreach { case entry => 
      val Array(year, month, date) = entry("timestamp").toString.split("-").map(_.toInt)
      val addValue = entry("count_qty").toString.toLong
      val newValue = dataByDate.get(date).getOrElse(0L) + addValue
      dataByDate(date) = newValue
    }

    val sortedData = dataByDate.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (date, value) =>
      ("name" -> s"$date 日") ~
      ("value" -> value) ~
      ("link" -> s"/total/$productName/$year/$month/$week/$date")
    }

    ("steps" -> List(productName, f"$year-$month%02d", week.toString)) ~
    ("dataSet" -> sortedJSON)
  }

  def productMonthWeekDate(productName: String, year: Int, month: Int, week: Int, date: Int): JValue = {

    val startDate = f"$year-$month%02d-${date}%02d"
    val endDate = f"$year-${month+1}%02d-${date+1}%02d"

    val data = MongoDB.zhenhaiDB(s"product-$productName").find("timestamp" $gte startDate $lt endDate)
    val dataByMachine = HashMap.empty[String, Long]

    data.foreach { case entry => 
      val machineID = entry("mach_id").toString
      val addValue = entry("count_qty").toString.toLong
      val newValue = dataByMachine.get(machineID).getOrElse(0L) + addValue
      dataByMachine(machineID) = newValue
    }

    val sortedData = dataByMachine.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (machineID, value) =>
      ("name" -> s"$machineID") ~
      ("value" -> value) ~
      ("link" -> s"/total/$productName/$year/$month/$week/$date/$machineID")
    }

    ("steps" -> List(productName, f"$year-$month%02d", week.toString, date.toString)) ~
    ("dataSet" -> sortedJSON)
  }

  def machineDetail(productName: String, year: Int, month: Int, week: Int, date: Int, machineID: String): JValue = {

    val cacheTableName = f"$year-$month%02d-$date%02d"
    val data = 
      MongoDB.zhenhaiDB(cacheTableName).
              find(MongoDBObject("product" -> productName, "mach_id" -> machineID)).
              sort(MongoDBObject("timestamp" -> 1))

    val jsonData = data.map { case entry => 
      ("timestamp" -> entry("timestamp").toString) ~
      ("defact_id" -> entry("defact_id").toString) ~
      ("count_qty" -> entry("count_qty").toString.toLong) ~
      ("bad_qty" -> entry("bad_qty").toString.toLong)
    }

    ("steps" -> List(productName, f"$year-$month%02d", week.toString, date.toString, machineID)) ~
    ("dataSet" -> jsonData.toList)
  }

}

