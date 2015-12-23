package code.lib

import java.util.Calendar
import java.text.SimpleDateFormat

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

  /**
   *  取得 yyyy-MM-dd 此日期對應到該月的第幾週
   *
   *  @param    dateString   yyyy-MM-dd 格式的日期
   *  @return                此日期是在該月的哪一週
   */
  def getWeek(dateString: String) = {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    val date = dateFormatter.parse(dateString)
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    calendar.get(Calendar.WEEK_OF_MONTH)
  }


}

