package bootstrap.liftweb

import code.util._
import code.json._

import net.liftweb.http.LiftRules
import net.liftweb.http.Req

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.JsonResponse

import net.liftweb.common.{Box, Empty}
import net.liftweb.common.Box._

import com.mongodb.casbah.Imports._

case class User(id: String, username: String)

object User {

  import code.db._
  import net.liftweb.util.BCrypt

  def loginAs(username: String, password: String): Box[User] = {

    val users = MongoDB.zhenhaiDB("user")
    val loggedInUser = for {
      userRecord <- users.findOne(MongoDBObject("username" -> username))
      id <- userRecord._id
      username <- userRecord.getAs[String]("username")
      hashedPassword <- userRecord.getAs[String]("password") if BCrypt.checkpw(password, hashedPassword)
    } yield { 
      User(id.toString, username) 
    }

    loggedInUser
  }
}

object Authentication {

}

object ProductHelper extends RestHelper {

  import net.liftweb.util.BasicTypesHelpers.AsInt

  serve("api" / "json" / "total" prefix {
    case Nil Get req => 
      JsonResponse(ProductJSON.overview)
    case productName :: Nil Get req => 
      JsonResponse(ProductJSON(productName))
    case productName :: AsInt(year) :: AsInt(month) :: Nil Get req => 
      JsonResponse(ProductJSON(productName, year, month))
    case productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: Nil Get req => 
      JsonResponse(ProductJSON(productName, year, month, week))
    case productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil Get req => 
      JsonResponse(ProductJSON(productName, year, month, week, date))
    case productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil Get req => 
      JsonResponse(ProductJSON(productName, year, month, week, date, machineID))
  })

  serve("api" / "json" / "monthly" prefix {
    case AsInt(year) :: Nil Get req => 
      JsonResponse(MonthlyJSON(year))
    case AsInt(year) :: AsInt(month) :: Nil Get req => 
      JsonResponse(MonthlyJSON(year, month))
    case AsInt(year) :: AsInt(month) :: AsInt(week) :: Nil Get req => 
      JsonResponse(MonthlyJSON(year, month, week))
    case AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil Get req => 
      JsonResponse(MonthlyJSON(year, month, week, date))
    case AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil Get req => 
      JsonResponse(MonthlyJSON(year, month, week, date, machineID))
  })

  serve("api" / "json" / "daily" prefix {
    case AsInt(year) :: AsInt(month) :: Nil Get req => 
      JsonResponse(DailyJSON(year, month))
    case AsInt(year) :: AsInt(month) :: AsInt(date) :: Nil Get req => 
      JsonResponse(DailyJSON(year, month, date))
    case AsInt(year) :: AsInt(month) :: AsInt(date) :: machineID :: Nil Get req => 
      JsonResponse(DailyJSON(year, month, date, machineID))
  })

  serve {
    case "api" :: "json" :: "alert" :: Nil Get req => JsonResponse(AlertJSON.overview)
  }

  serve("api" / "json" / "machine" prefix {
    case Nil Get req => JsonResponse(MachineJSON.overview)
    case machineType :: Nil Get req => JsonResponse(MachineJSON(machineType))
    case machineType :: machineModel :: Nil Get req => JsonResponse(MachineJSON(machineType, machineModel))
    case machineType :: machineModel :: machineID :: "pie" :: Nil Get req => JsonResponse(MachineJSON.detailPie(machineID))
    case machineType :: machineModel :: machineID :: "table" :: Nil Get req => JsonResponse(MachineJSON.detailTable(machineID))
  })


}

class Boot 
{
  def boot 
  {
    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.addToPackages("code")
    LiftRules.dispatch.append(ProductHelper)
  }
}
