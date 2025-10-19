package com.hacom.telco.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.hacom.telco.config.SpringExt;
import com.hacom.telco.dto.OrderDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("orderManagerActor")
@Scope("prototype")
public class OrderManagerActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderDTO.class, orderDTO -> {
                    ActorRef orderProcessor = getContext().actorOf(SpringExt.provider.get(getContext().getSystem()).props("orderProcessorActor"));
                    orderProcessor.forward(orderDTO, getContext());
                    
                })
                .build();
    }
}