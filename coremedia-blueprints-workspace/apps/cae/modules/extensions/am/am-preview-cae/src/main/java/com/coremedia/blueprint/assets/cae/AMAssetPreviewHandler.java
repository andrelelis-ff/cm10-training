package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.base.links.UriConstants.Patterns;
import com.coremedia.blueprint.base.links.UriConstants.RequestParameters;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.handlers.PageHandlerBase;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.beans.UnexpectedBeanTypeException;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;

@Link
@RequestMapping
public class AMAssetPreviewHandler extends PageHandlerBase {

  private static final String URI_PREFIX = "assets";
  private static final String ID_VARIABLE = "id";
  private static final String URI_PATTERN = "/" + URI_PREFIX + "/{" + ID_VARIABLE + ":" + Patterns.PATTERN_NUMBER + "}";

  protected static final String STUDIO_PREFERRED_SITE_PARAMETER = "studioPreferredSite";
  public static final String ASSET_FRAGMENT_PREFIX = "asset=";

  private SettingsService settingsService;

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @SuppressWarnings("UnusedDeclaration")
  @Link(type = AMAsset.class, uri = URI_PATTERN)
  public UriComponents buildAssetLink(AMAsset asset,
                                      UriTemplate uriTemplate,
                                      String viewName,
                                      HttpServletRequest request) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(uriTemplate.toString());
    if (!StringUtils.isEmpty(viewName)) {
      uriBuilder.queryParam(RequestParameters.VIEW_PARAMETER, viewName);
    }

    String studioPreferredSiteId = request.getParameter(STUDIO_PREFERRED_SITE_PARAMETER);
    if (!StringUtils.isEmpty(studioPreferredSiteId)) {
      uriBuilder.queryParam(STUDIO_PREFERRED_SITE_PARAMETER, studioPreferredSiteId);
    }

    // append asset fragment which is required by the download portal scripts
    uriBuilder.fragment(ASSET_FRAGMENT_PREFIX + String.valueOf(asset.getContentId()));

    return uriBuilder.buildAndExpand(asset.getContentId());
  }

  @GetMapping(value = URI_PATTERN)
  public ModelAndView handleAssetRequest(@PathVariable(ID_VARIABLE) AMAsset asset,
                                         @RequestParam(value = RequestParameters.VIEW_PARAMETER, required = false) String view,
                                         @RequestParam(value = STUDIO_PREFERRED_SITE_PARAMETER, required = false) String studioPreferredSiteId,
                                         HttpServletRequest webRequest) {
    ModelAndView modelAndView = HandlerHelper.createModelWithView(asset, view);
    Page assetContext = resolveAssetContextForPreferredSite(studioPreferredSiteId, webRequest);
    if (assetContext != null) {
      addPageModel(modelAndView, assetContext);
    }
    return modelAndView;
  }

  @Nullable
  private Page resolveAssetContextForPreferredSite(@Nullable String preferredSiteId,
                                                   @NonNull HttpServletRequest request) {
    if (preferredSiteId == null) {
      return null;
    }

    Site preferredSite = getSitesService().findSite(preferredSiteId).orElse(null);
    if (preferredSite == null) {
      return null;
    }

    SiteHelper.setSiteToRequest(preferredSite, request);

    Content rootDocument = preferredSite.getSiteRootDocument();
    Content downloadPortalContent = AMUtils.getDownloadPortalRootDocument(settingsService, preferredSite);
    ContentBeanFactory contentBeanFactory = getContentBeanFactory();
    if (downloadPortalContent != null) {
      try {
        CMChannel downloadPortalChannel = contentBeanFactory.createBeanFor(downloadPortalContent, CMChannel.class);
        return asPage(downloadPortalChannel, downloadPortalChannel, UserVariantHelper.getUser(request));
      } catch (IllegalArgumentException | UnexpectedBeanTypeException e) {
        LOG.info("Could not create Download Portal page from content with id {}.", downloadPortalContent.getId(), e);
      }

      // if the preferred site has no download portal then use at least the root channel JS and CSS to display the fragments
    } else if (rootDocument != null) {
      try {
        CMChannel rootChannel = contentBeanFactory.createBeanFor(rootDocument, CMChannel.class);
        return asPage(rootChannel, rootChannel, UserVariantHelper.getUser(request));
      } catch (IllegalArgumentException | UnexpectedBeanTypeException e) {
        LOG.info("Could not create root page from content with id {}.", rootDocument.getId(), e);
      }
    }

    return null;
  }
}
