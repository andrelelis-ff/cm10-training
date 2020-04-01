package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMExternalLink;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CMExternalLinkImplTest extends ContentBeanTestBase {

  private CMExternalLink externalLink;

  @Before
  public void setUp() throws Exception {
    externalLink = getContentBean(92);
  }

  @Test
  public void testGetAspectByName() throws Exception {
    Assert.assertEquals(0, externalLink.getAspectByName().size());
  }

  @Test
  public void testGetAspects() throws Exception {
    Assert.assertEquals(0, externalLink.getAspects().size());
  }

  @Test
  public void testGetLocalizations() throws Exception {
    Assert.assertEquals(1, externalLink.getLocalizations().size());
  }

  @Test
  public void testGetVariantsByLocale() throws Exception {
    Assert.assertEquals(1, externalLink.getVariantsByLocale().size());
  }

  @Test
  public void testGetMaster() throws Exception {
    Assert.assertNull(externalLink.getMaster());
  }

  @Test
  public void testGetUrl() throws Exception {
    Assert.assertEquals("http://www.coremedia.com/", externalLink.getUrl());
  }
}
