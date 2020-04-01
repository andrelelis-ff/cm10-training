package com.coremedia.livecontext.asset.impl;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.ContentSiteAspect;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetChangesTest {

  @Spy
  private AssetChanges assetChanges;

  @Mock
  private SitesService sitesService;
  @Mock
  private Content content;
  @Mock
  private ContentSiteAspect contentSiteAspect;
  @Mock
  private Site site;

  @Before
  public void setup() throws Exception {
    assetChanges.setSitesService(sitesService);
    assetChanges.afterPropertiesSet();
    when(sitesService.getContentSiteAspect(content)).thenReturn(contentSiteAspect);
    when(contentSiteAspect.getSite()).thenReturn(site);
  }

  @Test
  public void test() {
    // test the assetChanges is correctly filled
    doReturn(of("a", "b")).when(assetChanges).getExternalIds(content);
    assetChanges.update(content);
    Collection<Content> contents = assetChanges.get("a", site);
    assertEquals(1, contents.size());
    assertEquals(content, contents.iterator().next());
    contents = assetChanges.get("b", site);
    assertEquals(content, contents.iterator().next());
    // test the assetChanges is correctly updated
    when(contentSiteAspect.getSite()).thenReturn(null);
    assetChanges.update(content);
    assertTrue(assetChanges.get("a", site).isEmpty());
    assertTrue(assetChanges.get("b", site).isEmpty());
  }

  @Test
  public void testMultipleUpdatesOnSameContent() {
    doReturn(of("a", "b")).when(assetChanges).getExternalIds(content);
    assetChanges.update(content);
    doReturn(of("a")).when(assetChanges).getExternalIds(content);
    assetChanges.update(content);
    doReturn(of()).when(assetChanges).getExternalIds(content);
    assetChanges.update(content);

    Collection<Content> a = assetChanges.get("a", site);
    Collection<Content> b = assetChanges.get("b", site);
    assertTrue(a.isEmpty());
    assertTrue(b.isEmpty());
  }
}