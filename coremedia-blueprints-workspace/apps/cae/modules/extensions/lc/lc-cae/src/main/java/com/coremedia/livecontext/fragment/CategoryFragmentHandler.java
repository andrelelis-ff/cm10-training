package com.coremedia.livecontext.fragment;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.cap.user.User;
import com.coremedia.livecontext.context.ResolveContextStrategy;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.navigation.LiveContextCategoryNavigation;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Handles fragment request that depend on a category id.
 */
public class CategoryFragmentHandler extends FragmentHandler {

  private ResolveContextStrategy contextStrategy;
  private boolean useOriginalNavigationContext = false;
  private boolean useStableIds = false;

  // --- FragmentHandler --------------------------------------------

  /**
   * Renders the complete context (which is a CMChannel) of the given <code>category</code> using the given <code>view</code>.
   * If no context can be found for the category, the <code>view</code> of the root channel will be rendered. The site
   * is determined by the tuple <code>(storeId, locale)</code>, which must be unique across all sites. If the placement
   * value is passed as part of the fragment parameters, the model and view will be created for it.
   *
   * @param parameters All parameters that have been passed for the fragment call.
   * @return the {@link ModelAndView model and view} containing the {@link com.coremedia.blueprint.common.contentbeans.Page page}
   * as <code>self</code> object, that contains the context (CMChannel) that shall be rendered.
   */
  @Nullable
  @Override
  public ModelAndView createModelAndView(@NonNull FragmentParameters parameters, @NonNull HttpServletRequest request) {
    Site site = SiteHelper.getSiteFromRequest(request);
    if (site == null) {
      return HandlerHelper.notFound(getClass().getName() + " cannot derive a site from request "
              + request.getRequestURI());
    }

    String categoryExtId = parameters.getCategoryId();

    Category categoryById = findCategory(categoryExtId).orElse(null);
    if (categoryById == null) {
      return HandlerHelper.notFound(getClass().getName() + " did not find category for storeId \""
              + parameters.getStoreId() + "\", locale \"" + parameters.getLocale() + "\", category id \""
              + categoryExtId + "\"");
    }

    Navigation navigation = contextStrategy.resolveContext(site, categoryById).orElse(null);
    if (navigation == null) {
      return HandlerHelper.notFound(getClass().getName() + " did not find a navigation for storeId \""
              + parameters.getStoreId() + "\", locale \"" + parameters.getLocale() + "\", category id \""
              + categoryExtId + "\"");
    }

    String placement = parameters.getPlacement();
    String view = parameters.getView();
    Content siteRootDocument = site.getSiteRootDocument();
    CMChannel rootChannel = getContentBeanFactory().createBeanFor(siteRootDocument, CMChannel.class);
    User developer = UserVariantHelper.getUser(request);

    ModelAndView modelAndView = createBasicModelAndView(navigation, placement, view, rootChannel, developer);
    enhanceModelAndView(modelAndView, navigation);

    return modelAndView;
  }

  @NonNull
  private Optional<Category> findCategory(@NonNull String categoryExtId) {
    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection currentConnection = storeContext.getConnection();

    CatalogService catalogService = currentConnection.getCatalogService();
    CommerceIdProvider idProvider = currentConnection.getIdProvider();

    CatalogAlias catalogAlias = storeContext.getCatalogAlias();
    CommerceId formatCategoryId = useStableIds
            ? idProvider.formatCategoryId(catalogAlias, categoryExtId)
            : idProvider.formatCategoryTechId(catalogAlias, categoryExtId);

    Category category = catalogService.findCategoryById(formatCategoryId, storeContext);
    return Optional.ofNullable(category);
  }

  @NonNull
  private ModelAndView createBasicModelAndView(@NonNull Navigation navigation, @Nullable String placement,
                                               @Nullable String view, @NonNull CMChannel rootChannel,
                                               @Nullable User developer) {
    if (isNullOrEmpty(placement)) {
      if (useOriginalNavigationContext) {
        return createModelAndView(navigation, view, developer);
      } else {
        return createFragmentModelAndView(navigation, view, rootChannel, developer);
      }
    }

    return createFragmentModelAndViewForPlacementAndView(navigation, placement, view, rootChannel, developer);
  }

  @Override
  public boolean include(@NonNull FragmentParameters params) {
    String categoryId = params.getCategoryId();
    String externalRef = params.getExternalRef();

    return !isNullOrEmpty(categoryId) && (isNullOrEmpty(externalRef) || !externalRef.startsWith("cm-"));
  }

  // --- internal ---------------------------------------------------

  private void enhanceModelAndView(@NonNull ModelAndView modelAndView, @NonNull Navigation navigation) {
    if (navigation instanceof LiveContextCategoryNavigation) {
      modelAndView.addObject("lcNavigation", navigation);
    }
  }

  // ------------------- Config ---------------------------------

  @Required
  public void setContextStrategy(ResolveContextStrategy contextStrategy) {
    this.contextStrategy = contextStrategy;
  }

  /**
   * Enforce ModelAndViews with the actual Navigation, even if it is no
   * content backed bean.
   * <p>
   * Default is false (backward compatible).
   */
  public void setUseOriginalNavigationContext(boolean useOriginalNavigationContext) {
    this.useOriginalNavigationContext = useOriginalNavigationContext;
  }

  public void setUseStableIds(boolean useStableIds) {
    this.useStableIds = useStableIds;
  }
}
