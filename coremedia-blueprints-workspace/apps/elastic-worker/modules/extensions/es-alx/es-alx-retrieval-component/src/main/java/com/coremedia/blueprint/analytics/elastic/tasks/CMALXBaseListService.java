package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A service to retrieve CMALXBaseList instances.
 */
@Named
class CMALXBaseListService {

  private static final Logger LOG = LoggerFactory.getLogger(CMALXBaseListService.class);

  private static final String CMALXBASELIST_TYPE_NAME = "CMALXBaseList"; // super type of all ALX top-N-lists

  private ContentType cmAlxBaseListType;

  @Inject
  private ContentRepository contentRepository;

  @Inject
  private ElasticSocialPlugin elasticSocialPlugin;

  @PostConstruct
  public void initialize() {
    cmAlxBaseListType = contentRepository.getContentType(CMALXBASELIST_TYPE_NAME);
    if(cmAlxBaseListType == null) {
      throw new IllegalStateException("required content type "+ CMALXBASELIST_TYPE_NAME + " not found in "
              + contentRepository.getContentTypesByName());
    }
  }

  /**
   * Get the CMALXBaseList instances for the given tenant
   *
   * @param rootNavigation the root navigation
   * @param tenant the tenant owning the CMALXBaseList instances
   * @return all productive CMALXBaseList instances
   */
  public List<Content> getCMALXBaseLists(Content rootNavigation, final String tenant) {
    if(tenant == null) {
      throw new IllegalArgumentException("tenant must not be null");
    }
    final Set<Content> instances = cmAlxBaseListType.getInstances();
    final List<Content> result = new ArrayList<>(instances.size());
    for(Content content : instances) {
      if(content.isInProduction()) {
        if(tenant.equals(elasticSocialPlugin.getElasticSocialConfiguration(content, rootNavigation).getTenant())) {
          result.add(content);
        }
      } else {
        LOG.info("skipping non production analytics list: {}", content);
      }

    }
    return result;
  }

}
