package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.user.UserContextProvider;
import com.coremedia.livecontext.ecommerce.user.UserSessionService;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.inject.Inject;
import javax.security.auth.login.CredentialExpiredException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = LoginStatusHandlerTest.LocalConfig.class)
public class LoginStatusHandlerTest {

  private static final String STORE_ID = "4711";
  private static final Locale LOCALE = Locale.US;

  @Inject
  private WebApplicationContext wac;

  @Mock
  private UserSessionService userSessionService;

  @Inject
  private LiveContextSiteResolver liveContextSiteResolver;

  @Inject
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @Inject
  private LinkFormatter linkFormatter;

  private MockMvc mockMvc;

  @Mock
  private CommerceConnection commerceConnection;

  @Mock
  private StoreContext storeContext;

  @Before
  public void setUp() {
    assertEquals("Test started with a current store context set. This is probably because another different " +
                    "test run before and did not clean up properly.",
            Optional.empty(), CurrentStoreContext.find().map(StoreContext::getConnection));

    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

    when(commerceConnection.getStoreContext()).thenReturn(storeContext);
    when(storeContext.getConnection()).thenReturn(commerceConnection);
  }

  @After
  public void tearDown() {
    assertEquals(Optional.empty(), CurrentStoreContext.find().map(StoreContext::getConnection));
  }

  @Test
  public void testStatusSiteNotFound() throws Exception {
    loginStatus(STORE_ID, LOCALE.toLanguageTag()).andExpect(status().isNotFound());
  }

  @Test
  public void testStatusConnectionNotFound() throws Exception {
    mockSite(STORE_ID, LOCALE.toLanguageTag());
    loginStatus(STORE_ID, LOCALE.toLanguageTag()).andExpect(status().isNotFound());
  }

  @Test
  public void testStatusNotLoggedIn() throws Exception {
    Site site = mockSite(STORE_ID, LOCALE.toLanguageTag());
    mockConnection(site);
    when(userSessionService.isLoggedIn()).thenReturn(false);
    when(commerceConnection.getUserSessionService()).thenReturn(Optional.of(userSessionService));

    loginStatus(STORE_ID, LOCALE.toLanguageTag())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("{\"loggedIn\":false}"));
  }

  @Test
  public void testStatusLoggedIn() throws Exception {
    Site site = mockSite(STORE_ID, LOCALE.toLanguageTag());
    mockConnection(site);
    mockIsLoggedIn(commerceConnection);
    loginStatus(STORE_ID, LOCALE.toLanguageTag())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("{\"loggedIn\":true}"));
  }

  @Test
  public void testLinkStatus() {
    HttpServletRequest request = new MockHttpServletRequest();
    HttpServletResponse response = new MockHttpServletResponse();

    when(storeContext.getStoreId()).thenReturn(STORE_ID);
    when(storeContext.getLocale()).thenReturn(LOCALE);

    CurrentStoreContext.set(storeContext);

    try {
      String expected = "/dynamic/loginstatus?storeId=" + STORE_ID + "&locale=" + LOCALE.toLanguageTag();
      String actual = linkFormatter.formatLink(LoginStatusHandler.LinkType.STATUS, null, request, response, false);
      assertEquals(expected, actual);
    } finally {
      CurrentStoreContext.remove();
    }
  }

  private Site mockSite(String storeId, String locale) {
    Site site = mock(Site.class);
    when(liveContextSiteResolver.findSiteFor(storeId, Locale.forLanguageTag(locale))).thenReturn(Optional.of(site));
    return site;
  }

  private void mockConnection(Site site) {
    UserContextProvider userContextProvider = mockUserContextProvider();
    when(commerceConnection.getUserContextProvider()).thenReturn(userContextProvider);

    when(commerceConnectionInitializer.findConnectionForSite(site)).thenReturn(Optional.of(commerceConnection));
  }

  private UserContextProvider mockUserContextProvider() {
    UserContextProvider provider = mock(UserContextProvider.class);
    when(provider.createContext(any(HttpServletRequest.class))).thenReturn(UserContext.builder().build());
    return provider;
  }

  /**
   * Mock logged in user if the given connection is the current connection.
   */
  private void mockIsLoggedIn(CommerceConnection connection) throws CredentialExpiredException {
    when(userSessionService.isLoggedIn()).then(invocation -> isCurrentConnection(connection));
    when(connection.getUserSessionService()).thenReturn(Optional.of(userSessionService));
  }

  private static boolean isCurrentConnection(@NonNull CommerceConnection connection) {
    return CurrentStoreContext.find()
            .map(StoreContext::getConnection)
            .map(connection::equals)
            .orElse(false);
  }

  private ResultActions loginStatus(String storeId, String locale) throws Exception {
    return mockMvc.perform(get("/dynamic/loginstatus")
            .accept("application/json")
            .param("storeId", storeId)
            .param("locale", locale)
    );
  }

  @Configuration
  @EnableWebMvc
  @ImportResource(
          locations = {"classpath:/com/coremedia/cae/link-services.xml"},
          reader = ResourceAwareXmlBeanDefinitionReader.class)
  static class LocalConfig {

    @Inject
    private GenericConversionService conversionService;

    @Bean
    LoginStatusHandler loginStatusHandler() {
      return new LoginStatusHandler(liveContextSiteResolver(), commerceConnectionInitializer());
    }

    @Bean
    LiveContextSiteResolver liveContextSiteResolver() {
      return mock(LiveContextSiteResolver.class);
    }

    @Bean
    CommerceConnectionInitializer commerceConnectionInitializer() {
      return mock(CommerceConnectionInitializer.class);
    }
  }
}
