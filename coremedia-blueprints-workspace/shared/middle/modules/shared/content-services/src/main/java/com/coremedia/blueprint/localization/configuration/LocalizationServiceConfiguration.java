package com.coremedia.blueprint.localization.configuration;

import com.coremedia.blueprint.localization.BundleResolver;
import com.coremedia.blueprint.localization.ContentBundleResolver;
import com.coremedia.blueprint.localization.LocalResourcesBundleResolver;
import com.coremedia.blueprint.localization.LocalizationService;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.StructService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.io.File;

@Configuration
@ImportResource(
        value = {
                "classpath:/com/coremedia/cap/multisite/multisite-services.xml"
        },
        reader = ResourceAwareXmlBeanDefinitionReader.class)
public class LocalizationServiceConfiguration {
  private static final Logger LOG = LoggerFactory.getLogger(LocalizationServiceConfiguration.class);

  private StructService structService;
  private SitesService sitesService;

  private boolean useLocalResources;
  private File blueprintDir;

  /**
   * Set the file path to the local blueprint workspace, excl. the modules
   * directory.
   * <p>
   * Required if {@link #setUseLocalResources(boolean)} is true.
   */
  @Value("${coremedia.blueprint.project.directory:}")
  public void setBlueprintPath(String blueprintPath) {
    if (!blueprintPath.isEmpty()) {
      blueprintDir = new File(blueprintPath);
      if (!blueprintDir.exists() || !blueprintDir.isDirectory() || !blueprintDir.canRead()) {
        throw new IllegalArgumentException("blueprintPath \"" + blueprintPath + "\" is no suitable directory.");
      }
    }
  }

  /**
   * Activate a {@link LocalResourcesBundleResolver} for faster frontend
   * development roundtrips.
   * <p>
   * Do not set in production setups!
   */
  @Value("${cae.use.local.resources:false}")
  public void setUseLocalResources(boolean useLocalResources) {
    this.useLocalResources = useLocalResources;
  }

  @Inject
  public void setStructService(StructService structService) {
    this.structService = structService;
  }

  @Resource(name="sitesService")
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Bean(name="localizationService")
  public LocalizationService localizationService() {
    BundleResolver bundleResolver = new ContentBundleResolver();
    if (useLocalResources && blueprintDir!=null) {
      bundleResolver = new LocalResourcesBundleResolver(bundleResolver, structService, blueprintDir);
      LOG.info("Enabled local resource bundles in {}", blueprintDir.getAbsolutePath());
    }
    return new LocalizationService(structService, sitesService, bundleResolver);
  }
}
