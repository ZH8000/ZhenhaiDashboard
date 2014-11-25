package code.json

import code.util._
import code.model._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable.HashMap

import com.mongodb.casbah.Imports._

object TableSorting {

  def countQtyDefactID(entryA: JValue, entryB: JValue): Boolean = {

    val JString(timestampA) = (entryA \\ "timestamp")
    val JString(timestampB) = (entryB \\ "timestamp")
    
    if (timestampA > timestampB) { false } 
    else if (timestampA < timestampB) { true } 
    else {

      val JInt(countQtyA) = (entryA \\ "count_qty")
      val JInt(countQtyB) = (entryB \\ "count_qty")

      if (countQtyA > countQtyB) { true } 
      else if (countQtyA > countQtyB) { false } 
      else {
        val JString(defactIDA) = (entryA \\ "defact_id")
        val JString(defactIDB) = (entryB \\ "defact_id")
        defactIDA < defactIDB
      }
    }
  }

}

object ProductJSON {

  def getSumQty(dataList: List[DBObject]) = dataList.map(data => data("count_qty").toString.toInt).sum
  def getYearMonth(entry: DBObject) = entry("timestamp").toString.substring(0, 7)
  def getMachineID(entry: DBObject) = entry("mach_id").toString
  def getDate(entry: DBObject) = entry("timestamp").toString.split("-")(2).toInt
  def getWeek(entry: DBObject) = {
    val Array(year, month, date) = entry("timestamp").toString.split("-")
    DateUtils.getWeek(year.toInt, month.toInt, date.toInt)
  }

  def overview: JValue = {

    val sortedProducts = MongoDB.zhenhaiDB("product").toList.sortBy(_("product").toString)
    val dataSet = sortedProducts.map { case item => 
      val productName = item("product").toString
      val countQty = item("count_qty").toString.toInt
      ("name" -> productName) ~ ("value" -> countQty) ~ ("link" -> s"/total/$productName")
    }

    ("dataSet" -> dataSet)
  }

  def apply(productName: String): JValue = {

    val data = MongoDB.zhenhaiDB(s"product-$productName")
    val dataByMonth = data.toList.groupBy(getYearMonth).mapValues(getSumQty)

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

  def apply(productName: String, year: Int, month: Int): JValue = {

    val startDate = f"$year-$month%02d"
    val endDate = f"$year-${month+1}%02d"

    val data = MongoDB.zhenhaiDB(s"product-$productName").find("timestamp" $gte startDate $lt endDate)
    val dataByWeek = data.toList.groupBy(getWeek).mapValues(getSumQty)
    val sortedData = dataByWeek.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (week, value) =>
      ("name" -> s"第 $week 週") ~
      ("value" -> value) ~
      ("link" -> s"/total/$productName/$year/$month/$week")
    }

    ("steps" -> List(productName, f"$year-$month%02d")) ~
    ("dataSet" -> sortedJSON)
  }

  def apply(productName: String, year: Int, month: Int, week: Int): JValue = {

    val startDate = f"$year-$month%02d-01"
    val endDate = f"$year-${month+1}%02d-01"

    val data = MongoDB.zhenhaiDB(s"product-$productName").find("timestamp" $gte startDate $lt endDate)
    val dataInWeek = data.filter { entry => 
      val Array(year, month, date) = entry("timestamp").toString.split("-").map(_.toInt)
      DateUtils.getWeek(year, month, date) == week
    }

    val dataByDate = dataInWeek.toList.groupBy(getDate).mapValues(getSumQty)
    val sortedData = dataByDate.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (date, value) =>
      ("name" -> s"$date 日") ~
      ("value" -> value) ~
      ("link" -> s"/total/$productName/$year/$month/$week/$date")
    }

    ("steps" -> List(productName, f"$year-$month%02d", f"第 $week 週")) ~
    ("dataSet" -> sortedJSON)
  }

  def apply(productName: String, year: Int, month: Int, week: Int, date: Int): JValue = {

    val startDate = f"$year-$month%02d-${date}%02d"
    val endDate = f"$year-$month%02d-${date+1}%02d"

    val data = MongoDB.zhenhaiDB(s"product-$productName").find("timestamp" $gte startDate $lt endDate)
    val dataByMachine = data.toList.groupBy(getMachineID).mapValues(getSumQty)
    val sortedData = dataByMachine.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (machineID, value) =>
      ("name" -> s"$machineID") ~
      ("value" -> value) ~
      ("link" -> s"/total/$productName/$year/$month/$week/$date/$machineID")
    }

    ("steps" -> List(productName, f"$year-$month%02d", f"第 $week 週", f"$date 日")) ~
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

    ("steps" -> List(productName, f"$year-$month%02d", f"第 $week 週", f"$date 日", machineID)) ~
    ("dataSet" -> jsonData.toList.sortWith(TableSorting.countQtyDefactID))
  }

}

