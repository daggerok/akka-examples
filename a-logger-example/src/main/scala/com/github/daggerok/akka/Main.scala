package com.github.daggerok.akka

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.Logging

class ActorSystemLogger extends Actor {
  private val log = context.system.log
  override def receive: Receive = {
    case message => log.info("{} {} {}",
      self.path, getClass.getSimpleName, message)
  }
}

class ActorWithExplicitLogger extends Actor {
  private val log = Logging(context.system, this)
  override def receive: Receive = {
    case message => log.info("{} {} {}",
      self.path, getClass.getSimpleName, message)
  }
}

class ActorWithLoggingTrait extends Actor with ActorLogging {
  override def receive: Receive = {
    case message => log.info("{} {} {}",
      self.path, getClass.getSimpleName, message)
  }
}

object Main extends App {
  private val system = ActorSystem("simple")

  private val actorSystemLogger: ActorRef = system.actorOf(Props[ActorSystemLogger])
  actorSystemLogger ! System.nanoTime()

  private val actorWithExplicitLogger: ActorRef = system.actorOf(Props[ActorWithExplicitLogger])
  actorWithExplicitLogger ! System.nanoTime()

  private val actorWithLoggingTrait: ActorRef = system.actorOf(Props[ActorWithLoggingTrait])
  actorWithLoggingTrait ! System.nanoTime()

  system.terminate()
}
