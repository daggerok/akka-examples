package daggerok

import akka.actor.{Actor, ActorSystem, Props}

case class MyMessage(name: String)

class MyActor extends Actor {
  override def receive: Receive = {
    case MyMessage(name) => println(s"Hello, $name!")
  }
}

object Application {
  def main(args: Array[String]): Unit = {

    val mySystem = ActorSystem("my-actor-system")
    val myActor = mySystem.actorOf(Props[MyActor], "my-actor")

    myActor ! MyMessage("Максимко")
    Thread.sleep(1000)
    mySystem.terminate()
  }
}
