package com.coremedia.livecontext.fragment.links.transformers;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.layout.ContentBeanBackedPageGridPlacement;
import com.coremedia.blueprint.cae.web.taglib.FindNavigationContext;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMDownload;
import com.coremedia.blueprint.common.contentbeans.CMExternalLink;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.layout.DynamizableContainer;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.context.CurrentContextService;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.livecontext.commercebeans.CategoryInSite;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.fragment.FragmentContext;
import com.coremedia.livecontext.fragment.FragmentContextProvider;
import com.coremedia.livecontext.fragment.links.transformers.resolvers.LiveContextLinkResolver;
import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.objectserver.web.links.LinkTransformer;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.livecontext.fragment.links.CommerceLinkScheme.DUMMY_URI_STRING;
import static com.coremedia.livecontext.fragment.links.transformers.LiveContextLinkTransformerOrderChecker.validateOrder;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.left;
import static org.apache.commons.lang3.StringUtils.remove;

/**
 * LiveContextLinkTransformer that creates Commerce-Links with cm-Param containing the backend-url.
 * in addition:
 * - removing jsessionid
 * - passing ShopController-Redirect Requests
 */
public class LiveContextLinkTransformer implements LinkTransformer, ApplicationListener<ContextRefreshedEvent> {

  protected static final Logger LOG = LoggerFactory.getLogger(LiveContextLinkTransformer.class);

  private static final String VARIANT_PARAM = "variant";
  public static final String LIVECONTEXT_CONTENT_LED = "livecontext.contentLed";
  private static final String DYNAMIC_LINK_INDICATOR = "/" + PREFIX_DYNAMIC + "/";
  private static final String P13N_LINK_INDICATOR = "/p13n/";
  private static final String UNSUPPORTED_LINK = "#";

  private List<LiveContextLinkResolver> liveContextLinkResolverList;
  private boolean isRemoveJSession = true;
  private CurrentContextService currentContextService;
  private SettingsService settingsService;

