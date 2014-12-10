package code.json

import code.lib._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.math.Ordering

import com.mongodb.casbah.Imports._


trait JsonReport {

  case class Record(json: JValue) {
    val JString(timestamp) = (json \\ "timestamp")
    val JInt(countQty) = (json \\ "count_qty")
    val JString(defactID) = (json \\ "defact_id")
  }
  
  implicit object TableSorting extends Ordering[Record]{
    def compare(a: Record, b: Record): Int = {
  
      if (a.timestamp != b.timestamp) {
        a.timestamp compare b.timestamp
      } else {
        if (a.countQty == b.countQty) {
          a.defactID compare a.defactID
        } else {
          b.countQty compare b.countQty
        }
      }
    }
  }

  def getSumQty(dataList: List[DBObject]) = dataList.map(data => data("count_qty").toString.toLong).sum
  def getDate(entry: DBObject) = entry("timestamp").toString.split("-")(2).toLong
  def getMachineID(entry: DBObject) = entry("mach_id").toString
  def getYearMonth(entry: DBObject) = entry("timestamp").toString.substring(0, 7)
  def getWeek(entry: DBObject) = {
    val Array(year, month, date) = entry("timestamp").toString.split("-")
    DateUtils.getWeek(year.toInt, month.toInt, date.toInt)
  }
}


