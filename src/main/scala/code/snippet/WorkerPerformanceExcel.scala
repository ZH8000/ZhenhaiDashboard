package code.snippet

import code.model._
import net.liftweb.common._
import net.liftweb.http.{S, SHtml}
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Helpers._
import net.liftweb.json.JsonDSL._

/**
 *  用來顯示網站上的「網站管理」－＞「損耗金額設定」的 Snippet
 */
class ProductCostEditor {

  private var productCodeBox: Box[String] = Empty       // 用來儲存網頁下方表單傳入的「產品尺吋代碼」
  private var productTitleBox: Box[String] = Empty      // 用來儲存網頁下方表單傳入的「產品尺吋名稱」
  private var productCostBox: Box[String] = Empty   // 用來儲存網頁下方表單傳入的「損耗金額」

  /**
   *  在 ProductCost 資料表新增一筆資料
   */
  def createNewRecord() = {
    val newRecord = for {
      productCode    <- productCodeBox.filterNot(_.isEmpty)  ?~ "請輸入產品尺寸"
      productTitle   <- productTitleBox.filterNot(_.isEmpty)  ?~ "請輸入產品說明"
      productCost    <- productCostBox
      costInDecimal  <- tryo(BigDecimal(productCost))
    } yield {
      ProductCost.createRecord
                 .productCode(productCode)
                 .productTitle(productTitle)
                 .productCost(costInDecimal)
    }

    newRecord match {
      case Full(record) => 
        record.saveTheRecord match {
          case Full(savedRecord) => S.notice(s"成功儲存 ${savedRecord.productCode} 的損耗金額為 ${savedRecord.productCost}")
          case _ => S.error("無法儲存至資料庫，請稍候再試")
        }
      case _ => S.error("輸入的資料有誤，請檢查後重新輸入")
    }

  }

  /**
   *  檢查使用者輸入的是否是新的產品尺吋的資料並存入資料庫
   *
   *  如果使用者輸八的產品尺吋在資料庫已存在，則會顯示錯誤
   *  訊息。
   */
  def saveToDB() = {
    val oldRecord = for {
      productCode <- productCodeBox.filterNot(_.isEmpty)  ?~ "請輸入產品尺寸"
      record      <- ProductCost.find(("productCode" -> productCode))
    } yield record

    val isProductCodeEmpty = productCodeBox.filterNot(_.trim.isEmpty).isEmpty

    oldRecord match {
      case Full(record) => S.error("此產品尺寸資料已存在，請使用下面表格編輯")
      case Empty => createNewRecord()
      case _ if isProductCodeEmpty => S.error("請輸入產品尺吋代碼")
      case _ => S.error("無法取得資料庫資料，請稍候再試")
    }
  }

  /**
   *  用來綁定新增和修改產品損耗金額設定的表單
   */
  def editor = {
    ".productCode"  #> SHtml.onSubmit(x => productCodeBox = Full(x)) &
    ".productTitle" #> SHtml.onSubmit(x => productTitleBox = Full(x)) &
    ".productCost"  #> SHtml.onSubmit(x => productCostBox = Full(x)) &
    "type=submit" #> SHtml.submit("送出", saveToDB _)
  }

