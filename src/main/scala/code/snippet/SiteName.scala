package code.snippet

import net.liftweb.http.S
import net.liftweb.util.Helpers._

/**
 *  用來設定網頁右上角的廠區名稱的 Snippet
 */
class SiteName {

  def siteName = S.hostName match {
    case "221.4.141.146" | "192.168.20.200" | "xgback.zhenhai.com.tw" => "東莞台容"
    case "218.4.250.102" | "192.168.3.2" | "szback.zhenhai.com.tw" => "蘇州台容"
    case _ => "台灣電容器"
  }

  /**
   *  顯示廠區名稱
   */
  def render = {
    "*" #> siteName
  }
}
