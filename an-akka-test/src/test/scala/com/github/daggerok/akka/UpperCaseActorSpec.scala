package com.github.daggerok.akka

import java.time.LocalDate

import akka.actor.{Actor, ActorSystem, InvalidMessageException, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

object UpperCaseActor {
  class UpperCaseActor extends Actor {
    override def receive: Receive = {
      case None => sender() ! "NONE"
      case message: String => sender() ! message.toUpperCase
      case message => self forward message.toString
    }
  }
}

class UpperCaseActorSpec extends TestKit(ActorSystem("upper-case-system-under-test"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
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
}
