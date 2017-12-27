package daggerok

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import daggerok.App.log
import org.slf4j.LoggerFactory

import java.time.LocalDate.now

//fun main(args: Array<String>) {
//  println("Hello, World")
//}

internal class LogMyActorRefActor : AbstractActor() {

  override fun createReceive(): Receive {

    return receiveBuilder()
        .matchEquals("log-it", { p ->
          val secondRef = getContext().actorOf(Props.empty(), "1st-actor")
          log.info("{} 1st: {}", p, secondRef)
        })
        .match(String::class.java, { string ->
          val secondRef = getContext().actorOf(Props.empty(), "second-actor")
          log.info("{} Second: {}", string, secondRef)
        })
        .matchAny({ any ->
          val secondRef = getContext().actorOf(Props.empty(), "third-actor")
          log.info("{} Third: {}", any, secondRef)
        })
        .build()
  }
}

/*
object App {

  @JvmStatic
  fun main(args: Array<String>) {

    val actorSystem = ActorSystem.create("testSystem")
    val firstActorRef = actorSystem.actorOf(Props.create(LogMyActorRefActor::class.java), "log-my-actor-ref-actor")

    log.info("First: {}", firstActorRef)
    firstActorRef.tell("log-it", ActorRef.noSender())
    log.info("2nd: {}", firstActorRef)
    firstActorRef.tell("ololo-trololo", ActorRef.noSender())
    log.info("3rd: {}", firstActorRef)
    firstActorRef.tell(now(), ActorRef.noSender())
    log.info(">>> Press ENTER to exit <<<")

    try {
      System.`in`.read()
    } finally {
      actorSystem.terminate()
    }
  }
}
*/

object App {
  val log = LoggerFactory.getLogger(this.javaClass.name)
}

fun main(args: Array<String>) {

  val actorSystem = ActorSystem.create("testSystem")
  val firstActorRef = actorSystem.actorOf(Props.create(LogMyActorRefActor::class.java), "log-my-actor-ref-actor")

  log.info("First: {}", firstActorRef)
  firstActorRef.tell("log-it", ActorRef.noSender())
  log.info("2nd: {}", firstActorRef)
  firstActorRef.tell("ololo-trololo", ActorRef.noSender())
  log.info("3rd: {}", firstActorRef)
  firstActorRef.tell(now(), ActorRef.noSender())
  log.info(">>> Press ENTER to exit <<<")

  try {
    System.`in`.read()
  } finally {
    actorSystem.terminate()
  }
}
