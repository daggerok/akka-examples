package com.github.daggerok.akka

import akka.actor.ActorSystem

class ActorToBeTested

object Main extends App {
  private val system = ActorSystem("system")
  system.log.info("run command: sbt test")
  system.terminate()
}
