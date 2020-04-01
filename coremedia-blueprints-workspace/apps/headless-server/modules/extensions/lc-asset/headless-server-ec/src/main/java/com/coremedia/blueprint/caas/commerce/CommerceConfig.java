package com.coremedia.blueprint.caas.commerce;

import com.coremedia.blueprint.base.links.RuleProvider;
import com.coremedia.blueprint.base.links.impl.AbsoluteUrlPrefixRuleProvider;
import com.coremedia.blueprint.base.links.impl.ApplicationPropertyReplacerFormatter;
import com.coremedia.blueprint.base.links.impl.RuleUrlPrefixResolver;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.base.pagegrid.internal.PageGridConfiguration;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.caas.commerce.adapter.CommerceBeanPageGridAdapterFactory;
import com.coremedia.blueprint.caas.commerce.adapter.ProductListAdapterFactory;
import com.coremedia.blueprint.caas.commerce.model.CommerceFacade;
import com.coremedia.blueprint.caas.commerce.wiring.CommerceInstrumentation;
import com.coremedia.caas.model.adapter.ExtendedLinkListAdapterFactory;
import com.coremedia.caas.search.solr.SolrQueryBuilder;
import com.coremedia.caas.search.solr.SolrSearchResultFactory;
import com.coremedia.caas.wiring.ProvidesTypeNameResolver;
import com.coremedia.caas.wiring.TypeNameResolver;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.id.IdScheme;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.pagegrid.ContentAugmentedPageGridServiceImpl;
import com.coremedia.livecontext.pagegrid.ContentAugmentedProductPageGridServiceImpl;
import com.coremedia.livecontext.tree.ExternalChannelContentTreeRelation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords.PAGE_GRID_STRUCT_PROPERTY;
import static com.coremedia.blueprint.caas.commerce.adapter.CommerceBeanPageGridAdapterFactory.PDP_PAGEGRID_PROPERTY_NAME;

@Configuration
@ComponentScan({
        "com.coremedia.blueprint.base.livecontext.augmentation",
        "com.coremedia.livecontext.asset.impl",
})
@ImportResource("classpath:/META-INF/coremedia/lc-services.xml")
public class CommerceConfig {
  private static final Set<String> COMMERCE_BEAN_CLASS_NAMES = Stream.of(
          CommerceBean.class.getSimpleName(),
          Catalog.class.getSimpleName(),
          Category.class.getSimpleName(),
          Product.class.getSimpleName(),
          ProductVariant.class.getSimpleName())
          .collect(Collectors.toSet());

  @Value("${link.urlPrefixType:''}")
  private String urlPrefixType;

  @Bean
  public RuleUrlPrefixResolver ruleUrlPrefixResolver(List<RuleProvider> ruleProviders) {
    RuleUrlPrefixResolver ruleUrlPrefixResolver = new RuleUrlPrefixResolver();
    ruleUrlPrefixResolver.setRuleProviders(ruleProviders);
    return ruleUrlPrefixResolver;
  }

  @Bean
  public ApplicationPropertyReplacerFormatter applicationPropertyReplacerFormatter() {
    return new ApplicationPropertyReplacerFormatter();
  }

  @Bean
  public AbsoluteUrlPrefixRuleProvider absoluteUrlPrefixRuleProvider(SitesService sitesService,
                                                                     @Qualifier("settingsService") SettingsService settingsService,
                                                                     Cache cache,
                                                                     ApplicationPropertyReplacerFormatter urlPrefixProcessor) {
    AbsoluteUrlPrefixRuleProvider ruleProvider = new AbsoluteUrlPrefixRuleProvider();
    ruleProvider.setSitesService(sitesService);
    ruleProvider.setSettingsService(settingsService);
    ruleProvider.setCache(cache);
    ruleProvider.setUrlPrefixProcessor(urlPrefixProcessor);
    ruleProvider.setUrlPrefixType(urlPrefixType);
    return ruleProvider;
  }

  @Bean
  public CommerceFacade commerceFacade(@Qualifier("commerceConnectionInitializer") CommerceConnectionInitializer commerceConnectionInitializer,
                                       SitesService sitesService) {
    return new CommerceFacade(commerceConnectionInitializer, sitesService);
  }

  @Bean
  public ProvidesTypeNameResolver providesCommerceBeanTypeNameResolver() {
    return typeName ->
            COMMERCE_BEAN_CLASS_NAMES.contains(typeName)
                    ? Optional.of(true)
                    : Optional.empty();
  }

  @Bean
  public TypeNameResolver<CommerceBean> commerceBeanTypeNameResolver() {
    return commerceBean -> {
      String simpleClassName = commerceBean.getClass().getSimpleName();
      return Optional.of(simpleClassName.substring("Client".length()) + "Impl");
    };
  }

  @Bean
  public CommerceInstrumentation commerceInstrumentation() {
    return new CommerceInstrumentation();
  }

