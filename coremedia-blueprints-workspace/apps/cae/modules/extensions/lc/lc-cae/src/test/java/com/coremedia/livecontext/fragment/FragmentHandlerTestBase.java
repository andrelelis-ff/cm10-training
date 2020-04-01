package com.coremedia.livecontext.fragment;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.cae.view.DynamicInclude;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.layout.PageGridPlacement;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.ContentSiteAspect;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.ecommerce.test.TestVendors;
import com.coremedia.livecontext.contentbeans.CMExternalChannel;
import com.coremedia.livecontext.contentbeans.LiveContextExternalChannelImpl;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.context.ResolveContextStrategy;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.StoreContextProvider;
import com.coremedia.livecontext.fragment.pagegrid.PageGridPlacementResolver;
import com.coremedia.livecontext.fragment.resolver.ContentCapIdExternalReferenceResolver;
import com.coremedia.livecontext.fragment.resolver.ContentNumericIdExternalReferenceResolver;
import com.coremedia.livecontext.fragment.resolver.ContentNumericIdWithChannelIdExternalReferenceResolver;
import com.coremedia.livecontext.fragment.resolver.ContentPathExternalReferenceResolver;
import com.coremedia.livecontext.fragment.resolver.LinkableAndNavigation;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolverImpl;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.HttpError;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class FragmentHandlerTestBase<T extends FragmentHandler> {

  protected static final String STORE_ID = "Sirius Cybernetics Corporation";
  protected static final String CATEGORY_ID = "Sirius Cybernetics Corporation";
  protected static final String SITE_NAME = "Betelgeuse";
  protected static final String SITE_ID = "0987654321";
  protected static final String LOCALE_STRING = "en-US";
  protected static final Locale LOCALE = Locale.forLanguageTag(LOCALE_STRING);
  protected static final String EXTERNAL_TECH_ID = "Nutrimatic Drinks Dispenser";
  protected static final String VIEW = "Point of View Gun";
  protected static final String TITLE = "title";
  protected static final String KEYWORDS = "keywords";
  protected static final int CMCONTEXT_ID = 42;
  protected static final String PLACEMENT = "placement";
  protected static final String EXTERNAL_REF = "cm:coremedia:///cap/content/5678";
  protected static final String PARAMETER = "parameter";
  protected static final String SITE_ATTRIBUTE_NAME = SiteHelper.class.getName() + "site";

  @Mock
  protected BeanFactory beanFactory;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  protected SitesService sitesService;

  @Mock
  private StoreContextProvider storeContextProvider;

  @Mock
  protected ResolveContextStrategy resolveContextStrategy;

  @Mock
  private StoreContext storeContext;

  @Mock
  private FragmentContext fragmentContext;

  @Mock
  protected FragmentParameters fragmentParameters;

  @Mock
  protected Site site;

  @Mock
  protected ContentSiteAspect contentSiteAspect;

  @Mock
  private Content rootFolder;

  @Mock
  protected Content rootChannel;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  ContentRepository contentRepository;

  @Mock
  ContentType externalChannelContentType;

  @Mock
  private CMChannel rootChannelBean;

  @Mock
  protected LiveContextNavigation navigation;

  @Mock
  private CMChannel cmContext;

  @Mock
  private Content cmContextContent;

  @Mock
  protected ContentType cmContextContentType;

  @Mock
  protected LiveContextExternalChannelImpl cmExternalChannelContext;

  @Mock
  private Content cmExternalChannelContextContent;

  @Mock
  protected ContentType cmExternalChannelContextContentType;

  @Mock
  protected ContentBeanFactory contentBeanFactory;

  @Mock
  protected PageGridPlacement placement;

  @Mock
  protected PageGridPlacementResolver pageGridPlacementResolver;

  @Mock
  protected ValidationService validationService;

  @Mock
  protected Content linkable;

  @Mock
  protected Content navigationDoc;

  @Mock
  protected LinkableAndNavigation linkableAndNavigation;

  @Mock
  protected CMLinkable linkableBean;

  @Mock
  protected LiveContextSiteResolverImpl siteResolver;

  @Mock
  protected HttpServletRequest request;

  @Mock
  protected ContentCapIdExternalReferenceResolver contentCapIdExternalReferenceResolver;

  @Mock
  protected ContentNumericIdWithChannelIdExternalReferenceResolver contentNumericIdWithChannelIdExternalReferenceResolver;

  @Mock
  protected ContentNumericIdExternalReferenceResolver contentNumericIdExternalReferenceResolver;

  @Mock
  protected ContentPathExternalReferenceResolver contentPathExternalReferenceResolver;

  protected CommerceConnection connection;

  @Mock
  private CatalogService catalogService;

  @Mock
  private CommerceBeanFactory commerceBeanFactory;

  @Mock
  protected Product product;

  @Mock
  protected Category category;

  private T testling;

  protected final Cache cache = new Cache("test");

  protected void defaultSetup() {
    testling = createTestling();
    testling.setPageGridPlacementResolver(pageGridPlacementResolver);
    testling.setSitesService(sitesService);
    testling.setContentBeanFactory(contentBeanFactory);
    testling.setValidationService(validationService);
    testling.setBeanFactory(beanFactory);
    when(beanFactory.getBean("cmPage", PageImpl.class)).thenReturn(new PageImpl(false, sitesService, cache, null, null, null));

    siteResolver.setSitesService(sitesService);

    when(request.getAttribute(FragmentContextProvider.FRAGMENT_CONTEXT_ATTRIBUTE)).thenReturn(fragmentContext);

    Set<Site> sites = new HashSet<>();
    sites.add(site);
    when(site.getId()).thenReturn(SITE_ID);
    when(site.getName()).thenReturn(SITE_NAME);
    when(site.getLocale()).thenReturn(LOCALE);
    when(site.getSiteRootDocument()).thenReturn(rootChannel);
    when(sitesService.getSites()).thenReturn(sites);
    when(request.getAttribute(SITE_ATTRIBUTE_NAME)).thenReturn(site);

    when(fragmentParameters.getExternalRef()).thenReturn(EXTERNAL_REF);
    when(fragmentParameters.getStoreId()).thenReturn(STORE_ID);
    when(fragmentParameters.getLocale()).thenReturn(LOCALE);
    when(fragmentParameters.getParameter()).thenReturn(PARAMETER);
    when(fragmentParameters.getView()).thenReturn(VIEW);
    when(fragmentParameters.getParameter()).thenReturn(PLACEMENT);

    when(storeContextProvider.findContextBySite(site)).thenReturn(Optional.of(storeContext));
    when(storeContext.getStoreId()).thenReturn(STORE_ID);

    when(resolveContextStrategy.resolveContext(any(Site.class), any(Product.class))).thenReturn(Optional.of(navigation));
    when(resolveContextStrategy.resolveContext(any(Site.class), any(Category.class))).thenReturn(Optional.of(navigation));
    when(navigation.getContext()).thenReturn(cmContext);

    when(cmContext.getTitle()).thenReturn(TITLE);
    when(cmContext.getKeywords()).thenReturn(KEYWORDS);
    when(cmContext.getContentId()).thenReturn(CMCONTEXT_ID);
    when(cmContext.getContent()).thenReturn(cmContextContent);
    when(cmContextContent.getType()).thenReturn(cmContextContentType);
    when(cmContextContentType.getName()).thenReturn(CMContext.NAME);

    when(cmExternalChannelContext.getTitle()).thenReturn(TITLE);
    when(cmExternalChannelContext.getKeywords()).thenReturn(KEYWORDS);
    when(cmExternalChannelContext.getContentId()).thenReturn(CMCONTEXT_ID);
    when(cmExternalChannelContext.getContent()).thenReturn(cmExternalChannelContextContent);
    when(cmExternalChannelContextContent.getType()).thenReturn(cmExternalChannelContextContentType);
    when(cmExternalChannelContextContentType.getName()).thenReturn(CMExternalChannel.NAME);

    when(rootChannelBean.getTitle()).thenReturn(TITLE);
    when(rootChannelBean.getKeywords()).thenReturn(KEYWORDS);
    when(rootChannelBean.getContentId()).thenReturn(CMCONTEXT_ID);
    when(rootChannelBean.getContent()).thenReturn(cmContextContent);
    when(cmContextContent.getType()).thenReturn(cmContextContentType);
    when(cmContextContentType.getName()).thenReturn(CMContext.NAME);
    when(contentBeanFactory.createBeanFor(rootChannel, CMChannel.class)).thenReturn(rootChannelBean);
    when(contentBeanFactory.createBeanFor(rootChannel, Navigation.class)).thenReturn(rootChannelBean);
    when(contentBeanFactory.createBeanFor(rootChannel, Linkable.class)).thenReturn(rootChannelBean);

    when(pageGridPlacementResolver.resolvePageGridPlacement(any(CMChannel.class), eq(PLACEMENT))).thenReturn(placement);

    when(linkableAndNavigation.getLinkable()).thenReturn(linkable);
    when(linkableAndNavigation.getNavigation()).thenReturn(navigationDoc);

    when(site.getSiteRootFolder()).thenReturn(rootFolder);
    when(site.getSiteRootDocument()).thenReturn(rootChannel);
    when(rootFolder.getRepository()).thenReturn(contentRepository);
    when(contentRepository.getContentType("CMExternalChannel")).thenReturn(externalChannelContentType);

    connection = mockCommerceConnection();
  }

  protected void defaultTeardown() {
    CurrentStoreContext.remove();
  }

  private CommerceConnection mockCommerceConnection() {
    CommerceConnection connection = mock(CommerceConnection.class);

    StoreContextImpl storeContext = createStoreContext(connection);

    when(connection.getIdProvider()).thenReturn(TestVendors.getIdProvider("vendor"));
    when(connection.getCatalogService()).thenReturn(catalogService);
    when(connection.getCommerceBeanFactory()).thenReturn(commerceBeanFactory);

    CurrentStoreContext.set(storeContext);

    return connection;
  }

  private static StoreContextImpl createStoreContext(CommerceConnection connection) {
    return StoreContextBuilderImpl.from(connection, SITE_ID)
            .withStoreId("10001")
            .withStoreName("aurora")
            .withCatalogId(CatalogId.of("catalog"))
            .withCurrency(Currency.getInstance("USD"))
            .withLocale(Locale.US).build();
  }

  protected void assertDefaultPage(ModelAndView result) {
    assertNotNull(result);
    ModelMap modelMap = result.getModelMap();
    assertTrue(modelMap.containsKey("self"));
    assertTrue(modelMap.containsKey("cmpage"));
  }

  protected FragmentParameters getFragmentParameters4Product() {
    String url = "http://localhost:40081/blueprint/servlet/service/fragment/" + STORE_ID + "/" + LOCALE_STRING + "/params;";
    FragmentParameters params = FragmentParametersFactory.create(url);
    params.setProductId(EXTERNAL_TECH_ID);
    params.setExternalReference(EXTERNAL_REF);
    return params;
  }

  protected FragmentParameters getFragmentParameters4ProductWithCategory() {
    FragmentParameters params = getFragmentParameters4Product();
    params.setCategoryId(CATEGORY_ID);
    return params;
  }

  protected FragmentParameters getFragmentParameters4ProductAssets() {
    String url = "http://localhost:40081/blueprint/servlet/service/fragment/" + STORE_ID + "/" + LOCALE_STRING + "/params;view=asAssets;parameter=orientation%253Dportrait%252Ctypes%253Dall;";
    FragmentParameters params = FragmentParametersFactory.create(url);
    params.setProductId(EXTERNAL_TECH_ID);
    params.setExternalReference(EXTERNAL_REF);
    return params;
  }

  protected void assertDefaultPlacement(ModelAndView result) {
    assertNotNull(result);
    assertNotNull(result.getModel());
    Object self = result.getModel().get("self");
    assertNotNull(self);
    assertTrue(self instanceof DynamicInclude);
    DynamicInclude dynamicInclude = (DynamicInclude) self;
    assertTrue(dynamicInclude.getDelegate() instanceof PageGridPlacement);
  }

  protected Object unwrapDynamicIncludeModel(@NonNull ModelAndView modelAndView) {
    DynamicInclude dynamicInclude = (DynamicInclude) modelAndView.getModel().get("self");
    return dynamicInclude.getDelegate();
  }

  protected void assertErrorPage(ModelAndView result, int expectedErrorCode) {
    HttpError error = (HttpError) result.getModel().get(HandlerHelper.MODEL_ROOT);
    assertEquals(expectedErrorCode, error.getErrorCode());
  }

  public CMChannel getRootChannelBean() {
    return rootChannelBean;
  }

  protected void verifyDefault() {
    //verify(fragmentContext, times(1)).setFragmentRequest(true); => moved to interceptor
  }

  protected StoreContext getStoreContext() {
    return storeContext;
  }

  public ResolveContextStrategy getResolveContextStrategy() {
    return resolveContextStrategy;
  }

  protected SitesService getSitesService() {
    return sitesService;
  }

  public Site getSite() {
    return site;
  }

  public LiveContextNavigation getNavigation() {
    return navigation;
  }

  protected T getTestling() {
    return testling;
  }

  protected abstract T createTestling();
}
