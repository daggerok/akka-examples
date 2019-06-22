package com.github.daggerok.akka

import akka.actor.{Actor, ActorSystem, Props}
import com.github.daggerok.akka.Messages.{Decrement, Increment, Print}

object CounterRepository {
  private var counter: BigInt = 0
  def increment(by: BigInt = 1): Unit = counter += by
  def decrement(by: BigInt = 1): Unit = counter -= by
  def getCounter: BigInt = counter
}

class CounterActor extends Actor {
  private val log = context.system.log

  override def receive: Receive = {
    case Increment => CounterRepository.increment()
    case Decrement => CounterRepository.decrement()
    case Increment(value) => CounterRepository.increment(value)
    case Decrement(value) => CounterRepository.decrement(value)
    case Print => log.info("current counter: {}", CounterRepository.getCounter)
    case _ => log.warning("received unexpected...")
  }
}

object Messages {
  case class Increment(by: BigInt = 1)
  case class Decrement(by: BigInt = 1)
  object Print
}

object Main extends App {
  val system = ActorSystem("system")
  val counter = system.actorOf(Props[CounterActor], "counter")

  counter ! Print
  counter ! Increment
  counter ! Increment
  counter ! Print
  counter ! Increment
  counter ! Increment
  counter ! Print
  counter ! Decrement
  counter ! Print

  system.terminate()
}
