package code.json

import code.model._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import java.util.Calendar
import java.text.SimpleDateFormat

object WorkerStatisticsJSON {

  case class Record(workerMongoID: String, workerID: String, name: String, countQty: Long)
  case class TitleRecord[T](title: T, countQty: Long)

  def isDeleted(workerMongoID: String) = getWorker(workerMongoID).map(_.isDeleted.get).getOrElse(true)
  def getMonthlyTimestamp(record: WorkerDaily) = record.timestamp.get.substring(0, 7)
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
    val recordInMonth = WorkerDaily.findAll("workerMongoID", workerMongoID).filter(_.timestamp.get.startsWith(yearAndMonth))
    val recordByWeeks = recordInMonth.groupBy(record => getWeek(record.timestamp.get))
    val records = for {
      (week, records) <- recordByWeeks
      countQtys = records.map(_.countQty.get)
    } yield TitleRecord(week, countQtys.sum)

    records.toList.sortWith(_.title > _.title)
  }

  def apply(workerMongoID: String, yearAndMonth: String, week: String) = {

    val recordInMonth = WorkerDaily
                          .findAll("workerMongoID", workerMongoID)
                          .filter(x => x.timestamp.get.startsWith(yearAndMonth) && getWeek(x.timestamp.get).toString == week)

    val recordByDate = recordInMonth.groupBy(_.timestamp.get)
    val records = for {
      (date, records) <- recordByDate
      countQtys = records.map(_.countQty.get)
    } yield TitleRecord(date, countQtys.sum)

    records.toList.sortWith(_.title > _.title)

  }

  def apply(workerMongoID: String, yearAndMonth: String, week: String, date: String) = {

    val recordInDate = WorkerDaily
                          .findAll("workerMongoID", workerMongoID)
                          .filter(x => x.timestamp.get == s"$yearAndMonth-$date")

    val records = recordInDate.map(x => TitleRecord(x.machineID.get, x.countQty.get))

    records.toList.sortWith(_.title < _.title)
  }

}
