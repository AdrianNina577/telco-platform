package com.hacom.telco.config;

import net.devh.boot.grpc.server.config.GrpcServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class GrpcConfig {

    @Value("${apiPort}")
    private int apiPort;

    @Autowired
    private GrpcServerProperties grpcServerProperties;

    @PostConstruct
    public void setGrpcPort() {
        grpcServerProperties.setPort(apiPort);
    }
}
