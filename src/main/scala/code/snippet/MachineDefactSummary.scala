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
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.util._
import net.liftweb.util.Helpers._

class MachineDefactSummary {

  val Array(_, yearString, monthString, dateString, _*) = S.uri.drop(1).split("/")

  val dataTable = MongoDB.zhenhaiDB(s"defactSummary-$yearString-$monthString")
  val shiftDate = s"$yearString-$monthString-$dateString"

  def getSteps(uri: List[String]) = uri match {

    case "machineDefactSummary" :: year :: month :: date :: Nil =>

      List(
        Step("總覽", true, Some(s"/viewDetail")),
        Step(f"$year-$month-$date", true, Some(f"/machineDefactSummary/$year/$month/$date")),
        Step("班別"),
        Step("分類")
      )

      
    case "machineDefactSummary" :: year :: month :: date :: shift :: Nil =>

      val shiftTitle = shift match {
        case "M" => "早班"
        case "N" => "晚班"
        case _   => "Unknown"
      }

      List(
        Step("總覽", true, Some(s"/viewDetail")),
        Step(f"$year-$month-$date", true, Some(f"/machineDefactSummary/$year/$month/$date")),
        Step(shiftTitle, true, Some(f"/machineDefactSummary/$year/$month/$date/$shift")),
        Step("分類")
      )

    case "machineDefactSummary" :: year :: month :: date :: shift :: sort :: Nil =>

      val shiftTitle = shift match {
        case "M" => "早班"
        case "N" => "晚班"
        case _   => "Unknown"
      }

      val sortTitle = sort match {
        case "model" => "依機種排序"
        case "size"  => "依尺寸排序"
        case "area"  => "依區域排序"
        case _   => "Unknown"
      }

      List(
        Step("總覽", true, Some(s"/viewDetail")),
        Step(f"$year-$month-$date", true, Some(f"/machineDefactSummary/$year/$month/$date")),
        Step(shiftTitle, true, Some(f"/machineDefactSummary/$year/$month/$date/$shift")),
        Step(sortTitle, true)
      )


    case _ => Nil
  }

