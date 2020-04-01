package com.coremedia.blueprint.taxonomies.strategy;

import com.coremedia.blueprint.taxonomies.Taxonomy;
import com.coremedia.blueprint.taxonomies.TaxonomyNode;
import com.coremedia.blueprint.taxonomies.TaxonomyResolver;
import com.coremedia.blueprint.taxonomies.cycleprevention.TaxonomyCycleValidator;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.rest.cap.content.search.SearchService;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import static com.coremedia.blueprint.taxonomies.strategy.TaxonomyCreator.CM_TAXONOMY;
import static com.coremedia.blueprint.taxonomies.strategy.TaxonomyCreator.GLOBAL_CONFIG_PATH;
import static com.coremedia.blueprint.taxonomies.strategy.TaxonomyCreator.SITE_CONFIG_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

/**
 * Tests {@link TaxonomyResolverImpl}.
 */
@SpringJUnitConfig(classes = StrategyTestConfiguration.class)
@TestPropertySource(properties = "sitemodel.site.indicator.depth=0")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class TaxonomyResolverImplTest {
  private final SitesService sitesService;
  private final ContentRepository contentRepository;
  private final TaxonomyCreator taxonomyCreator;

  private String testMethodName;
  private Site site1;
  private Site site2;

  TaxonomyResolverImplTest(
          @Autowired SitesService sitesService,
          @Autowired ContentRepository contentRepository,
          @Autowired TaxonomyCreator taxonomyCreator
  ) {
    this.sitesService = sitesService;
    this.contentRepository = contentRepository;
    this.taxonomyCreator = taxonomyCreator;
  }

  @BeforeEach
  void setUp(TestInfo testInfo) {
    testMethodName = testInfo.getTestMethod().map(Method::getName).orElseThrow();
    site1 = taxonomyCreator.createSite("S1_", testInfo);
    site2 = taxonomyCreator.createSite("S2_", testInfo);
  }

  @AfterEach
  void tearDown() {
    /*
     * Discover test configuration on enclosing class for nested test class [SPR-15366] · Issue #19930 · spring-projects/spring-framework
     * https://github.com/spring-projects/spring-framework/issues/19930
     *
     * DirtiesContext does not work as expected for nested tests. Need to manually
     * clean repository.
     */
    cleanup(contentRepository.getRoot());
  }

  private static void cleanup(Content content) {
    if (content.isFolder()) {
      content.getChildren().forEach(TaxonomyResolverImplTest::cleanup);
    }
    if (!content.isRoot()) {
      content.destroy();
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Nested
  @DisplayName("TaxonomyResolverImpl.getTaxonomies")
  @SpringJUnitConfig(classes = StrategyTestConfiguration.class)
  class GetTaxonomiesMethod {

    private TaxonomyResolver resolver;

    @BeforeEach
    void setUp() {
      resolver = createResolver(Map.of());
    }

    @Test
    void shouldReturnEmptyTaxonomiesIfNoneAreAvailable() {
      Collection<Taxonomy> taxonomies = resolver.getTaxonomies();
      assertThat(taxonomies).isEmpty();
    }

    @Test
    void shouldReturnGlobalTaxonomies() {
      String taxonomyName = "global_" + testMethodName;
      taxonomyCreator.createGlobalSubjectTaxonomy(taxonomyName);
      Collection<Taxonomy> taxonomies = resolver.getTaxonomies();
      assertThat(taxonomies)
              .hasSize(1)
              .anySatisfy(tax -> {
                SoftAssertions assertions = new SoftAssertions();
                assertions.assertThat(tax.getTaxonomyId()).isEqualTo("Subject");
                assertions.assertThat(tax.getAllChildren()).hasSize(1);
                assertions.assertThat(tax.getAllChildren())
                        .anySatisfy((Consumer<TaxonomyNode>) node -> assertThat(node.getName()).isEqualTo(taxonomyName));
                assertions.assertAll();
              });
    }

    @Test
    void shouldReturnSiteSpecificTaxonomies() {
      String taxonomyName = "site1_" + testMethodName;
      taxonomyCreator.createSiteSpecificSubjectTaxonomy(site1, taxonomyName);
      Collection<Taxonomy> taxonomies = resolver.getTaxonomies();
      assertThat(taxonomies)
              .hasSize(1)
              .anySatisfy(tax -> {
                SoftAssertions assertions = new SoftAssertions();
                assertions.assertThat(tax.getTaxonomyId()).isEqualTo("Subject");
                assertions.assertThat(tax.getAllChildren()).hasSize(1);
                assertions.assertThat(tax.getAllChildren())
                        .anySatisfy((Consumer<TaxonomyNode>) node -> assertThat(node.getName()).isEqualTo(taxonomyName));
                assertions.assertAll();
              });
    }

    @Test
    void shouldReturnTaxonomiesFromAllSitesAndGlobal() {
      taxonomyCreator.createGlobalSubjectTaxonomy("global_" + testMethodName);
      taxonomyCreator.createSiteSpecificSubjectTaxonomy(site1, "site1_1_" + testMethodName);
      taxonomyCreator.createSiteSpecificSubjectTaxonomy(site1, "site1_2_" + testMethodName);
      taxonomyCreator.createSiteSpecificSubjectTaxonomy(site2, "site2_" + testMethodName);

      Collection<Taxonomy> taxonomies = resolver.getTaxonomies();

      assertThat(taxonomies)
              .hasSize(3)
              .anySatisfy(t -> assertThat(t.getSiteId()).isNull())
              .anySatisfy(t -> assertThat(t.getSiteId()).isEqualTo(site1.getId()))
              .anySatisfy(t -> assertThat(t.getSiteId()).isEqualTo(site2.getId()))
              .allSatisfy(tax -> {
                SoftAssertions assertions = new SoftAssertions();
                assertions.assertThat(tax.getTaxonomyId()).isEqualTo("Subject");
                assertions.assertThat(tax.getAllChildren()).isNotEmpty();
                assertions.assertAll();
              });
    }
  }

  @SuppressWarnings("rawtypes")
  @Nested
  @DisplayName("TaxonomyResolver.getTaxonomy")
  @SpringJUnitConfig(classes = StrategyTestConfiguration.class)
  class GetTaxonomyMethod {
    private TaxonomyResolver resolver;

    @BeforeEach
    void setUp() {
      resolver = createResolver(Map.of());
    }

    @Test
    void shouldReturnNullForNoTaxonomiesAvailable() {
      assertThat(resolver.getTaxonomy(null, "some")).isNull();
    }

    @Test
    void shouldAlwaysReturnGlobalTaxonomyIfNoSiteSpecificExists() {
      taxonomyCreator.createGlobalSubjectTaxonomy("global_" + testMethodName);
      SoftAssertions assertions = new SoftAssertions();
      Taxonomy globalTaxonomy = resolver.getTaxonomy(null, "Subject");
      assertions.assertThat(globalTaxonomy).satisfies(t -> assertThat(t.getSiteId()).isNull());
      assertions.assertThat(resolver.getTaxonomy(site1.getId(), "Subject")).isSameAs(globalTaxonomy);
      assertions.assertThat(resolver.getTaxonomy(site2.getId(), "Subject")).isSameAs(globalTaxonomy);
      assertions.assertThat(resolver.getTaxonomy("non-existing-site-id", "Subject")).isSameAs(globalTaxonomy);
      assertions.assertAll();
    }

    @Test
    void shouldPreferSiteSpecificTaxonomyOverGlobalTaxonomy() {
      taxonomyCreator.createGlobalSubjectTaxonomy("global_" + testMethodName);
      taxonomyCreator.createSiteSpecificSubjectTaxonomy(site1, "site1_" + testMethodName);

      Taxonomy globalTaxonomy = resolver.getTaxonomy(null, "Subject");
      Taxonomy siteTaxonomy = resolver.getTaxonomy(site1.getId(), "Subject");

      assertThat(globalTaxonomy).isNotNull();
      assertThat(siteTaxonomy).isNotNull();
      assertThat(siteTaxonomy).isNotSameAs(globalTaxonomy);

      assertThat(globalTaxonomy.getSiteId()).isNull();
      assertThat(siteTaxonomy.getSiteId()).isEqualTo(site1.getId());
    }

    @Test
    void shouldFallbackToGlobalTaxonomyForSiteWithoutTaxonomy() {
      taxonomyCreator.createGlobalSubjectTaxonomy("global_" + testMethodName);
      taxonomyCreator.createSiteSpecificSubjectTaxonomy(site1, "site1_" + testMethodName);

      Taxonomy globalTaxonomy = resolver.getTaxonomy(null, "Subject");
      Taxonomy site1Taxonomy = resolver.getTaxonomy(site1.getId(), "Subject");
      Taxonomy site2Taxonomy = resolver.getTaxonomy(site2.getId(), "Subject");

      assertThat(globalTaxonomy).isNotNull();
      assertThat(site1Taxonomy).isNotNull();
      assertThat(site1Taxonomy).isNotSameAs(globalTaxonomy);
      assertThat(site2Taxonomy).isSameAs(globalTaxonomy);
    }

    @Test
    void shouldRespectAliasForGlobalTaxonomy() {
      String alias = "tcejbuS";
      TaxonomyResolver aliasResolver = createResolver(Map.of(alias, "Subject"));
      taxonomyCreator.createGlobalSubjectTaxonomy("global_" + testMethodName);

      Taxonomy directTaxonomy = aliasResolver.getTaxonomy(null, "Subject");
      Taxonomy aliasTaxonomy = aliasResolver.getTaxonomy(null, alias);

      assertThat(aliasTaxonomy).isSameAs(directTaxonomy);
    }

    @Test
    void shouldRespectAliasForSiteSpecificTaxonomy() {
      String alias = "tcejbuS";
      TaxonomyResolver aliasResolver = createResolver(Map.of(alias, "Subject"));
      taxonomyCreator.createGlobalSubjectTaxonomy("global_" + testMethodName);
      taxonomyCreator.createSiteSpecificSubjectTaxonomy(site1, "site1_" + testMethodName);

      Taxonomy globalTaxonomy = aliasResolver.getTaxonomy(null, "Subject");
      Taxonomy directTaxonomy = aliasResolver.getTaxonomy(site1.getId(), "Subject");
      Taxonomy aliasTaxonomy = aliasResolver.getTaxonomy(site1.getId(), alias);

      assertThat(directTaxonomy).isNotSameAs(globalTaxonomy);
      assertThat(aliasTaxonomy).isSameAs(directTaxonomy);
    }

    @Test
    void shouldRespectAliasForSiteFallbackToGlobalTaxonomy() {
      String alias = "tcejbuS";
      TaxonomyResolver aliasResolver = createResolver(Map.of(alias, "Subject"));
      taxonomyCreator.createGlobalSubjectTaxonomy("global_" + testMethodName);
      taxonomyCreator.createSiteSpecificSubjectTaxonomy(site1, "site1_" + testMethodName);

      Taxonomy globalTaxonomy = aliasResolver.getTaxonomy(null, "Subject");
      Taxonomy directTaxonomy = aliasResolver.getTaxonomy(site2.getId(), "Subject");
      Taxonomy aliasTaxonomy = aliasResolver.getTaxonomy(site2.getId(), alias);

      assertThat(directTaxonomy).isSameAs(globalTaxonomy);
      assertThat(aliasTaxonomy).isSameAs(directTaxonomy);
    }
  }

  private TaxonomyResolver createResolver(Map<String, String> aliasMapping) {
    return new TaxonomyResolverImpl(
            sitesService,
            contentRepository,
            Mockito.mock(SearchService.class),
            Mockito.mock(TaxonomyCycleValidator.class),
            aliasMapping,
            CM_TAXONOMY,
            SITE_CONFIG_PATH,
            GLOBAL_CONFIG_PATH
    );
  }

}
