package com.coremedia.livecontext.fragment;

import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * The class encapsulates all parameters that may be past during a fragment request.
 * The origin request is handled by the {@link FragmentPageHandler} that may receive additional
 * parameters through matrix parameters.
 * </p>
 *
 * @see com.coremedia.livecontext.fragment.FragmentPageHandler
 * @cm.template.api
 */
public class FragmentParameters {
  private static final Logger LOG = LoggerFactory.getLogger(FragmentParameters.class);

  private static final String PAGE_ID = "pageId";
  private static final String CATALOG_ID = "catalogId";
  private static final String CATEGORY_ID = "categoryId";
  private static final String PRODUCT_ID = "productId";
  private static final String VIEW = "view";
  private static final String PLACEMENT = "placement";
  private static final String EXTERNAL_REFERENCE = "externalRef";
  private static final String PARAMETER = "parameter";
  private static final String ENVIRONMENT = "environment";

  private final String storeId;
  private final Locale locale;

  private String metaDataKeywords = "";
  private String metaDataTitle = "";
  private String metaDataDescription = "";

  private final Map<String, String> matrixParams;

  /**
   * @param storeId    The storeId of the store to work on.
   * @param locale     The locale of the store to work on.
   * @param matrixVars The matrix parameters passed by the fragment request.
   */
  protected FragmentParameters(@NonNull String storeId,
                               @NonNull Locale locale,
                               @NonNull Map<String, String> matrixVars) {
    this.storeId = storeId;
    this.locale = locale;
    this.matrixParams = matrixVars;
  }

  public String getPlacement() {
    return getParameter(PLACEMENT);
  }

  public String getView() {
    return getParameter(VIEW);
  }

  public String getExternalRef() {
    return getDecodedValue(EXTERNAL_REFERENCE);
  }

  /**
   * @cm.template.api
   */
  public String getParameter() {
    return getDecodedValue(PARAMETER);
  }

  public String getStoreId() {
    return storeId;
  }

  public Locale getLocale() {
    return locale;
  }

  public String getPageId() {
    return getParameter(PAGE_ID);
  }

  public Optional<CatalogId> getCatalogId() {
    String catalogId = getParameter(CATALOG_ID);
    if (StringUtils.isNotBlank(catalogId)) {
      return Optional.of(CatalogId.of(catalogId));
    }
    return Optional.empty();
  }

  public String getCategoryId() {
    return getParameter(CATEGORY_ID);
  }

  public String getProductId() {
    return getParameter(PRODUCT_ID);
  }

  public void setPageId(String pageId) {
    matrixParams.put(PAGE_ID, pageId);
  }

  public void setCategoryId(String categoryId) {
    matrixParams.put(CATEGORY_ID, categoryId);
  }

  public void setView(String view) {
    matrixParams.put(VIEW, view);
  }

  public void setExternalReference(String externalReference) {
    matrixParams.put(EXTERNAL_REFERENCE, externalReference);
  }

  public void setProductId(String productId) {
    matrixParams.put(PRODUCT_ID, productId);
  }

  public void setPlacement(String placement) {
    matrixParams.put(PLACEMENT, placement);
  }

  public String getEnvironment() {
    return matrixParams.get(ENVIRONMENT);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("FragmentParameters: ");
    for (Map.Entry<String, String> entry : matrixParams.entrySet()) {
      builder.append(entry.getKey());
      builder.append("=");
      builder.append(entry.getValue());
      builder.append(", ");
    }

    return builder.toString();
  }

  @NonNull
  public String toQueryParam() {
    // "/10851/en-US/params;view=..."
    StringBuilder builder = new StringBuilder();
    builder.append("/").append(storeId).append("/").append(getLocale().toLanguageTag()).append("/params");
    for (Map.Entry<String, String> entry : matrixParams.entrySet()) {
      builder.append(";");
      builder.append(entry.getKey());
      builder.append("=");
      builder.append(entry.getValue());
    }
    return builder.toString();
  }

  /**
   * Returns the value for the given key from the map of matrix parameters.
   */
  @Nullable
  public String getParameter(@NonNull String key) {
    return matrixParams.get(key);
  }

  public void setParameter(@NonNull String parameter) {
    matrixParams.put(PARAMETER, parameter);
  }

  @Nullable
  public String getDecodedValue(@NonNull String key) {
    String value = getParameter(key);
    try {
      //The parameter is 2x encoded, otherwise the path param would not work properly
      if (value != null) {
        value = URLDecoder.decode(value, "UTF-8");
      }
    } catch (UnsupportedEncodingException e) {
      LOG.error("Error decoding fragment parameter '" + key + "' with value '" + value + "': " + e.getMessage(), e);
    }
    return value;
  }

  /**
   * @cm.template.api
   */
  public String getMetaDataKeywords() {
    return metaDataKeywords;
  }

  public void setMetaDataKeywords(String metaDataKeywords) {
    this.metaDataKeywords = metaDataKeywords;
  }

  /**
   * @cm.template.api
   */
  public String getMetaDataTitle() {
    return metaDataTitle;
  }

  public void setMetaDataTitle(String metaDataTitle) {
    this.metaDataTitle = metaDataTitle;
  }

  /**
   * @cm.template.api
   */
  public String getMetaDataDescription() {
    return metaDataDescription;
  }

  public void setMetaDataDescription(String metaDataDescription) {
    this.metaDataDescription = metaDataDescription;
  }
}
