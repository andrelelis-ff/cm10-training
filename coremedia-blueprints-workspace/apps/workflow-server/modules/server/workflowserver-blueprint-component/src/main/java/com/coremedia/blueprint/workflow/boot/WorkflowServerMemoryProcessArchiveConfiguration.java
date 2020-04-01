package com.coremedia.blueprint.workflow.boot;

import com.coremedia.workflow.archive.memory.MemoryProcessArchiveConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

/**
 * Configuration class to optionally include cap list configuration XML.
 */
@Configuration
@ConditionalOnProperty(name = "elastic.core.persistence", havingValue = "memory")
@Import(MemoryProcessArchiveConfiguration.class)
public class WorkflowServerMemoryProcessArchiveConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(WorkflowServerMemoryProcessArchiveConfiguration.class);

  @PostConstruct
  void initialize() {
    LOG.info("Configuring memory process archive.");
  }

}
