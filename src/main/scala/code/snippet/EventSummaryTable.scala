package code.snippet

import code.model._
import code.lib._
import com.mongodb.casbah.Imports._

import net.liftweb.util.Helpers._

object EventSummaryTable {
  
  def apply(year: Int, month: Int, date: Int, machineID: String) = {

    val machineLevelBox = MachineLevel.find("machineID", machineID)
    val levelA = machineLevelBox.map(_.levelA.get.toString).getOrElse("尚未設定")
    val levelB = machineLevelBox.map(_.levelB.get.toString).getOrElse("尚未設定")
    val levelC = machineLevelBox.map(_.levelC.get.toString).getOrElse("尚未設定")
    val cacheTableName = f"shift-$year-$month%02d-$date%02d"
    val data = MongoDB.zhenhaiDB(cacheTableName).find(MongoDBObject("mach_id" -> machineID)).toList

    var countQty: Long = 0
    var eventSummary: Map[Int, Long] = Map.empty

    data.foreach { entry =>
      countQty += entry("count_qty").toString.toLong
      val defactID = entry("defact_id").toString.toInt
      val eventCount = entry("event_qty").toString.toLong
      val sumEventCount = eventSummary.get(defactID).getOrElse(0L) + eventCount
      
      eventSummary += (defactID -> sumEventCount)
    }

    case class EventTable(title: String, count: Long)

    val eventSummaryTable = eventSummary.toList.map { case(eventID, count) => 
      val eventTitle = MachineInfo.getErrorDesc(machineID, eventID)
      new EventTable(eventTitle, count)
    }

    val sortedTable = eventSummaryTable.sortWith(_.count > _.count).filterNot(_.count <= 0)
    val currentLevel = machineLevelBox.map(x => x.level(countQty)).openOr("無均線資料")
    val labelColor = currentLevel match {
      case "A" => "green"
      case "B" => "yellow"
      case "C" => "red"
      case "D" => "black"
      case _ => ""
    }

    ".levelA *" #> levelA &
    ".levelB *" #> levelB &
    ".levelC *" #> levelC &
    ".countQty *" #> countQty &
    ".label [class+]" #> labelColor &
    ".label *" #> currentLevel &
    ".eventRow" #> sortedTable.map { event =>
      ".eventName *" #> event.title &
      ".eventCount *" #> event.count
    }
 }

}

