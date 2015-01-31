package code.json

import code.lib._
import code.model._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable.HashMap

import com.mongodb.casbah.Imports._

object MachineJSON {

  def getMachineType(entry: DBObject) = entry("mach_type").toString
  def getMachineModel(entry: DBObject) = entry("mach_model").toString
  def getMachineID(entry: DBObject) = entry("mach_id").toString
  def getDefactID(entry: DBObject) = entry("defact_id").toString

  def getSumBadQty(dataList: List[DBObject]) = dataList.map(data => data("event_qty").toString.toLong).sum

  def overview: JValue = {

    val data = MongoDB.zhenhaiDB("reasonByMachine").find("event_qty" $gt 0)
    val dataByMachineID = data.toList.groupBy(getMachineType).mapValues(getSumBadQty)
    val dataJSON = dataByMachineID.map{ case (machineType, value) =>
      ("name" -> machineType) ~
      ("value" -> value) ~
      ("link" -> s"/machine/$machineType")
    }

    ("dataSet" -> dataJSON)
  }

  def apply(machineType: String): JValue = {

    val data = MongoDB.zhenhaiDB("reasonByMachine").find(MongoDBObject("mach_type" -> machineType) ++ ("event_qty" $gt 0))
    val dataByMachineModel = data.toList.groupBy(getMachineModel).mapValues(getSumBadQty)
    val dataJSON = dataByMachineModel.map{ case (machineModel, value) =>
      ("name" -> machineModel) ~
      ("value" -> value) ~
      ("link" -> s"/machine/$machineType/$machineModel")
    }

    ("dataSet" -> dataJSON)
  }

  def apply(machineType: String, machineModel: String): JValue = {

    val data = MongoDB.zhenhaiDB("reasonByMachine").find(
      MongoDBObject("mach_type" -> machineType) ++ 
      MongoDBObject("mach_model" -> machineModel) ++
      ("event_qty" $gt 0)
    )
    val dataByMachineID = data.toList.groupBy(getMachineID).mapValues(getSumBadQty)
    val dataJSON = dataByMachineID.map{ case (machineID, value) =>
      ("name" -> machineID) ~
      ("value" -> value) ~
      ("link" -> s"/machine/$machineType/$machineModel/$machineID")
    }

    ("dataSet" -> dataJSON)
  }

  def detailPie(machineID: String): JValue = {

    val data = MongoDB.zhenhaiDB("dailyDefact").find(MongoDBObject("mach_id" -> machineID) ++ ("event_qty" $gt 0))
    val dataByDefactID = data.toList.groupBy(getDefactID).mapValues(getSumBadQty)
    val sortedData = dataByDefactID.toList.sortBy(_._1.toLong)
    val dataJSON = sortedData.map{ case (defactID, value) =>
      ("name" -> MachineInfo.getErrorDesc(machineID, defactID.toInt)) ~
      ("value" -> value)
    }

    ("dataSet" -> dataJSON)
  }

  def detailTable(machineID: String): JValue = {

    def byTimestamp(objA: DBObject, objB: DBObject) = objA("timestamp").toString < objB("timestamp").toString

    val data = MongoDB.zhenhaiDB("dailyDefact").find(MongoDBObject("mach_id" -> machineID) ++ ("event_qty" $gt 0))
    val dataJSON = data.toList.sortWith(byTimestamp).map { entry =>
      ("time" -> entry("timestamp").toString) ~
      ("defact_id" -> MachineInfo.getErrorDesc(machineID, entry("defact_id").toString.toInt)) ~
      ("event_qty" -> entry("event_qty").toString.toLong)
    }

    dataJSON
  }

}

