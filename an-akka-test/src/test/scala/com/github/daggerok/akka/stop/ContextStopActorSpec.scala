package com.github.daggerok.akka.stop

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

object ContextStopActor {
  case object Stop

  class ContextStopActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case Stop =>
        log.info("about to stop with 'context.stop' functionality...")
        context stop self
      case message => sender() ! s"received: $message"
    }
  }
}

class ContextStopActorSpec extends TestKit(ActorSystem("stop-system-under-test"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A ContextStopActor" should {
    import ContextStopActor._
    import concurrent.duration._
    val actorRef = system.actorOf(Props[ContextStopActor])

    "stop with context.stop hook" in {
      within(1.second) {
        actorRef ! Stop
        receiveWhile(max = 1.second) {
          case Stop =>
        }
        actorRef ! "this wont be delivered"
        expectNoMessage()
      }
    }
  }
}
