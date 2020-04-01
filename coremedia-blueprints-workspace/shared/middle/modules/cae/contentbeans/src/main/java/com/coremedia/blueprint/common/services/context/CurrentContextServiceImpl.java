package com.coremedia.blueprint.common.services.context;

import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.coremedia.blueprint.common.util.ContextAttributes;

public class CurrentContextServiceImpl implements CurrentContextService {
  private static final Logger LOG = LoggerFactory.getLogger(CurrentContextServiceImpl.class);

  /**
   * {@inheritDoc}
   * <br/>
   * This implementation evaluates a well known attribute of the thread-local request.<br/>
   * <br/>
   * <i>Sets an uncachable dependency so that a {@link com.coremedia.cache.CacheKey CacheKey} or a
   * {@link com.coremedia.objectserver.dataviews.DataView DataView} that uses this method is invalidated instantly.</i>
   */
  @Override
  public CMContext getContext() {
    Cache.uncacheable(); // ensure that this method is not cached

    Navigation navigation = ContextAttributes
            .findRequestAttribute(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION, Navigation.class)
            .orElse(null);

    if (navigation == null) {
      LOG.debug("Navigation context not found in request");
      return null;
    }

    CMContext context = navigation.getContext();
    if (context == null) {
      LOG.warn("navigation.getContext() returned null, navigation is: {}", navigation);
    }

    return context;
  }
}
