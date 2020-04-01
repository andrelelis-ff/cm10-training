package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMCSS;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class CMCSSImplTest extends ContentBeanTestBase {

  private CMCSS contentBean;

  @Before
  public void setUp() throws Exception {
    contentBean = getContentBean(48);
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
  public void testGetContentType() throws Exception {
    Assert.assertEquals("text/css", contentBean.getContentType());
  }

  @Test
  public void testGetMedia() throws Exception {
    Assert.assertEquals("media", contentBean.getMedia());
  }

  @Test
  public void testGetDataUrl() throws Exception {
    Assert.assertEquals("http://coremedia.com/", contentBean.getDataUrl());
  }

  @Test
  public void testGetCode() throws Exception {
    Assert.assertNotNull(contentBean.getCode());
  }

  @Test
  public void testGetInclude() throws Exception {
    Assert.assertEquals(118, contentBean.getInclude().get(0).getContentId());
  }

  @Test
  public void testGetDescription() throws Exception {
    Assert.assertEquals("description", contentBean.getDescription());
  }

  @Test
  public void testHtmlAttributes() throws Exception {
    Assert.assertEquals(Collections.emptyMap(), contentBean.getHtmlAttributes());
  }
}
