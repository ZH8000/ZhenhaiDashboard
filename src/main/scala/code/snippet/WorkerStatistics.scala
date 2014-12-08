package code.snippet

import code.model._

import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml

import net.liftweb.util.Helpers._
import net.liftweb.util._

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery.JqJsCmds._
import java.util.Calendar
import java.text.SimpleDateFormat

class WorkerOverview {

  def getMonthlyTimestamp(record: WorkerDaily) = record.timestamp.get.substring(0, 7)

  def steps = {
    val Array(_, _, workerID) = S.uri.split("/")
    val name = Worker.find(workerID).map(_.name.get).getOrElse("查無此人")
    ".workerName *" #> name
  }

  def weeklySteps = {
    val Array(_, _, workerID,yearAndMonth) = S.uri.split("/")
    val name = Worker.find(workerID).map(_.name.get).getOrElse("查無此人")

    ".workerName *" #> name &
    ".yearAndMonth *" #> yearAndMonth
  }

  def dailySteps = {
    val Array(_, _,workerID,yearAndMonth, week) = S.uri.split("/")
    val name = Worker.find(workerID).map(_.name.get).getOrElse("查無此人")

    ".workerName *" #> name &
    ".yearAndMonth *" #> yearAndMonth &
    ".week *" #> s"第 $week 週"
  }

  def detailSteps = {
    val Array(_, _,workerID, yearAndMonth, week, date) = S.uri.split("/")
    val name = Worker.find(workerID).map(_.name.get).getOrElse("查無此人")

    ".workerName *" #> name &
    ".yearAndMonth *" #> yearAndMonth &
    ".week *" #> s"第 $week 週" &
    ".date *" #> s"$date 日"
  }

