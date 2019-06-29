package com.github.daggerok.akka.supervision

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

object Supervision {
  case object ShouldReport
  case object ShouldResume
  case object ShouldRestart
  case object ShouldStop
  case object ShouldEscalate
  case class ShouldResumedException(message: String = "resume me...") extends RuntimeException(message)
  case class ShouldRestartedException(message: String = "restart me...") extends RuntimeException(message)
  case class ShouldStoppedException(message: String = "stop me...") extends RuntimeException(message)
  case class ShouldEscalateException(message: String = "escalate me...") extends RuntimeException(message)
  class ChildActor extends Actor with ActorLogging {
    override def preStart(): Unit = log.info("{} preStart", self.path.name)
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.info("{} preRestart {}", self.path.name, reason)
    override def postRestart(reason: Throwable): Unit = log.info("{} postRestart", self.path.name)
    override def postStop(): Unit = log.info("{} postStop", self.path.name)
    override def receive: Receive = countable(0)
    def countable(counter: Int): Receive = {
      case ShouldResume => throw ShouldResumedException()
      case ShouldRestart => throw ShouldRestartedException()
      case ShouldStop => throw ShouldStoppedException()
      case ShouldEscalate => throw ShouldEscalateException()
      case ShouldReport => sender() ! counter
      case _ => context.become(countable(counter + 1))
    }
  }
  case object CreateChild
  class SupervisorActor extends Actor with ActorLogging {
    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: ShouldResumedException => Resume
      case _: ShouldRestartedException => Restart
      case _: ShouldStoppedException => Stop
      case _ => Escalate
    }
    override def receive: Receive = offline
    def offline: Receive = {
      case CreateChild =>
        val childRef = context.actorOf(Props[ChildActor], s"child-${System.nanoTime()}")
        sender() ! childRef
    }
  }
}

class SupervisionSpec extends TestKit(ActorSystem("supervision-system-under-test"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "a supervisor actor" should {
    "create a child" in {
      import Supervision._
      val parent = system.actorOf(Props[SupervisorActor], s"supervisor-${System.nanoTime()}")

      parent ! CreateChild
      val child = expectMsgType[ActorRef]

      child ! "ololo"
      child ! "trololo"
      child ! ShouldReport
      expectMsg(2)
    }

    "resume child" in {
      import Supervision._
      val parent = system.actorOf(Props[SupervisorActor], s"supervisor-${System.nanoTime()}")

      parent ! CreateChild
      val child = expectMsgType[ActorRef]

      child ! "ololo"
      child ! ShouldResume
      child ! ShouldReport
      expectMsg(1)
    }

    "restart child" in {
      import Supervision._
      val parent = system.actorOf(Props[SupervisorActor], s"supervisor-${System.nanoTime()}")

      parent ! CreateChild
      val child = expectMsgType[ActorRef]

      child ! "trololo"
      child ! ShouldRestart
      child ! ShouldReport
      expectMsg(0)
    }

    "stop child" in {
      import Supervision._
      val parent = system.actorOf(Props[SupervisorActor], s"supervisor-${System.nanoTime()}")

      parent ! CreateChild
      val child = expectMsgType[ActorRef]

      child ! "trololo"
      child ! ShouldStop
      child ! ShouldReport
      expectNoMessage()
    }

    "escalate error from child" in {
      import Supervision._
      val parent = system.actorOf(Props[SupervisorActor], s"supervisor-${System.nanoTime()}")

      parent ! CreateChild
      val child = expectMsgType[ActorRef]

      child ! "trololo"
      child ! ShouldEscalate
      Thread.sleep(500)

      child ! ShouldReport
      val messages = receiveWhile() { case msg => msg }
      assert(messages.isEmpty)
    }
  }
}
