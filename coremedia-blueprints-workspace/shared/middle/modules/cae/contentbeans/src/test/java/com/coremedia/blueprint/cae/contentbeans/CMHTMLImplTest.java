package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMHTML;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CMHTMLImplTest extends ContentBeanTestBase {

  private CMHTML contentBean;

  @Before
  public void setUp() throws Exception {
    contentBean = getContentBean(110);
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
  public void testGetDescription() throws Exception {
    Assert.assertEquals("description", contentBean.getDescription());
  }

  @Test
  public void testGetData() throws Exception {
    Assert.assertNotNull(contentBean.getData());
  }
}

