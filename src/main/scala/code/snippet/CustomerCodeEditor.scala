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
class CustomerCodeEditor {

  private var customerCodeBox: Box[String] = Empty       // 用來儲存網頁下方表單傳入的「客戶代碼」
  private var customerTitleBox: Box[String] = Empty      // 用來儲存網頁下方表單傳入的「客戶名稱」

  /**
   *  在 CustomerCode 資料表新增一筆資料
   */
  def createNewRecord() = {
    val newRecord = for {
      customerCode   <- customerCodeBox.filterNot(_.isEmpty)  ?~ "請輸入客戶代碼"
      customerTitle   <- customerTitleBox.filterNot(_.isEmpty)  ?~ "請輸入客戶名稱"
    } yield {
      CustomerCode.createRecord
                 .customerCode(customerCode)
                 .customerTitle(customerTitle)
    }

    newRecord match {
      case Full(record) => 
        record.saveTheRecord match {
          case Full(savedRecord) => S.notice(s"成功儲存 ${savedRecord.customerCode} 的名稱為 ${savedRecord.customerTitle}")
          case _ => S.error("無法儲存至資料庫，請稍候再試")
        }
      case _ => S.error("輸入的資料有誤，請檢查後重新輸入")
    }

  }

  /**
   *  檢查使用者輸入的是否是新的客戶代碼的資料並存入資料庫
   *
   *  如果使用者輸八的客戶代碼在資料庫已存在，則會顯示錯誤
   *  訊息。
   */
  def saveToDB() = {
    val oldRecord = for {
      customerCode <- customerCodeBox.filterNot(_.isEmpty)  ?~ "請輸入客戶代碼"
      record       <- CustomerCode.find(("customerCode" -> customerCode))
    } yield record

    val isProductCodeEmpty = customerCodeBox.filterNot(_.trim.isEmpty).isEmpty

    oldRecord match {
      case Full(record) => S.error("此客戶代碼已存在，請使用下面表格編輯")
      case Empty => createNewRecord()
      case _ if isProductCodeEmpty => S.error("請輸入客戶代碼")
      case _ => S.error("無法取得資料庫資料，請稍候再試")
    }
  }

  /**
   *  用來綁定新增和修改產品損耗金額設定的表單
   */
  def editor = {
    ".customerCode"  #> SHtml.onSubmit(x => customerCodeBox = Full(x)) &
    ".customerTitle" #> SHtml.onSubmit(x => customerTitleBox = Full(x)) &
    "type=submit" #> SHtml.submit("送出", saveToDB _)
  }

  /**
   *  顯示網頁下方的設定表單
   */
  def table = {
    
    /*
     *  資料庫內所有的客戶代碼
     */
    val dataList = CustomerCode.findAll.toList.sortBy(record => record.customerCode.get)

    /*
     *  刪除資料庫內的客戶代碼的設定
     *
     *  @param    customerCode    客戶代碼
     */
    def deleteCustomerCode(customerCode: String): JsCmd = {
      CustomerCode.delete(customerCode) match {
        case Full(true) => S.notice(s"已刪除 $customerCode 的客戶代碼設定")
        case _ => S.error(s"無法刪除 $customerCode 的客戶代碼設定，請稍候再試")
      }
      
      JsRaw(raw"$$('#row-$customerCode').remove()")

    }

    /*
     *  更新資料庫內的客戶代碼的客戶名稱
     *
     *  @param    data      要更新哪個 CustomerCode 記錄的損耗金額
     *  @param    value     新的客戶名稱
     */
    def updateCustomerTitle(data: CustomerCode)(value: String): JsCmd = {
      val newValueHolder = Full(value).filterNot(_.trim.isEmpty)
      
      newValueHolder match { 
        case Full(newValue) =>
          data.customerTitle(newValue).saveTheRecord match {
            case Full(record) => S.notice(s"已更新 ${data.customerCode} 的名稱為 $newValue")
            case _ => S.error(s"無法更新 ${data.customerCode} 的資料，請稍候再試")
          }
        case _ => S.error(s"${data.customerCode} 的說明不能為空白，請重新輸入")
      }
    }

    ".dataRow" #> dataList.map { data =>
      val customerCode = data.customerCode.get
      val customerTitle = data.customerTitle.get

      ".dataRow [id]" #> s"row-$customerCode" &
      ".customerCode *" #> customerCode &
      ".customerTitle" #> SHtml.ajaxText(data.customerTitle.get.toString, false, updateCustomerTitle(data)_) &
      ".delete [onclick]" #> SHtml.onEventIf(s"確定要刪除的 $customerCode 設定嗎？", s => deleteCustomerCode(customerCode))
    }

  }

}