  @Override
  public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
    validateOrder(contextRefreshedEvent);
  }

  @Override
  public String transform(@NonNull String cmsLink, @Nullable Object bean, String view,
                          @NonNull HttpServletRequest request,
                          @NonNull HttpServletResponse response,
                          boolean forRedirect) {

    if (!canHandle(cmsLink, bean, request)) {
      return cmsLink;
    }

    CMNavigation navigation = getNavigation(bean);
    // do not generate links if the navigation is null, discard the previously generated link
    if (navigation == null) {
      LOG.debug("Cannot transform link. Navigation is null. Link target: {}", bean);
      return UNSUPPORTED_LINK;
    }
    // do not generate links to content of a different site, discard the previously generated link
    if (isContentOfDifferentSite(navigation, request)) {
      LOG.debug("Cannot transform link to a different site. Link target: {}", bean);
      return UNSUPPORTED_LINK;
    }

    String modifiableSource = removeBaseUri(cmsLink, request);
    modifiableSource = removeJSession(modifiableSource);
    Object variant = ViewUtils.getParameters(request).get(VARIANT_PARAM);

    Object content = getContent(bean);
    return transform(modifiableSource, content, variant, navigation, request);
  }

  /**
   * Check prerequisites of live context link transformers
   */
  private boolean canHandle(@NonNull String cmsLink, @Nullable Object bean, @NonNull HttpServletRequest request) {
    if (CurrentStoreContext.find().isEmpty()) {
      // not a commerce request at all
      return false;
    }

    Site site = SiteHelper.getSiteFromRequest(request);
    if (site == null) {
      return false;
    }

    boolean isContentLed = settingsService.getSetting(LIVECONTEXT_CONTENT_LED, Boolean.class, site).orElse(false);
    if (isContentLed && !cmsLink.equals(DUMMY_URI_STRING)) {
      // do not transform links in content-led scenario
      return false;
    }

    // Only transform links for Fragment Requests or dynamic Ajax Requests in case of fragment scenario
    if (!(isFragmentRequest(request) || isDynamicRequest(request))) {
      return false;
    }

    // only transform dynamic links if they are p13n links
    if (cmsLink.contains(DYNAMIC_LINK_INDICATOR) && !(cmsLink.contains(P13N_LINK_INDICATOR))) {
      return false;
    }

    // do not post-process external links
    if (bean instanceof CMExternalLink) {
      return false;
    }

    // do not post-process Download links since directly delivered by the CAE
    // see also link building in CapBlobHandler
    if (bean instanceof CMDownload) {
      return false;
    }

    return isShopUrlSource(bean);
  }

  private static boolean isDynamicRequest(@NonNull HttpServletRequest request) {
    try {
      return request.getRequestURI().contains("/" + PREFIX_DYNAMIC + "/");
    } catch (UnsupportedOperationException ignored) {
      // we may end up here in case of elastic social registration which uses dummy requests internally :(
      return false;
    }
  }

  private static boolean isFragmentRequest(@NonNull HttpServletRequest request) {
    return FragmentContextProvider.findFragmentContext(request)
            .map(FragmentContext::isFragmentRequest)
            .orElse(false);
  }

  /**
   * Check if the bean may be the source of a shop URL
   */
  private static boolean isShopUrlSource(@Nullable Object bean) {
    return bean instanceof CMLinkable
            || bean instanceof LiveContextNavigation
            || bean instanceof Product
            || bean instanceof ProductInSite
            || bean instanceof Category
            || bean instanceof CategoryInSite
            || bean instanceof Page
            || bean instanceof ContentBeanBackedPageGridPlacement
            || bean instanceof DynamizableContainer;
  }

  @NonNull
  private String transform(String source, @Nullable Object content, Object variant, CMNavigation navigation,
                           @NonNull HttpServletRequest request) {
    Optional<String> nonBlankLcUrl = resolveUrl(source, content, variant, navigation, request)
            .filter(StringUtils::isNotBlank);
    if (nonBlankLcUrl.isPresent()) {
      return nonBlankLcUrl.get();
    }

    if (isNotBlank(source)) {
      return source;
    }

    return "#";
  }

  @NonNull
  private Optional<String> resolveUrl(String source, @Nullable Object content, @Nullable Object variant,
                                      @Nullable CMNavigation navigation, @NonNull HttpServletRequest request) {
    if (source == null) {
      return Optional.empty();
    }

    String variantStr = variant != null ? variant + "" : null;

    return liveContextLinkResolverList.stream()
            .filter(resolver -> resolver.isApplicable(content, request))
            .map(resolver -> resolver.resolveUrl(source, content, variantStr, navigation, request))
            .flatMap(Optional::stream)
            .findFirst();
  }

  @Nullable
  private static String removeBaseUri(@Nullable String source, @NonNull HttpServletRequest request) {
    String baseUri = ViewUtils.getBaseUri(request);

    if (source != null && source.startsWith(baseUri)) {
      return remove(source, baseUri);
    }

    return source;
  }

  @Nullable
  private String removeJSession(@Nullable String source) {
    if (isRemoveJSession && source != null && source.contains(";jsessionid")) {
      int jSessionIndex = StringUtils.indexOf(source, ";jsessionid");
      return left(source, jSessionIndex);
    }

    return source;
  }

  @Nullable
  private static Object getContent(@Nullable Object bean) {
    Object content = bean;

    if (content instanceof Page) {
      content = ((Page) content).getContent();
    }

    return content;
  }

  @Nullable
  private CMNavigation getNavigation(@Nullable Object bean) {
    if (bean instanceof Page) {
      bean = ((Page) bean).getNavigation();
    }

    if (bean instanceof CMNavigation) {
      return (CMNavigation) bean;
    }

    if (bean instanceof CMLinkable) {
      List<? extends CMContext> contexts = ((CMLinkable) bean).getContexts();
      if (!contexts.isEmpty()) {
        return contexts.get(0);
      }
    }

    if (bean instanceof ContentBeanBackedPageGridPlacement) {
      return ((ContentBeanBackedPageGridPlacement) bean).getNavigation();
    }

    CMNavigation navigation = currentContextService.getContext();
    if (navigation != null) {
      return navigation;
    }

    return null;
  }

  /**
   * Return true if this is content of a different site
   */
  protected boolean isContentOfDifferentSite(@NonNull CMNavigation navigation, @NonNull HttpServletRequest request) {
    try {
      CMNavigation targetRootNavigation = navigation.getRootNavigation();
      Navigation currentNavigation = FindNavigationContext.getNavigation(request);
      return !currentNavigation.getRootNavigation().equals(targetRootNavigation);
    } catch (Exception e) {
      LOG.error("Cannot determine whether the given content belongs to a different site: " + e.getMessage(), e);
      return false;
    }
  }

  @Required
  public void setLiveContextLinkResolverList(List<LiveContextLinkResolver> liveContextLinkResolverList) {
    this.liveContextLinkResolverList = ImmutableList.copyOf(liveContextLinkResolverList);
  }

  public void setRemoveJSession(boolean removeJSession) {
    isRemoveJSession = removeJSession;
  }

  @Required
  public void setCurrentContextService(CurrentContextService currentContextService) {
    this.currentContextService = currentContextService;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }
}
