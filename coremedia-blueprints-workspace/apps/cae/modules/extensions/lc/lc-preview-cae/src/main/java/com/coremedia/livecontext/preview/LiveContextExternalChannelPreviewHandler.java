package com.coremedia.livecontext.preview;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.contentbeans.LiveContextExternalChannelImpl;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.handler.ExternalNavigationHandler;
import com.coremedia.livecontext.handler.LiveContextPageHandlerBase;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.VIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_NAME;

@Link
@RequestMapping
public class LiveContextExternalChannelPreviewHandler extends LiveContextPageHandlerBase {

  private static final String SEGMENT_CATEGORY_PREVIEW = "categoryPreview";

  // e.g. /categoryPreview/shopName/segment-1234
  public static final String PREVIEW_URI_PATTERN
          = "/" + SEGMENT_CATEGORY_PREVIEW
          + "/{" + SHOP_NAME_VARIABLE + "}"
          + "/{" + SEGMENT_NAME + "}"
          + "-{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}";

  private ExternalNavigationHandler externalNavigationHandler;
  private TreeRelation<Content> treeRelation;

  @GetMapping({PREVIEW_URI_PATTERN})
  public ModelAndView handleRequest(@PathVariable(SEGMENT_ID) LiveContextExternalChannelImpl liveContextExternalChannel,
                                    @PathVariable(SEGMENT_NAME) String vanityName,
                                    @PathVariable(SHOP_NAME_VARIABLE) String siteSegment,
                                    @RequestParam(value = VIEW_PARAMETER, required = false) final String view,
                                    @NonNull HttpServletRequest request) {
    Navigation navigation = getNavigation(siteSegment);
    if (navigation == null || !vanityName.equals(getVanityName(liveContextExternalChannel))) {
      return HandlerHelper.notFound();
    }

    Page page = asPage(navigation, liveContextExternalChannel, treeRelation, UserVariantHelper.getUser(request));
    return createModelAndView(page, view);
  }

  @SuppressWarnings("unused")
  @Link(type = LiveContextExternalChannelImpl.class)
  @Nullable
  public UriComponents buildLinkForExternalChannel(@NonNull LiveContextExternalChannelImpl navigation, String viewName,
                                                   Map<String, Object> linkParameters, HttpServletRequest request) {
    Optional<StoreContext> storeContext = CurrentStoreContext.find();
    if (storeContext.isEmpty()) {
      // not responsible
      return null;
    }

    Category category = ExternalNavigationHandler.findCategory(navigation).orElse(null);
    if (category == null) {
      return null;
    }

    // in case of the root category another link scheme should build the link
    // ...and that should lead to a fragment preview of the page grid
    if (category.isRoot()) {
      return null;
    }

    if (!useCommerceCategoryLinks(navigation.getSite())) {
      return externalNavigationHandler.buildCaeLinkForCategory(navigation, viewName, linkParameters)
              .orElse(null);
    }
    return null;
  }

  private boolean useCommerceCategoryLinks(@NonNull Site site) {
    return externalNavigationHandler.useCommerceCategoryLinks(site);
  }

  @Required
  public void setExternalNavigationHandler(ExternalNavigationHandler externalNavigationHandler) {
    this.externalNavigationHandler = externalNavigationHandler;
  }

  public void setTreeRelation(TreeRelation<Content> treeRelation) {
    this.treeRelation = treeRelation;
  }
}
