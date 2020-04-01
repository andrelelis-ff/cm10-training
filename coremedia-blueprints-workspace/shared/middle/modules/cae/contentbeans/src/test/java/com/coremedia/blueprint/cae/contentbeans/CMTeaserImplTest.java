package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.blueprint.common.contentbeans.CMTeaser;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.struct.Struct;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CMTeaserImplTest extends ContentBeanTestBase {

  private CMTeaser teaser;

  @Before
  public void setUp() throws Exception {
    teaser = getContentBean(56);
  }

  @Test
  public void testGetLocalSettings() throws Exception {
    Struct localSettings = teaser.getLocalSettings();
    assertNotNull(localSettings);
    assertTrue(CapStructHelper.getBoolean(localSettings, "setIndirectly"));
    assertTrue(CapStructHelper.getBoolean(localSettings, "setDirectly"));
    assertFalse(CapStructHelper.getBoolean(localSettings, "willBeOverridden"));
    assertEquals("size", 3, localSettings.toNestedMaps().size());
  }

  @Test
  public void testGetAspectByName() throws Exception {
    assertEquals(0, teaser.getAspectByName().size());
  }

  @Test
  public void testGetAspects() throws Exception {
    assertEquals(0, teaser.getAspects().size());
  }

  @Test
  public void testGetLocalizations() throws Exception {
    assertEquals(1, teaser.getLocalizations().size());
  }

  @Test
  public void testGetVariantsByLocale() throws Exception {
    assertEquals(1, teaser.getVariantsByLocale().size());
  }

  @Test
  public void testGetMaster() throws Exception {
    assertNull(teaser.getMaster());
  }

  @Test
  public void testGetTarget() {
    assertEquals(6, teaser.getTarget().getContentId());
  }

  @Test
  public void testGetInvalidTarget() {
    teaser = getContentBean(160);
    assertNull(teaser.getTarget());
  }

  @Test
  public void testGetTargetsFromLecacyTarget() {
    teaser = getContentBean(148);
    Map<String, Object> targetMap = createTargetStructMap(getContentBean(144));
    assertEquals(createTargetsStructMap(targetMap), teaser.getTargets());

    teaser = getContentBean(164);
    targetMap = createTargetStructMap(getContentBean(144), true, null);
    assertEquals(createTargetsStructMap(targetMap), teaser.getTargets());

    teaser = getContentBean(166);
    targetMap = createTargetStructMap(getContentBean(144), true, "This is some custom cta text.");
    assertEquals(createTargetsStructMap(targetMap), teaser.getTargets());
  }

  private Map<String, List<Map<String, Object>>> createTargetsStructMap(Map<String, Object>... targetMaps) {
    return Collections.singletonMap("links", Arrays.asList(targetMaps));
  }

  @Test
  public void testGetTargetsWithInvalids() {
    teaser = getContentBean(160);
    assertEquals(createTargetsStructMap(), teaser.getTargets());

    teaser = getContentBean(162);
    Map<String, Object> targetMap = createTargetStructMap(getContentBean(6), true, "This is a CTA custom text");
    assertEquals(createTargetsStructMap(targetMap), teaser.getTargets());
    assertEquals(targetMap.get("target"), teaser.getTarget());
  }

  @Test
  public void testGetTargetFromTargets() {
    teaser = getContentBean(168);
    Map<String, Object> targetMap1 = createTargetStructMap(getContentBean(2), null, null);
    Map<String, Object> targetMap2 = createTargetStructMap(getContentBean(6), true, "This is a CTA custom text");

    assertEquals(createTargetsStructMap(targetMap1, targetMap2), teaser.getTargets());
    assertEquals(targetMap1.get("target"), teaser.getTarget());
  }

  @Test
  public void testGetLegacyAnnotatedLinksFromTargets() {
    teaser = getContentBean(168);
    Map<String, Object> targetMap1 = createTargetStructMap(getContentBean(2), null, null);
    Map<String, Object> targetMap2 = createTargetStructMap(getContentBean(6), true, "This is a CTA custom text");

    List<CMLinkable> links = ((CMTeaserBase) teaser).getLegacyAnnotatedLinks("targets", "target");
    assertEquals(2, links.size());
    assertEquals(targetMap1.get("target"), links.get(0));
    assertEquals(targetMap2.get("target"), links.get(1));
  }

  @Test
  public void testGetLegacyAnnotatedLinksFromTarget() {
    teaser = getContentBean(164);
    Map<String, Object> targetMap = createTargetStructMap(getContentBean(144), null, null);

    List<CMLinkable> links = ((CMTeaserBase) teaser).getLegacyAnnotatedLinks("targets", "target");

    assertEquals(1, links.size());
    assertEquals(targetMap.get("target"), links.get(0));
  }

  @Test
  public void testGetLegacyAnnotatedLinkFromTarget() {
    teaser = getContentBean(164);
    Map<String, Object> targetMap = createTargetStructMap(getContentBean(144), null, null);

    CMLinkable link = ((CMTeaserBase) teaser).getLegacyAnnotatedLink("targets", "target");

    assertNotNull(link);
    assertEquals(targetMap.get("target"), link);
  }

  private Map<String, Object> createTargetStructMap(CMLinkable target) {
    return createTargetStructMap(target, false, null);
  }

  private Map<String, Object> createTargetStructMap(CMLinkable target, Boolean ctaEnabled, String ctaCustomText) {
    Map<String, Object> targetMap = new LinkedHashMap<>(3);
    targetMap.put("target", target);
    if (ctaEnabled != null && ctaEnabled) {
      targetMap.put("callToActionEnabled", ctaEnabled);
    }
    if (ctaCustomText != null && !ctaCustomText.isEmpty()) {
      targetMap.put("callToActionCustomText", ctaCustomText);
    }
    return targetMap;
  }

  @Test
  public void testHandleTargetIsSelf() throws Exception {
    // reproducer for CMS-2361
    teaser = getContentBean(144);
    List<? extends CMPicture> pictures = teaser.getPictures();// no recursive call expected!
    assertEquals(1, pictures.size());
    assertEquals(16, pictures.get(0).getContentId());
  }

  @Test
  public void testHandlePicturesFromTarget() throws Exception {
    teaser = getContentBean(146);
    List<? extends CMPicture> pictures = teaser.getPictures();// no recursive call expected!
    assertEquals(1, pictures.size());
    assertEquals(16, pictures.get(0).getContentId());
  }

  @Test
  public void testHandlePicturesFromSelf() throws Exception {
    teaser = getContentBean(148);
    List<? extends CMPicture> pictures = teaser.getPictures();// no recursive call expected!
    assertEquals(1, pictures.size());
    assertEquals(20, pictures.get(0).getContentId());
  }

  @Test
  public void testLocalSettingsForTargetIsSelf() throws Exception {
    // may not result in endless recursion
    teaser = getContentBean(144);
    Struct localSettings = teaser.getLocalSettings();
    assertNotNull(localSettings);
    assertFalse(localSettings.getProperties().isEmpty());
  }
}
