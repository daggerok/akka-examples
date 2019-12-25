package com.github.daggerok;

import akka.actor.ActorRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Hello actor tests")
class AppTest {

  @Test
  void main() {
    App.main(new String[0]);
  }

  @Test
  void context() {
    var ctx = new AnnotationConfigApplicationContext(App.class);
    assertThat(ctx).isNotNull();

    ctx.getBean(ActorRef.class).tell(Message.withBody("test"), ActorRef.noSender());
  }
}