  def showStepsSelector = {
    val steps = getSteps(S.uri.drop(1).split("/").toList)

    ".step" #> steps.map { step => 
      "a [href]" #> step.link &
      "a *" #> step.title &
      "a [class+]" #> (if (step.isActive) "active" else "")
    }

  }

  def shiftLink = {

    "#morningShift [href]" #> s"/machineDefactSummary/$yearString/$monthString/$dateString/M" &
    "#nightShift [href]" #> s"/machineDefactSummary/$yearString/$monthString/$dateString/N"
  }

  def sortLink = {

    val Array(_, yearString, monthString, dateString, shiftLink) = S.uri.drop(1).split("/")

    "#sortByModel [href]" #> s"/machineDefactSummary/$yearString/$monthString/$dateString/$shiftLink/model" &
    "#sortBySize [href]" #> s"/machineDefactSummary/$yearString/$monthString/$dateString/$shiftLink/size" &
    "#sortByArea [href]" #> s"/machineDefactSummary/$yearString/$monthString/$dateString/$shiftLink/area"
  }

  def updatePolicy(shiftDate: String, shiftTag: String, machineID: String)(value: String): JsCmd = {
    val query = 
      MongoDBObject(
        "shiftDate" -> s"$yearString-$monthString-$dateString",
        "shift" -> shiftTag,
        "machineID" -> machineID
      )

    dataTable.update(query, $set("policy" -> value))
    Noop
  }

  def updateFixer(shiftDate: String, shiftTag: String, machineID: String)(value: String): JsCmd = {
    val query = 
      MongoDBObject(
        "shiftDate" -> s"$yearString-$monthString-$dateString",
        "shift" -> shiftTag,
        "machineID" -> machineID
      )

    dataTable.update(query, $set("fixer" -> value))
    Noop
  }

  def sortData(dataRow: List[DBObject], sortTag: String) = {
    sortTag match {
      case "model" => dataRow.sortWith((x, y) => x.get("machineModel").toString < y.get("machineModel").toString)
      case "size"  => dataRow.sortWith((x, y) => x.get("product").toString < y.get("product").toString)
      case "area"  => dataRow.sortWith { case (x, y) => 
        s"${x.get("floor").toString} 樓 ${x.get("area").toString} 區"  < 
        s"${y.get("floor").toString} 樓 ${y.get("area").toString} 區"
      }
      case _ => dataRow
    }
  }

  def step1Rows(shiftTag: String, sortTag: String) = {

    val dataRow = dataTable.find(MongoDBObject("shiftDate" -> shiftDate, "shift" -> shiftTag, "machineType" -> 1)).toList
    val sortedData = sortData(dataRow, sortTag)

    sortedData.map { record =>

      val machineID = record.get("machineID").toString
      val machineModel = record.get("machineModel").toString
      val standard = MachineLevel.find("machineID", machineID).map(x => x.levelA.get).toOption
      val product = record.get("product").toString
      val area = s"${record.get("floor").toString} 樓 ${record.get("area").toString} 區"
      val countQty = Option(record.get("countQty"))map(_.toString.toLong)
      val short = Option(record.get("short")).map(_.toString.toLong)
      val stick = Option(record.get("stick")).map(_.toString.toLong)
      val tape  = Option(record.get("tape")).map(_.toString.toLong)
      val roll  = Option(record.get("roll")).map(_.toString.toLong)
      val plus  = Option(record.get("plus")).map(_.toString.toLong)
      val minus = Option(record.get("minus")).map(_.toString.toLong)
      val total = countQty.getOrElse(0L) + short.getOrElse(0L) + stick.getOrElse(0L) + tape.getOrElse(0L) + roll.getOrElse(0L)
      val policy = Option(record.get("policy")).map(_.toString).getOrElse("")
      val fixer = Option(record.get("fixer")).map(_.toString).getOrElse("")
   
      val okRate = total match {
        case 0 => "總數為 0 無法計算"
        case x => f"${((countQty.getOrElse(0L) / total.toDouble) * 100)}%.2f" + " %"
      }

      val kadouRate = standard match {
        case None => "-"
        case Some(standardValue) => f"${(countQty.getOrElse(0L) / standard.getOrElse(0L).toDouble) * 100}%.2f %%"
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

      val rollRate = total match {
        case 0 => "總數為 0 無法計算"
        case x => roll match {
          case None => "-" 
          case Some(rollCount) => f"${((rollCount / total.toDouble) * 100)}%.2f" + " %"
        }
      }

      val plusRate = countQty.getOrElse(0) match {
        case 0 => "良品數為 0 無法計算"
        case x => plus match {
          case None => "-"
          case Some(plusCount) => f"${(plusCount / countQty.getOrElse(0L).toDouble) - 1}%.2f" + " %"
        }
      }

      val minusRate = countQty.getOrElse(0) match {
        case 0 => "良品數為 0 無法計算"
        case x => minus match {
          case None => "-"
          case Some(minusCount) => f"${(minusCount / countQty.getOrElse(0L).toDouble) - 1}%.2f" + " %"
        }
      }

      ".machineID *"    #> machineID &
      ".machineModel *" #> machineModel &
      ".product *"      #> product &
      ".area *"         #> area &
      ".standard *"     #> standard.getOrElse("-").toString &
      ".countQty *"     #> countQty.getOrElse(0L) &
      ".kadou *"        #> kadouRate &
      ".okRate *"       #> okRate &
      ".shortRate *"    #> shortRate &
      ".stickRate *"    #> stickRate &
      ".tapeRate *"     #> tapeRate &
      ".rollRate *"     #> rollRate &
      ".plusRate *"     #> plusRate &
      ".minusRate *"    #> minusRate &
      ".short *"        #> short.map(_.toString).getOrElse("沒資料") &
      ".stick *"        #> stick.map(_.toString).getOrElse("沒資料") &
      ".tape *"         #> tape.map(_.toString).getOrElse("沒資料") &
      ".roll *"         #> roll.map(_.toString).getOrElse("沒資料") &
      ".plus *"         #> plus.map(_.toString).getOrElse("沒資料") &
      ".minus *"        #> minus.map(_.toString).getOrElse("沒資料") &
      ".policy *"       #> SHtml.ajaxText(policy, false, updatePolicy(shiftDate, shiftTag, machineID)_) &
      ".fixer *"        #> SHtml.ajaxText(fixer, false, updateFixer(shiftDate, shiftTag, machineID)_)
    }
  }

  def step2Rows(shiftTag: String, sortTag: String) = {

    val dataRow = dataTable.find(
      MongoDBObject(
        "shiftDate" -> s"$yearString-$monthString-$dateString",
        "shift" -> shiftTag,
        "machineType" -> 2
      )
    ).toList

    val sortedData = sortData(dataRow, sortTag)

    sortedData.map { record =>

      val machineID = record.get("machineID").toString
      val machineModel = record.get("machineModel").toString
      val standard = MachineLevel.find("machineID", machineID).map(x => x.levelA.get).toOption
      val product = record.get("product").toString
      val area = s"${record.get("floor").toString} 樓 ${record.get("area").toString} 區"
      val countQty = Option(record.get("countQty")).map(_.toString.toLong)
      val defactD = Option(record.get("defactD")).map(_.toString.toLong)
      val white   = Option(record.get("white")).map(_.toString.toLong)
      val rubber  = Option(record.get("rubber")).map(_.toString.toLong)
      val shell   = Option(record.get("shell")).map(_.toString.toLong)
      val policy = Option(record.get("policy")).map(_.toString).getOrElse("")
      val fixer = Option(record.get("fixer")).map(_.toString).getOrElse("")
      val originalTotal = Option(record.get("total")).map(_.toString.toLong)
      val inaccurateTotal = Some(countQty.getOrElse(0L) + defactD.getOrElse(0L) + white.getOrElse(0L))
      val total = originalTotal orElse inaccurateTotal

      val kadouRate = standard match {
        case None => "-"
        case Some(standardValue) => f"${(countQty.getOrElse(0L) / standard.getOrElse(0L).toDouble) * 100}%.2f %%"
      }

      val okRate = total match {
        case None => "-"
        case Some(totalValue) => f"${(countQty.getOrElse(0L) / totalValue.toDouble) * 100}%.2f %%"
      }

      val insertRate = total match {
        case None => "-"
        case Some(totalValue) =>
          val rate = ((totalValue - defactD.getOrElse(0L) - white.getOrElse(0L) - countQty.getOrElse(0L)) / totalValue.toDouble)
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
        case Some(rubberValue) => f"${((rubberValue / countQty.getOrElse(0L).toDouble) - 1) * 100}%.2f %%"
      }

      val shellRate = shell match {
        case None => "-"
        case Some(shellValue) => f"${((shellValue / countQty.getOrElse(0L).toDouble) - 1) * 100}%.2f %%"
      }


      ".machineID *"     #> machineID &
      ".machineModel *"  #> machineModel &
      ".product *"       #> product &
      ".area *"          #> area &
      ".standard *"      #> standard.getOrElse("-").toString &
      ".countQty *"      #> countQty.getOrElse(0L) &
      ".kadou *"         #> kadouRate &
      ".okRate *"        #> okRate &
      ".insertRate *"    #> insertRate &
      ".defactDRate *"   #> defactDRateHolder.map(x => f"$x%.2f %%").getOrElse("-") &
      ".whiteRate *"     #> whiteRateHolder.map(x => f"$x%.2f %%").getOrElse("-") &
      ".rubberRate *"    #> rubberRate &
      ".shellRate *"     #> shellRate &
      ".total *"         #> total.map(_.toString).getOrElse("無資料") & 
      ".originalTotal *" #> originalTotal.map(_.toString).getOrElse("無資料") & 
      ".defactD *"       #> defactD.map(_.toString).getOrElse("無資料") & 
      ".white *"         #> white.map(_.toString).getOrElse("無資料") & 
      ".rubber *"        #> rubber.map(_.toString).getOrElse("無資料") & 
      ".shell *"         #> shell.map(_.toString).getOrElse("無資料") & 
      ".policy *"        #> SHtml.ajaxText(policy, false, updatePolicy(shiftDate, shiftTag, machineID)_) &
      ".fixer *"         #> SHtml.ajaxText(fixer, false, updateFixer(shiftDate, shiftTag, machineID)_)
    }
  }

  def step3Rows(shiftTag: String, sortTag: String) = {

    val dataRow = dataTable.find(
      MongoDBObject(
        "shiftDate" -> s"$yearString-$monthString-$dateString",
        "shift" -> shiftTag,
        "machineType" -> 3
      )
    ).toList

    val sortedData = sortData(dataRow, sortTag)

    sortedData.map { record =>

      val machineID = record.get("machineID").toString
      val machineModel = record.get("machineModel").toString
      val standard = MachineLevel.find("machineID", machineID).map(x => x.levelA.get).toOption
      val product = record.get("product").toString
      val area = s"${record.get("floor").toString} 樓 ${record.get("area").toString} 區"
      val countQty = Option(record.get("countQty")).map(_.toString.toLong)


      val short     = Option(record.get("short")).map(_.toString.toLong)
      val open      = Option(record.get("open")).map(_.toString.toLong)
      val capacity  = Option(record.get("capacity")).map(_.toString.toLong)
      val lose      = Option(record.get("lose")).map(_.toString.toLong)
      val lc        = Option(record.get("lc")).map(_.toString.toLong)
      val retest    = Option(record.get("retest")).map(_.toString.toLong)
      val policy = Option(record.get("policy")).map(_.toString).getOrElse("")
      val fixer = Option(record.get("fixer")).map(_.toString).getOrElse("")
      val originalTotal = Option(record.get("total")).map(_.toString.toLong)
      val inaccurateTotal = Some(countQty.getOrElse(0L) + capacity.getOrElse(0L) + lose.getOrElse(0L) + lc.getOrElse(0L) + retest.getOrElse(0L))
      val total = originalTotal orElse inaccurateTotal

      val kadouRate = standard match {
        case None => "-"
        case Some(standardValue) => f"${(countQty.getOrElse(0L) / standard.getOrElse(0L).toDouble) * 100}%.2f %%"
      }

      val okRate = total match {
        case None => "-"
        case Some(totalValue) => f"${(countQty.getOrElse(0L) / totalValue.toDouble) * 100}%.2f %%"
      }

      val shortHolder = for {
        totalValue <- total
        shortValue <- short
      } yield (shortValue / totalValue.toDouble)

      val openHolder = for {
        totalValue <- total
        openValue <- open
      } yield (openValue / totalValue.toDouble)

      val capacityHolder = for {
        totalValue <- total
        capacityValue <- capacity
      } yield (capacityValue / totalValue.toDouble)

      val loseHolder = for {
        totalValue <- total
        loseValue <- lose
      } yield (loseValue / totalValue.toDouble)

      val lcHolder = for {
        totalValue <- total
        lcValue <- lc
      } yield (lcValue / totalValue.toDouble)

      val retestHolder = for {
        totalValue <- total
        retestValue <- retest
      } yield (retestValue / totalValue.toDouble)

      ".machineID *"     #> machineID &
      ".machineModel *"  #> machineModel &
      ".product *"       #> product &
      ".area *"          #> area &
      ".standard *"      #> standard.getOrElse("-").toString &
      ".countQty *"      #> countQty.getOrElse(0L) &
      ".kadou *"         #> kadouRate &
      ".okRate *"        #> okRate &
      ".shortRate *"     #> shortHolder.map(x => f"$x%.2f %%").getOrElse("-") &
      ".openRate *"      #> openHolder.map(x => f"$x%.2f %%").getOrElse("-") &
      ".capacityRate *"  #> capacityHolder.map(x => f"$x%.2f %%").getOrElse("-") &
      ".loseRate *"      #> loseHolder.map(x => f"$x%.2f %%").getOrElse("-") &
      ".lcRate *"        #> lcHolder.map(x => f"$x%.2f %%").getOrElse("-") &
      ".retestRate *"    #> retestHolder.map(x => f"$x%.2f %%").getOrElse("-") &
      ".total   *"       #> total.map(_.toString).getOrElse("無資料") &
      ".originalTotal *" #> originalTotal.map(_.toString).getOrElse("無資料") & 
      ".short   *"       #> short.map(_.toString).getOrElse("無資料") &
      ".open   *"        #> open.map(_.toString).getOrElse("無資料") &
      ".capacity   *"    #> capacity.map(_.toString).getOrElse("無資料") &
      ".lose    *"       #> lose.map(_.toString).getOrElse("無資料") &
      ".lc      *"       #> lc.map(_.toString).getOrElse("無資料") &
      ".retest  *"       #> retest.map(_.toString).getOrElse("無資料") &
      ".policy *"        #> SHtml.ajaxText(policy, false, updatePolicy(shiftDate, shiftTag, machineID)_) &
      ".fixer *"         #> SHtml.ajaxText(fixer, false, updateFixer(shiftDate, shiftTag, machineID)_)
    }
  }

  def step5Rows(shiftTag: String, sortTag: String, prefix: String) = {

    val dataRow = dataTable.find(
      MongoDBObject(
        "shiftDate" -> s"$yearString-$monthString-$dateString",
        "shift" -> shiftTag,
        "machineType" -> 5
      )
    ).toList.filter(x => x.get("machineID").toString.startsWith(prefix))

    val sortedData = sortData(dataRow, sortTag)

    sortedData.map { record =>

      val machineID = record.get("machineID").toString
      val machineModel = record.get("machineModel").toString
      val standard = MachineLevel.find("machineID", machineID).map(x => x.levelA.get).toOption
      val product = record.get("product").toString
      val area = s"${record.get("floor").toString} 樓 ${record.get("area").toString} 區"
      val countQty = Option(record.get("countQty")).map(_.toString.toLong)
      val total   = Option(record.get("total")).map(_.toString.toLong)
      val policy = Option(record.get("policy")).map(_.toString).getOrElse("")
      val fixer = Option(record.get("fixer")).map(_.toString).getOrElse("")

      val kadouRate = standard match {
        case None => "-"
        case Some(standardValue) => f"${(countQty.getOrElse(0L) / standard.getOrElse(0L).toDouble) * 100}%.2f %%"
      }

      val okRate = total match {
        case None => "-"
        case Some(totalValue) => f"${(countQty.getOrElse(0L) / totalValue.toDouble) * 100}%.2f %%"
      }

      ".machineID *"    #> machineID &
      ".machineModel *" #> machineModel &
      ".product *"      #> product &
      ".area *"         #> area &
      ".standard *"     #> standard.getOrElse("-").toString &
      ".countQty *"     #> countQty.getOrElse(0L) &
      ".kadou *"        #> kadouRate &
      ".okRate *"       #> okRate &
      ".total *"        #> total.map(_.toString).getOrElse("無資料") &
      ".policy *"       #> SHtml.ajaxText(policy, false, updatePolicy(shiftDate, shiftTag, machineID)_) &
      ".fixer *"        #> SHtml.ajaxText(fixer, false, updateFixer(shiftDate, shiftTag, machineID)_)
    }
  }


  def render = {

    val Array(_, yearString, monthString, dateString, shiftTag, sortTag) = S.uri.drop(1).split("/")

    "#excel [href]" #> s"/api/excel/machineDefactSummary/$yearString/$monthString/$dateString/$shiftTag/$sortTag.xls" &
    ".step1Rows" #> step1Rows(shiftTag, sortTag) &
    ".step2Rows" #> step2Rows(shiftTag, sortTag) &
    ".step3Rows" #> step3Rows(shiftTag, sortTag) &
    ".step5Rows-1" #> step5Rows(shiftTag, sortTag, "T") &
    ".step5Rows-2" #> step5Rows(shiftTag, sortTag, "C")
  }

}

