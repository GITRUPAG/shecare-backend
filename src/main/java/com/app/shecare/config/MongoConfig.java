package com.app.shecare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Bean
    public MongoClient mongoClient() {

        String uri = System.getenv("SPRING_DATA_MONGODB_URI");

        System.out.println("✅ Using Mongo URI: " + uri);

        return MongoClients.create(new ConnectionString(uri));
    }
}