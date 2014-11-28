package code.json

import code.lib._
import code.model._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable.HashMap

import com.mongodb.casbah.Imports._

object AlertJSON {

  def overview: JValue = {
    val data = MongoDB.zhenhaiDB("alert").find().sort(MongoDBObject("timestamp" -> 1, "mach_id" -> 1, "defact_id" -> 1)).toList

    data.map { entry => 
      ("timestamp" -> entry("timestamp").toString) ~
      ("mach_id" -> entry("mach_id").toString) ~
      ("defact_id" -> entry("defact_id").toString)
    }
  }
}

