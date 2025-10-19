package com.hacom.telco.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricService {

    private final Counter processedOrdersCounter;

    public MetricService(MeterRegistry meterRegistry) {
        this.processedOrdersCounter = Counter.builder("telco_orders_processed_total")
                .description("Total number of processed orders")
                .register(meterRegistry);
    }

    public void incrementProcessedOrders() {
        processedOrdersCounter.increment();
    }
}