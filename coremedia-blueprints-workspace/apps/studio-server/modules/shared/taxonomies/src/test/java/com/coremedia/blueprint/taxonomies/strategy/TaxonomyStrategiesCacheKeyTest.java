package com.coremedia.blueprint.taxonomies.strategy;

import com.coremedia.blueprint.taxonomies.Taxonomy;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.reflect.Method;
import java.util.Map;

import static com.coremedia.blueprint.taxonomies.strategy.ContentTaxonomy.createTaxonomy;
import static com.coremedia.blueprint.taxonomies.strategy.TaxonomyCreator.CM_TAXONOMY;
import static com.coremedia.blueprint.taxonomies.strategy.TaxonomyCreator.GLOBAL_CONFIG_PATH;
import static com.coremedia.blueprint.taxonomies.strategy.TaxonomyCreator.SITE_CONFIG_PATH;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

/**
 * Tests {@link TaxonomyStrategiesCacheKey}.
 */
@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig(classes = StrategyTestConfiguration.class)
@TestPropertySource(properties = "sitemodel.site.indicator.depth=0")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class TaxonomyStrategiesCacheKeyTest {
  private final SitesService sitesService;
  private final CapConnection connection;
  private final TaxonomyCreator taxonomyCreator;

  private Site site;
  private ContentRepository contentRepository;
  private String testMethodName;

  TaxonomyStrategiesCacheKeyTest(@Autowired SitesService sitesService,
                                 @Autowired CapConnection connection,
                                 @Autowired TaxonomyCreator taxonomyCreator) {
    this.sitesService = sitesService;
    this.connection = connection;
    this.taxonomyCreator = taxonomyCreator;
  }

  @BeforeEach
  void setUp(TestInfo testInfo) {
    contentRepository = connection.getContentRepository();
    site = createSite(testInfo);
    testMethodName = testInfo.getTestMethod().map(Method::getName).orElseThrow();
  }

  @Test
  void taxonomiesShouldBeEmptyIfNotExisting() {
    CreateTaxonomyFunction createTaxonomyFunction = (rootFolder, siteId, type) -> createTaxonomy();
    Map<String, Taxonomy<Content>> taxonomyMap = connection.getCache().get(createCacheKey(createTaxonomyFunction));
    assertThat(taxonomyMap).isEmpty();
  }

  @Test
  void shouldSkipInvalidTaxonomies() {
    createGlobalSubjectTaxonomy(format("tax_%s", testMethodName));
    CreateTaxonomyFunction createTaxonomyFunction = (rootFolder, siteId, type) -> createTaxonomy(false);
    Map<String, Taxonomy<Content>> taxonomyMap = connection.getCache().get(createCacheKey(createTaxonomyFunction));
    assertThat(taxonomyMap).isEmpty();
  }

  @Test
  void shouldProvideExistingGlobalTaxonomies() {
    String taxonomyName = format("tax_%s", testMethodName);
    createGlobalSubjectTaxonomy(taxonomyName);

    CreateTaxonomyFunction createTaxonomyFunction = (rootFolder, siteId, type) -> createTaxonomy(taxonomyName, siteId, true);
    Map<String, Taxonomy<Content>> taxonomyMap = connection.getCache().get(createCacheKey(createTaxonomyFunction));
    assertThat(taxonomyMap).hasSize(1);
    assertThat(taxonomyMap).hasKeySatisfying(new Condition<>("key contains taxonomy id") {
      @Override
      public boolean matches(String value) {
        return value.contains(taxonomyName);
      }
    });
  }

  @Test
  void shouldUpdateOnCreatedGlobalTaxonomies() {
    String taxonomyName = format("tax_%s", testMethodName);

    CreateTaxonomyFunction createTaxonomyFunction = (rootFolder, siteId, type) -> createTaxonomy(taxonomyName, siteId, true);
    Map<String, Taxonomy<Content>> taxonomyMap = connection.getCache().get(createCacheKey(createTaxonomyFunction));
    assertThat(taxonomyMap).isEmpty();

    createGlobalSubjectTaxonomy(taxonomyName);

    taxonomyMap = connection.getCache().get(createCacheKey(createTaxonomyFunction));
    assertThat(taxonomyMap).hasSize(1);
    assertThat(taxonomyMap).hasKeySatisfying(new Condition<>("key contains taxonomy id") {
      @Override
      public boolean matches(String value) {
        return value.contains(taxonomyName);
      }
    });
  }

  @Test
  void shouldProvideExistingSiteSpecificTaxonomies() {
    String taxonomyName = format("tax_%s", testMethodName);
    createSiteSpecificSubjectTaxonomy(taxonomyName);

    CreateTaxonomyFunction createTaxonomyFunction = (rootFolder, siteId, type) -> createTaxonomy(taxonomyName, siteId, true);
    Map<String, Taxonomy<Content>> taxonomyMap = connection.getCache().get(createCacheKey(createTaxonomyFunction));
    assertThat(taxonomyMap).hasSize(1);
    assertThat(taxonomyMap).hasKeySatisfying(new Condition<>("key contains taxonomy id") {
      @Override
      public boolean matches(String value) {
        return value.contains(taxonomyName);
      }
    });
  }

  @Test
  void shouldProvideExistingSiteSpecificAndGlobalTaxonomies() {
    String taxonomyName = format("tax_%s", testMethodName);
    createGlobalSubjectTaxonomy(taxonomyName);
    createSiteSpecificSubjectTaxonomy(taxonomyName);

    CreateTaxonomyFunction createTaxonomyFunction = (rootFolder, siteId, type) -> createTaxonomy(taxonomyName, siteId, true);
    Map<String, Taxonomy<Content>> taxonomyMap = connection.getCache().get(createCacheKey(createTaxonomyFunction));
    assertThat(taxonomyMap).hasSize(2);
  }

  private TaxonomyStrategiesCacheKey createCacheKey(CreateTaxonomyFunction createTaxonomyFunction) {
    return new TaxonomyStrategiesCacheKey(contentRepository, sitesService, CM_TAXONOMY, SITE_CONFIG_PATH, GLOBAL_CONFIG_PATH, createTaxonomyFunction);
  }

  private void createGlobalSubjectTaxonomy(String id) {
    taxonomyCreator.createGlobalSubjectTaxonomy(id);
  }

  private void createSiteSpecificSubjectTaxonomy(String id) {
    taxonomyCreator.createSiteSpecificSubjectTaxonomy(site, id);
  }

  private Site createSite(TestInfo testInfo) {
    return taxonomyCreator.createSite(testInfo);
  }

}
