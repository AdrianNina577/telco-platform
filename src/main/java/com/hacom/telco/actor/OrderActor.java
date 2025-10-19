package com.hacom.telco.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.hacom.telco.grpc.CreateOrderResponse;
import com.hacom.telco.grpc.OrderRequest;
import com.hacom.telco.model.Order;
import com.hacom.telco.repository.OrderRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public class OrderActor extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(OrderActor.class);

    private final OrderRepository orderRepository;
    private final Validator validator;

    public static Props props(OrderRepository orderRepository, Validator validator) {
        return Props.create(OrderActor.class, () -> new OrderActor(orderRepository, validator));
    }

    public OrderActor(OrderRepository orderRepository, Validator validator) {
        this.orderRepository = orderRepository;
        this.validator = validator;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderProcessingRequest.class, this::processOrder)
                .build();
    }

    private void processOrder(OrderProcessingRequest request) {
        Order order = new Order();
        order.setOrderId(request.getRequest().getOrderId());
        order.setCustomerId(request.getRequest().getCustomerId());
        order.setCustomerPhoneNumber(request.getRequest().getCustomerPhoneNumber());
        order.setItems(request.getRequest().getItemsList());
        order.setStatus("PENDING");
        order.setTs(Instant.now());

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            request.getResponseObserver().onError(Status.INVALID_ARGUMENT
                    .withDescription(errorMessage)
                    .asException());
            return;
        }

        orderRepository.save(order).subscribe(savedOrder -> {
            logger.info("Order created by actor: {}", savedOrder.getOrderId());
            CreateOrderResponse response = CreateOrderResponse.newBuilder()
                    .setOrderId(savedOrder.getOrderId())
                    .setStatus("PROCESSED")
                    .build();

            request.getResponseObserver().onNext(response);
            request.getResponseObserver().onCompleted();
        });
    }

    public static class OrderProcessingRequest {
        private final OrderRequest request;
        private final StreamObserver<CreateOrderResponse> responseObserver;

        public OrderProcessingRequest(OrderRequest request, StreamObserver<CreateOrderResponse> responseObserver) {
            this.request = request;
            this.responseObserver = responseObserver;
        }

        public OrderRequest getRequest() {
            return request;
        }

        public StreamObserver<CreateOrderResponse> getResponseObserver() {
            return responseObserver;
        }
    }
}
