package com.coremedia.livecontext.ecommerce.ibm.cae.storefront;

import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.catalog.CatalogServiceImplBodBasedIT;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests for BOD REST interface.
 */
@ContextConfiguration(classes = {
        IbmServiceTestBase.LocalConfig.class,
        StoreFrontConfiguration.class,
        CatalogServiceImplBodBasedIT.LocalConfig.class})
public class B2BCatalogServiceBodBasedIT extends AbstractB2BCatalogServiceIT {
  @Configuration
  @Import(XmlRepoConfiguration.class)
  @Profile(IbmServiceTestBase.LocalConfig.PROFILE)
  public static class LocalConfig {
  }

  @Test
  @Override
  public void testFindSubCategoriesWithContract() throws Exception {
    super.testFindSubCategoriesWithContract();
  }
}
