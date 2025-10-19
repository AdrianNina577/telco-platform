package com.hacom.telco.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.hacom.telco.config.SpringExt;
import com.hacom.telco.dto.OrderDTO;
import com.hacom.telco.service.OrderService;
import com.hacom.telco.service.MetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static akka.pattern.Patterns.pipe;

@Component("orderProcessorActor")
@Scope("prototype")
public class OrderProcessorActor extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessorActor.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private MetricService metricService;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderDTO.class, orderDTO -> {
                    Mono<OrderDTO> resultMono = orderService.createOrder(orderDTO)
                            .doOnSuccess(savedOrder -> logger.info("Order created by actor: {}", savedOrder.getOrderId()))
                            .flatMap(savedOrder -> {
                                savedOrder.setStatus("PROCESSED");
                                return orderService.updateOrder(savedOrder.get_id(), savedOrder); // Use updateOrder
                            })
                            .doOnSuccess(processedOrder -> {
                                logger.info("Order {} status updated to PROCESSED.", processedOrder.getOrderId());
                                metricService.incrementProcessedOrders();
                                ActorRef smsSenderActor = getContext().actorOf(SpringExt.provider.get(getContext().getSystem()).props("smsSenderActor"));
                                smsSenderActor.tell(new SmsSenderActor.SmsRequest(processedOrder.getCustomerPhoneNumber(), "Your order " + processedOrder.getOrderId() + " has been processed."), getSelf());
                            });
                    pipe(resultMono.toFuture(), getContext().dispatcher()).to(getSender());
                })
                .build();
    }
}
