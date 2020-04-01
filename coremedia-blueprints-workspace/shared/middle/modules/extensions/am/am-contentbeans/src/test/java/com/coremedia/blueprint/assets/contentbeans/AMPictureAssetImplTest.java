package com.coremedia.blueprint.assets.contentbeans;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
public class AMPictureAssetImplTest extends AMTestBase {

  private AMPictureAssetImpl amPictureAsset;

  @Before
  public void setUp() throws Exception {
    amPictureAsset = getContentBean(4);
  }

  @Test
  public void test() {
    assertNotNull(amPictureAsset);
    assertNotNull(amPictureAsset.getRenditions());
    assertEquals(3, amPictureAsset.getRenditions().size());

    assertNotNull(amPictureAsset.getWeb());
    assertNotNull(amPictureAsset.getPrint());

    assertEquals("PictureAsset", amPictureAsset.getTitle());
    assertEquals(4, amPictureAsset.getContentId());
    assertEquals("test123", amPictureAsset.getKeywords());
  }
}
