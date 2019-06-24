package com.github.daggerok.akka

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
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

class ReverseActorSpec extends TestKit(ActorSystem("reverse-system-under-test"))
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
}
