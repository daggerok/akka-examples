package com.github.daggerok.akka

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class TestProbeSpec extends TestKit(ActorSystem("probe-system-under-test"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A TestProbe" should {
    "send message to default testActor" in {
      val probe = TestProbe("FirstBlood")
      val message = "Hello, testActor!"
      probe.send(testActor, message)
      expectMsg(message)
    }

    "receive a message from default testActor" in {
      val probe = TestProbe("FirstBlood")
      val message = "Hello, probe.ref!"
      probe.ref ! message
      probe.expectMsg(message)
    }

    "implement probe.receiveWhile partial function and reply back to testActor" in {
      val probe = TestProbe("FirstBlood")
      probe.ref ! "Hola!"
      val strings = probe.receiveWhile() {
        case string: String => testActor ! s"$string $string".trim
      }
      probe.reply(strings)
      expectMsgAnyOf("Hola! Hola!")
    }

    "direct send / replay between probe actors using probe receiveWhile partial function implementation" in {
      val first = TestProbe("1st")
      val second = TestProbe("2nd")

      //(second.ref ! "Hola!")(first.ref) // same as:
      first.send(second.ref, "Hola!")

      import concurrent.duration._
      second.receiveWhile(max = 1.seconds, messages = 1) {
        case string: String => first.ref ! s"$string $string".trim
      }

      first.expectMsg("Hola! Hola!")
    }
  }
}
