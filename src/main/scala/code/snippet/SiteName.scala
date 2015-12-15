package code.snippet

import net.liftweb.http.S
import net.liftweb.util.Helpers._

class SiteName {

  def siteName = S.hostName match {
    case "221.4.141.146" | "192.168.20.200" | "xgback.zhenhai.com.tw" => "謝崗廠"
    case "218.4.250.102" | "192.168.3.2" | "szback.zhenhai.com.tw" => "蘇州廠"
    case _ => "台灣電容器"
  }

  def render = {
    "*" #> siteName
  }
}
