package daggerok.helloworld

import akka.actor.{Actor, ActorSystem, Props}

case class HelloMessage(name: String)

class HelloActor extends Actor {
  override def receive: Receive = {
    case HelloMessage(name) => println(s"Hello, $name!")
  }
}

object HelloWorldApp {
  def main(args: Array[String]): Unit = {
    val helloSystem = ActorSystem("hello-actor-system")
    val helloActor = helloSystem.actorOf(Props[HelloActor], "hello-actor")

    helloActor ! HelloMessage("Максимко")
    Thread.sleep(1000)
    helloSystem.terminate()
  }
}
