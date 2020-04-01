package com.coremedia.catalog.studio.lib.validators;

import com.coremedia.blueprint.base.ecommerce.catalog.CmsCatalogService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.rest.invalidations.SimpleInvalidationSource;
import com.coremedia.rest.linking.Linker;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class RootCategoryInvalidationSource extends SimpleInvalidationSource implements ApplicationListener<ContextRefreshedEvent> {

  private static final Logger LOG = LoggerFactory.getLogger(RootCategoryInvalidationSource.class);

  @Autowired
  private Cache cache;

  @Autowired
  private Linker linker;

  @Autowired
  private SitesService sitesService;

  @Autowired
  private CommerceConnectionInitializer connectionInitializer;

  @Autowired
  private CmsCatalogService catalogService;

  private CacheKey<Collection<Category>> categoryCacheKey = new RootCategoriesCacheKey(this);

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    // pull root categories into cache
    getRootCategories();
  }

  Collection<Category> getRootCategories() {
    return cache.get(categoryCacheKey);
  }

  class RootCategoriesCacheKey extends CacheKey<Collection<Category>> {

    private final RootCategoryInvalidationSource rootCategoryInvalidationSource;

    public RootCategoriesCacheKey(RootCategoryInvalidationSource rootCategoryInvalidationSource) {
      this.rootCategoryInvalidationSource = rootCategoryInvalidationSource;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      RootCategoriesCacheKey that = (RootCategoriesCacheKey) o;

      return Objects.equals(rootCategoryInvalidationSource, that.rootCategoryInvalidationSource);
    }

    @Override
    public int hashCode() {
      return Objects.hash(rootCategoryInvalidationSource);
    }

    @Override
    public Collection<Category> evaluate(Cache cache) throws Exception {
      return sitesService.getSites().stream()
              .map(this::findRootCategory)
              .flatMap(Optional::stream)
              .collect(toList());
    }

    @NonNull
    private Optional<Category> findRootCategory(@NonNull Site site) {
      try {
        return connectionInitializer.findConnectionForSite(site)
                .filter(connection -> "cms".equals(connection.getVendor().value()))
                .map(connection -> {
                  CurrentStoreContext.set(connection.getStoreContext());

                  RootCategoryCacheKey cacheKey = new RootCategoryCacheKey(connection, catalogService, linker,
                          rootCategoryInvalidationSource);
                  return cache.get(cacheKey);
                });
      } catch (Exception e) {
        LOG.debug("unable to determine root category for site '{}'", site.getId(), e);
      } finally {
        CurrentStoreContext.remove();
      }
      return Optional.empty();
    }

    @Override
    public boolean recomputeOnInvalidation(Cache cache, Collection<Category> value, int numDependents) {
      return true;
    }
  }
}
