name := "a-simple-example"

version := "0.1"

scalaVersion := "2.13.0"

lazy val akkaVersion = "2.6.0-M3"
lazy val scalatestVersion = "3.0.8"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "org.scalactic" %% "scalactic" % scalatestVersion % Test
)

lazy val commonSettings = Seq(
  organization := "com.github.daggerok",
  scalaVersion := "2.12.6",
  test in assembly := {}
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("com.github.daggerok.akka.Main"),
    // more settings here ...
  )
