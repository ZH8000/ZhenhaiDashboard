package code.util

import java.util.Calendar

object DateUtils {

  def getWeek(year: Int, month: Int, date: Int) = {
    val calendar = Calendar.getInstance
    calendar.set(year, month-1, date)
    calendar.get(Calendar.WEEK_OF_MONTH)
  }

}

