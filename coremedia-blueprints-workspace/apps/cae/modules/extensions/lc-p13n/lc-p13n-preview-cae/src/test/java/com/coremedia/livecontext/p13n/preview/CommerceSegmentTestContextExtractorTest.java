package com.coremedia.livecontext.p13n.preview;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceConnection;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceIdProvider;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.personalization.contentbeans.CMUserProfile;
import com.coremedia.cap.content.Content;
import com.coremedia.ecommerce.test.TestVendors;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.StoreContextProvider;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.ContextCollectionImpl;
import com.coremedia.personalization.context.PropertyProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommerceSegmentTestContextExtractorTest {

  private CommerceSegmentTestContextExtractor testling;

  @Mock
  private Content content;

  @Mock
  private CMUserProfile cmUserProfile;

  @Mock
  private Map<String, Object> profileExtensions, properties, commerce;

  private ContextCollection contextCollection;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private StoreContextProvider storeContextProvider;

  @Before
  public void setUp() {
    testling = new CommerceSegmentTestContextExtractor();
    testling.setContentBeanFactory(contentBeanFactory);
    contextCollection = new ContextCollectionImpl();

    BaseCommerceConnection commerceConnection = new BaseCommerceConnection();

    BaseCommerceIdProvider idProvider = TestVendors.getIdProvider("vendor");
    StoreContextImpl storeContext = StoreContextBuilderImpl.from(commerceConnection, "any-site-id").build();
    when(storeContextProvider.buildContext(storeContext)).thenReturn(StoreContextBuilderImpl.from(storeContext));

    commerceConnection.setIdProvider(idProvider);
    commerceConnection.setStoreContextProvider(storeContextProvider);

    CurrentStoreContext.set(storeContext);
  }

  @After
  public void teardown() {
    CurrentStoreContext.remove();
  }

  @Test
  public void testExtractTestContextsFromContent() {
    String userSegmentsStr = "vendor:///catalog/segment/segment1,vendor:///catalog/segment/segment2";
    String userSegmentIds = "segment1,segment2";
    List<String> userSegments = Arrays.asList(userSegmentsStr.split(","));
    when(contentBeanFactory.createBeanFor(content, ContentBean.class)).thenReturn(cmUserProfile);
    when(cmUserProfile.getProfileExtensions()).thenReturn(profileExtensions);
    when(profileExtensions.get(CommerceSegmentTestContextExtractor.PROPERTIES_PREFIX)).thenReturn(properties);
    when(properties.get(CommerceSegmentTestContextExtractor.COMMERCE_CONTEXT)).thenReturn(commerce);
    when(commerce.get(CommerceSegmentTestContextExtractor.USER_SEGMENTS_PROPERTY)).thenReturn(userSegments);

    testling.extractTestContextsFromContent(content, contextCollection);

    //assert the user segments in the store context
    StoreContext storeContext = CurrentStoreContext.get();
    assertEquals(userSegmentIds, storeContext.getUserSegments().orElse(null));

    //assert the user segments in the context collection
    PropertyProfile commerceProperty = (PropertyProfile) contextCollection.getContext(
            CommerceSegmentTestContextExtractor.COMMERCE_CONTEXT);
    assertEquals(userSegmentsStr, commerceProperty.getProperty(CommerceSegmentTestContextExtractor.USER_SEGMENTS_PROPERTY));
  }
}
