package com.coremedia.livecontext.studio.asset;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.util.CommerceReferenceHelper;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import com.coremedia.ecommerce.studio.rest.cache.CommerceCacheInvalidationSource;
import com.coremedia.livecontext.asset.util.AssetHelper;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.rest.intercept.WriteReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CommerceReferenceHelper.class, AssetHelper.class})
public class AssetInvalidationWritePostProcessorTest {

  @Mock
  private CommerceConnectionSupplier commerceConnectionSupplier;

  @Mock
  private CommerceConnection commerceConnection;

  @Mock
  private CommerceCacheInvalidationSource invalidationSource;

  @Mock
  private ContentType cmPictureType;

  @Mock
  private WriteReport<Content> report;

  @Mock
  private Content content;

  @Mock
  private ContentRepository repository;

  @Mock
  private Struct localSettings;

  private AssetInvalidationWritePostProcessor testling;

  @Before
  public void setUp() {
    testling = new AssetInvalidationWritePostProcessor(invalidationSource, commerceConnectionSupplier);

    mockStatic(CommerceReferenceHelper.class);

    testling.setType(cmPictureType);

    when(commerceConnectionSupplier.findConnection(any(Content.class))).thenReturn(Optional.of(commerceConnection));

    when(report.getEntity()).thenReturn(content);

    Map<String, Object> properties = new HashMap<>();
    properties.put(CMPicture.DATA, new Object());
    when(report.getOverwrittenProperties()).thenReturn(properties);

    when(content.getRepository()).thenReturn(repository);
    when(content.get(AssetInvalidationWritePostProcessor.STRUCT_PROPERTY_NAME)).thenReturn(localSettings);

    mockStatic(AssetHelper.class);
  }

  @Test
  public void testPostProcess() {
    List<String> references = Arrays.asList("a", "b", "c");

    when(CommerceReferenceHelper.getExternalReferences(localSettings)).thenReturn(references);

    testling.postProcess(report);

    verify(invalidationSource).invalidateReferences(newHashSet(references));
  }
}
