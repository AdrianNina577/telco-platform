package com.hacom.telco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoReactiveAutoConfiguration.class, MongoReactiveDataAutoConfiguration.class})
@EnableReactiveMongoRepositories
public class HacomTelcoPlatformApplication {

	    public static void main(String[] args) {
	        new org.springframework.boot.builder.SpringApplicationBuilder(HacomTelcoPlatformApplication.class)
	                .web(org.springframework.boot.WebApplicationType.REACTIVE)
	                .run(args);
	    }
}