  /**
   *  顯示網頁下方的設定表單
   */
  def table = {
    
    /*
     *  資料庫內所有的機台產能設定
     */
    val dataList = ProductCost.findAll.toList.sortBy(record => record.productCode.get)

    /*
     *  刪除資料庫內的產品損耗金額設定
     *
     *  @param    productCode   產品尺吋
     */
    def deleteProductCost(productCode: String): JsCmd = {
      ProductCost.delete(productCode) match {
        case Full(true) => S.notice(s"已刪除 $productCode 的損耗金額設定")
        case _ => S.error(s"無法刪除 $productCode 的損耗金額設定，請稍候再試")
      }
      
      JsRaw(raw"$$('#row-$productCode').remove()")

    }

    /*
     *  更新資料庫內的產品尺吋損耗金額設定
     *
     *  @param    data      要更新哪個 ProductCost 記錄的損耗金額
     *  @param    value     新的損耗金額
     */
    def updateProductCost(data: ProductCost)(value: String): JsCmd = {
      val newValueHolder = tryo(BigDecimal(value))
      
      newValueHolder match { 
        case Full(newValue) =>
          data.productCost(newValue).saveTheRecord match {
            case Full(record) => S.notice(s"已更新 ${data.productCode} 的損耗金額為 $newValue")
            case _ => S.error(s"無法更新 ${data.productCode} 的資料，請稍候再試")
          }
        case _ => S.error(s"${data.productCode} 的金額有誤，請重新輸入")
      }
    }

    /*
     *  更新資料庫內的產品尺吋的尺吋說明
     *
     *  @param    data      要更新哪個 ProductCost 記錄的損耗金額
     *  @param    value     新的尺吋說明
     */
    def updateProductTitle(data: ProductCost)(value: String): JsCmd = {
      val newValueHolder = Full(value).filterNot(_.trim.isEmpty)
      
      newValueHolder match { 
        case Full(newValue) =>
          data.productTitle(newValue).saveTheRecord match {
            case Full(record) => S.notice(s"已更新 ${data.productCode} 的說明為 $newValue")
            case _ => S.error(s"無法更新 ${data.productCode} 的資料，請稍候再試")
          }
        case _ => S.error(s"${data.productCode} 的說明不能為空白，請重新輸入")
      }
    }

    ".dataRow" #> dataList.map { data =>
      val productCode = data.productCode.get
      val productTitle = data.productTitle.get

      ".dataRow [id]" #> s"row-$productCode" &
      ".productCode *" #> productCode &
      ".productTitle" #> SHtml.ajaxText(data.productTitle.get.toString, false, updateProductTitle(data)_) &
      ".productCost"  #> SHtml.ajaxText(data.productCost.get.toString, false, updateProductCost(data)_) &
      ".delete [onclick]" #> SHtml.onEventIf(s"確定要刪除的 $productCode 設定嗎？", s => deleteProductCost(productCode))
    }

  }

}


/**
 *  用來顯示網站上的「產量統計」－＞「人員效率」的 Snippet
 */
class WorkerPerformanceExcel {

  private var machineIDBox: Box[String] = Empty         // 用來儲存網頁下方表單傳入的「機台編號」
  private var productCodeBox: Box[String] = Empty       // 用來儲存網頁下方表單傳入的「產品尺吋」
  private var managementCountBox: Box[Long] = Empty     // 用來儲存網頁下方表單傳入的「日管理標準量」
  private var performanceCountBox: Box[Long] = Empty    // 用來儲存網頁下方表單傳入的「日效率標準量」

  /**
   *  用來顯示麵包屑和開啟 Excel 的按鈕
   */
  def detail = {
    val Array(_, _, yearString, monthString) = S.uri.drop(1).split("/")
    val year = f"${yearString.toInt}%02d"
    val month = f"${monthString.toInt}%02d"

    "#currentYearMonth *" #> f"$year-$month" &
    "#currentYearMonth [href]" #> s"/excel/workerPerformance/$year/$month" &
    "#downloadExcel [href]" #> s"/api/excel/workerPerformance/$year/$month.xls"
  }

  /**
   *  在 MachinePerformance 資料表新增一筆資料
   */
  def createNewRecord() = {
    val newRecord = for {
      machineID         <- machineIDBox.filterNot(_.isEmpty)    ?~ "請選擇機台編號"
      productCode       <- productCodeBox.filterNot(_.isEmpty)  ?~ "請輸入產品尺寸"
      managementCount   <- managementCountBox.filter(_ > 0)     ?~ "請輸入日管理標準量"
      performanceCount  <- performanceCountBox.filter(_ > 0)    ?~ "請輸入日效率標準量"
    } yield {
      MachinePerformance.createRecord
                        .machineID(machineID)
                        .productCode(productCode)
                        .managementCount(managementCount)
                        .performanceCount(performanceCount)
    }

    newRecord match {
      case Full(record) => 
        record.saveTheRecord match {
          case Full(savedRecord) => S.notice(s"成功儲存 ${savedRecord.machineID} 的 ${savedRecord.productCode} 尺寸資料")
          case _ => S.error("無法儲存至資料庫，請稍候再試")
        }
      case _ => S.error("輸入的資料有誤，請檢查後重新輸入")
    }

  }

