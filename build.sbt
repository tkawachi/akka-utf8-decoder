val commonSettings = Seq(
  organization := "com.github.tkawachi",
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/tkawachi/akka-utf8-decoder/"),
    "scm:git:github.com:tkawachi/akka-utf8-decoder.git"
  )),

  scalaVersion := "2.11.8",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint"
  ),

  doctestTestFramework := DoctestTestFramework.ScalaTest
)

lazy val root = project.in(file("."))
  .settings(commonSettings :_*)
  .settings(
    name := "akka-utf8-decoder",
    description := "Akka UTF8 docoder"
  )
