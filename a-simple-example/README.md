# A Simple Example
A simple akka getting started example quickly created in idea IDE (sbt wrapper, scala 2.13, akka 2.6.x + testkit, scalatest, scalactic)

## getting started

minimal _build.sbt_ file:

```sbtshell
name := "a-simple-example"

version := "0.1"

scalaVersion := "2.13.0"

val akkaVersion = "2.6.0-M3"
val scalatestVersion = "3.0.8"
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

```sbtshell
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")
```

see: https://github.com/sbt/sbt-assembly

update _build.sbt_ file:

```sbtshell
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
```

_build_ fat jar:

```bash
./sbtw clean assembly
# ...
[info] Packaging /path/to/a-simple-example/target/scala-2.13/a-simple-example-assembly-0.1.jar ...
# ...
java -jar ./target/scala-2.13/*assembly*.jar
```
