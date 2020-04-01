package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.cae.web.taglib.ImageFunctions;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.imagemap.ImageMapAreaFilterable;
import com.google.common.collect.ImmutableMap;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.util.CollectionUtils.isEmpty;


public class CMImageMapImpl extends CMImageMapBase {

  public static final String IMAGE_MAP = "image-map";
  public static final String DISPLAY_AS_INLINE_OVERLAY = "displayAsInlineOverlay";
  public static final String AREA_INLINE_OVERLAY_THEME = "inlineOverlayTheme";

  private List<ImageMapAreaFilterable> imageMapAreaFilters;

  public List<Map<String, Object>> getImageMapAreasUnfiltered() {
    List<Map<String, Object>> result = new ArrayList<>();
    final SettingsService settingsService = getSettingsService();

    final List<Map> imageMapConfiguration = settingsService.settingAsList(IMAGE_MAP, Map.class, this);

    if (!isEmpty(imageMapConfiguration)) {
      //noinspection ConstantConditions
      for (Map areaSettings : imageMapConfiguration) {

        @SuppressWarnings("unchecked")
        final Map<String, Object> item = areaSettings != null ? (Map<String, Object>) areaSettings : Collections.<String, Object>emptyMap();

        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        Object coords = item.get(ImageFunctions.COORDS);
        if (coords instanceof String) {
          String coordsAsString = ((String) coords).trim();
          builder.put(ImageFunctions.COORDS, coordsAsString);
          builder.put(ImageFunctions.COORDS_AS_POINTS, coordsAsList(coordsAsString));

          putIfNotNull(builder, ImageFunctions.SHAPE, item.get(ImageFunctions.SHAPE));
          putIfNotNull(builder, ImageFunctions.TARGET, item.get(ImageFunctions.TARGET));
          putIfNotNull(builder, ImageFunctions.ALT, item.get(ImageFunctions.ALT));
          putIfNotNull(builder, ImageFunctions.LINKED_CONTENT, item.get(ImageFunctions.LINKED_CONTENT));
          putIfNotNull(builder, DISPLAY_AS_INLINE_OVERLAY, item.get(DISPLAY_AS_INLINE_OVERLAY));
          putIfNotNull(builder, AREA_INLINE_OVERLAY_THEME, item.get(AREA_INLINE_OVERLAY_THEME));
          result.add(builder.build());
        }
      }
    }
    return result;
  }

  @Override
  public List<Map<String, Object>> getImageMapAreas() {
    List<Map<String, Object>> result = getImageMapAreasUnfiltered();
    for (ImageMapAreaFilterable areaFilter : imageMapAreaFilters) {
      result = areaFilter.filter(result, this);
    }
    return result;
  }

  private static void putIfNotNull(ImmutableMap.Builder<String, Object> builder, String key, Object value) {
    if (value != null) {
      builder.put(key, value);
    }
  }

  private static List<Point2D> coordsAsList(String coords) {
    final List<Point2D> result = new ArrayList<>();
    final String[] coordinates = coords.split(",");
    if (0 == coordinates.length % 2) {
      for (int i = 0; i < coordinates.length; i += 2) {
        result.add(new Point2D.Double(Integer.parseInt(coordinates[i]), Integer.parseInt(coordinates[i + 1])));
      }
    }
    return result;
  }

  public void setImageMapAreaFilters(List<ImageMapAreaFilterable> imageMapAreaFilters) {
    this.imageMapAreaFilters = imageMapAreaFilters;
  }
}
