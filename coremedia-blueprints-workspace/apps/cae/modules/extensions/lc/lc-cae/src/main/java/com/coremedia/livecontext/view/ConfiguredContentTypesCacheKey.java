package com.coremedia.livecontext.view;

import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

@DefaultAnnotation(NonNull.class)
class ConfiguredContentTypesCacheKey extends CacheKey<Collection<String>> {
  private final PrefetchConfigContentTypeDispatcher contentTypeDispatcher;
  private final Page page;

  ConfiguredContentTypesCacheKey(PrefetchConfigContentTypeDispatcher contentTypeDispatcher, Page page) {
    this.contentTypeDispatcher = contentTypeDispatcher;
    this.page = page;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConfiguredContentTypesCacheKey that = (ConfiguredContentTypesCacheKey) o;
    return Objects.equals(contentTypeDispatcher, that.contentTypeDispatcher) &&
      Objects.equals(page, that.page);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contentTypeDispatcher, page);
  }

  @Override
  public Collection<String> evaluate(Cache cache) throws Exception {
    //reset lookup cache of NoArgDispatcher when settings changed
    contentTypeDispatcher.reset();
    return contentTypeDispatcher.prefetchFragmentsConfigReader.getConfiguredContentTypes(page);
  }
}
