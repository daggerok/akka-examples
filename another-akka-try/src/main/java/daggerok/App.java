package daggerok;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class App {

  public enum Ping {INSTANCE}
  public enum Pong {INSTANCE}
  public enum Done {INSTANCE}

  public static final Consumer<Integer> sleep
      = n -> Try.run(() -> TimeUnit.MILLISECONDS.sleep(n))
                .onFailure(throwable -> log.error("{}", throwable.getLocalizedMessage(), throwable));

  public static class UntypedPingPongActor extends AbstractActor {

    private int timeout;
    private CountDownLatch countDownLatch;

    private void zzZzZzz(final Object msg) {
      log.info("{} {}", msg.getClass(), countDownLatch.getCount());
      App.sleep.accept(timeout);
      if (countDownLatch.getCount() > 1)
        countDownLatch.countDown();
      else sender().tell(Done.INSTANCE, self());
    }

    @Override public Receive createReceive() {
      return
          receiveBuilder()
              .match(Integer.class, sleep -> {
                if (sleep > 0) {
                  this.timeout = sleep;
                  countDownLatch = new CountDownLatch(sleep);
                }
                zzZzZzz(sleep);
                sender().tell(Ping.INSTANCE, self());
              })
              .match(Ping.class, msg -> {
                zzZzZzz(msg);
                sender().tell(Pong.INSTANCE, self());
              })
              .match(Pong.class, msg -> {
                zzZzZzz(msg);
                sender().tell(Ping.INSTANCE, self());
              })
              .matchAny(o -> {
                log.info("Done.");
                System.exit(0);
              })
              .build();
    }
  }

  @SneakyThrows
  public static void main(String[] args) {
    final Config config = ConfigFactory.load(App.class.getClassLoader(), "/application.conf");
    final ActorSystem actorSystem = ActorSystem.create("ping-pong-system", config);
    final ActorRef actorRef = actorSystem.actorOf(Props.create(UntypedPingPongActor.class), "ping-pong-actor");
    final int duration = 100;

    actorRef.tell(duration, actorRef);
    sleep.accept(duration);
  }
}
