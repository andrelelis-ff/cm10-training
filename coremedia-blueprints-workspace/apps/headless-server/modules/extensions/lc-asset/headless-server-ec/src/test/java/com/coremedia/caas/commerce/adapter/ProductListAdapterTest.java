package com.coremedia.caas.commerce.adapter;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.caas.commerce.adapter.ProductListAdapter;
import com.coremedia.blueprint.caas.commerce.model.CommerceFacade;
import com.coremedia.caas.model.adapter.ExtendedLinkListAdapter;
import com.coremedia.caas.model.adapter.ExtendedLinkListAdapterFactory;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapType;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.search.SearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductListAdapterTest {
  private ProductListAdapter productListAdapter;
  private static final String SITE_ID = "8283829dd";

  @Mock
  private ExtendedLinkListAdapterFactory extendedLinkListAdapterFactory;

  @Mock
  private Content productList;

  @Mock
  private SettingsService settingsService;

  @Mock
  private CommerceFacade commerceFacade;


  @Mock
  private ExtendedLinkListAdapter extendedLinkListAdapter;

  @Mock
  private Content fixedTarget1;

  @Mock
  private Content fixedTarget2;

  @Mock
  private Content fixedTarget3;

  @Mock
  private Product dynamicTarget1;

  @Mock
  private Product dynamicTarget2;

  @Mock
  private Product dynamicTarget3;

  @Mock
  private Struct struct;

  @Mock
  private CapType capType;

  @Mock
  private CapPropertyDescriptor propertyDescriptor;

  @Mock
  private SearchResult<Product> productSearchResult;

  @Before
  public void setup() {
    productListAdapter = new ProductListAdapter(extendedLinkListAdapterFactory, productList, settingsService, commerceFacade, SITE_ID);

    when(productSearchResult.getSearchResult()).thenReturn(Stream.of(dynamicTarget1, dynamicTarget2, dynamicTarget3).collect(Collectors.toList()));
    when(settingsService.setting(ProductListAdapter.STRUCT_KEY_PRODUCTLIST, Struct.class, productList)).thenReturn(struct);
    when(capType.getDescriptor(anyString())).thenReturn(propertyDescriptor);
    when(struct.getType()).thenReturn(capType);
    when(struct.getString(ProductListAdapter.STRUCT_KEY_PRODUCTLIST_SELECT_FACET_VALUE)).thenReturn("true");
    when(commerceFacade.searchProducts(eq(ProductListAdapter.ALL_QUERY), anyMap(), eq(SITE_ID))).thenReturn(productSearchResult);
  }

  private Map<String, Object> getFixedItemMap(Content target, int index) {
    Map<String, Object> map = new HashMap<>();
    map.put("target", target);
    map.put("index", index);
    return map;
  }

  @Test
  public void get_dynamicItems() {
    List<Object> dynamicItems = productListAdapter.getDynamicItems();

    assertEquals(dynamicTarget1, dynamicItems.get(0));
    assertEquals(dynamicTarget2, dynamicItems.get(1));
    assertEquals(dynamicTarget3, dynamicItems.get(2));
  }

  @Test
  public void get_mixedItems() {
    List<Map<String, Object>> fixedItems = new ArrayList<>();
    fixedItems.add(getFixedItemMap(fixedTarget1, 1));
    fixedItems.add(getFixedItemMap(fixedTarget2, 3));
    fixedItems.add(getFixedItemMap(fixedTarget3, 5));
    when(extendedLinkListAdapterFactory.to(productList)).thenReturn(extendedLinkListAdapter);
    when(extendedLinkListAdapter.getExtendedTargets()).thenReturn(fixedItems);

    List items = productListAdapter.getItems();

    assertFalse(items.isEmpty());
    assertEquals(6, items.size());
    assertEquals(fixedTarget1, items.get(0));
    assertEquals(dynamicTarget1, items.get(1));
    assertEquals(fixedTarget2, items.get(2));
    assertEquals(dynamicTarget2, items.get(3));
    assertEquals(fixedTarget3, items.get(4));
    assertEquals(dynamicTarget3, items.get(5));
  }

  @Test
  public void get_itemsLimitedOnlyFixed() {
    List<Map<String, Object>> fixedItems = new ArrayList<>();
    fixedItems.add(getFixedItemMap(fixedTarget1, 1));
    fixedItems.add(getFixedItemMap(fixedTarget2, 2));
    fixedItems.add(getFixedItemMap(fixedTarget3, 5));
    when(struct.getString(ProductListAdapter.STRUCT_KEY_PRODUCTLIST_MAX_LENGTH)).thenReturn("2");
    when(extendedLinkListAdapterFactory.to(productList)).thenReturn(extendedLinkListAdapter);
    when(extendedLinkListAdapter.getExtendedTargets()).thenReturn(fixedItems);

    List items = productListAdapter.getItems();

    assertFalse(items.isEmpty());
    assertEquals(2, items.size());
    assertEquals(fixedTarget1, items.get(0));
    assertEquals(fixedTarget2, items.get(1));
  }

  @Test
  public void get_itemsLimitedMixed() {
    List<Map<String, Object>> fixedItems = new ArrayList<>();
    fixedItems.add(getFixedItemMap(fixedTarget1, 1));
    fixedItems.add(getFixedItemMap(fixedTarget2, 3));
    fixedItems.add(getFixedItemMap(fixedTarget3, 5));
    when(struct.getString(ProductListAdapter.STRUCT_KEY_PRODUCTLIST_MAX_LENGTH)).thenReturn("3");
    when(extendedLinkListAdapterFactory.to(productList)).thenReturn(extendedLinkListAdapter);
    when(extendedLinkListAdapter.getExtendedTargets()).thenReturn(fixedItems);

    List items = productListAdapter.getItems();

    assertFalse(items.isEmpty());
    assertEquals(3, items.size());
    assertEquals(fixedTarget1, items.get(0));
    assertEquals(dynamicTarget1, items.get(1));
    assertEquals(fixedTarget2, items.get(2));
  }

  @Test
  public void get_itemsStructMatchingDIGITS() {
    List<Map<String, Object>> fixedItems = new ArrayList<>();
    fixedItems.add(getFixedItemMap(fixedTarget1, 1));
    fixedItems.add(getFixedItemMap(fixedTarget2, 3));
    fixedItems.add(getFixedItemMap(fixedTarget3, 5));
    when(struct.getString(ProductListAdapter.STRUCT_KEY_PRODUCTLIST_SELECT_FACET_VALUE)).thenReturn("12TestSomethingf4cet");

    Map<String, String> searchParams = getSearchParamsMap();
    searchParams.put("facet", "12TestSomethingf4cet");
    searchParams.remove("categoryId");
    when(commerceFacade.searchProducts(eq(ProductListAdapter.ALL_QUERY), eq(searchParams), eq(SITE_ID))).thenReturn(productSearchResult);
    // expect the facet to be: 12TestSomethingf4cet
    // expect the overrideCategoryId to be: ""
    productListAdapter.getProducts();

    verify(commerceFacade, times(1)).searchProducts(eq(ProductListAdapter.ALL_QUERY), eq(searchParams), eq(SITE_ID));
  }

  @Test
  public void get_itemsStructMatchingFacet() {
    List<Map<String, Object>> fixedItems = new ArrayList<>();
    fixedItems.add(getFixedItemMap(fixedTarget1, 1));
    fixedItems.add(getFixedItemMap(fixedTarget2, 3));
    fixedItems.add(getFixedItemMap(fixedTarget3, 5));
    when(struct.getString(ProductListAdapter.STRUCT_KEY_PRODUCTLIST_SELECT_FACET_VALUE)).thenReturn("213971239123");

    Map<String, String> searchParams = getSearchParamsMap();
    searchParams.remove("facet");
    searchParams.put("categoryId", "213971239123");
    when(commerceFacade.searchProducts(eq(ProductListAdapter.ALL_QUERY), eq(searchParams), eq(SITE_ID))).thenReturn(productSearchResult);
    productListAdapter.getProducts();

    verify(commerceFacade, times(1)).searchProducts(eq(ProductListAdapter.ALL_QUERY), eq(searchParams), eq(SITE_ID));
  }

  private Map<String, String> getSearchParamsMap() {
    Map<String, String> searchParams = new HashMap<>();
    searchParams.put("facetSupport", "true");
    searchParams.put("total", "10");
    searchParams.put("categoryId", "3074457345616676673");
    return searchParams;
  }
}
