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
      ".kadou *"        #> kadouRate &
      ".okRate *"       #> okRate &
      ".shortRate *"    #> shortRate &
      ".stickRate *"    #> stickRate &
      ".tapeRate *"     #> tapeRate &
      ".plusRate *"     #> plusRate &
      ".minusRate *"    #> minusRate
    }
  }

  def step2Rows = {

    val dataRow = dataTable.find(
      MongoDBObject(
        "insertDate" -> s"$yearString-$monthString-$dateString",
        "shit" -> shiftTag,
        "machineType" -> 2
      )
    )

    dataRow.toList.map { record =>

      val machineID = record.get("machineID").toString
      val machineModel = "Model"
      val standard = MachineLevel.find("machineID", machineID).map(x => x.levelA.get).toOption
      val product = record.get("product").toString
      val area = s"${record.get("floor").toString} 樓 ${record.get("area").toString} 區"
      val countQty = record.get("countQty").toString.toLong
      val total   = Option(record.get("total")).map(_.toString.toLong)
      val defactD = Option(record.get("defactD")).map(_.toString.toLong)
      val white   = Option(record.get("white")).map(_.toString.toLong)
      val rubber  = Option(record.get("rubber")).map(_.toString.toLong)
      val shell   = Option(record.get("shell")).map(_.toString.toLong)

      val kadouRate = standard match {
        case None => "-"
        case Some(standardValue) => f"${(countQty / standard.getOrElse(0L).toDouble) * 100}%.2f %%"
      }

      val okRate = total match {
        case None => "-"
        case Some(totalValue) => f"${(countQty / totalValue.toDouble) * 100}%.2f %%"
      }

      val insertRate = total match {
        case None => "-"
        case Some(totalValue) =>
          val rate = ((totalValue - defactD.getOrElse(0L) - white.getOrElse(0L) - countQty) / totalValue.toDouble) - 1
          f"$rate%.2f %%"
      }

      val defactDRateHolder = for {
        totalValue <- total
        defactDValue <- defactD
      } yield (defactDValue / totalValue.toDouble)

      val whiteRateHolder = for {
        totalValue <- total
        whiteValue <- white
      } yield (whiteValue / totalValue.toDouble)

      val rubberRate = rubber match {
        case None => "-"
        case Some(rubberValue) => f"${((rubberValue / countQty.toDouble) - 1) * 100}%.2f %%"
      }

      val shellRate = shell match {
        case None => "-"
        case Some(shellValue) => f"${((shellValue / countQty.toDouble) - 1) * 100}%.2f %%"
      }


      ".machineID *"    #> machineID &
      ".machineModel *" #> machineModel &
      ".product *"      #> product &
      ".area *"         #> area &
      ".standard *"     #> standard.getOrElse("-").toString &
      ".countQty *"     #> countQty &
      ".kadou *"        #> kadouRate &
      ".okRate *"       #> okRate &
      ".insertRate *"   #> insertRate &
      ".defactDRate *"  #> defactDRateHolder.map(x => f"$x%.2f %%").getOrElse("-") &
      ".whiteRate *"    #> whiteRateHolder.map(x => f"$x%.2f %%").getOrElse("-") &
      ".rubberRate *"   #> rubberRate &
      ".shellRate *"    #> shellRate
    }
  }

  def render = {
    ".step1Rows" #> step1Rows &
    ".step2Rows" #> step2Rows
  }

}

