package com.hacom.telco;

import com.hacom.telco.grpc.OrderServiceGrpc;
import com.hacom.telco.grpc.OrderRequest;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "grpc.server.inProcessChannelName=test",
        "grpc.server.port=-1",
        "grpc.client.orderService.address=in-process:test"
})
@SpringJUnitConfig
@DirtiesContext
class HacomTelcoPlatformApplicationTests {

    @GrpcClient("orderService")
    private OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub;

    @Test
    void contextLoads() {
    }

}
