package com.coremedia.livecontext.asset.util;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.struct.Struct;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_COMMERCE;
import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_LOCAL_SETTINGS;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AssetReadSettingsHelperTest {

  private AssetReadSettingsHelper testling;
  private List<String> PATHLIST = ImmutableList.of(NAME_LOCAL_SETTINGS, NAME_COMMERCE);
  @Mock
  private Map<String, Object> contentProperties;

  @Mock
  private Struct localSettings;

  @Mock
  private Struct commerceStruct;

  @Mock
  private SettingsService settingsService;

  private List<String> productList;

  @Before
  public void setup() {
    initMocks(this);
    testling = new AssetReadSettingsHelper();
    testling.setSettingsService(settingsService);

    productList = new ArrayList<>();
    productList.add("oneProduct");
    productList.add("secondProduct");

    contentProperties = new HashMap<>();
    contentProperties.put(NAME_LOCAL_SETTINGS, localSettings);
  }

  @Test
  public void hasCommerceStructNullInput() throws Exception {
    assertFalse(testling.hasCommerceStruct(null));
  }

  @Test
  public void hasCommerceStructExistingCommerceStruct() throws Exception {
    List<String> expectedReferences = new ArrayList<>();
    expectedReferences.add("newOne");
    AssetReferences assetReferences = mock(AssetReferences.class);
    when(assetReferences.getReferences()).thenReturn(expectedReferences);
    when(settingsService.nestedSetting(eq(PATHLIST), any(Class.class), eq(contentProperties))).thenReturn(commerceStruct);
    when(settingsService.createProxy(eq(AssetReferences.class), same(commerceStruct))).thenReturn(assetReferences);

    assertTrue(testling.hasCommerceStruct(contentProperties));
  }

  @Test
  public void hasCommerceStructNotExistingCommerceStruct() throws Exception {
    assertFalse(testling.hasCommerceStruct(contentProperties));
  }


  @Test
  public void testHasProductListNullInput() throws Exception {
    assertFalse(testling.hasReferencesList(null));
  }

  @Test
  public void testHasProductListNoCommerceStruct() throws Exception {
    assertFalse(testling.hasReferencesList(contentProperties));
  }

  @Test
  public void testHasProductListNoProductList() throws Exception {
    assertFalse(testling.hasReferencesList(contentProperties));
  }

  @Test
  public void testHasProductList() throws Exception {
    List<String> expectedReferences = new ArrayList<>();
    expectedReferences.add("newOne");
    AssetReferences assetReferences = mock(AssetReferences.class);
    when(assetReferences.getReferences()).thenReturn(expectedReferences);
    when(settingsService.nestedSetting(eq(PATHLIST), any(Class.class), eq(contentProperties))).thenReturn(commerceStruct);
    when(settingsService.createProxy(eq(AssetReferences.class), same(commerceStruct))).thenReturn(assetReferences);

    assertTrue(testling.hasReferencesList(contentProperties));
  }

  @Test
  public void testReadCommerceStructNullInput() throws Exception {
    assertNull(testling.readCommerceStruct(null));
  }

  @Test
  public void testReadCommerceNoCommerceStruct() throws Exception {
    AssetReferences result = testling.readCommerceStruct(contentProperties);
    assertNull(result);
  }

  @Test
  public void testReadCommerce() throws Exception {
    List<String> expectedReferences = new ArrayList<>();
    expectedReferences.add("newOne");
    AssetReferences assetReferences = mock(AssetReferences.class);
    when(assetReferences.getReferences()).thenReturn(expectedReferences);
    when(settingsService.nestedSetting(eq(PATHLIST), any(Class.class), eq(contentProperties))).thenReturn(commerceStruct);
    when(settingsService.createProxy(eq(AssetReferences.class), same(commerceStruct))).thenReturn(assetReferences);

    AssetReferences result = testling.readCommerceStruct(contentProperties);
    assertEquals(expectedReferences, result.getReferences());
  }

  @Test
  public void testGetProductsInputNull() throws Exception {
    List<String> actual = testling.getCommerceReferences(null);
    assertEquals(emptyList(), actual);
  }

  @Test
  public void testGetProductsNoCommerceStruct() throws Exception {
    AssetReferences expected = mock(AssetReferences.class);
    when(expected.getReferences()).thenReturn(new ArrayList<String>());
    when(settingsService.nestedSetting(eq(PATHLIST), any(Class.class), eq(contentProperties))).thenReturn(commerceStruct);
    when(settingsService.createProxy(any(Class.class), same(commerceStruct))).thenReturn(expected);

    List<String> actual = testling.getCommerceReferences(contentProperties);
    assertEquals(emptyList(), actual);
  }

  @Test
  public void testGetProductsNoProductList() throws Exception {
    AssetReferences expected = mock(AssetReferences.class);
    when(expected.getReferences()).thenReturn(new ArrayList<String>());
    when(settingsService.nestedSetting(eq(PATHLIST), any(Class.class), eq(contentProperties))).thenReturn(commerceStruct);
    when(settingsService.createProxy(any(Class.class), same(commerceStruct))).thenReturn(expected);

    List<String> actual = testling.getCommerceReferences(contentProperties);
    assertEquals(emptyList(), actual);
  }

  @Test
  public void testGetProducts() throws Exception {
    AssetReferences assetReferencesResult = mock(AssetReferences.class);
    when(assetReferencesResult.getReferences()).thenReturn(productList);
    when(settingsService.nestedSetting(eq(PATHLIST), any(Class.class), eq(contentProperties))).thenReturn(commerceStruct);
    when(settingsService.createProxy(any(Class.class), same(commerceStruct))).thenReturn(assetReferencesResult);

    List<String> actual = testling.getCommerceReferences(contentProperties);
    assertEquals(productList, actual);
  }

  @Test
  public void testReadInheritedFieldNullInput() throws Exception {
    assertFalse(testling.readInheritedField(null));
  }

  @Test
  public void testReadInheritedFieldNoCommerceStruct() throws Exception {
    when(settingsService.nestedSetting(eq(PATHLIST), any(Class.class), eq(contentProperties))).thenReturn(null);

    boolean actual = testling.readInheritedField(contentProperties);
    assertFalse(actual);
  }

  @Test
  public void testReadInheritedFieldInheritTrue() throws Exception {
    AssetReferences assetReferencesResult = mock(AssetReferences.class);
    when(assetReferencesResult.isInherit()).thenReturn(true);
    when(settingsService.nestedSetting(eq(PATHLIST), any(Class.class), eq(contentProperties))).thenReturn(commerceStruct);
    when(settingsService.createProxy(any(Class.class), same(commerceStruct))).thenReturn(assetReferencesResult);

    boolean actual = testling.readInheritedField(contentProperties);
    assertTrue(actual);
  }

  @Test
  public void testReadInheritedFieldInheritFalse() throws Exception {
    AssetReferences assetReferencesResult = mock(AssetReferences.class);
    when(assetReferencesResult.isInherit()).thenReturn(false);
    when(settingsService.nestedSetting(eq(PATHLIST), any(Class.class), eq(contentProperties))).thenReturn(commerceStruct);
    when(settingsService.createProxy(any(Class.class), same(commerceStruct))).thenReturn(assetReferencesResult);

    boolean actual = testling.readInheritedField(contentProperties);
    assertFalse(actual);
  }
}