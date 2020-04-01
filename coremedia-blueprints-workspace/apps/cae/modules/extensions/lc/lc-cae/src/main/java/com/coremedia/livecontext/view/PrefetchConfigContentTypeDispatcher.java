package com.coremedia.livecontext.view;

import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.dispatch.NoArgDispatcher;
import com.coremedia.dispatch.Type;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collection;

import static java.util.Collections.emptySet;

@DefaultAnnotation(NonNull.class)
class PrefetchConfigContentTypeDispatcher extends NoArgDispatcher {
  final PrefetchFragmentsConfigReader prefetchFragmentsConfigReader;

  PrefetchConfigContentTypeDispatcher(PrefetchFragmentsConfigReader prefetchFragmentsConfigReader) {
    this.prefetchFragmentsConfigReader = prefetchFragmentsConfigReader;
  }

  private Collection<String> configuredContentTypes = emptySet();

  @Nullable
  @Override
  protected Object doLookup(String typeName) {
    return configuredContentTypes.contains(typeName) ? typeName : null;
  }

  @Nullable
  @Override
  public Object lookup(@NonNull Type type) {
    updateConfiguredContentTypesAndCaches();
    return super.lookup(type);
  }

  void updateConfiguredContentTypesAndCaches() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes != null) {
      Object pageAttribute = requestAttributes.getAttribute(ContextHelper.ATTR_NAME_PAGE, 0);
      if (pageAttribute instanceof Page) {
        //read configured types from cache
        //no need for thread-safety here
        configuredContentTypes = prefetchFragmentsConfigReader.getCache().get(new ConfiguredContentTypesCacheKey(this, (Page) pageAttribute));
      }
    }
  }
}
