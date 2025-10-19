package com.hacom.telco.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import scala.concurrent.ExecutionContext;

@Configuration
public class AkkaConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public ActorSystem actorSystem() {
        ActorSystem system = ActorSystem.create("TelcoActorSystem");
        SpringExt.provider.get(system).initialize(applicationContext);
        return system;
    }

    @Bean("orderManagerActorRef")
    public ActorRef orderManagerActor(ActorSystem system) {
        return system.actorOf(SpringExt.provider.get(system).props("orderManagerActor"), "orderManagerActor");
    }

    @Bean
    public ExecutionContext executionContext(ActorSystem actorSystem) {
        return actorSystem.dispatcher();
    }
}
