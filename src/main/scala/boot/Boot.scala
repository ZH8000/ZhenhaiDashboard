package bootstrap.liftweb

import code.util._
import code.json._

import net.liftweb.http.LiftRules
import net.liftweb.http.Req
import net.liftweb.http.XHtmlInHtml5OutProperties

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.JsonResponse

object ProductHelper extends RestHelper {

  import net.liftweb.util.BasicTypesHelpers.AsInt

  serve( "api" / "json" / "total" prefix {
    case Nil Get req => 
      JsonResponse(ProductJSON.overview)
    case productName :: Nil Get req => 
      JsonResponse(ProductJSON.product(productName))
    case productName :: AsInt(year) :: AsInt(month) :: Nil Get req => 
      JsonResponse(ProductJSON.productMonth(productName, year, month))
    case productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: Nil Get req => 
      JsonResponse(ProductJSON.productMonthWeek(productName, year, month, week))
    case productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil Get req => 
      JsonResponse(ProductJSON.productMonthWeekDate(productName, year, month, week, date))
    case productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil Get req => 
      JsonResponse(ProductJSON.machineDetail(productName, year, month, week, date, machineID))
  })
  
}

class Boot 
{
  def boot 
  {
    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.addToPackages("code")
    LiftRules.htmlProperties.default.set { r: Req => new XHtmlInHtml5OutProperties(r.userAgent) }
    LiftRules.dispatch.append(ProductHelper)
  }
}
