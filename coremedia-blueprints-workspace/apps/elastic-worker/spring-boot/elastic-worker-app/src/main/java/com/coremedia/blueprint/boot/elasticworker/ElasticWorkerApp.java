package com.coremedia.blueprint.boot.elasticworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ElasticWorkerApp {

  // ... Bean definitions
  public static void main(String[] args) {
    SpringApplication.run(ElasticWorkerApp.class, args);
  }
}
