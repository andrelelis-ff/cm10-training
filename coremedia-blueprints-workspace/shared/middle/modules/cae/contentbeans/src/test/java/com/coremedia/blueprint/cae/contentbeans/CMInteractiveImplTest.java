package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMInteractive;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CMInteractiveImplTest extends ContentBeanTestBase {

  private CMInteractive contentBean;

  @Before
  public void setUp() throws Exception {
    contentBean = getContentBean(98);
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
  public void testGetMaster() throws Exception {
    Assert.assertNull(contentBean.getMaster());
  }


  @Test
  public void testGetData() throws Exception {
    Assert.assertNotNull(contentBean.getData());
  }

  @Test
  public void testGetDataUrl() throws Exception {
    Assert.assertEquals("http://www.coremedia.com/", contentBean.getDataUrl());
  }
}
