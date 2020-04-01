package com.coremedia.livecontext.imagemap;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.contentbeans.CMImageMapImpl;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.livecontext.contentbeans.CMProductTeaser;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OutOfStockImageMapAreaFilterTest {

  private static final String HIDE_OUT_OF_STOCK_PRODUCTS = "hideOutOfStockProducts";
  private static final String OVERLAY = "overlay";
  private static final String LINKED_CONTENT = "linkedContent";

  private static final boolean AVAILABLE = true;
  private static final boolean NOT_AVAILABLE = false;

  private CMTeasable teasable;
  private CMProductTeaser productTeaser;
  private Product product;
  private ProductVariant productVariant;
  private OutOfStockImageMapAreaFilter areaFilter;
  private SettingsService settingsService;
  private CMImageMapImpl imageMap;
  private Map<String, Boolean> overlayConfiguration;

  @Before
  public void setup() {
    imageMap = mock(CMImageMapImpl.class);
    teasable = mock(CMTeasable.class);
    productTeaser = mock(CMProductTeaser.class);
    product = mock(Product.class);
    productVariant = mock(ProductVariant.class);
    settingsService = mock(SettingsService.class);

    areaFilter = new OutOfStockImageMapAreaFilter();
    areaFilter.setSettingsService(settingsService);

    overlayConfiguration = new HashMap<>();
    overlayConfiguration.put(HIDE_OUT_OF_STOCK_PRODUCTS, true);
  }

  @SuppressWarnings("Duplicates")
  @Test
  public void testProductInStock() {
    when(productTeaser.getProduct()).thenReturn(product);
    when(product.isAvailable()).thenReturn(AVAILABLE);
    when(settingsService.setting("overlay", Map.class, imageMap)).thenReturn(overlayConfiguration);

    List<Map<String, Object>> filteredResult = areaFilter.filter(getAreasWithProductTeaserAndTeasable(), imageMap);
    assertThat(filteredResult).hasSize(2);
  }

  @SuppressWarnings("Duplicates")
  @Test
  public void testProductOutOfStock() {
    when(productTeaser.getProduct()).thenReturn(product);
    when(product.isAvailable()).thenReturn(NOT_AVAILABLE);
    when(settingsService.setting("overlay", Map.class, imageMap)).thenReturn(overlayConfiguration);

    List<Map<String, Object>> filteredResult = areaFilter.filter(getAreasWithProductTeaserAndTeasable(), imageMap);
    assertThat(filteredResult).hasSize(1);
  }

  @SuppressWarnings("Duplicates")
  @Test
  public void testProductVariantInStock() {
    when(productTeaser.getProduct()).thenReturn(productVariant);
    when(productVariant.isAvailable()).thenReturn(AVAILABLE);
    when(settingsService.setting("overlay", Map.class, imageMap)).thenReturn(overlayConfiguration);

    List<Map<String, Object>> filteredResult = areaFilter.filter(getAreasWithProductTeaserAndTeasable(), imageMap);
    assertThat(filteredResult).hasSize(2);
  }

  @Test
  public void testProductVariantOutOfStock() {
    when(productTeaser.getProduct()).thenReturn(productVariant);
    when(productVariant.isAvailable()).thenReturn(NOT_AVAILABLE);
    when(settingsService.setting("overlay", Map.class, imageMap)).thenReturn(overlayConfiguration);

    List<Map<String, Object>> filteredResult = areaFilter.filter(getAreasWithProductTeaserAndTeasable(), imageMap);
    assertThat(filteredResult).hasSize(1);
  }

  @Test
  public void testDoNotHideOutOfStock() {
    overlayConfiguration.put(HIDE_OUT_OF_STOCK_PRODUCTS, false);
    when(settingsService.setting(OVERLAY, Map.class, imageMap)).thenReturn(overlayConfiguration);

    List<Map<String, Object>> filteredResult = areaFilter.filter(getAreasWithProductTeaserAndTeasable(), imageMap);
    assertThat(filteredResult).hasSize(2);
  }

  private List<Map<String, Object>> getAreasWithProductTeaserAndTeasable() {
    ImmutableList<Object> contents = ImmutableList.of(productTeaser, teasable);
    return getAreasFor(contents);
  }

  private List<Map<String, Object>> getAreasFor(List<Object> contents) {
    return contents.stream()
            .map(content -> ImmutableMap.of(LINKED_CONTENT, content))
            .collect(toList());
  }
}
