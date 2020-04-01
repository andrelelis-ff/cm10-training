package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.web.IdRedirectHandlerBase;
import com.coremedia.objectserver.web.links.LinkFormatter;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;

@DefaultAnnotation(NonNull.class)
public abstract class AbstractUrlHandler extends IdRedirectHandlerBase {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractUrlHandler.class);

  private LinkFormatter linkFormatter;

  @Nullable
  private SitesService sitesService;

  public AbstractUrlHandler(LinkFormatter linkFormatter) {
    this.linkFormatter = linkFormatter;
  }

// --- Handler ----------------------------------------------------

  public ResponseEntity<String> getLink(String id,
                                        String view,
                                        String siteId,
                                        Object context,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {

    Optional<Site> site = Optional.empty();
    if (StringUtils.isNotBlank(siteId)) {
      site = storeSite(request, siteId);
      if (site.isEmpty()) {
        return ResponseEntity.badRequest().build();
      }
    }

    storeContext(request, context, site.orElse(null));


    Optional<Object> beanOpt = super.getBean(id);

    if (beanOpt.isEmpty()) {
      LOG.debug("Object not found: {}", id);
      return ResponseEntity.notFound().build();
    }

    Object bean = beanOpt.get();

    if (!isPermitted(bean, view)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    request.setAttribute(ABSOLUTE_URI_KEY, "true");
    String url = linkFormatter.formatLink(bean, view, request, response, false);
    LOG.debug("Url: {}", url);
    return ResponseEntity.ok(url);
  }

  @Override
  protected boolean isPermitted(Object o, String s) {
    return true;
  }

  /**
   * Stores the site parameter into the request.
   * The site parameter is used to resolve the context of a content if it does not belong to a specific site.
   *
   * @param siteId The id of the site
   */
  private Optional<Site> storeSite(HttpServletRequest request, @Nullable String siteId) {
    if (sitesService == null || siteId == null || StringUtils.isBlank(siteId)) {
      return Optional.empty();
    }
    Optional<Site> siteOpt = sitesService.findSite(siteId);
    if (siteOpt.isPresent()) {
      Site site = siteOpt.get();
      SiteHelper.setSiteToRequest(site, request);
    }
    return siteOpt;
  }

  private void storeContext(HttpServletRequest request, @Nullable Object context, @Nullable Site site) {
    // resolve navigation context either from "context" parameter or from site root document
    if (context == null && site != null) {
      storeSiteRootAsContext(request, site);
    } else {
      storeContext(request, context);
    }
  }

  private void storeSiteRootAsContext(HttpServletRequest request, Site site) {
    Content siteRootDocument = site.getSiteRootDocument();
    ContentBean bean = getContentBeanFactory().createBeanFor(siteRootDocument, ContentBean.class);
    if (bean instanceof Navigation) {
      storeNavigationContext(request, (Navigation) bean);
    }
  }

  /**
   * Stores the context parameter into the request.
   * The context parameter is used to resolve the context of a content
   *
   * @param context The context
   */
  protected void storeContext(HttpServletRequest request,
                              @Nullable Object context) {
    if (context == null) {
      return;
    }
    Navigation navContext = null;
    if (context instanceof String) {
      Optional<Object> beanOpt = super.getBean((String) context);
      if (beanOpt.isEmpty()) {
        return;
      }
      Object bean = beanOpt.get();
      if (bean instanceof Navigation) {
        navContext = (Navigation) bean;
      }
    }
    storeNavigationContext(request, navContext);
  }

  private static void storeNavigationContext(HttpServletRequest request, Navigation navContext) {
    if (navContext != null) {
      request.setAttribute(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION, navContext);
    }
  }


  public void setSitesService(@Nullable SitesService sitesService) {
    this.sitesService = sitesService;
  }

  public void setLinkFormatter(LinkFormatter linkFormatter) {
    this.linkFormatter = linkFormatter;
  }
}