  /**
   *  檢查使用者輸入的是否是新的 (機台編號，產品尺吋) 的資料並存入資料庫
   *
   *  如果使用者輸八的 (機台編號，產品尺吋) 在資料庫已存在，則會顯示錯誤
   *  訊息。
   */
  def saveToDB() = {
    val oldRecord = for {
      machineID   <- machineIDBox.filterNot(_.isEmpty)    ?~ "請選擇機台編號"
      productCode <- productCodeBox.filterNot(_.isEmpty)  ?~ "請輸入產品尺寸"
      record      <- MachinePerformance.find(machineID, productCode)
    } yield record

    oldRecord match {
      case Full(record) => S.error("此機台編號與產品尺寸資料已存在，請使用下面表格編輯")
      case Empty => createNewRecord()
      case _ => S.error("無法取得資料庫資料，請稍候再試")
    }
  }

  /**
   *  顯示網頁下方的設定表單
   */
  def table = {
    
    /*
     *  用來比較兩個 MachinePeformance 物件在排序時的順位
     *
     *  先比機台編號，若機台編號相同再用產品尺吋來排序
     *
     *  @return     當 record1 小於 record2 時為 true，否則為 false
     *
     */
    def machineIDThenProductCode(record1: MachinePerformance, record2: MachinePerformance) = {
      if (record1.machineID == record2.machineID) {
        record1.productCode.get < record2.productCode.get
      } else {
        record1.machineID.get < record2.machineID.get
      }
    }

    /*
     *  資料庫內所有的機台產能設定
     */
    val dataList = MachinePerformance.findAll.toList.sortWith(machineIDThenProductCode)

    /*
     *  更新資料庫內的機台產能的日效率標準量設定
     *
     *  @param    data      要更新哪個 MachinePerformance 的日效率標準量
     *  @param    value     新的日效率標準量的值
     */
    def updatePeformanceCount(data: MachinePerformance)(value: String): JsCmd = {
      asLong(value).foreach { newValue =>
        data.performanceCount(newValue).saveTheRecord match {
          case Full(record) => S.notice(s"已更新 ${data.machineID} / ${data.productCode} 的日效率標準量為 $newValue")
          case _ => S.error(s"無法更新 ${data.machineID} / ${data.productCode} 的資料，請稍候再試")
        }
      }
    }

    /*
     *  更新資料庫內的機台產能的日管理標準量設定
     *
     *  @param    data      要更新哪個 MachinePerformance 的日效率標準量
     *  @param    value     新的日效率標準量的值
     */
    def updateManagementCount(data: MachinePerformance)(value: String): JsCmd = {
      asLong(value).foreach { newValue =>
        data.managementCount(newValue).saveTheRecord match {
          case Full(record) => S.notice(s"已更新 ${data.machineID} / ${data.productCode} 的日管理標準量為 $newValue")
          case _ => S.error(s"無法更新 ${data.machineID} / ${data.productCode} 的資料，請稍候再試")
        }
      }
    }

    /*
     *  刪除資料庫內的機台產能設定
     *
     *  @param    machineID     機台編號
     *  @param    productCode   產品尺吋
     */
    def deletePeformanceCount(machineID: String, productCode: String): JsCmd = {
      MachinePerformance.delete(machineID, productCode) match {
        case Full(true) => S.notice(s"已刪除 $machineID 的 $productCode 設定")
        case _ => S.error(s"無法刪除 $machineID 的 $productCode 設定，請稍候再試")
      }
      
      JsRaw(raw"$$('#row-$machineID-$productCode').remove()")

    }

    ".dataRow" #> dataList.map { data =>
      val machineID = data.machineID.get
      val productCode = data.productCode.get

      ".dataRow [id]" #> s"row-$machineID-$productCode" &
      ".machineID *" #> data.machineID.get &
      ".productCode *" #> data.productCode.get &
      ".managementCount" #> SHtml.ajaxText(data.managementCount.get.toString, false, updateManagementCount(data)_) &
      ".performanceCount" #> SHtml.ajaxText(data.performanceCount.get.toString, false, updatePeformanceCount(data)_) &
      ".delete [onclick]" #> SHtml.onEventIf(s"確定要刪除 $machineID 的 $productCode 設定嗎？", s => deletePeformanceCount(machineID, productCode))

    }

  }

  /**
   *  用來綁定新增和修改機台產能的表單
   */
  def editor = {
    "#machineList" #> SHtml.onSubmit(x => machineIDBox = Full(x))  &
    ".productCode" #> SHtml.onSubmit(x => productCodeBox = Full(x)) &
    ".managementCount" #> SHtml.onSubmit(x => managementCountBox = asLong(x)) &
    ".performanceCount" #> SHtml.onSubmit(x => performanceCountBox = asLong(x)) &
    "type=submit" #> SHtml.submit("送出", saveToDB _)
  }
}

