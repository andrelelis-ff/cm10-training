package com.coremedia.blueprint.analytics.elastic.contentbeans;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CMALXBaseListImplTest {

  private Content content;
  private CMALXBaseListImpl<String> stringCMALXBaseList;
  private SettingsService settingsService;

  @Before
  public void testGetTrackedObjects() {
    content = mock(Content.class);
    settingsService = mock(SettingsService.class);
    stringCMALXBaseList = new TestCMALXBaseListImpl();
  }

  @Test
  public void testGetTimeRange() {
    assertEquals(CMALXBaseList.DEFAULT_TIME_RANGE, stringCMALXBaseList.getTimeRange());

    when(content.getInt(CMALXBaseList.TIME_RANGE)).thenReturn(5);
    assertEquals(5, stringCMALXBaseList.getTimeRange());
  }

  @Test
  public void testGetAnalyticsService() {
    Assert.assertNull(stringCMALXBaseList.getAnalyticsProvider());

    final String fromSettings = "fromSettings";
    when(settingsService.setting(CMALXBaseList.ANALYTICS_PROVIDER, String.class, stringCMALXBaseList)).thenReturn(fromSettings);
    assertEquals(fromSettings, stringCMALXBaseList.getAnalyticsProvider());

    final String fromContent = "fromContent";
    when(content.getString(CMALXBaseList.ANALYTICS_PROVIDER)).thenReturn(fromContent);
    assertEquals(fromContent, stringCMALXBaseList.getAnalyticsProvider());
  }

  private class TestCMALXBaseListImpl extends CMALXBaseListImpl<String> {
    @Override
    public SettingsService getSettingsService() {
      return settingsService;
    }

    @Override
    public Content getContent() {
      return content;
    }

    @Override
    public List<String> getItems() {
      throw new UnsupportedOperationException("not implemented");
    }
  }
}
