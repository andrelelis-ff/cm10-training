package com.coremedia.livecontext.studio.asset;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.util.CommerceReferenceHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import com.coremedia.ecommerce.studio.rest.cache.CommerceCacheInvalidationSource;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_LOCAL_SETTINGS;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CommerceReferenceHelper.class)
public class AssetInvalidationWriteInterceptorTest {

  @Mock
  private CommerceConnectionSupplier commerceConnectionSupplier;

  @Mock
  private CommerceConnection commerceConnection;

  @Mock
  private CommerceCacheInvalidationSource invalidationSource;

  @Mock
  private ContentType cmPictureType;

  @Mock
  private ContentWriteRequest contentWriteRequest;

  @Mock
  private Content content;

  @Mock
  private ContentRepository repository;

  @Mock
  private Struct oldLocalSettings, newLocalSettings;

  private AssetInvalidationWriteInterceptor testling;

  @Before
  public void setUp() throws Exception {
    testling = new AssetInvalidationWriteInterceptor(commerceConnectionSupplier, invalidationSource);

    mockStatic(CommerceReferenceHelper.class);
    testling.setType(cmPictureType);
    testling.afterPropertiesSet();

    when(commerceConnectionSupplier.findConnection(any(Content.class))).thenReturn(Optional.of(commerceConnection));

    when(contentWriteRequest.getEntity()).thenReturn(content);
    when(content.getRepository()).thenReturn(repository);
    Map<String, Object> properties = new HashMap<>();
    properties.put(NAME_LOCAL_SETTINGS, newLocalSettings);
    when(contentWriteRequest.getProperties()).thenReturn(properties);
  }

  @Test
  public void testReferencesChange() {
    //the old references
    when(CommerceReferenceHelper.getExternalReferences(content)).thenReturn(Arrays.asList("a", "b", "c"));
    //the new references
    when(CommerceReferenceHelper.getExternalReferences(newLocalSettings)).thenReturn(Arrays.asList("c", "d", "e"));

    testling.intercept(contentWriteRequest);

    Set<String> expected = newHashSet("d", "e", "b", "a");
    verify(invalidationSource, times(1)).invalidateReferences(argThat(expected::containsAll));
  }

  @Test
  public void testLocalSettingsChange() {
    //the references are not changed...
    when(CommerceReferenceHelper.getExternalReferences(content)).thenReturn(Arrays.asList("a", "b", "c"));
    when(CommerceReferenceHelper.getExternalReferences(newLocalSettings)).thenReturn(Arrays.asList("a", "b", "c"));
    //but the local seetings are changed.
    when(content.getStruct(AssetInvalidationWriteInterceptor.STRUCT_PROPERTY_NAME)).thenReturn(oldLocalSettings);

    testling.intercept(contentWriteRequest);

    Set<String> expected = newHashSet("a", "b", "c");
    verify(invalidationSource, times(1)).invalidateReferences(argThat(expected::containsAll));
  }
}