  @Bean
  public ProductListAdapterFactory productListAdapter(@Qualifier("settingsService") SettingsService settingsService,
                                                      @Qualifier("sitesService") SitesService sitesService,
                                                      @Qualifier("collectionExtendedItemsAdapter") ExtendedLinkListAdapterFactory extendedLinkListAdapterFactory,
                                                      @Qualifier("commerceFacade") CommerceFacade commerceFacade) {
    return new ProductListAdapterFactory(settingsService, sitesService, extendedLinkListAdapterFactory, commerceFacade);
  }

  @Bean("query-root:commerce")
  @Qualifier("queryRoot")
  public Object commerce(CommerceFacade commerceFacade) {
    // A simple Object suffices because all commerce root fields are implemented via @fetch directives
    return new Object();
  }

  @Bean
  public CaasAssetSearchService caasAssetSearchService(@Qualifier("searchResultFactory") SolrSearchResultFactory searchResultFactory,
                                                       ContentRepository contentRepository,
                                                       List<IdScheme> idSchemes,
                                                       @Qualifier("dynamicContentSolrQueryBuilder") SolrQueryBuilder solrQueryBuilder,
                                                       @Value("${caas.search.cache.seconds}") Integer cacheForSeconds) {
    return new CaasAssetSearchService(searchResultFactory, contentRepository, idSchemes, solrQueryBuilder, cacheForSeconds);
  }

  @Bean
  public ContentAugmentedPageGridServiceImpl categoryContentBackedPageGridService(
          Cache cache,
          SitesService sitesService,
          PageGridConfiguration pageGridConfiguration,
          ExternalChannelContentTreeRelation externalChannelContentTreeRelation) {
    ContentAugmentedPageGridServiceImpl pageGridService = new ContentAugmentedPageGridServiceImpl();
    pageGridService.setCache(cache);
    pageGridService.setSitesService(sitesService);
    pageGridService.setConfiguration(pageGridConfiguration);
    pageGridService.setTreeRelation(externalChannelContentTreeRelation);
    return pageGridService;
  }

  @Bean
  public ContentAugmentedPageGridServiceImpl pdpContentBackedPageGridService(
          Cache cache,
          SitesService sitesService,
          PageGridConfiguration pageGridConfiguration,
          ExternalChannelContentTreeRelation externalChannelContentTreeRelation) {
    ContentAugmentedPageGridServiceImpl pageGridService = new ContentAugmentedPageGridServiceImpl();
    pageGridService.setStructPropertyName(PDP_PAGEGRID_PROPERTY_NAME);
    pageGridService.setCache(cache);
    pageGridService.setSitesService(sitesService);
    pageGridService.setConfiguration(pageGridConfiguration);
    pageGridService.setTreeRelation(externalChannelContentTreeRelation);
    pageGridService.setFallbackStructPropertyName(PAGE_GRID_STRUCT_PROPERTY);
    return pageGridService;
  }

  @Bean
  public ContentAugmentedProductPageGridServiceImpl productContentBackedPageGridService(
          Cache cache,
          SitesService sitesService,
          PageGridConfiguration pageGridConfiguration,
          ExternalChannelContentTreeRelation externalChannelContentTreeRelation) {
    ContentAugmentedProductPageGridServiceImpl pageGridService = new ContentAugmentedProductPageGridServiceImpl();
    pageGridService.setStructPropertyName(PDP_PAGEGRID_PROPERTY_NAME);
    pageGridService.setCache(cache);
    pageGridService.setSitesService(sitesService);
    pageGridService.setConfiguration(pageGridConfiguration);
    pageGridService.setTreeRelation(externalChannelContentTreeRelation);
    pageGridService.setFallbackStructPropertyName(PAGE_GRID_STRUCT_PROPERTY);
    return pageGridService;
  }

  @Bean
  // uses autoconfigured beans exported by lib bpbase-lc-common
  public CommerceBeanPageGridAdapterFactory categoryPageGridAdapter(
          AugmentationService categoryAugmentationService,
          ExternalChannelContentTreeRelation externalChannelContentTreeRelation,
          ContentBackedPageGridService categoryContentBackedPageGridService,
          SitesService sitesService) {
    return new CommerceBeanPageGridAdapterFactory(
            PAGE_GRID_STRUCT_PROPERTY,
            categoryAugmentationService,
            externalChannelContentTreeRelation,
            categoryContentBackedPageGridService,
            sitesService);
  }

  @Bean
  // uses autoconfigured beans exported by lib bpbase-lc-common
  public CommerceBeanPageGridAdapterFactory productPageGridAdapter(
          AugmentationService productAugmentationService,
          ExternalChannelContentTreeRelation externalChannelContentTreeRelation,
          ContentBackedPageGridService productContentBackedPageGridService,
          SitesService sitesService) {
    return new CommerceBeanPageGridAdapterFactory(
            PDP_PAGEGRID_PROPERTY_NAME,
            productAugmentationService,
            externalChannelContentTreeRelation,
            productContentBackedPageGridService,
            sitesService);
  }
}
