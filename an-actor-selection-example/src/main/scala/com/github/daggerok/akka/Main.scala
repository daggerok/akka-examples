package com.github.daggerok.akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

class Child extends Actor {
  override def receive: Receive = {
    case message => context.system.log.info("{} got message: {}", self.path, message)
  }
}

object Patent {
  case class CreateChildActor(name: String)
  case class TellToChild(message: String)
}

class Patent extends Actor {
  import Patent._

  override def receive: Receive = withoutChild

  def withoutChild: Receive = {
    case CreateChildActor(name) =>
      val childRef = context.actorOf(Props[Child], name)
      context become withChild(childRef)
  }

  def withChild(childRef: ActorRef): Receive = {
    case TellToChild(message) => childRef forward message
    case message => context.system.log.warning("received unknown message: {}", message)
  }
}

object Main extends App {
  val system = ActorSystem("actor-selection-system")
  val parent: ActorRef = system.actorOf(Props[Patent], "parent")

  import Patent._
  parent ! CreateChildActor("child")

  val child = system actorSelection "/user/parent/child"
  child ! TellToChild("Direct hello!")

  system.terminate()
}
