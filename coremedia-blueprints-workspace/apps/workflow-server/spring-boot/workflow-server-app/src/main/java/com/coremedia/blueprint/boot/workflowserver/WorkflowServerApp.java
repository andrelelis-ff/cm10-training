package com.coremedia.blueprint.boot.workflowserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * we need to exclude some autoconfigurations:
 * - the datasource autoconfiguration
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class WorkflowServerApp {

  // ... Bean definitions
  public static void main(String[] args) {
    SpringApplication.run(WorkflowServerApp.class, args);
  }
}
