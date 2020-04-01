package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMJavaScript;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CMJavaScriptImplTest extends ContentBeanTestBase {

  private CMJavaScript contentBean;

  @Before
  public void setUp() throws Exception {
    contentBean = getContentBean(50);
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
    Assert.assertEquals("text/javascript", contentBean.getContentType());
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
    Assert.assertEquals(120, contentBean.getInclude().get(0).getContentId());
  }

  @Test
  public void testGetDescription() throws Exception {
    Assert.assertEquals("description", contentBean.getDescription());
  }

  @Test
  public void testGetIeExpression() throws Exception {
    Assert.assertEquals("ieExpression", contentBean.getIeExpression());
  }

  @Test
  public void testIsIeExpression() throws Exception {
    Assert.assertTrue(contentBean.isIeRevealed());
  }

  @Test
  public void testHtmlAttributes() throws Exception {
    Assert.assertEquals(ImmutableMap.of("defer", true), contentBean.getHtmlAttributes());
  }
}
