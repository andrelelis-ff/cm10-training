package com.coremedia.blueprint.cae.web.taglib;

import com.coremedia.blueprint.common.contentbeans.CMImageMap;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.image.ImageDimensionsExtractor;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * A adapter for {@link com.coremedia.blueprint.base.cae.web.taglib.ImageFunctions} used in JSP Taglibs.
 * For Freemarker use {@link BlueprintFreemarkerFacade} instead.
 */
public final class ImageFunctions {

  static final ImageDimensionsExtractor IMAGE_DIMENSIONS_EXTRACTOR = new ImageDimensionsExtractor();

  // static class
  private ImageFunctions() {
  }

  /**
   * Return list of area configurations with the 'coords' attribute being transformed according to the image map's
   * picture transformations. If cropping is disabled, an empty list is returned.
   */
  public static List<Map<String, Object>> responsiveImageMapAreas(CMImageMap imageMap,
                                                                  ImageDimensionsExtractor imageDimensionsExtractor,
                                                                  List<String> transformationNames) {
    List<Map<String, Object>> result = Collections.emptyList();
    final CMPicture picture = imageMap.getPicture();

    if (picture != null) {
      // determine which transformations to apply
      final Map<String, String> transformMap = picture.getTransformMap();
      final List<Map<String, Object>> imageMapAreas = imageMap.getImageMapAreas();

      result = com.coremedia.blueprint.base.cae.web.taglib.ImageFunctions.responsiveImageMapAreas(picture.getData(), picture.getDisableCropping(), imageMapAreas, transformMap, imageDimensionsExtractor, transformationNames);
    }

    return result;
  }

  public static List<Map<String, Object>> responsiveImageMapAreasAll(CMImageMap imageMap) {
    return responsiveImageMapAreas(imageMap, Collections.<String>emptyList());
  }

  public static List<Map<String, Object>> responsiveImageMapAreas(CMImageMap imageMap,
                                                                  List<String> transformationNames) {
    return responsiveImageMapAreas(imageMap, IMAGE_DIMENSIONS_EXTRACTOR, transformationNames);
  }

  public static Map<String, Object> responsiveImageMapAreaData(Map<String, Object> coords) {
    return com.coremedia.blueprint.base.cae.web.taglib.ImageFunctions.responsiveImageMapAreaData(coords);
  }
}
