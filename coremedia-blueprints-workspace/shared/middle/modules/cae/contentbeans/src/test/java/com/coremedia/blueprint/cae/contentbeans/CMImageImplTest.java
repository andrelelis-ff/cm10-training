package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMImage;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CMImageImplTest extends ContentBeanTestBase {

  private CMImage image;

  @Before
  public void setUp() throws Exception {
    image = getContentBean(94);
  }

  @Test
  public void testGetAspectByName() throws Exception {
    Assert.assertEquals(0, image.getAspectByName().size());
  }

  @Test
  public void testGetAspects() throws Exception {
    Assert.assertEquals(0, image.getAspects().size());
  }

  @Test
  public void testGetLocalizations() throws Exception {
    Assert.assertEquals(1, image.getLocalizations().size());
  }

  @Test
  public void testGetMaster() throws Exception {
    Assert.assertNull(image.getMaster());
  }


  @Test
  public void testGetData() throws Exception {
    Assert.assertNotNull(image.getData());
  }

  @Test
  public void testGetDescription() throws Exception {
    Assert.assertEquals("description", image.getDescription());
  }
}
