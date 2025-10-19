package com.hacom.telco.service;

import com.hacom.telco.dto.OrderDTO;
import com.hacom.telco.model.Order;
import org.bson.types.ObjectId;
import com.hacom.telco.repository.OrderRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    public Flux<OrderDTO> getAllOrders() {
        return orderRepository.findAll().map(this::toDTO);
    }

    public Mono<OrderDTO> getOrderById(String id) {
        return orderRepository.findById(new ObjectId(id)).map(this::toDTO);
    }

    public Mono<OrderDTO> getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId).map(this::toDTO);
    }

    public Flux<OrderDTO> getOrdersByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        return orderRepository.findByTsBetween(startDate.toInstant(), endDate.toInstant()).map(this::toDTO);
    }

    public Mono<OrderDTO> createOrder(OrderDTO orderDTO) {
        logger.info("Creating order: {}", orderDTO);
        orderDTO.setStatus("PENDING");
        orderDTO.setTs(Instant.now());
        Order order = toEntity(orderDTO);
        return orderRepository.save(order)
                .doOnError(error -> logger.error("Error saving order: {}", error.getMessage()))
                .map(this::toDTO);
    }

    public Mono<OrderDTO> updateOrder(String id, OrderDTO orderDTO) {
        return orderRepository.findById(new ObjectId(id))
                .flatMap(existingOrder -> {
                    existingOrder.setOrderId(orderDTO.getOrderId());
                    existingOrder.setCustomerId(orderDTO.getCustomerId());
                    existingOrder.setCustomerPhoneNumber(orderDTO.getCustomerPhoneNumber());
                    existingOrder.setStatus(orderDTO.getStatus());
                    existingOrder.setItems(orderDTO.getItems());
                    existingOrder.setTs(orderDTO.getTs());
                    return orderRepository.save(existingOrder);
                })
                .map(this::toDTO);
    }

    public Mono<Void> deleteOrder(String id) {
        return orderRepository.deleteById(new ObjectId(id));
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.set_id(order.get_id().toHexString());
        dto.setOrderId(order.getOrderId());
        dto.setCustomerId(order.getCustomerId());
        dto.setCustomerPhoneNumber(order.getCustomerPhoneNumber());
        dto.setStatus(order.getStatus());
        dto.setItems(order.getItems());
        dto.setTs(order.getTs());
        return dto;
    }

    private Order toEntity(OrderDTO dto) {
        Order order = new Order();
        if (dto.get_id() != null) {
            order.set_id(new ObjectId(dto.get_id()));
        }
        order.setOrderId(dto.getOrderId());
        order.setCustomerId(dto.getCustomerId());
        order.setCustomerPhoneNumber(dto.getCustomerPhoneNumber());
        order.setStatus(dto.getStatus());
        order.setItems(dto.getItems());
        order.setTs(dto.getTs());
        return order;
    }
}
