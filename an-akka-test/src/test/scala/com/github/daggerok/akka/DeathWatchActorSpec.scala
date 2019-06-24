package com.github.daggerok.akka

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, ActorSystem, Kill, PoisonPill, Props, Terminated}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import concurrent.duration._

object DeathWatchActor {
  case class Initialize(name: String)
  case class Watch(actorRef: ActorRef)

  class ChildActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message =>
        sender() ! s"${self.path.name} actor received: $message"
    }
  }

  class ParentActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case Initialize(name) =>
        val childActorRef = context.actorOf(Props[ChildActor], name)
        log.info("{}: initialized {} actor", self.path.name, childActorRef.path.name)
        sender() ! childActorRef
        log.info("{}: sent {} ActorRef back to {} sender", self.path.name, childActorRef.path.name, sender().path.name)
      case Watch(actorRef: ActorRef) =>
        context.watch(actorRef)
        log.info("{}: watching {} actor...", self.path.name, actorRef.path.name)
        context.become(withAuditOf(actorRef))
      case message =>
        sender() ! s"unwatched: ${self.path.name} actor received: $message"
    }
    def withAuditOf(childActorRef: ActorRef): Receive = {
      case Terminated =>
        log.info("{}: {} actor is died... No sense to me live without it... So, I'm going to kill myself right now...", self.path.name, childActorRef.path.name)
        self ! PoisonPill
      case message =>
        childActorRef forward s"watching ${self.path.name} actor forwarded: $message"
    }
  }

  class IndependentActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case Watch(actorRef: ActorRef) =>
        context.watch(actorRef)
        context.become(withAuditOf(actorRef))
      case message =>
        sender() ! s"unwatched: ${self.path.name} actor received: $message"
    }
    def withAuditOf(watchingRef: ActorRef): Receive = {
      case Terminated(ref) =>
        log.info("{}: {} actor is died...", self.path.name, ref.path.name)
        if (ref == watchingRef) {
          log.info("{}: No sense to me live without it... So, I'm also killing myself...", self.path.name)
          self ! Kill
        }
      case message =>
        watchingRef forward s"watching: ${self.path.name} actor forward: $message"
    }
  }
}

class DeathWatchActorSpec extends TestKit(
  ActorSystem("death-watch-system-under-test", ConfigFactory.load().getConfig("death-watch")))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A ParentActor" should {
    import DeathWatchActor._

    "be able to monitor ChildActor" in {
      val parent = system.actorOf(Props[ParentActor], "parent")
      val childName = s"child-${System.nanoTime()}"
      parent ! Initialize(childName)

      val actorRefs = receiveWhile(messages = 1) {
        case actorRef: ActorRef => actorRef
      } // same as we would expectMsg(child) but with produced actor
      system.log.info("received actor: {}", actorRefs)

      parent ! "this message is for parent"
      val parentResponses = receiveWhile(messages = 1) {
        case string: String => string
      }
      assert(parentResponses.head == "unwatched: parent actor received: this message is for parent")

      val auditingActorRef = actorRefs.head
      parent ! Watch(auditingActorRef)
      receiveWhile(messages = 1) {
        case watchMessage => // ignore...
      }

      auditingActorRef ! "this message is for auditingActorRef"
      expectMsg(s"$childName actor received: this message is for auditingActorRef")

      parent ! "this message is for auditingActorRef"
      expectMsg(s"$childName actor received: watching parent actor forwarded: this message is for auditingActorRef")

      auditingActorRef ! PoisonPill
      auditingActorRef ! "are you still here?"
      expectNoMessage(1.second)

      parent ! "hey!"
      expectNoMessage(1.second)
    }
  }

  "Any other external actor like for example: IndependentActor" should {
    import DeathWatchActor._

    "also be able to monitor actors like: ChildActor" in {
      val nonRelatedActor = system.actorOf(Props[IndependentActor], "non-related")
      nonRelatedActor ! "this message is for unwatched non-related actor"
      expectMsg("unwatched: non-related actor received: this message is for unwatched non-related actor")

      val papa = system.actorOf(Props[ParentActor], "papa")
      val childName = s"child-${System.nanoTime()}"
      papa ! Initialize(childName)
      val actorRefs = receiveWhile(messages = 1) {
        case actorRef: ActorRef => actorRef
      }
      val auditingActorRef = actorRefs.head
      nonRelatedActor ! Watch(auditingActorRef)
      papa ! Watch(auditingActorRef)

      nonRelatedActor ! "this message should be forwarded to watching actor!"
      expectMsg(s"$childName actor received: watching: non-related actor forward: this message should be forwarded to watching actor!")

      auditingActorRef ! PoisonPill
      auditingActorRef ! "are you here?"
      expectNoMessage(1.second)

      papa ! "papa, are you here??!"
      expectNoMessage(1.second)

      nonRelatedActor ! "hey, nonRelatedActor! are you also not here???"
      expectNoMessage(1.second)
    }
  }
}