  def render = {
    val Array(_, _, workerID) = S.uri.split("/")
    val recordByMonth = WorkerDaily.findAll("workerMongoID", workerID).groupBy(getMonthlyTimestamp)
    val sumByMonth = recordByMonth.mapValues(x => x.map(_.countQty.get).sum)
    val sortedRows = sumByMonth.toList.sortWith(_._1 > _._1)
    val ratio = sortedRows match {
      case Nil => 0
      case record :: _ => 800 / sortedRows.map(_._2).max.toDouble
    }

    ".row" #> sortedRows.map { case (timestamp, countQty) =>
      val width = (countQty * ratio).toInt

      ".timestamp *" #> timestamp &
      ".barText *" #> countQty &
      ".barRect [width]" #> width &
      ".barText [x]" #> (width + 10) &
      ".barRect [onclick]" #> s"window.location='/workers/$workerID/$timestamp'"
    }
  }

  def weekly = {
    def getWeek(dateString: String) = {
      val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
      val date = dateFormatter.parse(dateString)
      val calendar = Calendar.getInstance
      calendar.setTime(date)
      calendar.get(Calendar.WEEK_OF_MONTH)
    }

    val Array(_, _, workerID,yearAndMonth) = S.uri.split("/")
    val recordInMonth = WorkerDaily.findAll("workerMongoID", workerID).filter(_.timestamp.get.startsWith(yearAndMonth))
    val recordByWeeks = recordInMonth.groupBy(record => getWeek(record.timestamp.get))
    val sumByWeeks = recordByWeeks.mapValues(x => x.map(_.countQty.get).sum)

    val sortedRows = sumByWeeks.toList.sortWith(_._1 > _._1)
    val ratio = sortedRows match {
      case Nil => 0
      case record :: _ => 800 / sortedRows.map(_._2).max.toDouble
    }

    ".row" #> sortedRows.map { case (week, countQty) =>
      val width = (countQty * ratio).toInt

      ".timestamp *" #> s"第 $week 週" &
      ".barText *" #> countQty &
      ".barRect [width]" #> width &
      ".barText [x]" #> (width + 10) &
      ".barRect [onclick]" #> s"window.location='/workers/$workerID/$yearAndMonth/$week'"
    }

  }

  def daily = {
    def getWeek(dateString: String) = {
      val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
      val date = dateFormatter.parse(dateString)
      val calendar = Calendar.getInstance
      calendar.setTime(date)
      calendar.get(Calendar.WEEK_OF_MONTH)
    }

    val Array(_, _,workerID,yearAndMonth, week) = S.uri.split("/")
    val recordInMonth = WorkerDaily
                          .findAll("workerMongoID", workerID)
                          .filter(x => x.timestamp.get.startsWith(yearAndMonth) && getWeek(x.timestamp.get) == week.toInt)

    val recordByDate = recordInMonth.groupBy(_.timestamp.get)
    val sumByDate = recordByDate.mapValues(x => x.map(_.countQty.get).sum)

    val sortedRows = sumByDate.toList.sortWith(_._1 > _._1)
    val ratio = sortedRows match {
      case Nil => 0
      case record :: _ => 800 / sortedRows.map(_._2).max.toDouble
    }

    ".row" #> sortedRows.map { case (timestamp, countQty) =>
      val width = (countQty * ratio).toInt
      val Array(year, month, date) = timestamp.split("-")

      ".timestamp *" #> s"$year 年 $month 月 $date 日" &
      ".barText *" #> countQty &
      ".barRect [width]" #> width &
      ".barText [x]" #> (width + 10) &
      ".barRect [onclick]" #> s"window.location='/workers/$workerID/$yearAndMonth/$week/$date'"
    }

  }

  def detail = {
    val Array(_, _,workerID,yearAndMonth, week, date) = S.uri.split("/")
    val recordInDate = WorkerDaily
                          .findAll("workerMongoID", workerID)
                          .filter(x => x.timestamp.get == s"$yearAndMonth-$date")

    val sortedRows = recordInDate.sortWith(_.machineID.get < _.machineID.get)
    val ratio = sortedRows match {
      case Nil => 0
      case record :: _ => 800 / sortedRows.map(_.countQty.get).max.toDouble
    }

    ".row" #> sortedRows.map { record =>
      val width = (record.countQty.get * ratio).toInt
      val machineLevel = MachineLevel.find("machineID", record.machineID.toString)
      val currentLevel = machineLevel.map(x => x.level(record.countQty.get)).openOr("無均線資料")
      val labelColor = currentLevel match {
        case "A" => "green"
        case "B" => "yellow"
        case "C" => "red"
        case _ => ""
      }

      ".machineID *" #> record.machineID &
      ".barText *" #> record.countQty &
      ".barRect [width]" #> width &
      ".barText [x]" #> (width + 10) &
      ".level *" #> currentLevel &
      ".level [class+]" #> labelColor
    }

  }

}

class WorkerStatistics {

  def getWorker(workerMongoID: String) = Worker.find(workerMongoID)
  def isDeleted(workerMongoID: String) = getWorker(workerMongoID).map(_.isDeleted.get).getOrElse(true)

  def totalTable = {

    val recordByWorkers = WorkerDaily.findAll.groupBy(_.workerMongoID.get).filterNot(x => isDeleted(x._1))
    val sumByWorkers = recordByWorkers.mapValues(x => x.map(_.countQty.get).sum)
    val sortedRows = sumByWorkers.toList.sortWith(_._2 > _._2)
    val ratio = sortedRows match {
      case Nil => 0
      case (id, value) :: _ => 800 / value.toDouble
    }

    ".row" #> sortedRows.map { case (workerMongoID, countQty) =>
      val worker = getWorker(workerMongoID)
      val width = (countQty * ratio).toInt

      ".workerID *" #> worker.map(_.workerID.get).getOrElse("查無此人") &
      ".workerName *" #> worker.map(_.name.get).getOrElse("查無此人") &
      ".barText *" #> countQty &
      ".barRect [width]" #> width &
      ".barText [x]" #> (width + 10) &
      ".barRect [onclick]" #> s"window.location='/workers/$workerMongoID'"

    }
  }
}

