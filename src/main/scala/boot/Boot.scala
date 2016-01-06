package bootstrap.liftweb

import javax.mail.{Authenticator, PasswordAuthentication}

import code.model._
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import net.liftweb.common.Full
import net.liftweb.http.{LiftRules, Req, S, _}
import net.liftweb.mongodb.MongoDB
import net.liftweb.util.BasicTypesHelpers._
import net.liftweb.util.Props.RunModes
import net.liftweb.util.{DefaultConnectionIdentifier, Mailer, Props}

/**
 *  此物件用來設定 Lift 框架的基本參數和資料庫連線等
 */
class Boot 
{
  /**
   *  此函式用來確保 REST API 只有在使用者是登入的情況下
   *  可以存取。
   *
   *  只有在被 case 序述式定義到的 Req 物件（代表了 HTTP Reqeust），
   *  才能夠存取被這個函式守護的 REST API。
   */
  val ensureLogin: PartialFunction[Req, Unit] = {
    case req if User.isLoggedIn =>  // 送出 HTTP 的 Request 時使用者已登入 
  }

  /**
   *  客製化的 HTTP 錯誤頁面
   *
   *  此函式會到 webapp/template-hidden/ 資料夾中取得名稱為「錯誤代碼.html」
   *  的檔案做為該錯誤的 HTTP Request 的反回頁面。
   *
   *  @param    req       HTTP Reqeust 物件
   *  @param    code      HTTP 錯誤代碼
   *  @return             客製化的 HTTP 錯誤頁面
   */
  def errorPageResponse(req: Req, code: Int) = {
    val content = S.render(<lift:embed what={code.toString} />, req.request)
    XmlResponse(content.head, code, "text/html", req.cookies)
  }

  /**
   *  設定網頁伺服器啟動時的初始化設定
   */
  def boot 
  {
    // 設定 MongoDB 資料庫連線
    MongoDB.defineDb(DefaultConnectionIdentifier, new MongoClient(code.model.MongoDB.DatabaseURI), code.model.MongoDB.DatabaseName)

    // 強迫文字使用 UTF-8 編碼
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))     

    // 程式碼都位於 code 這個 package 下
    LiftRules.addToPackages("code")

    // 設定此網站的所有網址，與權限控管，詳見 SiteMapDefine.scala 檔案
    LiftRules.setSiteMap(SiteMapDefine.siteMap)

    // 設定 JsonRestAPI / CsvRestAPI / ExcelRestAPI 三個 REST API 物件，
    // 且這三個 REST API 物件都需要在使用者是登入的情況下才能用。
    //
    // 此處的 REST API 主要為網頁上產生 JSON / CSV / Excel 檔所使用。
    LiftRules.dispatch.append(ensureLogin guard JsonRestAPI)
    LiftRules.dispatch.append(ensureLogin guard CsvRestAPI)
    LiftRules.dispatch.append(ensureLogin guard ExcelRestAPI)

    // 設定 MachineStatusRestAPI，這個 API 是蘇州廠新版的 RaspberryPi
    // 機上盒需要從 Server 上取回資料時，會透過這個 REST API 物件中
    // 定義的網址來取得
    LiftRules.dispatch.append(MachineStatusRestAPI)

    LiftRules.explicitlyParsedSuffixes += "csv"

    // 設定 404 NotFound 的客制化頁面
    LiftRules.uriNotFound.prepend({
      case (request, failure) => NotFoundAsResponse(errorPageResponse(request, 404))
    })

    // 設定如何定義「使用者已登入」的測試條件
    //
    // 此處的條件為若 User.CurrentUser 這個 Session 變數若已被設定（非空值），
    // 則視為使用者已登入。
    LiftRules.loggedInTest = Full(() => !User.CurrentUser.get.isEmpty)

    // 設定當網站程式發發生錯誤時如何處理
    //
    // 此處的設定為若在 Production 模式下，若程式發生錯誤，則將錯誤的 Exception
    // 記錄到 Jetty 的 log 檔中，並且顯示 webapp/template-hidden/500.html 的
    // HTML 模板。
    LiftRules.exceptionHandler.prepend {
      case (runMode, req, exception) if runMode == RunModes.Production =>
        println(runMode)
        println(s"========== ${req.uri} =============")
        println(s"DateTime: ${new java.util.Date}")
        exception.printStackTrace()
        println(s"===================================")
        errorPageResponse(req, 500)
    }

    // 設定郵件伺服器的認證方式
    //
    // 設定檔位於 src/main/resource/production.default.props 這個檔案，
    // 設定檔的格式如下：
    //
    // mail.smtp.host=smtp.example.org
    // mail.user=使用者名稱
    // mail.password=使用者密碼
    //
    Mailer.authenticator = for {
      user <- Props.get("mail.user")        // 若設定檔中有設定 mail.user 變數，則指定到 user 變數並繼續
      pass <- Props.get("mail.password")    // 若設定檔中有設定 mail.password 變數，則指定到 pass 變數並反回下面 new 出來的物件
    } yield new Authenticator {
      override def getPasswordAuthentication = new PasswordAuthentication(user,pass)
    }
  }
}
