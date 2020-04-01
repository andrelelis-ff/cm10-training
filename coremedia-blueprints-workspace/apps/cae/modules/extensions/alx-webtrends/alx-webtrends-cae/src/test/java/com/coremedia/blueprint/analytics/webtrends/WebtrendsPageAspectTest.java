package com.coremedia.blueprint.analytics.webtrends;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMJavaScript;
import com.coremedia.blueprint.common.contentbeans.Page;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebtrendsPageAspectTest {

  private WebtrendsPageAspect aspect;

  @Mock
  private Page page;

  @Mock
  private SettingsService settingsService;

  private final Map<String, Object> settingsMap = new HashMap<>();

  @Before
  public void setup() {
    aspect = new WebtrendsPageAspect(page, settingsService);

    when(settingsService.settingAsMap(aspect.getServiceKey(), String.class, Object.class, page)).thenReturn(settingsMap);

    settingsMap.put("associatedScripts", asList(mock(CMJavaScript.class)));
  }

  @After
  public void tearDown() {
    settingsMap.clear();
  }

  @Test
  public void testFullyConfigured() {
    settingsMap.put("dcsid", "123");
    settingsMap.put("dcssip", "domain");
    settingsMap.put("domain", "testDomain");

    Assert.assertTrue(aspect.isEnabled());
    Assert.assertEquals("123", aspect.getDcsid());
    Assert.assertEquals("domain", aspect.getDcssip());
  }

  @Test
  public void testWithPageWithoutWebtrendsSettings() {
    Assert.assertFalse(aspect.isEnabled());
    Assert.assertNull(aspect.getDcsid());
    Assert.assertNull(aspect.getDcssip());
  }

  @Test
  public void testWithDisabledWebtrekk() {
    settingsMap.put("disabled", false);
    Assert.assertFalse(aspect.isEnabled());
    settingsMap.put("dcsid", "123");
    Assert.assertFalse(aspect.isEnabled());
    settingsMap.put("dcssip", "domain");
    Assert.assertTrue(aspect.isEnabled());
    settingsMap.put("disabled", true);
    Assert.assertFalse(aspect.isEnabled());
  }

}
