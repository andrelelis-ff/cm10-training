package com.coremedia.livecontext.imagemap;

import com.coremedia.blueprint.base.cae.web.taglib.ImageFunctions;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMImageMap;
import com.coremedia.blueprint.common.imagemap.ImageMapAreaFilterable;
import com.coremedia.livecontext.contentbeans.CMProductTeaser;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;
import java.util.Map;

/**
 * The OutOfStockImageMapAreaFilter class checks, if linked products or product variants - throughout the CMProductTeaser -
 * of Image Map areas are out of stock.
 *
 * All correlated areas will be removed, if the 'hide out of stock products' overlay configuration is set, .
 */
public class OutOfStockImageMapAreaFilter implements ImageMapAreaFilterable {

  private static final String OVERLAY = "overlay";
  private static final String HIDE_OUT_OF_STOCK_PRODUCTS = "hideOutOfStockProducts";
  private SettingsService settingsService;

  @Override
  public List<Map<String, Object>> filter(List<Map<String, Object>> areas, CMImageMap imageMap) {
    Map overlayConfiguration = settingsService.setting(OVERLAY, Map.class, imageMap);
    if (overlayConfiguration == null || overlayConfiguration.isEmpty()) {
      return areas;
    }

    Object hide = overlayConfiguration.get(HIDE_OUT_OF_STOCK_PRODUCTS);
    if (!(hide instanceof Boolean) || !(Boolean) hide) {
      return areas;
    }

    Iterable<Map<String, Object>> filteredAreas = Iterables.filter(areas, new Predicate<Map<String, Object>>() {
      @Override
      public boolean apply(@Nullable Map<String, Object> map) {
        if (map == null) {
          return false;
        }

        Object linkedContent = map.get(ImageFunctions.LINKED_CONTENT);
        // ignore non product teasers
        if (!(linkedContent instanceof CMProductTeaser)) {
          return true;
        }

        Product product = ((CMProductTeaser) linkedContent).getProduct();
        return null != product && product.isAvailable();
      }
    });
    return Lists.newArrayList(filteredAreas);
  }

  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }
}
