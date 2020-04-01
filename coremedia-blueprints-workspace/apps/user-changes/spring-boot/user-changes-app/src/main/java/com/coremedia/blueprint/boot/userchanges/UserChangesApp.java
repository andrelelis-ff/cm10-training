package com.coremedia.blueprint.boot.userchanges;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
public class UserChangesApp {

  // ... Bean definitions
  public static void main(String[] args) {
    SpringApplication.run(UserChangesApp.class, args);
  }
}
