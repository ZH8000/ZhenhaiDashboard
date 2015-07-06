name := "ZhenhaiDashboard"

version := "0.0.1"

scalaVersion := "2.11.2"

seq(webSettings :_*)

scalacOptions := List("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.1.v20110908" % "container"
)

libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-webkit" % "2.6-RC2" % "compile->default",
  "net.liftweb" %% "lift-mongodb" % "2.6-RC2",
  "net.liftweb" %% "lift-mongodb-record" % "2.6-RC2",
  "org.mongodb" %% "casbah" % "2.7.3",
  "com.itextpdf" % "itextpdf" % "5.5.3",
  "com.itextpdf" % "itext-asian" % "5.2.0",
  "net.sourceforge.jexcelapi" % "jxl" % "2.6.12"
)

port in container.Configuration := 8081

