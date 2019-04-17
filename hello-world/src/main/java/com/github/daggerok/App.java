package com.github.daggerok;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import io.vavr.control.Try;
import lombok.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

// tag::content[]
@Value(staticConstructor = "withBody")
class Message {
  private final String body;
}

class HelloWorld extends AbstractLoggingActor {

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(Message.class, message -> onMessage(message))
                           .build();
  }

  private void onMessage(Message message) {
    log().info("received {}", message);
  }
}

@Configuration
class AkkaCfg {

  @Bean
  ActorSystem helloSystem() {
    return ActorSystem.create("hello-world");
  }

  @Bean
  Props helloProps() {
    return Props.create(HelloWorld.class);
  }

  @Bean
  ActorRef helloRef(ActorSystem helloSystem, Props helloProps) {
    return helloSystem.actorOf(helloProps, "hello-actor");
  }
}

@Configuration
@ComponentScan(basePackageClasses = App.class)
public class App {

  public static void main(String[] args) {
    var ctx = new AnnotationConfigApplicationContext(App.class);
    var helloRef = ctx.getBean(ActorRef.class);

    helloRef.tell(Message.withBody("Max"), ActorRef.noSender());
    helloRef.tell(Message.withBody("Bob"), ActorRef.noSender());
    helloRef.tell(Message.withBody("Everyone"), ActorRef.noSender());

    Try.run(() -> TimeUnit.SECONDS.sleep(1))
       .andFinally(() -> ctx.getBean(ActorSystem.class).terminate());
  }
}
// end::content[]
