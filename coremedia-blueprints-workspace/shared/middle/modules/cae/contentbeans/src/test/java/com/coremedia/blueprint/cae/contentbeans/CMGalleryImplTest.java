package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMGallery;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CMGalleryImplTest extends ContentBeanTestBase {

  private CMGallery cmGallery;

  @Before
  public void setUp() throws Exception {
    cmGallery = getContentBean(58);
  }

  @Test
  public void testGetAspectByName() throws Exception {
    Assert.assertEquals(0, cmGallery.getAspectByName().size());
  }

  @Test
  public void testGetAspects() throws Exception {
    Assert.assertEquals(0, cmGallery.getAspects().size());
  }

  @Test
  public void testGetLocalizations() throws Exception {
    Assert.assertEquals(1, cmGallery.getLocalizations().size());
  }

  @Test
  public void testGetVariantsByLocale() throws Exception {
    Assert.assertEquals(1, cmGallery.getVariantsByLocale().size());
  }

  @Test
  public void testGetMaster() throws Exception {
    Assert.assertNull(cmGallery.getMaster());
  }
}
