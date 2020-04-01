package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.base.links.ContentLinkBuilder;
import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.cae.web.HttpHead;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.user.User;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.coremedia.objectserver.web.HandlerHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.VIEW_PARAMETER;

/**
 * Base implementation of resources that deal with {@link Page pages}
 * (or {@link CMLinkable} / {@link CMNavigation} respectively)
 */
public abstract class PageHandlerBase extends HandlerBase implements BeanFactoryAware {

  private static final String CMPAGE_PROTOTYPE_BEAN_NAME = "cmPage";

  private BeanFactory beanFactory;

  private ContextHelper contextHelper;
  private NavigationSegmentsUriHelper navigationSegmentsUriHelper;
  private SitesService sitesService;
  private ContentBeanFactory contentBeanFactory;

  private Cache cache;

  // --- configuration ----------------------------------------------

  @Override
  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Required
  public void setContextHelper(ContextHelper contextHelper) {
    this.contextHelper = contextHelper;
  }

  @Required
  public void setNavigationSegmentsUriHelper(NavigationSegmentsUriHelper navigationSegmentsUriHelper) {
    this.navigationSegmentsUriHelper = navigationSegmentsUriHelper;
  }

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  @Required
  public void setCache(Cache cache) {
    this.cache = cache;
  }

  // --- features ---------------------------------------------------

  protected BeanFactory getBeanFactory() {
    return beanFactory;
  }

  protected Cache getCache() {
    return cache;
  }

  protected ContentBeanFactory getContentBeanFactory() {
    return contentBeanFactory;
  }

  protected NavigationSegmentsUriHelper getNavigationSegmentsUriHelper() {
    return navigationSegmentsUriHelper;
  }

  protected SitesService getSitesService() {
    return sitesService;
  }

  protected ContentLinkBuilder getContentLinkBuilder() {
    return contentLinkBuilder;
  }

  /**
   * Create a page.
   * <p>
   * Consider the developer's work in progress for particular features.
   */
  @NonNull
  protected Page asPage(Navigation context, Linkable content, @Nullable User developer) {
    return asPage(context, content, null, developer);
  }

  /**
   * Create a page with a non-default navigation tree relation.
   * <p>
   * Consider the developer's work in progress for particular features.
   */
  @NonNull
  protected Page asPage(Navigation context,
                        Linkable content,
                        @Nullable TreeRelation<Content> treeRelation,
                        @Nullable User developer) {
    PageImpl page = createPageImpl(content, context, developer);
    page.setTitle(content.getTitle());
    page.setDescription(page.getTitle());
    page.setKeywords(content.getKeywords());
    if (treeRelation != null) {
      page.setContentTreeRelation(treeRelation);
    }
    if (content instanceof CMLinkable) {
      CMLinkable cmLinkable = (CMLinkable) content;
      page.setContentId(String.valueOf(cmLinkable.getContentId()));
      page.setContentType(cmLinkable.getContent().getType().getName());
      page.setValidFrom(cmLinkable.getValidFrom());
      page.setValidTo(cmLinkable.getValidTo());
    }

    // load a dataview for the page
    DataViewFactory dataViewFactory = getDataViewFactory();
    if (dataViewFactory != null) {
      page = dataViewFactory.loadCached(page, null);
    }

    return page;
  }

  /**
   * Create a page.
   * <p>
   * Consider the developer's work in progress for particular features.
   */
  @NonNull
  protected PageImpl createPageImpl(Object content, Navigation context, User developer) {
    PageImpl page = beanFactory.getBean(CMPAGE_PROTOTYPE_BEAN_NAME, PageImpl.class);
    page.setContent(content);
    page.setNavigation(context);
    page.setDeveloper(developer);
    return page;
  }

  @NonNull
  protected ModelAndView createModelAndView(Page page, String view) {
    ModelAndView result = HandlerHelper.createModelWithView(page, view);
    addPageModel(result, page);
    return result;
  }

  @NonNull
  protected ModelAndView createModelAndView(Page page, String view, String orientation) {
    ModelAndView result = createModelAndView(page, view);

    if (!StringUtils.isEmpty(orientation)) {
      result.addObject("orientation", orientation);
    }

    return result;
  }

  /**
   * Adds a page to the model and view as additional model
   *
   * @param modelAndView The target model and view
   * @param page         The page to add as model
   */
  protected void addPageModel(@NonNull ModelAndView modelAndView, Page page) {
    RequestAttributeConstants.setPage(modelAndView, page);
    NavigationLinkSupport.setNavigation(modelAndView, page.getNavigation());
  }

