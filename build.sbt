import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.ww",
      scalaVersion := "2.12.1",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "RssFilter",
    libraryDependencies ++= Seq(scalaTest % Test,
      "com.typesafe.akka" %% "akka-http" % "10.0.1",
      "com.typesafe.akka" %% "akka-http-xml" % "10.0.1")
  )
