package com.coremedia.blueprint.common.services.context;

import com.coremedia.blueprint.base.navigation.context.ContextStrategy;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;

/**
 * A utility class for {@link CMContext} determination for a {@link CMLinkable}.
 */
public class ContextHelper {
  public static final String ATTR_NAME_PAGE = "cmpage";

  public static final String NAME_CONTEXTHELPER = ContextHelper.class.getName();

  private ContextStrategy<CMLinkable, CMContext> contextStrategy;
  private DataViewFactory dataViewFactory;
  private CurrentContextService currentContextService;

  /**
   * Find the context for the linkable.
   * <p>
   * The result must not be cached, because the method uses the context of
   * the current request.  If you know your context, you should rather use
   * {@link #findAndSelectContextFor(CMContext, CMLinkable)}.
   */
  public CMNavigation contextFor(CMLinkable target) {
    if (target instanceof CMNavigation) {
      // convention: navigation's context is the navigation itself
      return (CMNavigation) target;
    }

    // linkable's context can be determined by ContextStrategy
    CMContext currentContext = currentContextService.getContext();
    return findAndSelectContextFor(currentContext, target);
  }

  public CMContext findAndSelectContextFor(CMContext currentContext, CMLinkable linkable) {
    CMContext context = contextStrategy.findAndSelectContextFor(linkable, currentContext);
    return dataViewFactory.loadCached(context, null);
  }

  /**
   * Find the top level context of the current site.
   * <p>
   * Must not be cached, since it depends on the request.
   */
  @Nullable
  public Navigation currentSiteContext() {
    CMContext currentContext = currentContextService.getContext();
    return currentContext == null ? null : currentContext.getRootNavigation();
  }

  // --- configuration ----------------------------------------------

  @Required
  public void setContextStrategy(ContextStrategy<CMLinkable, CMContext> contextStrategy) {
    this.contextStrategy = contextStrategy;
  }

  @Required
  public void setDataViewFactory(DataViewFactory dataViewFactory) {
    this.dataViewFactory = dataViewFactory;
  }

  @Required
  public void setCurrentContextService(CurrentContextService currentContextService) {
    this.currentContextService = currentContextService;
  }
}
