# simple example [![Build Status](https://travis-ci.org/daggerok/akka-examples.svg?branch=master)](https://travis-ci.org/daggerok/akka-examples)
A simple akka getting started example quickly created in idea IDE (sbt wrapper, scala 2.13, akka 2.6.x + testkit, scalatest, scalactic)

## getting started

minimal _build.sbt_ file:

```scala
name := "a-simple-example"

version := "0.1"

scalaVersion := "2.13.0"

val akkaVersion = "2.6.0-M8"
val scalatestVersion = "3.2.0-M1"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "org.scalactic" %% "scalactic" % scalatestVersion % Test
)
```

minimal _Main.scala_ file:

```scala
object Main extends App {
  println("hey!")
}
```

_test_ and _run_ app:

```bash
./sctw test
./sctw clean run
```

## implement fatJar

create _project/assembly.sbt_ file:

```scala
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")
```

update _build.sbt_ file:

```scala
lazy val commonSettings = Seq(
  organization := "com.github.daggerok",
  scalaVersion := "2.13.0",
  test in assembly := {}
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("com.github.daggerok.akka.Main"),
    // more settings here ...
  )
```

_build_ fat jar:

```bash
./sbtw clean assembly
# ...
[info] Packaging /path/to/a-simple-example/target/scala-2.13/a-simple-example-assembly-0.1.jar ...
# ...
java -jar ./target/scala-*/*-assembly-*.jar
```

resources:

* [sbt-assembly plugin](https://github.com/sbt/sbt-assembly)
* [Creating Scala Fat Jars on SBT with sbt-assembly Plugin](http://queirozf.com/entries/creating-scala-fat-jars-for-spark-on-sbt-with-sbt-assembly-plugin)
