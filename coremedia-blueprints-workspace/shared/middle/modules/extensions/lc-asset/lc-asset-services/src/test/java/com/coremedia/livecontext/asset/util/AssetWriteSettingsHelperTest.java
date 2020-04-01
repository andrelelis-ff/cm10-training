package com.coremedia.livecontext.asset.util;

import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_LOCAL_SETTINGS;
import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_ORIGIN_REFERENCES;
import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_REFERENCES;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AssetWriteSettingsHelperTest {

  private AssetWriteSettingsHelper testling;

  @Mock
  private Struct emptyStruct;
  @Mock
  private StructService structService;
  @Mock
  private AssetReadSettingsHelper assetReadSettingsHelper;

  private Map<String, Object> contentProperties;

  @Mock
  private Struct localSettings;



  //Deep stubs
  private StructBuilder structBuilder;

  private ContentRepository contentRepository;

  List<String> productReferences;

  @Before
  public void setup() {
    testling = new AssetWriteSettingsHelper();
    initMocks(this);
    contentProperties = new HashMap<>();
    contentRepository = mock(ContentRepository.class, RETURNS_DEEP_STUBS);
    structBuilder = mock(StructBuilder.class, RETURNS_DEEP_STUBS);
    when(contentRepository.getConnection().getStructService()).thenReturn(structService);
    when(structService.emptyStruct()).thenReturn(emptyStruct);
    contentProperties.put(NAME_LOCAL_SETTINGS, localSettings);
    testling.setAssetReadSettingsHelper(assetReadSettingsHelper);
    testling.setContentRepository(contentRepository);
    productReferences = new ArrayList<>();
    productReferences.add("ProductRef1");
    productReferences.add("ProductRef2");
    productReferences.add("ProductRef3");
    when(localSettings.builder()).thenReturn(structBuilder);
  }

  @Test
  public void createNewSettingsStructWithProductsHasCommerceStructNoInherit() throws Exception {
    when(assetReadSettingsHelper.hasCommerceStruct(contentProperties)).thenReturn(Boolean.TRUE);
    testling.createNewSettingsStructWithReferences(contentProperties, productReferences, false);

    //should not be called when the inheritance from origin is inactive
    verify(structBuilder, times(0)).declareStrings(NAME_REFERENCES, Integer.MAX_VALUE, productReferences);
    //even if inheritance is not active the origin products must be written.
    verify(structBuilder, times(1)).declareStrings(NAME_ORIGIN_REFERENCES, Integer.MAX_VALUE, productReferences);
    //should build the struct
    verify(structBuilder, times(1)).build();
  }

  @Test
  public void createNewSettingsStructWithProductsHasCommerceStructInherit() throws Exception {
    when(assetReadSettingsHelper.hasCommerceStruct(contentProperties)).thenReturn(Boolean.TRUE);
    testling.createNewSettingsStructWithReferences(contentProperties, productReferences, true);

    //must be called when inheritance is active. The products must be the same as the origin products
    verify(structBuilder, times(1)).declareStrings(NAME_REFERENCES, Integer.MAX_VALUE, productReferences);
    //even if inheritance is not active the origin products must be written.
    verify(structBuilder, times(1)).declareStrings(NAME_ORIGIN_REFERENCES, Integer.MAX_VALUE, productReferences);
    //should build the struct
    verify(structBuilder, times(1)).build();
  }
}