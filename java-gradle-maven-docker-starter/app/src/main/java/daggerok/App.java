package daggerok;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static java.time.LocalDate.now;

@Slf4j
class LogMyActorRefActor extends AbstractActor {

  @Override
  public Receive createReceive() {

    return receiveBuilder()
        .matchEquals("log-it", p -> {
          final ActorRef secondRef = getContext().actorOf(Props.empty(), "1st-actor");
          log.info("{} 1st: {}", p, secondRef);
        })
        .match(String.class, string -> {
          final ActorRef secondRef = getContext().actorOf(Props.empty(), "second-actor");
          log.info("{} Second: {}", string, secondRef);
        })
        .matchAny(any -> {
          final ActorRef secondRef = getContext().actorOf(Props.empty(), "third-actor");
          log.info("{} Third: {}", any, secondRef);
        })
        .build();
  }
}

@Slf4j
public class App {

  @SneakyThrows
  public static void main(String[] args) {

    val actorSystem = ActorSystem.create("testSystem");
    val firstActorRef = actorSystem.actorOf(Props.create(LogMyActorRefActor.class), "log-my-actor-ref-actor");

    log.info("First: {}", firstActorRef);
    firstActorRef.tell("log-it", ActorRef.noSender());
    log.info("2nd: {}", firstActorRef);
    firstActorRef.tell("ololo-trololo", ActorRef.noSender());
    log.info("3rd: {}", firstActorRef);
    firstActorRef.tell(now(), ActorRef.noSender());
    log.info(">>> Press ENTER to exit <<<");

    try { System.in.read(); }
    finally { actorSystem.terminate(); }
  }
}
