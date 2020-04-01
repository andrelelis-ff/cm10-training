package com.coremedia.livecontext.ecommerce.ibm.common;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class encapsulates convenience methods to transform data maps
 * retrieved by the good old BOD handlers for categories and products
 * to the new leading search handler format.
 */
@DefaultAnnotation(NonNull.class)
public class DataMapTransformationHelper {
  private static final Map<String, String> bodKeyMappings = ImmutableMap.<String, String>builder()
          .put("productType", "catalogEntryTypeCode")
          .put("xcatentry_seoSegment", "seo_token_ntk")
          .put("xcatgroup_seoSegment", "seo_token_ntk")
          .put("xcatentry_cmSeoSegment", "cm_seo_token_ntk")
          .put("xcatgroup_cmSeoSegment", "cm_seo_token_ntk")
          .put("parentProductID", "parentCatalogEntryID")
          .put("parentCategoryID", "parentCatalogGroupID")
          .build();

  private DataMapTransformationHelper() {
  }

  /**
   * Transforms a map of product data retrieved by the BOD service to the search handler format.
   *
   * @param bodResponseMap The BOD handler based format.
   * @return The search handler based format.
   */
  public static Map<String, Object> transformProductBodMap(Map<String, Object> bodResponseMap) {
    Map<String, Object> lowerCasedMap = transformKeysStartLowerCase(bodResponseMap);
    unifyProductWrapperKeys(lowerCasedMap);
    return lowerCasedMap;
  }

  /**
   * Transforms a map of category data retrieved by the BOD service to the search handler format.
   *
   * @param bodResponseMap The BOD handler based format.
   * @return The search handler based format.
   */
  public static Map<String, Object> transformCategoryBodMap(Map<String, Object> bodResponseMap) {
    Map<String, Object> lowerCasedMap = transformKeysStartLowerCase(bodResponseMap);
    unifyCategoryWrapperKeys(lowerCasedMap);
    return lowerCasedMap;
  }

  /**
   * Brings all map keys to lower case.
   */
  private static Map<String, Object> transformKeysStartLowerCase(Map<String, Object> map) {
    return map.entrySet().stream()
            // Collectors.map returns a mutable map (HashMap)
            .collect(Collectors.toMap(
                    e -> decapitalize(e.getKey()),
                    e -> transformKeysStartLowerCase(e.getValue())
            ));
  }

  /**
   * Inspect map value and recursively transform, if value is a collection or a map itself.
   */
  private static Object transformKeysStartLowerCase(Object value) {
    if (value instanceof Collection) {
      return ((Collection<?>) value).stream()
              .map(o -> {
                if (o instanceof Map) {
                  //noinspection unchecked
                  return transformKeysStartLowerCase((Map<String, Object>) o);
                }
                return o;
              })
              .collect(Collectors.toList());
    } else if (value instanceof Map) {
      //noinspection unchecked
      return transformKeysStartLowerCase((Map<String, Object>) value);
    }
    // else it is a primitive value which does not have a key, i.e. nothing to do
    return value;
  }

  private static String decapitalize(String key) {
    char firstChar = key.charAt(0);
    if (Character.isUpperCase(firstChar)) {
      // replace first character of map key with the one in lower case
      // append the remaining key untouched
      return Character.toLowerCase(firstChar) + StringUtils.right(key, key.length() - 1);
    }
    return key;
  }

  /**
   * Replaces mpa keys to match search handler based format.
   *
   * @param mapList List of catalog entry or catalog group data.
   */
  private static void replaceKeys(List<Map<String, Object>> mapList) {
    for (Map<String, Object> entryMap : mapList) {
      for (Map.Entry<String, String> entry : bodKeyMappings.entrySet()) {
        if (entryMap.containsKey(entry.getKey())) {
          entryMap.put(bodKeyMappings.get(entry.getKey()), entryMap.remove(entry.getKey()));
        }
      }
    }
  }

