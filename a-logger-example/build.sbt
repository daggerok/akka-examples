name := "a-logger-example"

version := "0.1"

scalaVersion := "2.13.0"

lazy val akkaVersion = "2.6.0-M8"
lazy val logbackVersion = "1.2.3"
lazy val scalatestVersion = "3.0.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "org.scalactic" %% "scalactic" % scalatestVersion % Test
)

lazy val commonSettings = Seq(
  organization := "com.github.daggerok",
  test in assembly := {}
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    mainClass in assembly := Some("com.github.daggerok.akka.Main"),
    // more settings here ...
  )
