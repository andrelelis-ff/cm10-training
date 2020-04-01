package com.coremedia.blueprint.taxonomies.semantic;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.taxonomies.semantic.service.SemanticService;
import com.coremedia.blueprint.taxonomies.semantic.service.SemanticServiceStrategy;
import com.coremedia.blueprint.taxonomies.semantic.service.matching.NameMatchingStrategy;
import com.coremedia.blueprint.taxonomies.semantic.service.opencalais.CalaisService;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.undoc.common.spring.CapRepositoriesConfiguration;
import com.coremedia.rest.cap.CapRestServiceSearchConfiguration;
import com.coremedia.rest.cap.content.search.SearchService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import java.util.List;
import java.util.Map;

@Configuration
@ImportResource(locations = {"classpath:/com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml",
                             "classpath:/com/coremedia/cache/cache-services.xml",
                             "classpath:/com/coremedia/cap/multisite/multisite-services.xml"})
@Import({CapRepositoriesConfiguration.class,
         CapRestServiceSearchConfiguration.class
})
public class SemanticTaxonomyConfiguration {

  @Bean
  SemanticService calaisService(Cache cache,
                                SettingsService settingsService,
                                SitesService sitesService,
                                List<String> semanticDocumentProperties,
                                Map<String, String> calaisSemanticProperties,
                                @Value("${semantic.service.opencalais.api.key:enter-api-key-here}") String apiKey,
                                @Value("${semantic.service.opencalais.api.url:https://api.thomsonreuters.com/permid/calais}") String apiUrl) {
    CalaisService calaisService = new CalaisService();
    calaisService.setCache(cache);
    calaisService.setSettingsService(settingsService);
    calaisService.setSitesService(sitesService);
    calaisService.setDocumentProperties(semanticDocumentProperties);
    calaisService.setSemanticProperties(calaisSemanticProperties);
    calaisService.setApiKey(apiKey);
    calaisService.setApiUrl(apiUrl);
    return calaisService;
  }

  @Bean
  SemanticServiceStrategy semanticService(ContentRepository contentRepository,
                                          SearchService searchService,
                                          @Qualifier("calaisService") SemanticService calaisService) {
    SemanticServiceStrategy semanticServiceStrategy = new SemanticServiceStrategy();
    semanticServiceStrategy.setContentRepository(contentRepository);
    semanticServiceStrategy.setSearchService(searchService);
    semanticServiceStrategy.setSemanticService(calaisService);
    semanticServiceStrategy.setNameMatchingPropertyName("value");
    semanticServiceStrategy.setReferencePropertyName("externalReference");
    semanticServiceStrategy.setServiceId("semantic");
    return semanticServiceStrategy;
  }

  //The CM taxonomy node strategy beans working on folders with parent and child properties on the CMTaxonomy content type
  @Bean
  SemanticStrategy nameMatching(ContentRepository contentRepository) {
    NameMatchingStrategy nameMatchingStrategy = new NameMatchingStrategy();
    nameMatchingStrategy.setServiceId("nameMatching");
    nameMatchingStrategy.setContentRepository(contentRepository);
    return nameMatchingStrategy;
  }

  @Bean
  Map<String, String> calaisSemanticProperties() {
    return ImmutableMap.of(SemanticEntity.ID, "_uri",
                           SemanticEntity.NAME, "name",
                           SemanticEntity.TYPE, "_type",
                           "typeRef", "_typeReference",
                           "relevance", "relevance");
  }

  @Bean
  List<String> semanticDocumentProperties() {
    return ImmutableList.of("title", "teaserTitle", "detailText", "teaserText");
  }

  @Bean
  List<SemanticStrategy> semanticServiceStrategies(@Qualifier("nameMatching") SemanticStrategy nameMatchingStrategy,
                                                   @Qualifier("semanticService") SemanticStrategy semanticServiceStrategy) {
    return ImmutableList.of(nameMatchingStrategy, semanticServiceStrategy);
  }
}
