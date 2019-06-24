package com.github.daggerok.akka.stop

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

object ContextHierarchicalStopActor {
  case object Stop

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message =>
        val name = self.path.name
        log.info("{} received {}", name, message)
        sender() ! s"$name received $message"
    }
  }

  class Master extends Actor with ActorLogging {
    private val childRef = context.actorOf(Props[Child], "child")
    private val name = self.path.name
    override def receive: Receive = {
      case Stop =>
        log.info("{} about to stop with 'context.stop' functionality...", name)
        context stop self
      case message =>
        log.info("{} received {}", name, message)
        sender() ! s"$name received $message"
    }
  }
}

class ContextHierarchicalStopActorSpec extends TestKit(ActorSystem("hierarchical-stop-system-under-test"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A ContextHierarchicalStopActor" should {
    import ContextHierarchicalStopActor._
    import concurrent.duration._
    val masterRef = system.actorOf(Props[Master], "master")
    val childRef = system.actorSelection("user/master/child")

    "stop with context.stop hook" in {
      masterRef ! "hola!"
      childRef ! "hej!"
      expectMsgAllOf("master received hola!", "child received hej!")
      within(1.second) {
        masterRef ! Stop
        receiveWhile(max = 1.second) {
          case Stop =>
        }
        childRef ! "this wont be delivered"
        masterRef ! "this wont be delivered as well"
        expectNoMessage()
      }
    }
  }
}
