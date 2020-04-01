package com.coremedia.livecontext.asset.impl;


import com.coremedia.cap.common.infos.CapLicenseInfo;
import com.coremedia.cap.content.ContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@PropertySource("classpath:/framework/spring/lc-asset-services.properties")
class AssetConfiguration implements SmartLifecycle {

  static final String LICENSE_KEY_LC_ASSET_MANAGEMENT = "asset-management";
  private static final Logger LOG = LoggerFactory.getLogger(AssetConfiguration.class);

  private final AtomicBoolean running = new AtomicBoolean(false);

  @Inject
  private CapLicenseInfo capLicenseInfo;
  @Inject
  private ContentRepository contentRepository;

  private boolean licensed = false;
  private boolean preview = false;

  @Bean
  AssetServiceImpl assetService() {
    // asset service is an optional bean
    if (isFeatureActive()) {
      return new AssetServiceImpl();
    }
    LOG.warn("asset management is not active, bean 'assetService' not initialized");
    return null;
  }

  @Bean
  @Lazy
  AssetResolvingStrategy assetResolvingStrategy() {
    return new AssetResolvingStrategyImpl();
  }

  @Bean
  @Lazy
  AssetChangesRepositoryListener assetChangesRepositoryListener() {
    return new AssetChangesRepositoryListener();
  }

  @Bean
  @Lazy
  AssetChanges assetChanges() {
    return new AssetChanges();
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }

  @Override
  public void start() {
    if(!running.getAndSet(true)) {
      if(isFeatureActive()) {
        LOG.info("activating repository event listener for asset changes");
        assetChangesRepositoryListener().start();
      } else {
        LOG.warn("asset management is not active: disabling asset change events");
      }
    }
  }

  @Override
  public void stop() {
    if(running.getAndSet(false)) {
      assetChangesRepositoryListener().stop();
      LOG.info("disabled repository event listener for asset changes");
    }
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  @Override
  public int getPhase() {
    return 0;
  }

  @PostConstruct
  void initialize() {
    this.licensed = capLicenseInfo.isEnabled(LICENSE_KEY_LC_ASSET_MANAGEMENT);
    this.preview = contentRepository.isContentManagementServer();
  }

  boolean isFeatureActive() {
    return licensed || preview;
  }
}
