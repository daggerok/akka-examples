package com.github.daggerok.akka

import akka.actor.ActorSystem

object Main extends App {
  val system = ActorSystem("simple")
  println(system.name)
  system.terminate()
}
