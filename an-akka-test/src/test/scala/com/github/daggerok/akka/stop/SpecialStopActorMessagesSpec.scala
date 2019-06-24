package com.github.daggerok.akka.stop

import akka.actor.{Actor, ActorLogging, ActorSystem, Kill, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

object SpecialMessagesToStopActor {
  case class Forward(message: String)

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message =>
        val name = self.path.name
        log.info("{} received: {}", name, message)
        sender() ! s"$name received: $message"
    }
  }

  class Master extends Actor with ActorLogging {
    private val childRef = context.actorOf(Props[Child], "child")
    private val name = self.path.name
    override def receive: Receive = {
      case Forward(message) =>
        log.info("{} forward: {}", name, message)
        childRef forward message
      case message =>
        log.info("{} received: {}", name, message)
        sender() ! s"$name received: $message"
    }
  }
}

class SpecialMessagesToStopActorSpec extends TestKit(ActorSystem("poison-pill-and-kill-system-under-test"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A Master / Child actors" should {
    import SpecialMessagesToStopActor._
    import concurrent.duration._

    "stop all actors with special message: PoisonPill" in {
      val masterRef = system.actorOf(Props[Master], "master")

      masterRef ! "hola!"
      masterRef ! Forward("hej!")
      expectMsgAllOf("master received: hola!", "child received: hej!")
      within(1.second) {
        masterRef ! PoisonPill
        receiveWhile(max = 1.second) {
          case message =>
        }
        masterRef ! Forward("this wont be delivered")
        masterRef ! "this wont be delivered as well"
        expectNoMessage()
      }
    }

    "stop actor hierarchy with special message: Kill, which is makes an actor throw an ActorKillException" in {
      val masterRef = system.actorOf(Props[Master], "master")

      masterRef ! "hello, killer!"
      masterRef ! Forward("hej, mördare!")
      expectMsgAllOf("master received: hello, killer!", "child received: hej, mördare!")
      within(1.second) {
        masterRef ! Kill
        receiveWhile(max = 1.second) {
          case message =>
        }
        masterRef ! Forward("this wont be delivered")
        masterRef ! "this wont be delivered as well"
        expectNoMessage()
      }
    }
  }
}
