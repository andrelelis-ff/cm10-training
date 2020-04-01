package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.IdRedirectHandlerBase;
import com.coremedia.objectserver.web.links.LinkFormatter;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.requireNonNull;

/**
 * A handler used for preview purposes: Takes a "id" request parameter and redirect to the resource that is denoted
 * by this id.
 */
@RequestMapping
public class PreviewHandler extends IdRedirectHandlerBase {

  private static final Logger LOG = LoggerFactory.getLogger(PreviewHandler.class);

  /**
   * Uri pattern for preview URLs.
   * e.g. /preview?id=123&view=fragmentPreview
   */
  public static final String URI_PATTERN = "/preview";

  public static final String REQUEST_ATTR_IS_STUDIO_PREVIEW = "isStudioPreview";

  private LinkFormatter linkFormatter;

  // --- Handler ----------------------------------------------------

  @GetMapping(value = URI_PATTERN)
  public ModelAndView handleId(@RequestParam(value = "id", required = true) String id,
                               @RequestParam(value = "view", required = false) String view,
                               @RequestParam(value = "site", required = false) String siteId,
                               @RequestParam(value = "taxonomyId", required = false) String taxonomyId,
                               @NonNull HttpServletRequest request,
                               @NonNull HttpServletResponse response) {
    request.setAttribute(REQUEST_ATTR_IS_STUDIO_PREVIEW, true);
    storeSite(request, siteId);
    storeTaxonomy(request, taxonomyId);

    ModelAndView modelAndView = super.handleId(id, view);
    String viewName = modelAndView.getViewName();
    if (viewName == null || !viewName.startsWith(UrlBasedViewResolver.REDIRECT_URL_PREFIX)) {
      // not a redirect view - may occur in case of errors
      return modelAndView;
    }

    Object rootModel = requireNonNull(HandlerHelper.getRootModel(modelAndView));

    // check if link to root model can be build - let common spring MVC exception handling kick in if link building fails
    // note that this is necessary because exceptions during rendering of RedirectView cannot be handled anymore
    String link = linkFormatter.formatLink(rootModel, view, request, response, true);
    LOG.debug("redirecting '{}' with view '{}' for bean '{}' to '{}'", id, view, rootModel, link);
    return modelAndView;
  }

  @Required
  public void setLinkFormatter(LinkFormatter linkFormatter) {
    this.linkFormatter = linkFormatter;
  }

  public static boolean isStudioPreviewRequest(@NonNull HttpServletRequest request) {
    Object attributeValue = request.getAttribute(PreviewHandler.REQUEST_ATTR_IS_STUDIO_PREVIEW);

    if (attributeValue == null) {
      return false;
    }

    return Boolean.valueOf(attributeValue + "");
  }

  @Override
  protected boolean isPermitted(Object o, String s) {
    return true;
  }

  /**
   * Stores the site parameter into the request.
   * The site parameter is used to resolve the context of a content if it does not belong to a specific site.
   * Therefore the studio default site will be passed as parameter to resolve the context.
   *
   * @param siteId The id of the site
   */
  private static void storeSite(@NonNull HttpServletRequest request, @Nullable String siteId) {
    String attributeValue = emptyToNull(siteId);
    setSessionAttribute(request, RequestAttributeConstants.ATTR_NAME_PAGE_SITE, attributeValue);
  }

  /**
   * Stores the taxonomy content into the request.
   * The taxonomy parameter is used for the custom topic pages.
   *
   * @param taxonomyId The numeric content id of the taxonomy node.
   */
  private static void storeTaxonomy(@NonNull HttpServletRequest request, @Nullable String taxonomyId) {
    if (!isNullOrEmpty(taxonomyId)) {
      String id = IdHelper.formatContentId(taxonomyId);
      setSessionAttribute(request, RequestAttributeConstants.ATTR_NAME_PAGE_MODEL, id);
    }
  }

  private static void setSessionAttribute(@NonNull HttpServletRequest request, @NonNull String name,
                                          @Nullable String value) {
    HttpSession session = request.getSession(true);
    session.setAttribute(name, value);
  }
}
