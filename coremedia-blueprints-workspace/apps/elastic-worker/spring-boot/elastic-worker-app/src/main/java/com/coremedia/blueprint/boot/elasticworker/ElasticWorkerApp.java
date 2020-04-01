package com.coremedia.blueprint.boot.elasticworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
public class ElasticWorkerApp {

  // ... Bean definitions
  public static void main(String[] args) {
    SpringApplication.run(ElasticWorkerApp.class, args);
  }
}
