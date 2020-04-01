package com.coremedia.catalog.studio.lib.validators;

import com.coremedia.blueprint.base.ecommerce.catalog.CmsCatalogService;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.rest.linking.Linker;

import java.util.Collections;
import java.util.Objects;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService.DEFAULT_CATALOG_ALIAS;

/**
 * A special purpose cache key that propagates changes of the catalog's root category to a {@link RootCategoryInvalidationSource}.
 */
class RootCategoryCacheKey extends CacheKey<Category> {

  private final CommerceConnection connection;
  private final CmsCatalogService catalogService;
  private final Linker linker;
  private final RootCategoryInvalidationSource rootCategoryInvalidationSource;

  public RootCategoryCacheKey(CommerceConnection connection, CmsCatalogService catalogService, Linker linker, RootCategoryInvalidationSource rootCategoryInvalidationSource) {
    this.connection = connection;
    this.catalogService = catalogService;
    this.linker = linker;
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
    RootCategoryCacheKey that = (RootCategoryCacheKey) o;
    return Objects.equals(connection, that.connection) &&
            Objects.equals(catalogService, that.catalogService) &&
            Objects.equals(rootCategoryInvalidationSource, that.rootCategoryInvalidationSource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(connection, catalogService, rootCategoryInvalidationSource);
  }

  @Override
  public Category evaluate(Cache cache) throws Exception {
    return catalogService.findRootCategory(DEFAULT_CATALOG_ALIAS, connection.getStoreContext());
  }

  @Override
  public void inserted(Cache cache, Category value) {
    if(null != value) {
      rootCategoryInvalidationSource.addInvalidations(Collections.singleton(linker.link(value).toString()));
    }
  }

}
