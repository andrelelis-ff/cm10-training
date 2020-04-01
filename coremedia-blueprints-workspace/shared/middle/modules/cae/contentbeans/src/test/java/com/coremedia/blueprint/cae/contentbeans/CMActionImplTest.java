package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CMActionImplTest extends ContentBeanTestBase {

  private CMAction action;

  @Before
  public void setUp() throws Exception {
    action = getContentBean(90);
  }

  @Test
  public void testGetAspectByName() throws Exception {
    action = getContentBean(90);
    Assert.assertEquals(0, action.getAspectByName().size());
  }

  @Test
  public void testGetAspects() throws Exception {
    action = getContentBean(90);
    Assert.assertEquals(0, action.getAspects().size());
  }

  @Test
  public void testGetLocalizations() throws Exception {
    action = getContentBean(90);
    Assert.assertEquals(1, action.getLocalizations().size());
  }

  @Test
  public void testGetMaster() throws Exception {
    action = getContentBean(90);
    Assert.assertNull(action.getMaster());
  }

  @Test
  public void testGetId() throws Exception {
    action = getContentBean(90);
    Assert.assertEquals("content contains String property \"id\", must not return anything else",
            "com.coremedia.blueprint.es.webflow.Login",
            action.getId());
  }

  @Test
  public void testIsWebflow() throws Exception {
    action = getContentBean(90);
        Assert.assertTrue("content contains String property \"type\" with value \"webflow\", must return true",
                action.isWebFlow());
  }
}
