package com.hacom.telco.grpc;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.google.protobuf.Timestamp;
import com.hacom.telco.dto.OrderDTO;
import com.hacom.telco.grpc.OrderServiceGrpc.OrderServiceImplBase;
import com.hacom.telco.service.OrderService;
import com.hacom.telco.service.MetricService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Mono;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

@GrpcService
public class OrderServiceImpl extends OrderServiceImplBase {

    private final ActorRef orderManagerActor;
    private final ExecutionContext executionContext;
    private final OrderService orderService;
    private final MetricService metricService;

    public OrderServiceImpl(@Qualifier("orderManagerActorRef") ActorRef orderManagerActor, ExecutionContext executionContext, OrderService orderService, MetricService metricService) {
        this.orderManagerActor = orderManagerActor;
        this.executionContext = executionContext;
        this.orderService = orderService;
        this.metricService = metricService;
    }

    @Override
    public void createOrder(OrderRequest request, StreamObserver<CreateOrderResponse> responseObserver) {
        if (request.getCustomerId().isEmpty()) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Customer ID cannot be null")
                    .asRuntimeException());
            return;
        }
        if (request.getOrderId().isEmpty()) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Order ID cannot be empty")
                    .asRuntimeException());
            return;
        }
        if (request.getCustomerPhoneNumber().isEmpty()) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Customer Phone Number cannot be empty")
                    .asRuntimeException());
            return;
        }
        // Basic phone number validation (e.g., +1234567890, 123-456-7890, 123 456 7890)
        if (!request.getCustomerPhoneNumber().matches("^\\+?[0-9\\s\\-]{7,20}$")) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Invalid Customer Phone Number format")
                    .asRuntimeException());
            return;
        }
        if (request.getItemsList().isEmpty()) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Item List cannot be empty")
                    .asRuntimeException());
            return;
        }
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(request.getOrderId());
        orderDTO.setCustomerId(request.getCustomerId());
        orderDTO.setCustomerPhoneNumber(request.getCustomerPhoneNumber());
        orderDTO.setItems(request.getItemsList());



        Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
        Future<Object> future = Patterns.ask(orderManagerActor, orderDTO, timeout);

        future.onComplete(result -> {
            if (result.isSuccess()) {
                OrderDTO createdOrder = (OrderDTO) result.get();
                CreateOrderResponse response = CreateOrderResponse.newBuilder()
                        .setOrderId(createdOrder.getOrderId())
                        .setStatus(createdOrder.getStatus())
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error creating order: " + result.failed().get().getMessage())
                        .asRuntimeException());
            }
            return null;
        }, executionContext);
    }

    @Override
    public void getOrderStatus(GetOrderStatusRequest request, StreamObserver<GetOrderStatusResponse> responseObserver) {
        if (request.getOrderId().isEmpty()) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Order ID cannot be empty")
                    .asRuntimeException());
            return;
        }

        orderService.getOrderByOrderId(request.getOrderId())
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .subscribe(orderDTO -> {
                    GetOrderStatusResponse response = GetOrderStatusResponse.newBuilder()
                            .setOrderId(orderDTO.getOrderId())
                            .setStatus(orderDTO.getStatus())
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }, error -> {
                    responseObserver.onError(io.grpc.Status.NOT_FOUND
                            .withDescription(error.getMessage())
                            .asRuntimeException());
                });
    }

    @Override
    public void getOrdersByDateRange(GetOrdersByDateRangeRequest request, StreamObserver<GetOrdersByDateRangeResponse> responseObserver) {
        Timestamp startTimestamp = request.getStartDate();
        Timestamp endTimestamp = request.getEndDate();

        Instant startInstant = Instant.ofEpochSecond(startTimestamp.getSeconds(), startTimestamp.getNanos());
        OffsetDateTime startDate = OffsetDateTime.ofInstant(startInstant, ZoneOffset.UTC);

        Instant endInstant = Instant.ofEpochSecond(endTimestamp.getSeconds(), endTimestamp.getNanos());
        OffsetDateTime endDate = OffsetDateTime.ofInstant(endInstant, ZoneOffset.UTC);

        orderService.getOrdersByDateRange(startDate, endDate)
                .map(orderDTO -> {
                    Timestamp timestamp = Timestamp.newBuilder()
                            .setSeconds(orderDTO.getTs().getEpochSecond())
                            .setNanos(orderDTO.getTs().getNano())
                            .build();
                    return Order.newBuilder()
                            .setId(orderDTO.get_id())
                            .setOrderId(orderDTO.getOrderId())
                            .setCustomerId(orderDTO.getCustomerId())
                            .setCustomerPhoneNumber(orderDTO.getCustomerPhoneNumber())
                            .setStatus(orderDTO.getStatus())
                            .addAllItems(orderDTO.getItems())
                            .setTs(timestamp)
                            .build();
                })
                .collectList()
                .subscribe(orderResponses -> {
                    GetOrdersByDateRangeResponse response = GetOrdersByDateRangeResponse.newBuilder()
                            .addAllOrders(orderResponses)
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }, error -> {
                    responseObserver.onError(io.grpc.Status.INTERNAL
                            .withDescription("Error getting orders by date range: " + error.getMessage())
                            .asRuntimeException());
                });
    }
}
