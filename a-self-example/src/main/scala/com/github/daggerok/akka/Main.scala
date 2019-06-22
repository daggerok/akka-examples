package com.github.daggerok.akka

import java.util.UUID

import akka.actor.{AbstractActor, AbstractLoggingActor, Actor, ActorRef, ActorSystem, Props}

sealed trait SelfRepliedMessage
case class StringMessage(string: String) extends SelfRepliedMessage
case class NumberMessage(number: Number) extends SelfRepliedMessage

class CapabilitiesActor extends Actor {
  private val log = context.system.log
  override def receive: Receive = {
    case string: String => log.info(s"${self.path}: received a string: $string")
    case number: Number => log.info(s"${self.path}: received a number: $number")
    case StringMessage(string) => self ! string
    case NumberMessage(number) => self ! number
    case _ => log.warning("received unexpected...")
  }
}

object Main extends App {
  val system = ActorSystem("simple")
  val actor = system.actorOf(Props[CapabilitiesActor], "selfActor")

  actor ! "Hello, World!"
  actor ! 42
  actor ! UUID.randomUUID()
  actor ! StringMessage("hola!")
  actor ! NumberMessage(System.nanoTime())

  system.terminate()
}
