package com.coremedia.blueprint.elastic.social.cae.guid;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.elastic.core.api.settings.Settings;
import com.coremedia.objectserver.beans.ContentBeanIdConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.elastic.social.cae.guid.GuidCookieHandler.GUID_COOKIE_PREFIX;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;
import static com.coremedia.elastic.core.test.Injection.inject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GuidCookieHandlerTest {

  private GuidCookieHandler testling;

  @Mock
  private CMChannel rootChannelBean;

  @Mock
  private SettingsService settingsService;

  @Mock
  private ContentBeanIdConverter contentBeanIdConverter;

  @Before
  public void initializeGuid() throws NoSuchAlgorithmException {
    Settings settings = mock(Settings.class);
    testling = new GuidCookieHandler(settings);
  }

  @Test
  public void setGuid() {
    GuidCookieHandler.setCurrentGuid("4711");
    assertEquals("4711", GuidCookieHandler.getCurrentGuid());
  }

  @Test
  public void testValidateGuidNoContent() {
    boolean isValid = testling.validateGuid("a");
    assertFalse(isValid);
  }

  @Test
  public void testValidateGuid() {
    String guid1 = testling.createGuid();
    String guid2 = testling.createGuid();
    assertNotSame(guid1, guid2);

    assertTrue(testling.validateGuid(guid1));
    assertTrue(testling.validateGuid(guid2));
  }

  @Test(expected = RuntimeException.class)
  public void testGetCurrentGuidInvalidKeys() throws NoSuchAlgorithmException {
    Settings settings = mock(Settings.class);
    when(settings.getString("signCookie.privateKey")).thenReturn("error");

    GuidCookieHandler handler = new GuidCookieHandler(settings);
    inject(handler, settings);

    handler.createGuid();
  }

  /**
   * test link generation
   */
  @Test
  public void buildLink() {
    testling.setContentBeanIdConverter(contentBeanIdConverter);

    int DEFAULT_CONTENT_ID = 42;

    when(contentBeanIdConverter.convert(rootChannelBean)).thenReturn(Integer.toString(DEFAULT_CONTENT_ID));
    when(rootChannelBean.isRoot()).thenReturn(true);
    when(rootChannelBean.getRootNavigation()).thenReturn(rootChannelBean);

    UriComponents uriComponents = testling.buildGUIDLink(rootChannelBean);

    assertNotNull(uriComponents);

    String expectedURI = "/" + PREFIX_DYNAMIC + "/" + PREFIX_SERVICE + "/" + GUID_COOKIE_PREFIX + "/" + DEFAULT_CONTENT_ID;
    assertEquals(expectedURI, uriComponents.toUriString());
  }

  /**
  * no cookie in the session scope => new cookie will be set
  */
  @Test
  public void handlerSetsCookie() {
    testling.setSettingsService(settingsService);
    when(settingsService.nestedSetting(Arrays.asList("elasticSocial", "enabled"), Boolean.class, rootChannelBean)).thenReturn(true);

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getCookies()).thenReturn(new Cookie[0]);

    testling.handleRequest(rootChannelBean, request, response);

    verify(response).addCookie(argThat(cookie -> "guid".equals(cookie.getName())));
    verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  /**
   * cookie is already there and it is correct => do nothing, return OK
   */
  @Test
  public void handlerExtractsExistingCookie() {
    testling.setSettingsService(settingsService);
    when(settingsService.nestedSetting(Arrays.asList("elasticSocial", "enabled"), Boolean.class, rootChannelBean)).thenReturn(true);

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    String existingGUID = testling.createGuid();
    when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("guid", existingGUID)});

    testling.handleRequest(rootChannelBean, request, response);

    verify(response, never()).addCookie(any());
    verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  /**
   * ES is turned off in settings => no cookie should be set
   */
  @Test
  public void handlerESisOFF() {
    testling.setSettingsService(settingsService);
    when(settingsService.nestedSetting(Arrays.asList("elasticSocial", "enabled"), Boolean.class, rootChannelBean)).thenReturn(false);

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getCookies()).thenReturn(new Cookie[0]);

    testling.handleRequest(rootChannelBean, request, response);

    verify(response, never()).addCookie(any());
    verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
