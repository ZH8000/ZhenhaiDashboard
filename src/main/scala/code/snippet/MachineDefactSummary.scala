package code.snippet

import code.lib._
import code.model._
import com.mongodb.casbah.Imports._
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import net.liftweb.common._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.util._
import net.liftweb.util.Helpers._

class MachineDefactSummary {

  val Array(_, yearString, monthString, dateString, shiftTag) = S.uri.drop(1).split("/")
  val dataTable = MongoDB.zhenhaiDB(s"defactSummary-$yearString-$monthString")

  def step1Rows = {

    val dataRow = dataTable.find(
      MongoDBObject(
        "insertDate" -> s"$yearString-$monthString-$dateString",
        "shit" -> shiftTag,
        "machineType" -> 1
      )
    )

    dataRow.toList.map { record =>

      val machineID = record.get("machineID").toString
      val machineModel = "Model"
      val standard = MachineLevel.find("machineID", machineID).map(x => x.levelA.get).toOption
      val product = record.get("product").toString
      val area = s"${record.get("floor").toString} 樓 ${record.get("area").toString} 區"
      val countQty = record.get("countQty").toString.toLong
      val short = Option(record.get("short")).map(_.toString.toLong)
      val stick = Option(record.get("stick")).map(_.toString.toLong)
      val tape  = Option(record.get("tape")).map(_.toString.toLong)
      val roll  = Option(record.get("roll")).map(_.toString.toLong)
      val plus  = Option(record.get("plus")).map(_.toString.toLong)
      val minus = Option(record.get("minus")).map(_.toString.toLong)
      val total = countQty + short.getOrElse(0L) + stick.getOrElse(0L) + tape.getOrElse(0L) + roll.getOrElse(0L)
    
      val okRate = total match {
        case 0 => "總數為 0 無法計算"
        case x => f"${((countQty / total.toDouble) * 100)}%.2f" + " %"
      }

      val kadouRate = standard match {
        case None => "-"
        case Some(standardValue) => f"${(countQty / standard.getOrElse(0L).toDouble) * 100}%.2f %%"
      }

      val shortRate = total match {
        case 0 => "總數為 0 無法計算"
        case x => short match {
          case None => "-"
          case Some(shortCount) => f"${((shortCount / total.toDouble) * 100)}%.2f" + " %"
        }
      }

      val stickRate = total match {
        case 0 => "總數為 0 無法計算"
        case x => stick match {
          case None => "-"
          case Some(stickCount) => f"${((stickCount / total.toDouble) * 100)}%.2f" + " %"
        }
      }

      val tapeRate = total match {
        case 0 => "總數為 0 無法計算"
        case x => tape match {
          case None => "-" 
          case Some(tapeCount) => f"${((tapeCount / total.toDouble) * 100)}%.2f" + " %"
        }
      }

      val plusRate = countQty match {
        case 0 => "良品數為 0 無法計算"
        case x => plus match {
          case None => "-"
          case Some(plusCount) => f"$plusCount / $countQty - 1" + " %"
        }
      }

      val minusRate = countQty match {
        case 0 => "良品數為 0 無法計算"
        case x => minus match {
          case None => "-"
          case Some(minusCount) => f"$minusCount / $countQty - 1" + " %"
        }
      }

      ".machineID *"    #> machineID &
      ".machineModel *" #> machineModel &
      ".product *"      #> product &
      ".area *"         #> area &
      ".standard *"     #> standard.getOrElse("-").toString &
      ".countQty *"     #> countQty &
      ".okRate *"       #> okRate &
      ".kadou *"        #> kadouRate &
      ".shortRate *"    #> shortRate &
      ".stickRate *"    #> stickRate &
      ".tapeRate *"     #> tapeRate &
      ".plusRate *"     #> plusRate &
      ".minusRate *"    #> minusRate
    }
  }

  def render = {
    ".step1Rows" #> step1Rows
  }

}

