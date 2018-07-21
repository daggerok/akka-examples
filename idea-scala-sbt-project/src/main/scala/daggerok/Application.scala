package daggerok

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import daggerok.API.{RestartError, ResumeError, StopError}

object API {
  sealed trait ErrorMessage extends RuntimeException
  case object StopError extends ErrorMessage
  case object ResumeError extends ErrorMessage
  case object RestartError extends ErrorMessage
}

class Child extends Actor {

  override def preStart(): Unit = {
    println("Child: preStart")
    Thread.sleep(100)
    super.preStart()
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("Child: preRestart")
    Thread.sleep(100)
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable): Unit = {
    println("Child: postRestart")
    Thread.sleep(100)
    super.postRestart(reason)
  }

  override def postStop(): Unit = {
    println("Child: postStop")
    Thread.sleep(100)
    super.postStop()
  }

  override def receive: Receive = {
    case "Stop" => throw StopError
    case "Resume" => throw ResumeError
    case "Restart" => throw RestartError
    case _ => throw new RuntimeException
  }
}

class Parent extends Actor {

  var childRef: ActorRef = _

  import scala.concurrent.duration._ // seconds
  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 1 second) {
      case StopError => Stop
      case ResumeError => Resume
      case RestartError => Restart
      case _: RuntimeException => Escalate
    }

  override def preStart(): Unit = {
    println("Parent: preStart")
    childRef = context.actorOf(Props[Child], "child")
    Thread.sleep(100)
    super.preStart()
  }

  override def receive: Receive = {
    case message =>
      println(s"Parent message: $message")
      childRef ! message
      Thread.sleep(100)
  }
}

object Application {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("supervision-system")
    val parent = system.actorOf(Props[Parent], "parent")
//    parent! "Stop"
//    parent! "Resume"
    parent! "Restart"
    Thread.sleep(1000)
    system.terminate()
  }
}
