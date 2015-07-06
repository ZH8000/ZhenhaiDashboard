package code.lib

case class MachineInfo(ip: String, machineID: String, machineType: Int, model: String, note: Option[String])

object MachineInfo {

  val maintenanceCodes = List("1", "2", "3", "4", "5", "6", "7", "8")
  val defactDescription = Map(
    0 -> "短路不良(計數)",
    1 -> "素子卷取不良(計數)",
    2 -> "胶帯贴付不良(計數)",
    3 -> "素子导线棒不良(計數)",
    4 -> "负导线棒不良",
    5 -> "正导线棒不良",
    6 -> "卷针定位不良",
    7 -> "负电解纸无",
    8 -> "正电解纸无",
    9 -> "正导线棒测试（計數，非錯誤）",
    10 -> "负导线棒测试（計數，非錯誤）",
    11 -> "无胶带",
    12 -> "TP轮无定位",
    13 -> "TP纸带无",
    14 -> "TP胶带无",
    15 -> "无正箔",
    16 -> "无负箔",
    17 -> "无正导线棒",
    18 -> "无负导线棒",
    19 -> "正加締前斷箔",
    20 -> "正加缔后断箔",
    21 -> "負加締前斷箔",
    22 -> "负加缔后断箔",
    23 -> "正铝箔供给",
    24 -> "负铝箔供给断箔",
    25 -> "负铝箔供给",
    26 -> "正铝箔供给断箔",
    27 -> "素子卷取不良",
    28 -> "短路不良",
    29 -> "胶带贴付不良",
    30 -> "素子過大",
    31 -> "素子排料",
    32 -> "墊紙無料",
    33 -> "預沖孔",
    34 -> "膠帶座未退",
    101 -> "不良品A",
    102 -> "不良品B",
    103 -> "不良品C",
    104 -> "不良品D",
    105 -> "真空A",
    106 -> "真空B",
    107 -> "液面A",
    108 -> "液面B",
    109 -> "液面  B2",
    110 -> "外殼有無",
    111 -> "素子有無",
    112 -> "橡皮有無",
    113 -> "含浸槽上下",
    114 -> "素子殘留",
    115 -> "素子位置",
    116 -> "TP位置不良",
    117 -> "前段差斷帶",
    118 -> "後段差故障",
    119 -> "露白測試",
    120 -> "外殼測試",
    121 -> "橡皮測試",
    122 -> "套管打折",
    123 -> "套管有無",
    124 -> "插入",
    125 -> "封口",
    126 -> "檢出",
    201 -> "開路不良計數",
    202 -> "短路不良計數",
    203 -> "LC不良計數",
    204 -> "LC2不良計數",
    205 -> "容量不良計數",
    206 -> "損失不良計數",
    207 -> "重測不良計數",
    208 -> "極性不良"
  )