  /**
   * Creates a {@link ModelAndView model} for a given page
   *
   * @param page The page
   * @return The model
   */
  protected final ModelAndView createModel(Page page) {
    return createModelAndView(page, null);
  }

  /**
   * Fetches the path segments from a {@link CMNavigation}
   *
   * @see #getNavigation(java.util.List)
   */
  protected List<String> getPathSegments(Navigation navigation) {
    return navigationSegmentsUriHelper.getPathList(navigation);
  }

  /**
   * Returns the ContextHelper
   */
  protected ContextHelper getContextHelper() {
    return contextHelper;
  }

  /**
   * Provides a {@link CMNavigation} from a sequence of segments
   */
  @Nullable
  protected Navigation getNavigation(String navigationPathElement) {
    return getNavigation(Collections.singletonList(navigationPathElement));
  }

  /**
   * Provides a {@link CMNavigation} from a sequence of segments
   */
  @Nullable
  protected Navigation getNavigation(List<String> navigationPath) {
    return navigationSegmentsUriHelper.parsePath(navigationPath);
  }

  /**
   * Determines the navigation context for a target {@link CMLinkable}, using
   * {@link com.coremedia.blueprint.common.services.context.ContextHelper#contextFor(CMLinkable)}.
   */
  protected Navigation getNavigation(CMLinkable target) {
    return contextHelper.contextFor(target);
  }

  /**
   * Chooses an appropriate navigation context for a target {@link CMLinkable}, given a current context,
   * using {@link com.coremedia.blueprint.common.services.context.ContextHelper#findAndSelectContextFor}.
   */
  @Nullable
  protected CMContext getContext(@NonNull CMContext current, @NonNull CMLinkable target) {
    return contextHelper.findAndSelectContextFor(current, target);
  }

  /**
   * Appends vanity name and id of the linkable to the urlPath.
   */
  protected void appendNameAndId(CMLinkable linkable, List<String> urlPath) {
    urlPath.add(getVanityName(linkable) + '-' + getId(linkable));
  }

  /**
   * Provides a (vanity) name for a linkable to be used inside a link
   */
  @NonNull
  protected String getVanityName(@NonNull CMLinkable bean) {
    return getContentLinkBuilder().getVanityName(bean.getContent());
  }

  protected void addViewAndParameters(UriComponentsBuilder uriBuilder, String viewName, Map<String, Object> linkParameters) {
    // add optional view query parameter
    if (viewName != null) {
      uriBuilder.queryParam(VIEW_PARAMETER, viewName);
    }
    // add additional query parameters
    addLinkParametersAsQueryParameters(uriBuilder, linkParameters);
  }

  /**
   * Don't try this at home!
   * <p>
   * In order to calculate the content length, a page is always rendered
   * completely, even for a HEAD request.  This methods speeds up HEAD requests
   * by provision of a reduced ModelAndView with a {@link HttpHead} bean, which
   * is rendered by a {@link com.coremedia.blueprint.cae.view.HttpHeadView}.
   * <p>
   * However, there is a serious drawback!  While the
   * <a href="https://tools.ietf.org/html/rfc7230#section-3.3.2">HTTP spec</a>
   * requires to set the Content-length header correctly or not at all, the
   * {@link javax.servlet.http.HttpServletResponse} does not support omission
   * of the Content-length header.  Therefore, responses based on this model
   * have a wrong Content-length header of 0.
   * <p>
   * Because of this violation of the HTTP standard the Blueprint does not use
   * this method, but provides it only as a utility for projects.  Use it only
   * if your CAE serves as an internal private back end for well known clients
   * which do not care for the Content-length header.  Never use this method if
   * your CAE is open for search crawlers or other black box clients.
   */
  protected ModelAndView optimizeForHeadRequest(ModelAndView modelAndView, HttpServletRequest request) {
    boolean isHeadRequest = "HEAD".equals(request.getMethod());
    if (HandlerHelper.isError(modelAndView) || !isHeadRequest) {
      return modelAndView;
    } else {
      return HandlerHelper.createModel(createHttpHeadBean(modelAndView, request));
    }
  }

  /**
   * Create a {@link HttpHead} bean for
   * {@link #optimizeForHeadRequest(ModelAndView, HttpServletRequest)}
   * <p>
   * To be overridden by particular page handlers that use
   * {@link #optimizeForHeadRequest(ModelAndView, HttpServletRequest)}.
   * Make sure that the HttpHead resembles the HTTP head of the response for
   * an according GET request as closely as possible.
   */
  protected HttpHead createHttpHeadBean(ModelAndView modelAndView, HttpServletRequest request) {
    return new HttpHead();
  }
}
