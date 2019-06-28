package com.github.daggerok.akka.lifecycle

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

object LifeCycleActor {
  case object CreateChild
  class LifeCycleActor extends Actor with ActorLogging {
    override def preStart(): Unit = log.info("{} starting", self.path)
    override def postStop(): Unit = log.info("{} stopped", self.path)
    override def receive: Receive = {
      case CreateChild =>
        context.actorOf(Props[LifeCycleActor], s"child-${System.nanoTime()}")
    }
  }
}

class LifeCycleActorSpec extends TestKit(ActorSystem("lifecycle-system-under-test"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "parent actor" should {
    "stop all it's children" in {
      import LifeCycleActor._
      val parent = system.actorOf(Props[LifeCycleActor], s"parent-${System.nanoTime()}")
      parent ! CreateChild
      parent ! PoisonPill
    }
  }
}
