package code.lib

/**
 *  用來代表網頁上的麵包屑的其中一個 Step
 *
 *  @param      title     該 Step 的標題
 *  @param      isActive  是否為已經反白的狀態
 *  @param      link      該 Step 點下去時的連結
 */
case class Step(title: String, isActive: Boolean = false, link: Option[String] = None)
