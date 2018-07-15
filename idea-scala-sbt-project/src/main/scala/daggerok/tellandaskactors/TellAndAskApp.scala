package daggerok.tellandaskactors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import daggerok.tellandaskactors.CheckerAPI.{BlacklistUserResponse, CheckUserRequest, WhitelistUserResponse}
import daggerok.tellandaskactors.RecorderAPI.CreateUser
import daggerok.tellandaskactors.StorageAPI.AddUser

case class User(name: String)

object RecorderAPI {
  sealed trait RecorderMessage
  case class CreateUser(user: User) extends RecorderMessage
}

class Recorder(checker: ActorRef, storage: ActorRef) extends Actor {
  import scala.concurrent.duration._ // seconds
  implicit val timeout = Timeout(5 seconds)

  import akka.pattern.ask // actor ask map
  import scala.concurrent.ExecutionContext.Implicits.global
  override def receive: Receive = {
    case CreateUser(user) =>
      checker ? CheckUserRequest(user) map {
        case WhitelistUserResponse(user) =>
          storage ! AddUser(user)
        case BlacklistUserResponse(user) =>
          println(s"Recorder black user: $user")
      }
    case _ => println("Checker: unknown message received")
  }
}

object CheckerAPI {
  sealed trait CheckerRequest
  case class CheckUserRequest(user: User) extends CheckerRequest

  sealed trait CheckerResponse
  case class BlacklistUserResponse(user: User) extends CheckerResponse
  case class WhitelistUserResponse(user: User) extends CheckerResponse
}

class Checker extends Actor {
  val blackList = List("bad", "black", "evil")
  override def receive: Receive = {
    case CheckUserRequest(user) if (blackList.exists(name => user.name.contains(name))) =>
      sender() ! BlacklistUserResponse(user)
    case CheckUserRequest(user) =>
      sender() ! WhitelistUserResponse(user)
    case _ => println("Checker: unknown message received")
  }
}

object StorageAPI {
  sealed trait StorageMessage
  case class AddUser(user: User) extends StorageMessage
}

class Storage extends Actor {
  private var users = List.empty[User]
  override def receive: Receive = {
    case AddUser(user) =>
      println(s"user $user added")
      users = user :: users
    case _ => println("Storage: unknown message received")
  }
}

object TellAndAskApp {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("tell-ask-system")
    val checker = system.actorOf(Props[Checker], "checker")
    val storage = system.actorOf(Props[Storage], "storage")
    val recorder = system.actorOf(Props(new Recorder(checker, storage)), "recorder")

    recorder ! CreateUser(User("white"))
    recorder ! CreateUser(User("black"))
    recorder ! CreateUser(User("one more white"))
    Thread.sleep(1000)
    system.terminate()
  }
}
