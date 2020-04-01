package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMCSS;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMJavaScript;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.feeds.FeedFormat;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import com.coremedia.cap.struct.Struct;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE;

/**
 * Unit test for {@link com.coremedia.blueprint.cae.contentbeans.CMChannelImpl}.
 */
public class CMChannelImplTest extends ContentBeanTestBase {

  private CMChannelImpl grandfather;
  private CMChannel father;
  private CMChannel kid;
  private CMTeasable teasable1;
  private CMTeasable teasable2;
  private CMCSS css;
  private CMJavaScript javaScript;
  @Inject
  private SettingsService settingsService;

  @Before
  public void before() {
    grandfather = getContentBean(10);
    father = getContentBean(12);
    kid = getContentBean(14);
    teasable1 = getContentBean(4);
    teasable2 = getContentBean(6);
    css = getContentBean(48);
    javaScript = getContentBean(50);
    setUpPreviewDate(REQUEST_ATTRIBUTE_PREVIEW_DATE);
  }

  @Test
  public void testGetHeader() {
    List<? extends Linkable> header = grandfather.getHeader();
    Assert.assertEquals(1, header.size());
    Assert.assertFalse(header.contains(teasable1)); //is filtered
    Assert.assertTrue(header.contains(teasable2));
  }

  @Test
  public void testGetFooter() {
    List<? extends Linkable> footer = grandfather.getFooter();
    Assert.assertEquals(1, footer.size());
    Assert.assertFalse(footer.contains(teasable1)); //is filtered
    Assert.assertTrue(footer.contains(teasable2));
  }

  @Test
  public void testGetCss() {
    List<? extends CMCSS> cssList = grandfather.getCss();
    Assert.assertEquals(1, cssList.size());
    Assert.assertTrue(cssList.contains(css));
  }

  @Test
  public void testGetJavaScript() {
    List<? extends CMJavaScript> javaScript = grandfather.getJavaScript();
    Assert.assertEquals(1, javaScript.size());
    Assert.assertTrue(javaScript.contains(this.javaScript));
  }

  @Test
  public void testSettingsMechanism() {
    Assert.assertTrue(getSettingsService().setting("kid", Boolean.class, kid));
    Assert.assertTrue(getSettingsService().setting("father", Boolean.class, kid));
    Assert.assertTrue(getSettingsService().setting("grandfather", Boolean.class, kid));

    Assert.assertTrue(getSettingsService().setting("father", Boolean.class, father));
    Assert.assertTrue(getSettingsService().setting("grandfather", Boolean.class, father));

    Assert.assertTrue(getSettingsService().setting("grandfather", Boolean.class, grandfather));
  }

  @Test
  public void testMergingOfSettings() {
    Map<String, Object> grandfatherSettings = settingsService.settingAsMap("elasticSocial", String.class, Object.class, grandfather);
    Assert.assertNotNull(grandfatherSettings);
    Assert.assertTrue((Boolean) grandfatherSettings.get("enabled"));
    Assert.assertEquals(79, grandfatherSettings.get("port"));

    Map<String, Object> fatherSettings = settingsService.settingAsMap("elasticSocial", String.class, Object.class, father);
    Assert.assertNotNull(fatherSettings);
    Assert.assertFalse((Boolean) fatherSettings.get("enabled"));
    Assert.assertEquals(79, fatherSettings.get("port"));
    Assert.assertEquals(81, fatherSettings.get("anotherPort"));
    Assert.assertTrue((Boolean) fatherSettings.get("inheritedFromGrandfather"));
    Assert.assertTrue((Boolean) fatherSettings.get("inheritedFromFather"));

    Map<String, Object> kidSettings = settingsService.settingAsMap("mergeTest", String.class, Object.class, kid);
    Assert.assertNotNull(kidSettings);
    Assert.assertTrue((Boolean) kidSettings.get("enabled"));
    Assert.assertTrue((Boolean) kidSettings.get("thisIsTrue"));
  }

  @Test
  public void testParentNavigation() {
    Assert.assertEquals(father, kid.getParentNavigation());
    Assert.assertEquals(grandfather, father.getParentNavigation());
    Assert.assertNull(grandfather.getParentNavigation());
  }

  @Test
  public void testGetAspectByName() {
    Assert.assertEquals(0, kid.getAspectByName().size());
  }

  @Test
  public void testGetAspects() {
    Assert.assertEquals(0, kid.getAspects().size());
  }

  @Test
  public void testGetLocalizations() {
    Assert.assertEquals(1, kid.getLocalizations().size());
  }

  @Test
  public void testGetVariantsByLocale() {
    Assert.assertEquals(1, kid.getVariantsByLocale().size());
  }

  @Test
  public void testGetMaster() {
    Assert.assertNull(kid.getMaster());
  }

  @Test
  public void testGetVisibleChildren() {
    Assert.assertEquals(0, grandfather.getVisibleChildren().size());
  }

  @Test
  public void testGetChildren() {
    Assert.assertEquals(1, grandfather.getChildren().size());
  }

  @Test
  public void testGetSitemapChildren() {
    Assert.assertEquals(0, grandfather.getSitemapChildren().size());
    Assert.assertEquals(0, father.getSitemapChildren().size());
  }

  @Test
  public void testIsHidden() {
    Assert.assertTrue(father.isHidden());
  }

  @Test
  public void testIsHiddenInSitemap() {
    Assert.assertTrue(kid.isHiddenInSitemap());
  }

  @Test
  public void testGetNavigationPathList() {
    Assert.assertEquals(3, kid.getNavigationPathList().size());
  }

  @Test
  public void testGetRootNavigations() {
    Assert.assertEquals(1, kid.getRootNavigations().size());
  }

  @Test
  public void testGetFeedTitle() {
    Assert.assertEquals("", kid.getFeedTitle());
  }

  @Test
  public void testGetFeedFormat() {
    Assert.assertEquals(FeedFormat.Rss_2_0, kid.getFeedFormat());
  }

  @Test
  public void testGetLocalSettings() {
    Struct localSettingsMerged = grandfather.getLocalAndLinkedSettings().getStruct("elasticSocial");
    Assert.assertNotNull(localSettingsMerged);
    Assert.assertTrue(localSettingsMerged.getBoolean("enabled"));
    Assert.assertEquals(79, localSettingsMerged.getInt("port"));
    Assert.assertTrue(localSettingsMerged.getBoolean("linkedSetting"));
  }

  @Test
  public void testGetContexts() {
    List<? extends CMContext> contexts = father.getContexts();
    Assert.assertTrue(contexts.contains(father));
    Assert.assertEquals(1, contexts.size());
  }

  private SettingsService getSettingsService() {
    return settingsService;
  }
}
