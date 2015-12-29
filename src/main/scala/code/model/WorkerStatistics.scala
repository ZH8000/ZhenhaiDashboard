package code.model

import code.lib._

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

  /**
   *  檢查某個員工是否已被刪除
   *
   *  @param    workerMongoID     該員工在 MongoDB 中的 ID
   *  @return                     若已刪除則為 true，否則為 false
   */
  def isDeleted(workerMongoID: String) = Worker.find(workerMongoID).map(_.isDeleted.get).getOrElse(true)

  /**
   *  取得某筆資料的工班月份
   *
   *  例如若某筆資料的工班日期為 "2015-02-12"，則此函式會回傳 "2015-02" 這個字串
   *
   *  @param    record      MongoDB 中的 WorkerDaily 的 Record 物件
   *  @return               工班月份
   *
   */
  def getMonthlyTimestamp(record: WorkerDaily) = record.shiftDate.get.substring(0, 7)

  /**
   *  取得「依人員」總覽頁的資料
   *
   *  @return   每個員工到目前為止的總良品量的 List
   */
  def apply() = {
    val records = for {
      (workerMongoID, records) <- WorkerDaily.findAll.groupBy(_.workerMongoID.get) if !isDeleted(workerMongoID)
      worker <- Worker.find(workerMongoID)
      countQtys = records.map(_.countQty.get)
    } yield Record(workerMongoID, worker.workerID.get, worker.name.get, countQtys.sum)

    records.toList.sortWith(_.countQty > _.countQty)
  }

  /**
   *  取得「依人員」－＞「員工」頁面的資料
   *
   *  @param    workerMongoID     要查詢的員工的 MongoDB 中的 ID
   *  @return                     該員工各月份良品量的 List
   */
  def apply(workerMongoID: String) = {
    val records = for {
      (yearAndMonth, records) <- WorkerDaily.findAll("workerMongoID", workerMongoID).groupBy(getMonthlyTimestamp)
      countQtys = records.map(_.countQty.get)
    } yield TitleRecord(yearAndMonth, countQtys.sum)

    records.toList.sortWith(_.title > _.title)
  }

  /**
   *  取得「依人員」－＞「員工」－＞「月份」頁面的資料
   *
   *  @param    workerMongoID     要查詢的員工的 MongoDB 中的 ID
   *  @param    yearAndMonth      年月
   *  @return                     該員工該月份各週的良品量的 List
   */
  def apply(workerMongoID: String, yearAndMonth: String) = {
    val recordInMonth = WorkerDaily.findAll("workerMongoID", workerMongoID).filter(_.shiftDate.get.startsWith(yearAndMonth))
    val recordByWeeks = recordInMonth.groupBy(record => DateUtils.getWeek(record.shiftDate.get))
    val records = for {
      (week, records) <- recordByWeeks
      countQtys = records.map(_.countQty.get)
    } yield TitleRecord(week, countQtys.sum)

    records.toList.sortWith(_.title > _.title)
  }

  /**
   *  取得「依人員」－＞「員工」－＞「月份」－＞「週份」頁面的資料
   *
   *  @param    workerMongoID     要查詢的員工的 MongoDB 中的 ID
   *  @param    yearAndMonth      年月
   *  @param    week              週份
   *  @return                     該員工該週中各天的良品量的 List
   */
  def apply(workerMongoID: String, yearAndMonth: String, week: String) = {

    val recordInMonth = WorkerDaily
                          .findAll("workerMongoID", workerMongoID)
                          .filter(x => x.shiftDate.get.startsWith(yearAndMonth) && DateUtils.getWeek(x.shiftDate.get).toString == week)

    val recordByDate = recordInMonth.groupBy(_.shiftDate.get)
    val records = for {
      (date, records) <- recordByDate
      countQtys = records.map(_.countQty.get)
    } yield TitleRecord(date, countQtys.sum)

    records.toList.sortWith(_.title > _.title)

  }

  /**
   *  取得「依人員」－＞「員工」－＞「月份」－＞「週份」－＞「日期」頁面的資料
   *
   *  @param    workerMongoID     要查詢的員工的 MongoDB 中的 ID
   *  @param    yearAndMonth      年月
   *  @param    week              週份
   *  @param    date              日期
   *  @return                     該員日中各機台的良品量的 List
   */
  def apply(workerMongoID: String, yearAndMonth: String, week: String, date: String) = {

    val recordInDate = WorkerDaily
                          .findAll("workerMongoID", workerMongoID)
                          .filter(x => x.shiftDate.get == s"$yearAndMonth-$date")

    val records = recordInDate.map(x => TitleRecord(x.machineID.get, x.countQty.get))

    records.toList.sortWith(_.title < _.title)
  }

}
