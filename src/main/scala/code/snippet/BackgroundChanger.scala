package code.snippet

import code.lib._

import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import net.liftweb.http.SessionVar
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._

import net.liftweb.util._

object BackgroundColor extends SessionVar[Option[String]](None)

class BackgroundChanger {

  def setupColor(color: String): JsCmd = {

    if (color.startsWith("#") && color.size == 7) {
      BackgroundColor(Option(color))
    }

    JsRaw(s"""$$('body').css('background-color', '$color')""").cmd &
    JsRaw(s"""$$('html').css('background-color', '$color')""").cmd
  }

  def render = {
    val bgColor = BackgroundColor.get.getOrElse("#CEECF5")
    "#bgColorJS *" #> OnLoad(JsRaw(s"""$$('body').css('background-color', '$bgColor'); $$('html').css('background-color', '$bgColor');""").cmd ) &
    "#bgColor [value]" #> bgColor &
    "#bgColor [onblur]" #> SHtml.onEvent(setupColor _)
  }
}


