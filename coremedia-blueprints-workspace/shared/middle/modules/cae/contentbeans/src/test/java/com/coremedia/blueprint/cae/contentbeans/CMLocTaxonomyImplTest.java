package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMLocTaxonomy;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CMLocTaxonomyImplTest extends ContentBeanTestBase {

  private CMLocTaxonomy world;

  @Before
  public void setUp() throws Exception {
    world = getContentBean(62);
  }

  @Test
  public void testGetAspectByName() throws Exception {
    Assert.assertEquals(0, world.getAspectByName().size());
  }

  @Test
  public void testGetAspects() throws Exception {
    Assert.assertEquals(0, world.getAspects().size());
  }

  @Test
  public void testGetPostcode() throws Exception {
    Assert.assertEquals("20459", world.getPostcode());
  }

  @Test
  public void testGetLatitude() throws Exception {
    Assert.assertEquals("53.523982", world.getLatitude());
  }

  @Test
  public void testGetLongitude() throws Exception {
    Assert.assertEquals("10.003052", world.getLongitude());
  }
}
