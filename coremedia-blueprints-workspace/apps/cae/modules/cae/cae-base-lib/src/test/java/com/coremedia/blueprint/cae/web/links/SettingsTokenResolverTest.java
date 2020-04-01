package com.coremedia.blueprint.cae.web.links;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SettingsTokenResolverTest {
  @Mock
  private HttpServletRequest request;

  @Mock
  private Page page;

  @Mock
  private SettingsService settingsService;

  private SettingsTokenResolver testling;

  @Before
  public void setup() {
    testling = new SettingsTokenResolver();
    testling.setSettingsService(settingsService);
  }

  @Test
  public void testNullNullNull() {
    Object[] expected = new Object[]{null};
    Object[] actual = SettingsTokenResolver.grabBeans(null, request);
    assertTrue(Arrays.equals(expected, actual));
  }

  @Test
  public void testNullRequest() {
    Object[] expected = new Object[]{"bean", null};
    Object[] actual = SettingsTokenResolver.grabBeans("bean", request);
    assertTrue(Arrays.equals(expected, actual));
  }

  @Test
  public void testBeanSelfPage() {
    when(request.getAttribute("self")).thenReturn("self");
    when(request.getAttribute(ContextHelper.ATTR_NAME_PAGE)).thenReturn(page);
    Object[] expected = new Object[] {"bean", "self", page};
    Object[] actual = SettingsTokenResolver.grabBeans("bean", request);
    assertTrue(Arrays.equals(expected, actual));
  }

  @Test
  public void testDuplicateBeans() {
    when(request.getAttribute("self")).thenReturn(page);
    when(request.getAttribute(ContextHelper.ATTR_NAME_PAGE)).thenReturn(page);
    Object[] expected = new Object[]{page};
    Object[] actual = SettingsTokenResolver.grabBeans(page, request);
    assertTrue(Arrays.equals(expected, actual));
  }

  @Test
  public void testSimpleTokenPrecedence() {
    when(settingsService.setting("foo.bar", String.class, "bean", null)).thenReturn("bla");
    when(settingsService.nestedSetting(Arrays.asList("foo", "bar"), String.class, "bean", null)).thenReturn("blub");
    String actual = testling.resolveToken("foo.bar", "bean", null);
    assertEquals("bla", actual);
  }

  @Test
  public void testCompoundToken() {
    when(settingsService.nestedSetting(Arrays.asList("foo", "bar"), String.class, "bean", null)).thenReturn("blub");
    String actual = testling.resolveToken("foo.bar", "bean", null);
    assertEquals("blub", actual);
  }
}
