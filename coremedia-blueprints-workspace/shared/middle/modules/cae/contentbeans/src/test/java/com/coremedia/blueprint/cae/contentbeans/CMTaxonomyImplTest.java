package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMLocTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CMTaxonomyImplTest extends ContentBeanTestBase {

  private CMLocTaxonomy world;
  private CMLocTaxonomy unitedStates;

  private CMLocTaxonomy cyclicWorld;
  private CMLocTaxonomy europe;

  @Before
  public void setUp() throws Exception {
    world = getContentBean(62);
    unitedStates = getContentBean(64);

    cyclicWorld = getContentBean(762);
    europe = getContentBean(782);
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
  public void testGetValue() throws Exception {
    Assert.assertEquals("World", world.getValue());
  }

  @Test
  public void testGetParent() throws Exception {
    Assert.assertNull(world.getParent());

    Assert.assertEquals(world, unitedStates.getParent());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetParentWithCyclicDependency() throws Exception {
    Assert.assertNotNull(cyclicWorld.getParent());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetParentWithCyclicDependency2() throws Exception {
    Assert.assertEquals(cyclicWorld, ((CMLocTaxonomy) getContentBean(766)).getParent().getParent());
  }

  @Test
  public void testGetChildren() throws Exception {
    Assert.assertTrue(world.getChildren().contains(unitedStates));
  }

  @Test
  public void testGetExternalReference() throws Exception {
    Assert.assertEquals("World", world.getExternalReference());
  }

  @Test
  public void testGetTaxonomyPathList() throws Exception {
    List<CMTaxonomy> result = new ArrayList<>();
    result.add(world);
    Assert.assertEquals(result, world.getTaxonomyPathList());
    result.add(unitedStates);
    Assert.assertEquals(result, unitedStates.getTaxonomyPathList());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetTaxonomyPathListCycle() throws Exception {
    List<CMTaxonomy> result = new ArrayList<>();
    result.add(cyclicWorld);
    result.add(europe);
    CMTaxonomy cycle = getContentBean(766);
    result.add(cycle);
    Assert.assertEquals(result, cycle.getTaxonomyPathList());
  }
}
