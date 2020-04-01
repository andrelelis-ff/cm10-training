package com.coremedia.livecontext.p13n;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.ecommerce.test.TestVendors;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.p13n.Segment;
import com.coremedia.livecontext.ecommerce.p13n.SegmentService;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.ContextCollectionImpl;
import com.coremedia.personalization.context.MapPropertyMaintainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper.parseCommerceIdOrThrow;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommerceSegmentSourceTest {

  private static final String USER1_NAME = "testUser";
  private static final String USER1_ID = "4711";

  private CommerceSegmentSource testling;

  @Mock
  private CommerceConnection commerceConnection;

  @Mock
  private StoreContext storeContext;

  private MockHttpServletRequest request;

  private MockHttpServletResponse response;

  private ContextCollection contextCollection;

  @Mock
  private SegmentService segmentService;

  @Before
  public void setup() {
    testling = new CommerceSegmentSource();
    testling.setContextName("commerce");

    UserContext userContext = UserContext.builder()
            .withUserId(USER1_ID)
            .withUserName(USER1_NAME)
            .build();

    when(commerceConnection.getIdProvider()).thenReturn(TestVendors.getIdProvider("vendor"));
    when(commerceConnection.getSegmentService()).thenReturn(Optional.of(segmentService));

    when(storeContext.getConnection()).thenReturn(commerceConnection);

    CurrentStoreContext.set(storeContext);
    CurrentUserContext.set(userContext);

    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    contextCollection = new ContextCollectionImpl();
  }

  @After
  public void teardown() {
    CurrentStoreContext.remove();
    CurrentUserContext.remove();
  }

  @Test
  public void testPreHandle() {
    Segment seg1 = mockSegment("vendor:///catalog/segment/id1");
    Segment seg2 = mockSegment("vendor:///catalog/segment/id2");

    List<Segment> segmentList = newArrayList(seg1, seg2);
    when(segmentService.findSegmentsForCurrentUser(any(StoreContext.class))).thenReturn(segmentList);

    StoreContext storeContext = StoreContextBuilderImpl.from(commerceConnection, "any-site-id").build();
    when(commerceConnection.getStoreContext()).thenReturn(storeContext);

    testling.preHandle(request, response, contextCollection);

    verifyThatProfileContainsSegments();
  }

  @Test
  public void testPreHandleFromUserContext() {
    StoreContext storeContext = StoreContextBuilderImpl.from(commerceConnection, "any-site-id")
            .withUserSegments("id1,id2")
            .build();
    CurrentStoreContext.set(storeContext);

    testling.preHandle(request, response, contextCollection);

    verifyThatProfileContainsSegments();
    verify(segmentService, times(0)).findSegmentsForCurrentUser(storeContext);
  }

  // --------------- Helper ----------------------

  private Segment mockSegment(String id) {
    Segment seg = mock(Segment.class);
    when(seg.getId()).thenReturn(parseCommerceIdOrThrow(id));
    return seg;
  }

  private void verifyThatProfileContainsSegments() {
    MapPropertyMaintainer profile = (MapPropertyMaintainer) contextCollection.getContext("commerce");
    assertThat(profile).isNotNull();
    assertThat(profile.getProperty("usersegments"))
            .isEqualTo("vendor:///catalog/segment/id1,vendor:///catalog/segment/id2,");
  }
}
