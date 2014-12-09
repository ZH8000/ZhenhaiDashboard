package code.snippet

import code.model._
import code.lib._
import code.json._

import net.liftweb.http.S
import net.liftweb.util.Helpers._

class WorkerStatistics {

  def workerSteps = {
    val workerID = S.request.map(_.path(1)).openOr("")
    val name = Worker.find(workerID).map(_.name.get).getOrElse("查無此人")
    ".workerName *" #> name
  }

  def weeklySteps = {
    val workerID = S.request.map(_.path(1)).openOr("")
    val yearAndMonth = S.request.map(_.path(2)).openOr("")
    val name = Worker.find(workerID).map(_.name.get).getOrElse("查無此人")

    ".workerName *" #> name &
    ".yearAndMonth *" #> yearAndMonth
  }

  def dailySteps = {
    val workerID = S.request.map(_.path(1)).openOr("")
    val yearAndMonth = S.request.map(_.path(2)).openOr("")
    val week = S.request.map(_.path(3)).openOr("")
    val name = Worker.find(workerID).map(_.name.get).getOrElse("查無此人")

    ".workerName *" #> name &
    ".yearAndMonth *" #> yearAndMonth &
    ".week *" #> s"第 $week 週"
  }

  def detailSteps = {
    val workerID = S.request.map(_.path(1)).openOr("")
    val yearAndMonth = S.request.map(_.path(2)).openOr("")
    val week = S.request.map(_.path(3)).openOr("")
    val date = S.request.map(_.path(4)).openOr("")
    val name = Worker.find(workerID).map(_.name.get).getOrElse("查無此人")

    ".workerName *" #> name &
    ".yearAndMonth *" #> yearAndMonth &
    ".week *" #> s"第 $week 週" &
    ".date *" #> s"$date 日"
  }


  def overviewTable = {

    val records = WorkerStatisticsJSON()
    val maxValue = if (records.isEmpty) 0 else records.map(_.countQty).max
    val scale = Scale(0, maxValue, 10, 800)

    ".row" #> records.map { record =>

      val width = scale(record.countQty)
      ".workerID *" #> record.workerID &
      ".workerName *" #> record.name &
      ".barText *" #> record.countQty &
      ".barRect [width]" #> width &
      ".barText [x]" #> (width + 10) &
      ".barRect [onclick]" #> s"window.location='/workers/${record.workerMongoID}'"

    }
  }

  def workerTable = {

    val workerID = S.request.map(_.path(1)).openOr("")
    val records = WorkerStatisticsJSON(workerID)
    val maxValue = if (records.isEmpty) 0 else records.map(_.countQty).max
    val scale = Scale(0, maxValue, 10, 800)

    ".row" #> records.map { record =>
      val width = scale(record.countQty)

      ".timestamp *" #> record.title &
      ".barText *" #> record.countQty &
      ".barRect [width]" #> width &
      ".barText [x]" #> (width + 10) &
      ".barRect [onclick]" #> s"window.location='/workers/$workerID/${record.title}'"
    }

  }

  def weeklyTable = {

    val workerID = S.request.map(_.path(1)).openOr("")
    val yearAndMonth = S.request.map(_.path(2)).openOr("")
    val records = WorkerStatisticsJSON(workerID, yearAndMonth)
    val maxValue = if (records.isEmpty) 0 else records.map(_.countQty).max
    val scale = Scale(0, maxValue, 10, 800)

    ".row" #> records.map { record =>
      val width = scale(record.countQty)

      ".timestamp *" #> s"第 ${record.title} 週" &
      ".barText *" #> record.countQty &
      ".barRect [width]" #> width &
      ".barText [x]" #> (width + 10) &
      ".barRect [onclick]" #> s"window.location='/workers/$workerID/$yearAndMonth/${record.title}'"
    }

  }

  def dailyTable = {
    val workerID = S.request.map(_.path(1)).openOr("")
    val yearAndMonth = S.request.map(_.path(2)).openOr("")
    val week = S.request.map(_.path(3)).openOr("")
    val records = WorkerStatisticsJSON(workerID, yearAndMonth, week)
    val maxValue = if (records.isEmpty) 0 else records.map(_.countQty).max
    val scale = Scale(0, maxValue, 10, 800)

    ".row" #> records.map { record =>
      val width = scale(record.countQty)
      val Array(year, month, date) = record.title.split("-")

      ".timestamp *" #> s"$year 年 $month 月 $date 日" &
      ".barText *" #> record.countQty &
      ".barRect [width]" #> width &
      ".barText [x]" #> (width + 10) &
      ".barRect [onclick]" #> s"window.location='/workers/$workerID/$yearAndMonth/$week/$date'"
    }

  }

  def detailTable = {
    val workerID = S.request.map(_.path(1)).openOr("")
    val yearAndMonth = S.request.map(_.path(2)).openOr("")
    val week = S.request.map(_.path(3)).openOr("")
    val date = S.request.map(_.path(4)).openOr("")

    val records = WorkerStatisticsJSON(workerID, yearAndMonth, week, date)
    val maxValue = if (records.isEmpty) 0 else records.map(_.countQty).max
    val scale = Scale(0, maxValue, 10, 800)

    ".row" #> records.map { record =>
      val machineID = record.title
      val width = scale(record.countQty)
      val machineLevelBox = MachineLevel.find("machineID", machineID)
      val currentLevel = machineLevelBox.map(x => x.level(record.countQty)).openOr("無均線資料")
      val labelColor = currentLevel match {
        case "A" => "green"
        case "B" => "yellow"
        case "C" => "red"
        case "D" => "black"
        case _ => ""
      }

      val nextLevelBar = for {
        machineLevel <- machineLevelBox
        (nextLevelCount, percent) <- machineLevel.nextLevel(record.countQty)
      } yield {
        ".percent [data-percent]" #> percent &
        ".count *" #> nextLevelCount
      }

      ".machineID *" #> machineID &
      ".barText *" #> record.countQty &
      ".barRect [width]" #> width &
      ".barText [x]" #> (width + 10) &
      ".level *" #> currentLevel &
      ".level [class+]" #> labelColor &
      ".nextLevel" #> nextLevelBar

    }


  }

}


