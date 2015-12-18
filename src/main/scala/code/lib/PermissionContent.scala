package code.lib

/**
 *  這個 Singleton 物件用來設定網站上有哪些權限
 */
object PermissionContent extends Enumeration {

  type Permission = Value

  val ReportPhi              = Value("產量統計－依 Φ 別")
  val ReportCapacity         = Value("產量統計－依容量")
  val ReportMonthly          = Value("產量統計－月報表")
  val ReportDaily            = Value("產量統計－日報表")
  val ReportWorker           = Value("人員產量統計")
  val ReportTodayOrder       = Value("今日工單")
  val ReportOrderStatus      = Value("訂單狀態")
  val ReportMaintainLog      = Value("維修記錄")
  val ReportBug              = Value("事件分析")
  val ManagementWorker       = Value("網站管理－員工列表")
  val ManagementAlarm        = Value("網站管理－維修行事曆")
  val ManagementMachineLevel = Value("網站管理－機台生產均線")
  val ManagementAnnouncement = Value("網站管理－跑馬燈公告")
  val ManagementAccount      = Value("帳號管理")

  /**
   *  全部的權限的列表
   */
  val allPermissions = List(
    ReportPhi, ReportCapacity, ReportMonthly, ReportDaily,
    ReportWorker, ReportTodayOrder, ReportOrderStatus, ReportMaintainLog,
    ReportBug, ManagementWorker, ManagementAlarm, ManagementMachineLevel,
    ManagementAnnouncement, ManagementAccount
  )
}

