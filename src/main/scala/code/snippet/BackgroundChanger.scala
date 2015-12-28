package code.snippet

import code.lib._

import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import net.liftweb.http.SessionVar
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._

import net.liftweb.util._

/**
 *  用來記錄使用者設定背景顏色的 Session 變數
 */
object BackgroundColor extends SessionVar[Option[String]](None)

/**
 *  用來顯示網頁上方的「背景顏色更換」對話框的 Snippet
 */
class BackgroundChanger {

  /**
   *  在 Client 端執行 JavaScript 來設定背景顏色，並將新的顏色記錄到 Session 變數中。
   *
   *  @param    color     新的背景顏色的 HEX 字串（以 # 號開頭的 #RRGGBB 的格式的字串）
   *  @return             要在瀏覽器上的 JavaScript
   */
  def setupColor(color: String): JsCmd = {

    // 若傳入的字串正確的話，記錄到 BackgroundColor 這個 Session 變數中
    if (color.startsWith("#") && color.size == 7) {
      BackgroundColor(Option(color))
    }

    // 在瀏覽器上執行這些 JavaScript 設定背景顏色
    JsRaw(s"""$$('body').css('background-color', '$color')""").cmd &
    JsRaw(s"""$$('html').css('background-color', '$color')""").cmd
  }

  /**
   *  設定網頁上方的「背景顏色」對話框
   */
  def render = {
    val bgColor = BackgroundColor.get.getOrElse("#CEECF5")
    "#bgColorJS *" #> OnLoad(JsRaw(s"""$$('body').css('background-color', '$bgColor'); $$('html').css('background-color', '$bgColor');""").cmd ) &
    "#bgColor [value]" #> bgColor &
    "#bgColor [onblur]" #> SHtml.onEvent(setupColor _)
  }
}


