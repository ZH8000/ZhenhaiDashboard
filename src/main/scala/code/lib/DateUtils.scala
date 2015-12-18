package code.lib

import java.util.Calendar

/**
 *  用來處理日期的轉換的工具程式
 */
object DateUtils {

  /**
   *  取得某個日期對應到該月的第幾週
   *
   *  @param    year      年
   *  @param    month     月
   *  @param    date      日
   *  @return             此日期是在此月的哪一週
   */
  def getWeek(year: Int, month: Int, date: Int) = {
    val calendar = Calendar.getInstance
    calendar.set(year, month-1, date)
    calendar.get(Calendar.WEEK_OF_MONTH)
  }

}