  /**
   * Unifies the catalog entry data by replacing map keys, replacing attribute keys, replacing sku keys and
   * formatting the catalog group id if required.
   *
   * @param productWrapper The catalog entry wrapper retrieved by the wrapper service.
   */
  private static void unifyProductWrapperKeys(Map<String, Object> productWrapper) {
    //noinspection unchecked
    List<Map<String, Object>> catalogEntryView = DataMapHelper.getList(productWrapper, "catalogEntryView");
    replaceKeys(catalogEntryView);
    replaceProductAttributeKeys(catalogEntryView);
    replaceSkus(catalogEntryView);
  }

  /**
   * Unifies catalog group data by replacing map keys and formatting the catalog group id if required.
   *
   * @param categoryWrapper The catalog group wrapper retrieved by the wrapper service.
   */
  private static void unifyCategoryWrapperKeys(Map<String, Object> categoryWrapper) {
    //noinspection unchecked
    List<Map<String, Object>> catalogGroupView = DataMapHelper.getList(categoryWrapper, "catalogGroupView");
    replaceKeys(catalogGroupView);
  }

  public static List<String> getParentCatGroupIdForSingleWrapper(Map<String, Object> delegate,
                                                                 String currentCatalogId) {
    Object origParentCategoryIds = DataMapHelper.getValueForPath(delegate, "parentCatalogGroupID");
    List<String> parentCategoryIdList = new ArrayList<>();
    if (origParentCategoryIds instanceof List) {
      // create new list copy to avoid  ConcurrentModificationException
      //noinspection unchecked
      for (String origParentCatString : (List<String>) origParentCategoryIds) {
        String parentCategoryId = filterByCatalogId(currentCatalogId, origParentCatString);
        if (parentCategoryId != null) {
          parentCategoryIdList.add(parentCategoryId);
        }
      }
    } else if (origParentCategoryIds instanceof String) {
      String parent = filterByCatalogId(currentCatalogId, (String) origParentCategoryIds);
      if (parent != null) {
        parentCategoryIdList = Collections.singletonList(parent);
      }
    }
    return parentCategoryIdList;
  }

  @Nullable
  private static String filterByCatalogId(String catalogId, String catalogIdAndCategoryId) {
    if (catalogIdAndCategoryId.matches(".+_.+")) {
      String[] catalogAndCategoryIdSplit = catalogIdAndCategoryId.split("_");
      if (catalogAndCategoryIdSplit.length > 0 && catalogId.equals(catalogAndCategoryIdSplit[0])) {
        return catalogAndCategoryIdSplit[1];
      } else {
        return null;
      }
    }
    return catalogIdAndCategoryId;
  }

  /**
   * Replaces the key <code>sKUUniqueID</code> with <code>uniqueID</code> (<code>sKUUniqueID -> uniqueID</code>)
   *
   * @param mapList The list containing the catalog entry or catalog group data.
   */
  private static void replaceSkus(List<Map<String, Object>> mapList) {
    for (Map<String, Object> listEntry : mapList) {
      List<Map<String, Object>> sKUs = (List<Map<String, Object>>) DataMapHelper.getList(listEntry, "sKUs");
      for (Map<String, Object> sKU : sKUs) {
        if (sKU.containsKey("sKUUniqueID")) {
          sKU.put("uniqueID", sKU.remove("sKUUniqueID"));
        }
      }
    }
  }

  /**
   * Replaces all <code>values</code> keys in <code>attributes.values</code> (<code>attributes.values.values -> attributes.values.value</code>).
   *
   * @param mapList The map containing the catalog entry data.
   */
  private static void replaceProductAttributeKeys(List<Map<String, Object>> mapList) {
    for (Map<String, Object> listEntry : mapList) {
      List<Map<String, Object>> attributes = (List<Map<String, Object>>) DataMapHelper.getList(listEntry, "attributes");
      for (Map<String, Object> attribute : attributes) {
        //rename inner values only
        List<Map<String, Object>> values = DataMapHelper.getList(attribute, "values");
        for (Map<String, Object> value : values) {
          if (value.containsKey("values")) {
            value.put("value", value.remove("values"));
          }
        }
      }
    }
  }
}
