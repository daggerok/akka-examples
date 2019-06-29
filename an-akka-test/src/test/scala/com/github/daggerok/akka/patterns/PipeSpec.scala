package com.github.daggerok.akka.patterns

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Pipe {
  trait PingPong
  case object Ping extends PingPong
  case object Pong extends PingPong
  case class ForwardRequestReplyResponse(pingPongRequest: Any, to: ActorRef)
  class PingPongActor extends Actor with ActorLogging {
    import akka.pattern.ask
    import akka.pattern.pipe
    import akka.util.Timeout
    import scala.concurrent.duration._
    implicit val timeout = Timeout(1.second)
    implicit val executionContext: ExecutionContext = context.dispatcher
    override def receive: Receive = {
      case Ping =>
        log.info("Ping {}", self.path.name)
        sender() ! Pong
      case Pong =>
        log.info("Pong {}", self.path.name)
        sender() ! Ping
      // here we are going to ask for result given ref and on response send it back...
      case ForwardRequestReplyResponse(request, to) =>
        log.info("{} forward {} to {}", self.path.name, request, to.path.name)
        val future = to ? request
        future.pipeTo(sender()) //shortest
        //future.mapTo[PingPong].pipeTo(sender()) //shorter
        ////longer:
        //val successFuture = future.map {
        //  case response =>
        //    log.info("{} completed future with {}", self.path.name, response)
        //    response
        //  case Failure(exception) =>
        //    log.error("oops {}", exception.getLocalizedMessage)
        //    throw new RuntimeException(exception)
        //}
        //successFuture
        //  .mapTo[PingPong]
        //  .pipeTo(sender())
    }
  }
}

/**
  * Ask allows actors request Future result with Success ot Failure responses as payload
  */
class PipeSpec extends TestKit(ActorSystem("pipe-system-under-test"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "a Pipe actor" should {
    import Pipe._

    "respond with Pong response on direct Ping request" in {
      val pingPongActor = system.actorOf(Props[PingPongActor], s"ping-pong-${System.nanoTime()}")
      pingPongActor ! Ping
      expectMsg(Pong)
    }

    "respond with Ping response on direct Pong request" in {
      val pingPongActor = system.actorOf(Props[PingPongActor], s"ping-pong-${System.nanoTime()}")
      pingPongActor ! Pong
      expectMsg(Ping)
    }

    "forward Ping request to other actor and respond with Pong response back" in {
      val pingPongActor1 = system.actorOf(Props[PingPongActor], s"ping-pong-1-${System.nanoTime()}")
      val pingPongActor2 = system.actorOf(Props[PingPongActor], s"ping-pong-2-${System.nanoTime()}")
      pingPongActor1 ! ForwardRequestReplyResponse(Ping, pingPongActor2)
      expectMsg(Pong)
    }

    "forward Pong request to other actor and respond with Ping response back" in {
      val pingPongActor1 = system.actorOf(Props[PingPongActor], s"ping-pong-1-${System.nanoTime()}")
      val pingPongActor2 = system.actorOf(Props[PingPongActor], s"ping-pong-2-${System.nanoTime()}")
      pingPongActor2 ! ForwardRequestReplyResponse(Pong, pingPongActor1)
      expectMsg(Ping)
    }
  }
}
