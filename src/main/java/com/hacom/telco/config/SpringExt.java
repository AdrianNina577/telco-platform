package com.hacom.telco.config;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import akka.actor.Props;
import org.springframework.context.ApplicationContext;

public class SpringExt extends AbstractExtensionId<SpringExt.SpringExtImpl> {

    public static final SpringExt provider = new SpringExt();

    @Override
    public SpringExtImpl createExtension(ExtendedActorSystem system) {
        return new SpringExtImpl();
    }

    public static class SpringExtImpl implements Extension {
        private volatile ApplicationContext applicationContext;

        public void initialize(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        public Props props(String actorBeanName) {
            return Props.create(SpringActorProducer.class, applicationContext, actorBeanName);
        }
    }
}
