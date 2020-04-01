package com.coremedia.livecontext.asset.util;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public class AssetReadSettingsHelper {

  public static final String NAME_ORIGIN_REFERENCES = "originReferences";
  public static final String NAME_LOCAL_SETTINGS = "localSettings";
  public static final String NAME_REFERENCES = "references";
  public static final String NAME_COMMERCE = "commerce";
  public static final String NAME_INHERIT = "inherit";

  private SettingsService settingsService;

  /**
   * Returns the commerceReferences from localSettings by using the path:
   * localSettings.commerce.references
   * <p>
   * If references is not reachable (e.g. commerce struct does not exist) or does not exist, an empty list will be
   * returned.
   *
   * @param contentProperties the struct where to read the commerce references from
   * @return commerceReferences the commerce references from the given property
   */
  @NonNull
  public List<String> getCommerceReferences(@Nullable Map<String, Object> contentProperties) {
    AssetReferences assetReferences = readAssetReferences(contentProperties);

    if (assetReferences == null) {
      return emptyList();
    }

    List<String> references = assetReferences.getReferences();

    if (references == null) {
      return emptyList();
    }

    return references;
  }

  /**
   * Return the origin references from the given contentProperties.
   *
   * @param contentProperties a map which describes the content. In this special case a map which contains a
   *                          localSettings struct
   * @return originReferences the originReferences
   */
  @NonNull
  public List<String> getOriginCommerceReferences(@Nullable Map<String, Object> contentProperties) {
    AssetReferences assetReferences = readAssetReferences(contentProperties);

    if (assetReferences == null) {
      return emptyList();
    }

    List<String> originReferences = assetReferences.getOriginReferences();

    if (originReferences == null) {
      return emptyList();
    }

    return originReferences;
  }

  /**
   * Read the inherited field from the given localSettings Struct.
   * The path which will be used is localSettings.commerce.inherited.
   * <p>
   * If any property in the path is null, the inherited field is defined as false.
   *
   * @param contentProperties the localSettings to resolve the inherited field from
   * @return true if the inherited field can be read and is true, otherwise false.
   */
  public boolean readInheritedField(@Nullable Map<String, Object> contentProperties) {
    AssetReferences assetReferences = readAssetReferences(contentProperties);
    return assetReferences != null && assetReferences.isInherit();
  }

  /**
   * evaluate if the given localSettings has a commerce struct.
   * the path which is checked: localSettings.commerce
   *
   * @param contentProperties struct with localSettings
   * @return true if the commerce struct exist in the localSettings struct, false if the
   * commerce struct or one property in the path do not exist.
   */
  public boolean hasCommerceStruct(@Nullable Map<String, Object> contentProperties) {
    return readAssetReferences(contentProperties) != null;
  }

  /**
   * Return the commerce struct. The path to be used is: localSettings.commerce
   * If the localSettings doesn't have a commerce struct {@link #hasCommerceStruct(Map)} this method will return null
   *
   * @param contentProperties struct with localSettings
   * @return the commerce struct from the localSettings or null if no commerce struct exist.
   */
  @Nullable
  public AssetReferences readCommerceStruct(@Nullable Map<String, Object> contentProperties) {
    return readAssetReferences(contentProperties);
  }

  /**
   * Return true if the localSettings is a struct with a path:
   * localSettings.commerce.references
   *
   * @param contentProperties struct with localSettings
   * @return true if a commerceReferencesList (even if it is empty) exist below thepath: localSettings.commerce.references. false otherwise.
   */
  public boolean hasReferencesList(@Nullable Map<String, Object> contentProperties) {
    if (contentProperties == null) {
      return false;
    }

    AssetReferences assetReferences = readCommerceStruct(contentProperties);
    return assetReferences != null && assetReferences.getReferences() != null;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Nullable
  private AssetReferences readAssetReferences(Map<String, Object> contentProperties) {
    if (contentProperties == null) {
      return null;
    }

    List<String> commerce = ImmutableList.of(NAME_LOCAL_SETTINGS, NAME_COMMERCE);
    Object commerceStruct = settingsService.nestedSetting(commerce, Object.class, contentProperties);

    if (commerceStruct == null) {
      return null;
    }

    return settingsService.createProxy(AssetReferences.class, commerceStruct);
  }
}
