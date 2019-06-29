package com.github.daggerok.akka.patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

object Stash {

  /**
    * Actor has Close and Open states:
    *   - in Close state actor should receive:
    *     - Open switches into opened state
    *     - otherwise everything else should be postponed for next message handler...
    *   - in Open state can handle:
    *     - Read
    *     - Write
    *     - Close switches into closed state
    *     - otherwise everything else should be just logged...
    */
  case object Open
  case class Read(key: String)
  case class Write(key: String, value: String)
  case object Close
  class ResourceActor extends Actor with ActorLogging with Stash {
    override def receive: Receive = closed(Map())
    def closed(db: Map[String, String]): Receive = {
      case Open =>
        log.info("opening state and unstashing (prepend) messages...")
        unstashAll() // everything stashed will be handled in opened state
        context.become(opened(db))
      case message =>
        log.info("cannot handle '{}' message in closed state, stashing...", message)
        stash()
    }
    def opened(db: Map[String, String]): Receive = {
      case Close =>
        log.info("closing state and unstashing (prepend) all messages...")
        unstashAll()
        context.become(closed(db))
      case Read(key) =>
        log.info("reading for key: {}", key)
        sender() ! db(key)
      case Write(key, value) =>
        log.info("writing a key-value pair: {}", key -> value)
        val map = db + (key -> value)
        sender() ! map
        context.become(opened(map))
      case message =>
        log.info("cannot handle '{}' message in opened state, stashing...", message)
        stash()
    }
  }
}

/**
  * Stashes allow actors to set messages aside for later if they cannot or should
  * not process them at exact moment.
  * When actor changes behavior with context.become or context.unbecome, it's usually
  * good time to prepend messages into mailbox and start processing them again
  */
class StashSpec extends TestKit(ActorSystem("stash-system-under-test"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "a stash actor" should {
    "behave as expected" in {
      import Stash._
      val resource = system.actorOf(Props[ResourceActor], s"resource-${System.nanoTime()}")

      resource ! Open
      expectNoMessage()

      resource ! Write("ololo", "trololo")
      val map = expectMsgType[Map[String, String]]
      assert(map.size == 1)

      resource ! Close
      resource ! Read("ololo")
      resource ! Write("trololo", "ololo")
      expectNoMessage()

      resource ! Open
      expectMsg("trololo")
      val mapPlus1 = expectMsgType[Map[String, String]]
      assert(mapPlus1.size == map.size + 1)
    }
  }
}
