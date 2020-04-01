package com.coremedia.blueprint.assets.contentbeans;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
public class AMTaxonomyImplTest extends AMTestBase {

  private AMTaxonomyImpl amTaxonomy;
  private AMAsset amAsset;

  @Before
  public void setUp() throws Exception {
    amTaxonomy = getContentBean(2);
    amAsset = getContentBean(4);
  }

  @Test
  public void test() {
    assertNotNull(amTaxonomy);
    assertEquals(amAsset, amTaxonomy.getAssetThumbnail());
  }
}
