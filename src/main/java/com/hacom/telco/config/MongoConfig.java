package com.hacom.telco.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;

@Configuration
public class MongoConfig {

    @Value("${mongodbDatabase}")
    private String mongodbDatabase;

    @Value("${mongodbUri}")
    private String mongodbUri;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongodbUri);
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(MongoClient mongoClient) {
        return new ReactiveMongoTemplate(new SimpleReactiveMongoDatabaseFactory(mongoClient, mongodbDatabase));
    }
}
