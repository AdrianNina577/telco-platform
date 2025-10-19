package com.hacom.telco.repository;

import com.hacom.telco.model.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, ObjectId> {
    Mono<Order> findByOrderId(String orderId);
    Flux<Order> findByTsBetween(Instant start, Instant end);
}
