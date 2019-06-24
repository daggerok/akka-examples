package com.github.daggerok.akka

import java.time.LocalDate

import akka.actor.{Actor, ActorSystem, InvalidMessageException, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

object ReverseActor {
  case class Msg[T](body: T)
  class ReverseActor extends Actor {
    override def receive: Receive = {
      case Msg(message: String) => sender() ! message.reverse
      case Msg(message) => sender() ! message
    }
  }
}

object UpperCaseActor {
  class UpperCaseActor extends Actor {
    override def receive: Receive = {
      case None => sender() ! "NONE"
      case message: String => sender() ! message.toUpperCase
      case message => self forward message.toString
    }
  }
}

object SplitterActor {
  class SplitterActor extends Actor {
    override def receive: Receive = {
      case string: String => string.trim.split("\\s+").foreach(sender() ! _)
    }
  }
}

class ActorsSpec extends TestKit(ActorSystem("system-under-test"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A ReverseActor" should {
    import ReverseActor._
    val actorRef = system.actorOf(Props[ReverseActor])

    "send back reverse order message body in case if it's type is String" in {
      actorRef ! Msg("12345")
      expectMsg("54321")
    }

    "otherwise just send same message body back as is" in {
      actorRef ! Msg(12345)
      expectMsg(12345)
    }
  }

  "An UpperCaseActor" should {
    import UpperCaseActor._
    import concurrent.duration._
    val actor = system.actorOf(Props[UpperCaseActor])

    "send fail in case if null-message was sent" in {
      try {
        actor ! null
      }
      catch {
        case e: InvalidMessageException =>
          assert(e.getLocalizedMessage == "Message is null")
      }
      expectNoMessage(2.seconds)
    }

    "send back NONE in case if empty message was sent" in {
      actor ! Option.empty
      val reply = expectMsgType[String]
      assert(reply == "NONE")
    }

    "send back upper cased message in case if String typed message was sent" in {
      actor ! " ololo trololo "
      val reply = expectMsgAnyOf("OLOLO", "TROLOLO", "OLOLO TROLOLO", " OLOLO TROLOLO ")
      assert(reply == " OLOLO TROLOLO ")
    }

    "send back message.toString.toUpperCase in case if message wasn't null or String" in {
      actor ! LocalDate.of(1111, 11, 11)
      expectMsg("1111-11-11")
    }
  }

  "A SplitterActor" should {
    import SplitterActor._
    val ref = system.actorOf(Props[SplitterActor])

    "send back strings as individual messages back" in {
      ref ! " give me more!!!11one "
      expectMsgAllOf("give", "me", "more!!!11one")
    }
  }

  "A TestProbe" should {
    val probe = TestProbe("FirstBlood")

    "send message to default testActor" in {
      val message = "Hello, testActor!"
      probe.send(testActor, message)
      expectMsg(message)
    }

    "receive a message from default testActor" in {
      val message = "Hello, probe.ref!"
      probe.ref ! message
      probe.expectMsg(message)
    }
  }
}
