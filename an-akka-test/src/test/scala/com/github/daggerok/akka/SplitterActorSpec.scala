package com.github.daggerok.akka

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

object SplitterActor {
  class SplitterActor extends Actor {
    override def receive: Receive = {
      case string: String => string.trim.split("\\s+").foreach(sender() ! _)
    }
  }
}

class SplitterActorSpec extends TestKit(ActorSystem("splitter-system-under-test"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A SplitterActor" should {
    import SplitterActor._
    val ref = system.actorOf(Props[SplitterActor])

    "send back strings as individual messages back" in {
      ref ! " give me more!!!11one "
      expectMsgAllOf("give", "me", "more!!!11one")
    }
  }
}
