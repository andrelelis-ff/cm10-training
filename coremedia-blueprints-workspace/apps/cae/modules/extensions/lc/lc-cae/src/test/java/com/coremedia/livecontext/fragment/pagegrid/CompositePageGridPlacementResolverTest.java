package com.coremedia.livecontext.fragment.pagegrid;

import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.layout.PageGridPlacement;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CompositePageGridPlacementResolverTest{

  @Mock
  private DataViewFactory dataViewFactory;
  @Mock
  private PageGridPlacementResolver resolver;

  private CompositePageGridPlacementResolver testling;

  @Before
  public void setup() {
    initMocks(this);
    testling = new CompositePageGridPlacementResolver();
    testling.setDataViewFactory(dataViewFactory);
    testling.setResolvers(Arrays.asList(resolver));
  }


  @Test
  public void testResolvePageGridPlacement() {
    CMChannel context = mock(CMChannel.class);
    String placementName = "placementName";
    PageGridPlacement placement = mock (PageGridPlacement.class);
    when(resolver.resolvePageGridPlacement(context, placementName)).thenReturn(placement);
    when(dataViewFactory.loadCached(placement, null)).thenReturn(placement);
    assertEquals(placement, testling.resolvePageGridPlacement(context, placementName));

    when(resolver.resolvePageGridPlacement(context, placementName)).thenReturn(null);
    assertNull(testling.resolvePageGridPlacement(context, placementName));

  }
}