package com.coremedia.blueprint.assets.cae.tags;


import com.coremedia.blueprint.assets.cae.AMUtils;
import com.coremedia.blueprint.assets.cae.DownloadPortal;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.web.FreemarkerEnvironment;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.inject.Inject;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AMUtils.class, SiteHelper.class, FreemarkerEnvironment.class})
public class AMFreemarkerFacadeTest {

  @Inject
  private MockHttpServletRequest request;

  @Test
  public void hasDownloadPortal() {
    SettingsService settingsService = mock(SettingsService.class);
    Site site = mock(Site.class);
    Content content = mock(Content.class);

    mockStatic(SiteHelper.class);
    mockStatic(AMUtils.class);
    mockStatic(FreemarkerEnvironment.class);
    when(FreemarkerEnvironment.getCurrentRequest()).thenReturn(request);
    when(SiteHelper.findSite(request)).thenReturn(Optional.of(site));
    when(AMUtils.getDownloadPortalRootDocument(settingsService, site)).thenReturn(content);

    AMFreemarkerFacade facade = new AMFreemarkerFacade();
    facade.setSettingsService(settingsService);
    assertTrue(facade.hasDownloadPortal());
  }

  @Test
  public void hasNoDownloadPortal() {
    mockStatic(SiteHelper.class);
    mockStatic(FreemarkerEnvironment.class);
    when(FreemarkerEnvironment.getCurrentRequest()).thenReturn(request);
    when(SiteHelper.findSite(request)).thenReturn(Optional.empty());
    AMFreemarkerFacade facade = new AMFreemarkerFacade();
    assertFalse(facade.hasDownloadPortal());
  }

  @Test
  public void downloadPortal() {
    DownloadPortal portal = mock(DownloadPortal.class);
    AMFreemarkerFacade facade = new AMFreemarkerFacade();
    facade.setDownloadPortal(portal);
    assertEquals(portal, facade.getDownloadPortal());
  }
}
