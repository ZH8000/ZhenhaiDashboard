package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.http.OutputStreamResponse
import net.liftweb.sitemap.Loc._
import net.liftweb.common._

/**
 *  維修代號列表
 */
object MaintenanceCode {

  val mapping = Map(
    // 加締
    1 -> Map(
      // 維修代碼     說明
          1       -> "一、CP線供料", 
          2       -> "二、CP線測試、移轉", 
          3       -> "三、加締部",
          4       -> "四、卷取部", 
          5       -> "五、膠帶部", 
          6       -> "六、TP部",
          7       -> "七、卷取卷盤", 
          8       -> "八、主傳動部", 
          9       -> "九、電路維修"
    ),
    // 組立
    2 -> Map(
      // 維修代碼     說明
          1       -> "一、含浸部", 
          2       -> "二、橡皮部", 
          3       -> "三、插入、引拔部",
          4       -> "四、外殼部", 
          5       -> "五、素子壓入外殼部", 
          6       -> "六、封口部",
          7       -> "七、仕上部、套管部", 
          8       -> "八、主傳動部", 
          9       -> "九、電路維修"
    ),
    // 老化
    3 -> Map(
      // 維修代碼     說明
          1       -> "一、入料改車", 
          2       -> "二、插入部", 
          3       -> "三、砲臺調整",
          4       -> "四、爐內", 
          5       -> "五、電配箱、板面故障燈", 
          6       -> "六、溫度故障",
          7       -> "七、出料改車", 
          8       -> "八、電路維修", 
          9       -> "未定義"
    ),
    // 選別
    4 -> Map(
      1 -> "未定義", 2 -> "未定義", 3 -> "未定義",
      4 -> "未定義", 5 -> "未定義", 6 -> "未定義",
      7 -> "未定義", 8 -> "未定義", 9 -> "未定義"
    ),
    // 加工切腳
    5 -> Map(
      // 維修代碼     說明
          1       -> "一、改車", 
          2       -> "二、進料部調整", 
          3       -> "三、正常品排料維修",
          4       -> "四、傳動部維修", 
          5       -> "五、氣壓系統維修", 
          6       -> "六、移送部維修",
          7       -> "七、品質維修", 
          8       -> "八、電路維修", 
          9       -> "末定義"
    )
  )

  /**
   *  輸出 PDF 檔案
   */
  def barcodePDF = new EarlyResponse(() => 
    Full(OutputStreamResponse(MaintenanceCodePDF.createPDF _, -1, List("Content-Type" -> "application/pdf")))
  )

}

