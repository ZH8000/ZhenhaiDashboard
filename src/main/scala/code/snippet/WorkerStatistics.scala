package code.snippet

import code.lib._
import code.model._
import net.liftweb.http.S
import net.liftweb.util.Helpers._

import scala.xml.NodeSeq

/**
 *  用來顯示網頁上「依人員」的頁面的 Snippet
 */
class WorkerStatistics {

  /**
   *  用來設定第二層（年月列表）的麵包屑
   */
  def workerSteps = {
    val workerID = S.request.map(_.path(1)).openOr("")
    val name = Worker.find(workerID).map(_.name.get).getOrElse("查無此人")

    ".workerName *" #> name &
    ".workerName [href]" #> s"/workers/$workerID"
  }

  /**
   *  用來設定第三層（週列表）的麵包屑
   */
  def weeklySteps = {
    val workerID = S.request.map(_.path(1)).openOr("")
    val yearAndMonth = S.request.map(_.path(2)).openOr("")
    val name = Worker.find(workerID).map(_.name.get).getOrElse("查無此人")

    ".workerName *" #> name &
    ".workerName [href]" #> s"/workers/$workerID" &
    ".yearAndMonth *" #> yearAndMonth &
    ".yearAndMonth [href]" #> s"/workers/$workerID/$yearAndMonth"
  }

  /**
   *  用來設定第四層（日期列表）的麵包屑
   */
  def dailySteps = {
    val workerID = S.request.map(_.path(1)).openOr("")
    val yearAndMonth = S.request.map(_.path(2)).openOr("")
    val week = S.request.map(_.path(3)).openOr("")
    val name = Worker.find(workerID).map(_.name.get).getOrElse("查無此人")

    ".workerName *" #> name &
    ".workerName [href]" #> s"/workers/$workerID" &
    ".yearAndMonth *" #> yearAndMonth &
    ".yearAndMonth [href]" #> s"/workers/$workerID/$yearAndMonth" &
    ".week *" #> s"第 $week 週" &
    ".week [href]" #> s"/workers/$workerID/$yearAndMonth/$week"
  }

  /**
   *  用來設定第五層（機台列表）的麵包屑
   */
  def detailSteps = {
    val workerID = S.request.map(_.path(1)).openOr("")
    val yearAndMonth = S.request.map(_.path(2)).openOr("")
    val week = S.request.map(_.path(3)).openOr("")
    val date = S.request.map(_.path(4)).openOr("")
    val name = Worker.find(workerID).map(_.name.get).getOrElse("查無此人")


    ".workerName *" #> name &
    ".workerName [href]" #> s"/workers/$workerID" &
    ".yearAndMonth *" #> yearAndMonth &
    ".yearAndMonth [href]" #> s"/workers/$workerID/$yearAndMonth" &
    ".week *" #> s"第 $week 週" &
    ".week [href]" #> s"/workers/$workerID/$yearAndMonth/$week" &
    ".date *" #> s"$date 日" &
    ".date [href]" #> s"/workers/$workerID/$yearAndMonth/$week/$date"
  }

  /**
   *  顯示錯誤訊息
   */
  def showErrorBox(message: String) = {
    S.error(message)
    "table" #> NodeSeq.Empty &
    "#csvURL" #> NodeSeq.Empty
  }

  /**
   *  用來設定第一層（員工列表）的長條圖表格
   */
  def overviewTable = {

    val records = WorkerStatistics()
    val maxValue = if (records.isEmpty) 0 else records.map(_.countQty).max
    val scale = Scale(0, maxValue, 10, 300)

    records.isEmpty match {
      case true  => showErrorBox("查無資料")
      case false =>

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
  }

  /**
   *  用來設定第二層（年月份）的長條圖表格
   */
  def workerTable = {

    val workerID = S.request.map(_.path(1)).filterNot(_ == "index").openOr("")
    val records = WorkerStatistics(workerID)
    val maxValue = if (records.isEmpty) 0 else records.map(_.countQty).max
    val scale = Scale(0, maxValue, 10, 300)

    records.isEmpty match {
      case true  => showErrorBox("查無資料")
      case false =>

        "#csvURL [href]" #> s"/api/csv/workers/$workerID.csv" &
        ".row" #> records.map { record =>
          val width = scale(record.countQty)

          ".timestamp *" #> record.title &
          ".barText *" #> record.countQty &
          ".barRect [width]" #> width &
          ".barText [x]" #> (width + 10) &
          ".barRect [onclick]" #> s"window.location='/workers/$workerID/${record.title}'"
        }
    }
  }

  /**
   *  用來設定第三層（週列表）的長條圖表格
   */
  def weeklyTable = {

    val workerID = S.request.map(_.path(1)).filterNot(_ == "index").openOr("")
    val yearAndMonth = S.request.map(_.path(2)).filterNot(_ == "index").openOr("")
    val records = WorkerStatistics(workerID, yearAndMonth)
    val maxValue = if (records.isEmpty) 0 else records.map(_.countQty).max
    val scale = Scale(0, maxValue, 10, 300)

    records.isEmpty match {
      case true  => showErrorBox("查無資料")
      case false =>

        "#csvURL [href]" #> s"/api/csv/workers/$workerID/$yearAndMonth.csv" &
        ".row" #> records.map { record =>
          val width = scale(record.countQty)
          ".timestamp *" #> s"第 ${record.title} 週" &
          ".barText *" #> record.countQty &
          ".barRect [width]" #> width &
          ".barText [x]" #> (width + 10) &
          ".barRect [onclick]" #> s"window.location='/workers/$workerID/$yearAndMonth/${record.title}'"
        }
    }

  }

  /**
   *  用來設定第四層（日期列表）的長條圖表格
   */
  def dailyTable = {

    val workerID = S.request.map(_.path(1)).filterNot(_ == "index").openOr("")
    val yearAndMonth = S.request.map(_.path(2)).filterNot(_ == "index").openOr("")
    val week = S.request.map(_.path(3)).filterNot(_ == "index").openOr("")
    val records = WorkerStatistics(workerID, yearAndMonth, week)

    val maxValue = if (records.isEmpty) 0 else records.map(_.countQty).max
    val scale = Scale(0, maxValue, 10, 300)

    records.isEmpty match {
      case true  => showErrorBox("查無資料")
      case false =>

        "#csvURL [href]" #> s"/api/csv/workers/$workerID/$yearAndMonth/$week.csv" &
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

  }

  /**
   *  用來設定第五層（機台列表）的長條圖表格
   */
  def detailTable = {
    val workerID = S.request.map(_.path(1)).filterNot(_ == "index").openOr("")
    val yearAndMonth = S.request.map(_.path(2)).filterNot(_ == "index").openOr("")
    val week = S.request.map(_.path(3)).filterNot(_ == "index").openOr("")
    val date = S.request.map(_.path(4)).filterNot(_ == "index").openOr("")

    val records = WorkerStatistics(workerID, yearAndMonth, week, date)
    val maxValue = if (records.isEmpty) 0 else records.map(_.countQty).max
    val scale = Scale(0, maxValue, 10, 300)

    records.isEmpty match {
      case true  => showErrorBox("查無資料")
      case false =>

        "#csvURL [href]" #> s"/api/csv/workers/$workerID/$yearAndMonth/$week/$date.csv" &
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
}


