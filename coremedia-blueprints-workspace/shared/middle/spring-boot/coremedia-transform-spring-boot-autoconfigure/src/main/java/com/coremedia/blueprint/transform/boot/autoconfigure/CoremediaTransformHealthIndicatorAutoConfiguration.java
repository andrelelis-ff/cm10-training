package com.coremedia.blueprint.transform.boot.autoconfigure;

import com.coremedia.transform.impl.TransformedBlobCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.system.DiskSpaceHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.io.File;

@Configuration
public class CoremediaTransformHealthIndicatorAutoConfiguration {

  @Configuration
  @ConditionalOnBean(name = "transformedBlobCache", value = TransformedBlobCache.class)
  @ConditionalOnClass(TransformedBlobCache.class)
  @ConditionalOnEnabledHealthIndicator("transformedBlobCacheDiskSpace")
  @ConditionalOnProperty("com.coremedia.transform.blobCache.basePath")
  public static class TransformedBlobCacheDiskSpaceHealthIndicatorConfiguration {
    @Value("${management.health.transformedBlobCacheDiskspace.threshold:20971520}")
    private long diskspaceThreshold;

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public DiskSpaceHealthIndicator transformedBlobCacheDiskSpaceHealthIndicator(TransformedBlobCache transformedBlobCache) {
      File transformedBlobCachePath = transformedBlobCache.getBasePath();
      return new DiskSpaceHealthIndicator(transformedBlobCachePath, DataSize.ofBytes(diskspaceThreshold));
    }
  }
}
