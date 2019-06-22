package com.github.daggerok.akka

import akka.actor.ActorSystem

object Main extends App {
  val system = ActorSystem("simple")
  system.log.info(system.name)
  system.terminate()
}
