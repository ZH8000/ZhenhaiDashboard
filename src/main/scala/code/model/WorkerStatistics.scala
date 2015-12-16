package code.model

import code.model._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import java.util.Calendar
import java.text.SimpleDateFormat

/**
 *  用來取得「依人員」中的相關統資料的 Singleton 物件
 */
object WorkerStatistics {

  /**
   *  用來代表某個員工的產能
   *
   *  @param    workerMongoID     員工在 MongoDB 中的 Primary Key
   *  @param    workerID          員工在工廠的員工編號
   *  @param    name              員工姓名
   *  @param    countQty          員工的產能（累計良品數）
   */
  case class Record(workerMongoID: String, workerID: String, name: String, countQty: Long)

  /**
   *  用來表示最終的 JSON 輸出的其中一筆資料
   *
   *  @param    title     該筆資料的標題（第一欄）
   *  @param    countQty  該筆資料對應到的良品數
   */
  case class TitleRecord[T](title: T, countQty: Long)

  def isDeleted(workerMongoID: String) = getWorker(workerMongoID).map(_.isDeleted.get).getOrElse(true)

  def getMonthlyTimestamp(record: WorkerDaily) = record.shiftDate.get.substring(0, 7)

  def getWorker(workerMongoID: String) = Worker.find(workerMongoID)

  def getWeek(dateString: String) = {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    val date = dateFormatter.parse(dateString)
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    calendar.get(Calendar.WEEK_OF_MONTH)
  }

  def apply() = {
    val records = for {
      (workerMongoID, records) <- WorkerDaily.findAll.groupBy(_.workerMongoID.get) if !isDeleted(workerMongoID)
      worker <- getWorker(workerMongoID)
      countQtys = records.map(_.countQty.get)
    } yield Record(workerMongoID, worker.workerID.get, worker.name.get, countQtys.sum)

    records.toList.sortWith(_.countQty > _.countQty)
  }

  def apply(workerMongoID: String) = {
    val records = for {
      (yearAndMonth, records) <- WorkerDaily.findAll("workerMongoID", workerMongoID).groupBy(getMonthlyTimestamp)
      countQtys = records.map(_.countQty.get)
    } yield TitleRecord(yearAndMonth, countQtys.sum)

    records.toList.sortWith(_.title > _.title)
  }

  def apply(workerMongoID: String, yearAndMonth: String) = {
    val recordInMonth = WorkerDaily.findAll("workerMongoID", workerMongoID).filter(_.shiftDate.get.startsWith(yearAndMonth))
    val recordByWeeks = recordInMonth.groupBy(record => getWeek(record.shiftDate.get))
    val records = for {
      (week, records) <- recordByWeeks
      countQtys = records.map(_.countQty.get)
    } yield TitleRecord(week, countQtys.sum)

    records.toList.sortWith(_.title > _.title)
  }

  def apply(workerMongoID: String, yearAndMonth: String, week: String) = {

    val recordInMonth = WorkerDaily
                          .findAll("workerMongoID", workerMongoID)
                          .filter(x => x.shiftDate.get.startsWith(yearAndMonth) && getWeek(x.shiftDate.get).toString == week)

    val recordByDate = recordInMonth.groupBy(_.shiftDate.get)
    val records = for {
      (date, records) <- recordByDate
      countQtys = records.map(_.countQty.get)
    } yield TitleRecord(date, countQtys.sum)

    records.toList.sortWith(_.title > _.title)

  }

  def apply(workerMongoID: String, yearAndMonth: String, week: String, date: String) = {

    val recordInDate = WorkerDaily
                          .findAll("workerMongoID", workerMongoID)
                          .filter(x => x.shiftDate.get == s"$yearAndMonth-$date")

    val records = recordInDate.map(x => TitleRecord(x.machineID.get, x.countQty.get))

    records.toList.sortWith(_.title < _.title)
  }

}
