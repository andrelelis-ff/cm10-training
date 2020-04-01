package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.common.AMSettingKeys;
import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AMUtils {

  private static final String DATE_PATTERN = "MM/dd/yyyy";

  private AMUtils() {
    // hide default constructor of utility classes
  }

  public static Content getDownloadPortalRootDocument(@NonNull SettingsService settingsService, @NonNull Site site) {
    Content rootDocument = site.getSiteRootDocument();
    List<String> pathToRootPage = Arrays.asList(
            AMSettingKeys.ASSET_MANAGEMENT,
            AMSettingKeys.ASSET_MANAGEMENT_DOWNLOAD_PORTAL,
            AMSettingKeys.ASSET_MANAGEMENT_DOWNLOAD_PORTAL_ROOT_PAGE
    );
    return settingsService.nestedSetting(pathToRootPage, Content.class, rootDocument);
  }

  /**
   * Casts the values in the given Map to String. The order the keys were added will be preserved.
   *
   * @param properties a Map with values
   * @return a new Map with values as Strings
   * or an empty Map if no value could be cast to String
   */
  public static Map<String, String> getPropertiesAsString(@NonNull Map<String, Object> properties) {
    Map<String, String> metadata = new LinkedHashMap<>();
    if (!properties.isEmpty()) {
      DateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
      for (Map.Entry<String, Object> propertyMap : properties.entrySet()) {
        String value = asString(propertyMap.getValue(), formatter);
        if (StringUtils.isNotEmpty(value)) {
          metadata.put(propertyMap.getKey(), value);
        }
      }
    }
    return metadata;
  }

  /**
   * Casts the given property to a String.
   *
   * @param property the property to cast
   * @return the property as a String,
   * or an empty String if the property could not be casted
   */
  public static String asString(Object property) {
    return asString(property, new SimpleDateFormat(DATE_PATTERN));
  }

  private static String asString(Object property, DateFormat formatter) {
    String propertyAsString = "";
    if (property instanceof String) {
      propertyAsString = (String) property;
    } else if (property instanceof Integer || property instanceof Boolean) {
      propertyAsString = property.toString();
    } else if (property instanceof GregorianCalendar) {
      GregorianCalendar calendar = (GregorianCalendar) property;
      formatter.setCalendar(calendar);
      propertyAsString = formatter.format(calendar.getTime());
    }

    return propertyAsString;
  }

  public static List<String> getAssetSubtypes(ContentRepository contentRepository) {
    List<String> assetTypes = new ArrayList<>();
    ContentType assetBaseType = contentRepository.getContentType(AMAsset.NAME);
    if (null != assetBaseType) {
      for (ContentType subtype : assetBaseType.getSubtypes()) {
        if (subtype.isConcrete()) {
          assetTypes.add(subtype.getName());
        }
      }
    }
    return assetTypes;
  }
}