  val machineList = List(
    MachineInfo("192.168.10.1",   "E01",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.2",   "G01",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.3",   "E03",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.4",   "G03",    2, "FTO-2200",    None),
    MachineInfo("192.168.10.5",   "E04",    1, "HSW-800",     None),
    MachineInfo("192.168.10.6",   "G04",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.7",   "E05",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.8",   "G05",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.9",   "E06",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.10",  "G06",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.11",  "E07",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.12",  "G07",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.13",  "E08",    1, "HSW-800",     None),
    MachineInfo("192.168.10.14",  "G08",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.15",  "E09",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.16",  "G09",    2, "FTO-2200A",   Some("未安裝")),
    MachineInfo("192.168.10.17",  "E10",    1, "HSW-880",     Some("未安裝")),
    MachineInfo("192.168.10.18",  "G10",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.19",  "E11",    1, "TSW-168T",    Some("未安裝")),
    MachineInfo("192.168.10.20",  "E12",    1, "HSW-800",     None),
    MachineInfo("192.168.10.21",  "G12",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.22",  "E13",    1, "HSW-800",     None),
    MachineInfo("192.168.10.23",  "G13",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.24",  "E15",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.25",  "G15",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.26",  "E16",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.27",  "G16",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.28",  "E17",    1, "HSW-800",     None),
    MachineInfo("192.168.10.29",  "G17",    2, "FTO-2500",    Some("未安裝")),
    MachineInfo("192.168.10.30",  "E18",    1, "HSW-160A",    Some("未安裝")),
    MachineInfo("192.168.10.31",  "G18",    2, "FTO-3100",    None),
    MachineInfo("192.168.10.32",  "E37",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.33",  "G37",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.34",  "E38",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.35",  "G38",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.36",  "E39",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.37",  "G39",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.38",  "E40",    1, "HSW-800",     None),
    MachineInfo("192.168.10.39",  "G40",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.40",  "E41",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.41",  "G41",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.42",  "E42",    1, "HSW-800",     None),
    MachineInfo("192.168.10.43",  "G42",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.44",  "E43",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.45",  "G43",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.46",  "E44",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.47",  "G44",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.48",  "E45",    1, "TSW-168T",    None),
    MachineInfo("192.168.10.49",  "G45",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.50",  "G46",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.51",  "E47",    1, "HSW-250",     Some("未安裝")),
    MachineInfo("192.168.10.52",  "G47",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.53",  "E49-K1", 1, "KAIDO",       Some("未安裝")),
    MachineInfo("192.168.10.54",  "E49-K2", 1, "KAIDO",       Some("未安裝")),
    MachineInfo("192.168.10.55",  "E49-S1", 1, "SPH-3000",    Some("未安裝")),
    MachineInfo("192.168.10.56",  "G49",    2, "SBB-1800",    Some("未安裝")),
    MachineInfo("192.168.10.57",  "G49",    2, "SBB-1800",    Some("未安裝")),
    MachineInfo("192.168.10.58",  "E50",    1, "HSW-800",     None),
    MachineInfo("192.168.10.59",  "G50",    2, "TIA-2600",     None),
    MachineInfo("192.168.10.60",  "E51",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.61",  "G51",    2, "TIA-2600",    None),
    MachineInfo("192.168.10.62",  "E52",    1, "TSW-168",     Some("封裝機器")),
    MachineInfo("192.168.10.63",  "G52",    2, "SBB-1800",    Some("未安裝")),
    MachineInfo("192.168.10.64",  "G52",    2, "SBB-1800",    Some("未安裝")),
    MachineInfo("192.168.10.65",  "E53",    1, "TSW-168T",    None),
    MachineInfo("192.168.10.66",  "G53",    2, "TIA-2600",    None),
    MachineInfo("192.168.10.67",  "E72",    1, "TSW-168T",    Some("封裝機器")),
    MachineInfo("192.168.10.68",  "G72",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.69",  "E99",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.70",  "G99",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.71",  "E73",    1, "HSW-500",     None),
    MachineInfo("192.168.10.72",  "G73",    2, "FTO-3000",    None),
    MachineInfo("192.168.10.73",  "E74",    1, "HSW-800",     None),
    MachineInfo("192.168.10.74",  "G74",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.75",  "E75",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.76",  "G75",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.77",  "E76",    1, "HSW-800",     None),
    MachineInfo("192.168.10.78",  "G76",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.79",  "E77",    1, "HSW-800",     None),
    MachineInfo("192.168.10.80",  "G77",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.81",  "E78",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.82",  "G78",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.83",  "E79",    1, "HSW-800",     None),
    MachineInfo("192.168.10.84",  "G79",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.85",  "E80",    1, "HSW-800",     None),
    MachineInfo("192.168.10.86",  "G80",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.87",  "E81",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.88",  "G81",    2, "FTO-3000",    None),
    MachineInfo("192.168.10.89",  "E82",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.90",  "G82",    2, "FTO-3000",    Some("未安裝")),
    MachineInfo("192.168.10.91",  "E83",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.92",  "G83",    2, "TIA-2600",    None),
    MachineInfo("192.168.10.93",  "E84",    1, "HSW-880B",    Some("未安裝")),
    MachineInfo("192.168.10.94",  "G84",    2, "FTO-3000",    Some("未安裝")),
    MachineInfo("192.168.10.95",  "E85",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.96",  "G85",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.97",  "E86",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.98",  "G86",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.99",  "E87",    1, "HSW-160A",    Some("未安裝")),
    MachineInfo("192.168.10.100", "E88",    1, "HSW-160B",    Some("未安裝")),
    MachineInfo("192.168.10.101", "E90",    1, "HSW-250",     Some("未安裝")),
    MachineInfo("192.168.10.102", "E91",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.103", "G91",    2, "TIA-2600",    None),
    MachineInfo("192.168.10.104", "E110",   1, "TSW-100T",    None),
    MachineInfo("192.168.10.105", "G93",    2, "FTO-2200A",   Some("未安裝")),
    MachineInfo("192.168.10.106", "G94",    2, "FTO-3100",    None),
    MachineInfo("192.168.10.107", "G95",    2, "FTO-2700",    None),
    MachineInfo("192.168.10.108", "G97",    2, "FTO-3100",    None),
    MachineInfo("192.168.10.109", "E19",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.110", "G19",    2, "FTO-2700",    None),
    MachineInfo("192.168.10.111", "E20",    1, "HSW-160A",    Some("未安裝")),
    MachineInfo("192.168.10.112", "G20",    2, "FTO-2700",    None),
    MachineInfo("192.168.10.113", "E21",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.114", "G21",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.115", "E22",    1, "HSW-800",     None),
    MachineInfo("192.168.10.116", "G22",    2, "FTO-2200",    None),
    MachineInfo("192.168.10.117", "E23",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.118", "G23",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.119", "E24",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.120", "G24",    2, "FTO-2200",    None),
    MachineInfo("192.168.10.121", "E25",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.122", "G25",    2, "FTO-2200",    None),
    MachineInfo("192.168.10.123", "E26",    1, "HSW-500",     None),
    MachineInfo("192.168.10.124", "G26",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.125", "E27",    1, "HSW-800",     None),
    MachineInfo("192.168.10.126", "G27",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.127", "E28",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.128", "G28",    2, "FTO-2700",    None),
    MachineInfo("192.168.10.129", "E29",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.130", "G29",    2, "TIA-2600",    None),
    MachineInfo("192.168.10.131", "E30",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.132", "G30",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.133", "E31",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.134", "G31",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.135", "E32",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.136", "G32",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.137", "E33",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.138", "G33",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.139", "E34",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.140", "G34",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.141", "E35",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.142", "G35",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.143", "E36",    1, "HSW-800",     None),
    MachineInfo("192.168.10.144", "G36",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.145", "E54",    1, "HSW-800",     None),
    MachineInfo("192.168.10.146", "G54",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.147", "E55",    1, "HSW-800",     None),
    MachineInfo("192.168.10.148", "G55",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.149", "E56",    1, "HSW-800",     None),
    MachineInfo("192.168.10.150", "G56",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.151", "E57",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.152", "G57",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.153", "E58",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.154", "G58",    2, "TIA-2600",    None),
    MachineInfo("192.168.10.155", "E59",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.156", "G59",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.157", "E60",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.158", "G60",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.159", "E61",    1, "HSW-800",     None),
    MachineInfo("192.168.10.160", "G61",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.161", "E62",    1, "TSW-168T",    None),
    MachineInfo("192.168.10.162", "G62",    2, "TIA-2600",    None),
    MachineInfo("192.168.10.163", "E63",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.164", "G63",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.165", "E64",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.166", "G64",    2, "TIA-2600",    None),
    MachineInfo("192.168.10.167", "E65",    1, "HSW-800",     None),
    MachineInfo("192.168.10.168", "G65",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.169", "E66",    1, "HSW-800",     None),
    MachineInfo("192.168.10.170", "G66",    2, "FTO-2400",    None),
    MachineInfo("192.168.10.171", "E67",    1, "HSW-168",     Some("未安裝")),
    MachineInfo("192.168.10.172", "G67",    2, "FTO-2510",    None),
    MachineInfo("192.168.10.173", "E68",    1, "HSW-800",     None),
    MachineInfo("192.168.10.174", "G68",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.175", "E69",    1, "HSW-800",     None),
    MachineInfo("192.168.10.176", "G69",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.177", "E70",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.178", "G70",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.179", "E71",    1, "HSW-800",     None),
    MachineInfo("192.168.10.180", "G71",    2, "FTO-2500",    None),
    MachineInfo("192.168.10.181", "E92",    1, "TSW-100T",    None),
    MachineInfo("192.168.10.182", "G92",    2, "TIA-2600",    None),
    MachineInfo("192.168.10.183", "G96",    2, "FTO-2700",    None),
    MachineInfo("192.168.10.184", "G110",   2, "FTO-2500",    None),
    MachineInfo("192.168.10.185", "G98",    2, "FTO-3100",    None),
    MachineInfo("192.168.10.186", "G99",    2, "FTO-2200A",   None),
    MachineInfo("192.168.10.187", "G100",   2, "FTO-2700",    None),
    MachineInfo("192.168.10.188", "E101",   1, "ARCO",        Some("未安裝")),
    MachineInfo("192.168.10.189", "E102",   1, "ARCO",        Some("未安裝")),
    MachineInfo("192.168.10.190", "G103",   2, "SAA-1800",    Some("未安裝")),
    MachineInfo("192.168.10.191", "G103",   2, "SAA-1800",    Some("未安裝")),
    MachineInfo("192.168.10.192", "G104",   2, "FTO-2700",    None),
    MachineInfo("192.168.10.193", "G105",   2, "FTO-3100",    None),
    MachineInfo("192.168.10.194", "G106",   2, "SAA-1800",    Some("未安裝")),
    MachineInfo("192.168.10.195", "G106",   2, "SAA-1800",    Some("未安裝")),
    MachineInfo("192.168.10.196", "E106",   1, "HSW-800",     None),
    MachineInfo("192.168.10.197", "E107",   1, "HSW-160B",    Some("未安裝")),
    MachineInfo("192.168.10.198", "E108",   1, "HSW-250JW",   Some("未安裝")),
    MachineInfo("192.168.10.199", "E100",   1, "TSW-100T",    None),
    MachineInfo("192.168.10.200", "E111",   1, "HSW-160A/B",  Some("未安裝")),
    MachineInfo("192.168.10.201", "E112",   1, "HSW-160A",    Some("未安裝")),
    MachineInfo("192.168.10.202", "E104",   1, "HSW-168",     Some("未安裝")),
    MachineInfo("192.168.10.203", "E49-S2", 1, "SPH-3000",    Some("未安裝")),
    MachineInfo("192.168.10.204", "E49-S3", 1, "SPH-3000",    Some("未安裝")),
    MachineInfo("192.168.10.205", "E49-S4", 1, "SPH-3000",    Some("未安裝")),
    MachineInfo("192.168.10.206", "E49-S5", 1, "SPH-3000",    Some("未安裝")),
    MachineInfo("192.168.10.207", "G107",   2, "FTO-3050",    None),
    MachineInfo("192.168.10.208", "G108",   2, "FTO-3050",    None),
    MachineInfo("192.168.10.209", "G109",   2, "FTO-3050",    None),
    MachineInfo("192.168.10.210", "E49-S6", 1, "SPH-3000",    Some("未安裝")),
    MachineInfo("192.168.10.211", "E49-S7", 1, "SPH-3000",    Some("未安裝")),
    MachineInfo("192.168.10.212", "E49-S8", 1, "SPH-3000",    Some("未安裝")),
    MachineInfo("192.168.10.213", "E114",   1, "TSW-168T",    None),
    MachineInfo("192.168.10.214", "E115",   1, "TSW-168T",    None),
    MachineInfo("192.168.10.215", "E116",   1, "TSW-168T",    None),
    MachineInfo("192.168.10.216", "E113",   1, "TSW-168T",    None),
    MachineInfo("192.168.10.217", "E117",   1, "TSW-168T",    None),
    MachineInfo("192.168.10.218", "E118",   1, "TSW-303",     None),
    MachineInfo("192.168.10.219", "E119",   1, "TSW-303",     None),
    MachineInfo("192.168.10.220", "E120",   1, "TSW-303",     None),
    MachineInfo("192.168.10.221", "E121",   1, "TSW-303",     None),
    MachineInfo("192.168.10.222", "E122",   1, "TSW-303",     None),
    MachineInfo("192.168.10.223", "E123",   1, "TSW-303",     None),
    MachineInfo("192.168.10.224", "G111",   2, "富信成",    Some("未安裝")),
    MachineInfo("192.168.10.225", "G111",   2, "富信成",    Some("未安裝")),
    MachineInfo("192.168.20.82",  "G112",   2, "FTO-3050",      None),
    MachineInfo("192.168.20.83",  "G113",   2, "FTO-3050",      None),
    MachineInfo("192.168.20.83",  "G114",   2, "FTO-3050",      None),
    MachineInfo("192.168.20.83",  "G115",   2, "FTO-3050",      None),
    MachineInfo("192.168.20.83",  "G116",   2, "FTO-3050",      None),
    MachineInfo("192.168.20.83",  "G117",   2, "FTO-3050",      None),
    MachineInfo("192.168.20.83",  "G118",   2, "FTO-3050",      None),
    MachineInfo("192.168.10.227", "A01",    3, "CS-204K",     None),
    MachineInfo("192.168.10.228", "A02",    3, "CS-206",      None),
    MachineInfo("192.168.10.229", "A03",    3, "CS-205",      None),
    MachineInfo("192.168.10.230", "A04",    3, "CS-205",      None),
    MachineInfo("192.168.10.231", "A05",    3, "CS-205",      Some("修機中")),
    MachineInfo("192.168.10.232", "A06",    3, "CS-210",      None),
    MachineInfo("192.168.10.233", "A07",    3, "ATS-630J",    Some("無法開機")),
    MachineInfo("192.168.10.234", "A08",    3, "CS-210",      None),
    MachineInfo("192.168.10.235", "A09",    3, "CS-206K",     None),
    MachineInfo("192.168.10.236", "A10",    3, "CS-204K",     None),
    MachineInfo("192.168.10.237", "A11",    3, "CS-205",      None),
    MachineInfo("192.168.10.238", "A12",    3, "CS-210",      None),
    MachineInfo("192.168.10.239", "A13",    3, "CS-210",      None),
    MachineInfo("192.168.10.240", "A14",    3, "CS-210",      None),
    MachineInfo("192.168.10.241", "A15",    3, "ATS-720",     None),
    MachineInfo("192.168.10.242", "A16",    3, "ATS-720",     None),
    MachineInfo("192.168.10.243", "A17",    3, "CS-205",      None),
    MachineInfo("192.168.10.244", "A18",    3, "CS-206",      None),
    MachineInfo("192.168.10.245", "A19",    3, "CS-210",      None),
    MachineInfo("192.168.10.246", "A20",    3, "CS-210",      None),
    MachineInfo("192.168.10.247", "A21",    3, "CS-210",      None),
    MachineInfo("192.168.10.248", "A22",    3, "CS-210",      None),
    MachineInfo("192.168.10.249", "A23",    3, "ATS-720",    None),
    MachineInfo("192.168.10.250", "A24",    3, "CS-206",      None),
    MachineInfo("192.168.10.251", "A25",    3, "CS-206",      None),
    MachineInfo("192.168.10.252", "A26",    3, "CS-206",      None),
    MachineInfo("192.168.20.77",  "A27",    3, "CS-223",      None),
    MachineInfo("192.168.20.1",   "A28",    3, "CS-210",      None),
    MachineInfo("192.168.20.2",   "A29",    3, "CS-231",      Some("備料機")),
    MachineInfo("192.168.20.3",   "A30",    3, "ATS-720",     None),
    MachineInfo("192.168.20.4",   "A31",    3, "CS-210",      None),
    MachineInfo("192.168.20.5",   "A32",    3, "CS-210",      None),
    MachineInfo("192.168.20.6",   "A33",    3, "ATS-100M",    None),
    MachineInfo("192.168.20.7",   "A34",    3, "ATS-900",     None),
    MachineInfo("192.168.20.8",   "A35",    3, "ATS-600",     None),
    MachineInfo("192.168.20.9",   "A36",    3, "ATS-600",     None),
    MachineInfo("192.168.20.10",  "A37",    3, "ATS-600",     None),
    MachineInfo("192.168.20.11",  "A38",    3, "ATS-720",     None),
    MachineInfo("192.168.20.12",  "A39",    3, "ATS-630J",    None),
    MachineInfo("192.168.20.13",  "A40",    3, "CAS-3000SA",  None),
    MachineInfo("192.168.20.14",  "A41",    3, "CAS-3000SA",  None),
    MachineInfo("192.168.20.15",  "A42",    3, "CAS-3000SA",  None),
    MachineInfo("192.168.20.16",  "A43",    3, "ATS-100M",    None),
    MachineInfo("192.168.20.17",  "A44",    3, "ATS-110M",    Some("封裝機器")),
    MachineInfo("192.168.20.18",  "A45",    3, "CAS-3000SA",  None),
    MachineInfo("192.168.20.19",  "A46",    3, "ATS-600M",    None),
    MachineInfo("192.168.20.20",  "A47",    3, "CAS-3000SA",  None),
    MachineInfo("192.168.20.21",  "A48",    3, "CAS-4500S",   Some("未安裝")),
    MachineInfo("192.168.20.22",  "A49",    4, "新益昌",      Some("未安裝")),
    MachineInfo("192.168.20.23",  "A50",    4, "新益昌",      Some("未安裝")),
    MachineInfo("192.168.20.24",  "A57",    4, "新益昌",      Some("未安裝")),
    MachineInfo("192.168.20.25",  "A51",    3, "GT-480P",     None),
    MachineInfo("192.168.20.26",  "A58",    3, "GT-480P",     None),
    MachineInfo("192.168.20.27",  "A52",    3, "GT-1318P",    None),
    MachineInfo("192.168.20.28",  "A53",    3, "GT-1318P",    None),
    MachineInfo("192.168.20.29",  "A54",    3, "GT-1318P",    None),
    MachineInfo("192.168.20.30",  "A55",    3, "GT-1318P",    None),
    MachineInfo("192.168.20.31",  "A56",    3, "GT-1318P",    None),
    MachineInfo("192.168.20.32",  "A59",    3, "GT-1318P",    None),
    MachineInfo("192.168.20.33",  "A60",    3, "GT-1318P",    None),
    MachineInfo("192.168.20.34",  "A61",    3, "GT-1318P",    None),
    MachineInfo("192.168.20.35",  "A62",    3, "GT-480P",     None),
    MachineInfo("192.168.20.36",  "A63",    3, "GT-1318P",    None),
    MachineInfo("192.168.20.37",  "A64",    3, "GT-1318P",    None),
    MachineInfo("192.168.20.38",  "A65",    3, "GT-1318P",    None),
    MachineInfo("192.168.20.39",  "A66",    3, "GT-480P",     None),
    MachineInfo("192.168.20.40",  "A67",    4, "新益昌",      Some("未安裝")),
    MachineInfo("192.168.20.78",  "A68",    4, "ACG-308S-H",    Some("未安裝")),
    MachineInfo("192.168.20.79",  "A69",    4, "ACG-508F",      Some("未安裝")),
    MachineInfo("192.168.20.80",  "A70",    4, "ACG-508F",      Some("未安裝")),
    MachineInfo("192.168.20.81",  "A71",    4, "ACG-508F",      Some("未安裝")),
    MachineInfo("192.168.20.41",  "T01",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.42",  "T02",    5, "NCR-236A",    Some("無網路")),
    MachineInfo("192.168.20.43",  "T03",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.44",  "T04",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.45",  "T05",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.46",  "T06",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.47",  "T07",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.48",  "T08",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.49",  "T09",    5, "NCR-356B",    None),
    MachineInfo("192.168.20.50",  "T10",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.51",  "T11",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.52",  "T12",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.53",  "T13",    5, "NCR-356B",    None),
    MachineInfo("192.168.20.54",  "T16",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.55",  "T17",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.56",  "T18",    5, "JET-250E",    Some("不在地圖上")),
    MachineInfo("192.168.20.57",  "T19",    5, "JET-250E",    Some("不在地圖上")),
    MachineInfo("192.168.20.58",  "T20",    5, "NCR-236A",    None),
    MachineInfo("192.168.20.59",  "T21",    5, "NCR-356B",    None),
    MachineInfo("192.168.20.60",  "T22",    5, "CFT-450E",    Some("未安裝")),
    MachineInfo("192.168.20.61",  "T23",    5, "合鴻",        Some("未安裝")),
    MachineInfo("192.168.20.62",  "C01",    5, "富信成",      None),
    MachineInfo("192.168.20.63",  "C03",    5, "富信成",      None),
    MachineInfo("192.168.20.64",  "C04",    5, "TAICON",      None),
    MachineInfo("192.168.20.65",  "C05",    5, "TAICON",      None),
    MachineInfo("192.168.20.66",  "C06",    5, "TAICON",      None),
    MachineInfo("192.168.20.67",  "C07",    5, "富信成",      None),
    MachineInfo("192.168.20.68",  "C08",    5, "富信成",      None),
    MachineInfo("192.168.20.69",  "B01",    6, "TAP-306",     Some("不在地圖上")),
    MachineInfo("192.168.20.70",  "B02",    6, "TAP-306",     Some("不在地圖上")),
    MachineInfo("192.168.20.71",  "B03",    6, "TAP-306",     Some("不在地圖上")),
    MachineInfo("192.168.20.72",  "B04",    6, "TAP-306",     Some("不在地圖上")),
    MachineInfo("192.168.20.73",  "B05",    6, "TAP-306",     Some("不在地圖上")),
    MachineInfo("192.168.20.74",  "B06",    6, "TAP-306",     Some("不在地圖上")),
    MachineInfo("192.168.20.75",  "B07",    6, "TAP-306",     Some("不在地圖上")),
    MachineInfo("192.168.20.76",  "B08",    6, "TAP-306",     Some("不在地圖上"))
  )


  val machineTypeName = Map(
    1 -> "加締卷取",  // E
    2 -> "組立",      // G
    3 -> "老化",      // A
    4 -> "選別",      // A 左邊四台
    5 -> "加工切角"   // T, C
  )

  lazy val machineList = machineInfoList.map(_.machineID)
  lazy val ipTable = machineInfoList.map(machineInfo => machineInfo.ip -> machineInfo).toMap
  lazy val idTable = machineInfoList.map(machineInfo => machineInfo.machineID -> machineInfo).toMap

  def getMachineTypeName(machineID: String): Option[String] = {
    for {
      machineType <- idTable.get(machineID).map(_.machineType)
      machineTypeName <- machineTypeName.get(machineType)
    } yield machineTypeName
  }

  def getErrorDesc(machineID: String, defactID: Int): String = {
    defactDescription.get(defactID).getOrElse(defactID.toString)
  }
}
