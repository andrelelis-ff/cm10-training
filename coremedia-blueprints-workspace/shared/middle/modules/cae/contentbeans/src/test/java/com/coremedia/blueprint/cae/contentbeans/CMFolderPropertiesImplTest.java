package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMFolderProperties;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CMFolderPropertiesImplTest extends ContentBeanTestBase {

  private CMFolderProperties contentBean;

  @Before
  public void setUp() throws Exception {
    contentBean = getContentBean(114);
  }

  @Test
  public void testGetAspectByName() throws Exception {
    Assert.assertEquals(0, contentBean.getAspectByName().size());
  }

  @Test
  public void testGetAspects() throws Exception {
    Assert.assertEquals(0, contentBean.getAspects().size());
  }

  public void testGetContexts() throws Exception {
      Assert.assertEquals(10, contentBean.getContexts().get(0).getContentId());
    }
}
