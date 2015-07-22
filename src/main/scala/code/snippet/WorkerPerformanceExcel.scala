package code.snippet

import code.model._
import code.lib._

import com.mongodb.casbah.Imports._
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmd
import net.liftweb.common._

import java.util.Date
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Calendar

import net.liftweb.http.S

class WorkerPerformanceExcel {

  private var machineIDBox: Box[String] = Empty
  private var productCodeBox: Box[String] = Empty
  private var managementCountBox: Box[Long] = Empty
  private var performanceCountBox: Box[Long] = Empty

  def detail = {
    val Array(_, _, yearString, monthString) = S.uri.drop(1).split("/")
    val year = f"${yearString.toInt}%02d"
    val month = f"${monthString.toInt}%02d"

    "#currentYearMonth *" #> f"$year-$month" &
    "#currentYearMonth [href]" #> s"/excel/workerPerformance/$year/$month" &
    "#downloadExcel [href]" #> s"/api/excel/workerPerformance/$year/$month"
  }

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

  def table = {
    
    def machineIDThenProductCode(record1: MachinePerformance, record2: MachinePerformance) = {
      if (record1.machineID == record2.machineID) {
        record1.productCode.get < record2.productCode.get
      } else {
        record1.machineID.get < record2.machineID.get
      }
    }

    val dataList = MachinePerformance.findAll.toList.sortWith(machineIDThenProductCode)

    def updatePeformanceCount(data: MachinePerformance)(value: String): JsCmd = {
      asLong(value).foreach { newValue =>
        data.performanceCount(newValue).saveTheRecord match {
          case Full(record) => S.notice(s"已更新 ${data.machineID} / ${data.productCode} 的日效率標準量為 $newValue")
          case _ => S.error(s"無法更新 ${data.machineID} / ${data.productCode} 的資料，請稍候再試")
        }
      }
    }

    def updateManagementCount(data: MachinePerformance)(value: String): JsCmd = {
      asLong(value).foreach { newValue =>
        data.managementCount(newValue).saveTheRecord match {
          case Full(record) => S.notice(s"已更新 ${data.machineID} / ${data.productCode} 的日管理標準量為 $newValue")
          case _ => S.error(s"無法更新 ${data.machineID} / ${data.productCode} 的資料，請稍候再試")
        }
      }
    }

    ".dataRow" #> dataList.map { data =>
      ".machineID *" #> data.machineID.get &
      ".productCode *" #> data.productCode.get &
      ".managementCount" #> SHtml.ajaxText(data.managementCount.get.toString, false, updateManagementCount(data)_) &
      ".performanceCount" #> SHtml.ajaxText(data.performanceCount.get.toString, false, updatePeformanceCount(data)_)
    }

  }

  def editor = {

    "#machineList" #> SHtml.onSubmit(x => machineIDBox = Full(x))  &
    ".productCode" #> SHtml.onSubmit(x => productCodeBox = Full(x)) &
    ".managementCount" #> SHtml.onSubmit(x => managementCountBox = asLong(x)) &
    ".performanceCount" #> SHtml.onSubmit(x => performanceCountBox = asLong(x)) &
    "type=submit" #> SHtml.submit("送出", saveToDB _)

  }
}

