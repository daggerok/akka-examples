package com.github.daggerok.akka

import akka.actor.{Actor, ActorSystem, Props}

class PingPong extends Actor {
  override def receive: Receive = {
    case "ping" => println("pong")
    case "pong" => println("ping")
    case _ => println("unknown...")
  }
}

object PingPong {
  val name = "pingPong"
  def props = Props[PingPong]
}

object Main extends App {
  val system = ActorSystem("simple")
  val pingPong = system.actorOf(PingPong.props, PingPong.name)

  pingPong ! "Hey!"
  pingPong ! "ping"
  pingPong ! "pong"

  system.terminate()
}
