package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.tree.CommerceTreeRelation;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.util.List;

import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for CommerceTreeRelation below module bpbase-lc-common.
 * Since we want to make use of the ibm hoverfly infrastructure the test is below the ibm module for now.
 */
@ExtendWith({SwitchableHoverflyExtension.class, SpringExtension.class})
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_CommerceTreeRelationTestIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true
        )
)
@ContextConfiguration(classes = {IbmServiceTestBase.LocalConfig.class, CommerceTreeRelationTestIT.LocalConfig.class})
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
public class CommerceTreeRelationTestIT extends IbmServiceTestBase {

  @Configuration
  @Profile(IbmServiceTestBase.LocalConfig.PROFILE)
  public static class LocalConfig {
    @Bean
    public CommerceTreeRelation commerceTreeRelation() {
      return new CommerceTreeRelation();
    }
  }

  private String CATEGORY_SEO_SEGMENT = "pc-on-the-table";

  @Inject
  private CommerceTreeRelation testling;

  @Inject
  private CatalogService catalogService;

  @Test
  void testGetChildrenOf() {
    Category testCategory = getCategory();
    assertThat(testling.getChildrenOf(testCategory)).isNotNull();
  }

  @Test
  void testGetRootCategory() {
    Category topCategory = getCategory();
    Category rootCategory = testling.getParentOf(topCategory);
    assertThat(rootCategory).isNotNull();
    assertThat(rootCategory.getParent()).isNull();
    assertThat(rootCategory.getExternalId()).isEqualTo(CategoryImpl.ROOT_CATEGORY_ROLE_ID);
    assertThat(testling.isRoot(rootCategory)).isTrue();
    assertThat(rootCategory.getTitle()).isNull();
  }

  @Test
  void testPathToRoot() {
    Category category1 = getCategory();
    Category category2 = category1.getChildren().get(0);
    assertThat(category2).isNotNull();

    List<Category> pathToRoot = testling.pathToRoot(category2);
    assertThat(pathToRoot.size()).isEqualTo(3);
    assertThat(category2.getBreadcrumb().size()).isEqualTo(2);

    assertThat(pathToRoot.get(0).getExternalId()).isEqualTo(CategoryImpl.ROOT_CATEGORY_ROLE_ID);
  }

  private Category getCategory() {
    return catalogService.findCategoryBySeoSegment(CATEGORY_SEO_SEGMENT, storeContext);
  }
}
