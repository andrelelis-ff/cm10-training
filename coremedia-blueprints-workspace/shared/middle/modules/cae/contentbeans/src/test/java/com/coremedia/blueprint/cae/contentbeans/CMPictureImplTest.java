package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.awt.geom.Point2D;

public class CMPictureImplTest extends ContentBeanTestBase {

  private CMPicture contentBean;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  SettingsService settingsService;

  @Before
  public void setUp() throws Exception {
    contentBean = getContentBean(16);
  }

  @Test
  public void testGetAspectByName() throws Exception {
    Assert.assertEquals(0, contentBean.getAspectByName().size());
  }

  @Test
  public void testGetAspects() throws Exception {
    Assert.assertEquals(0, contentBean.getAspects().size());
  }

  @Test
  public void testGetLocalizations() throws Exception {
    Assert.assertEquals(1, contentBean.getLocalizations().size());
  }

  @Test
  public void testGetVariantsByLocale() throws Exception {
    Assert.assertEquals(1, contentBean.getVariantsByLocale().size());
  }

  @Test
  public void testGetMaster() throws Exception {
    Assert.assertNull(contentBean.getMaster());
  }

  @Test
  public void testGetData() throws Exception {
    Assert.assertNotNull(contentBean.getData());
  }

  @Test
  public void testGetDataUrl() throws Exception {
    Assert.assertEquals("http://coremedia.com/", contentBean.getDataUrl());
  }

  @Test
  public void testGetWidth() throws Exception {
    Assert.assertEquals(800, contentBean.getWidth().intValue());
  }

  @Test
  public void testGetHeight() throws Exception {
    Assert.assertEquals(600, contentBean.getHeight().intValue());
  }

  @Test
  public void testGetFocusPoint() throws Exception {
    Assert.assertEquals(new Point2D.Double(0.6, 0.3), contentBean.getFocusPoint());

    CMPicture pictureWithMovedFocusArea = getContentBean(170);
    Assert.assertEquals(new Point2D.Double(0.4, 0.6), pictureWithMovedFocusArea.getFocusPoint());

    CMPicture pictureWithMovedFocusAreaAndFocusPoint = getContentBean(172);
    Assert.assertEquals(new Point2D.Double(0.6, 0.3), pictureWithMovedFocusAreaAndFocusPoint.getFocusPoint());
  }
}
