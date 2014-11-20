name := "ZhenhaiDashboard"

version := "0.0.1"

scalaVersion := "2.11.2"

seq(webSettings :_*)

libraryDependencies ++= Seq(
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.1.v20110908" % "container"
)

libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-webkit" % "2.6-RC1" % "compile->default",
  "net.liftweb" %% "lift-mongodb" % "2.6-RC1",
  "net.liftweb" %% "lift-mongodb-record" % "2.6-RC1"
)

port in container.Configuration := 8081

