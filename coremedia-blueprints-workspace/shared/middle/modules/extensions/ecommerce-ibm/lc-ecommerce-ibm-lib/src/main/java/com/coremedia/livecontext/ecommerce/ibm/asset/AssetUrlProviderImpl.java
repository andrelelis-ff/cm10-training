package com.coremedia.livecontext.ecommerce.ibm.asset;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommercePropertyHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.livecontext.ecommerce.asset.AssetUrlProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Provides commerce image urls
 *
 * @deprecated use #LinkServiseImpl instead
 */
public class AssetUrlProviderImpl implements AssetUrlProvider {
  private static final Logger LOG = LoggerFactory.getLogger(AssetUrlProviderImpl.class);

  private static final String PREVIEW_URL_PATTERN = "/preview/";
  private static final String CMS_HOST_PLACEHOLDER = "[cmsHost]";
  private static final String STORE_ID_PLACEHOLDER = "[storeId]";
  private static final String CATALOG_ID_PLACEHOLDER = "[catalogId]";

  private String commercePreviewUrl;
  private String commerceProductionUrl;
  private String cmsHost;
  private String storefrontAssetPathPrefix;

  @Override
  public String getImageUrl(@NonNull String imageSegment) {
    return buildUrl(imageSegment, false);
  }

  @Override
  public String getImageUrl(@NonNull String imageSegment, boolean prependCatalogPath) {
    return buildUrl(imageSegment, prependCatalogPath);
  }

  @Nullable
  private String buildUrl(@NonNull String segment, boolean prependCatalogPath) {
    checkState(isNotBlank(commercePreviewUrl), "Wrong configuration of the commerce preview host of the asset url provider: " + commercePreviewUrl);
    checkState(isNotBlank(commerceProductionUrl), "Wrong configuration of the commerce production host of the asset url provider: " + commerceProductionUrl);
    if (isNullOrEmpty(segment)) {
      return null;
    }

    //TODO: workaround for asset management urls from WCS:
    // they can look like /wcsstore/ExtendedSitesCatalogAssetStore/http://[cmsHost]/blueprint/servlet/product/catalogimage/10202/en_US/thumbnail/PC_FRENCH_PRESS.jpg
    String url = resolveUrlFromWCS(segment);

    //if segment is a fully qualified url, do not prepend host
    if (url.startsWith("http") || url.startsWith("//")) {
      //make absolute url scheme relative
      url = url.startsWith("http") ? url.substring(url.indexOf("//")) : url;
      return url;
    }

    if (prependCatalogPath) {
      checkState(storefrontAssetPathPrefix != null, "Storefront asset path prefix must not be null if it shell be appended");
      url = prependStorefrontCatalog(url);
    }

    String segmentWithLeadingSlash = ensureLeadingSlash(url);
    try {
      URI uri;
      if (segmentWithLeadingSlash.contains(PREVIEW_URL_PATTERN)) {
        uri = new URI(removeTrailingSlash(commercePreviewUrl) + segmentWithLeadingSlash);
      } else {
        uri = new URI(removeTrailingSlash(commerceProductionUrl) + segmentWithLeadingSlash);
      }

      StoreContext storeContext = CurrentStoreContext.find().orElse(null);

      return CommercePropertyHelper.replaceTokens(uri.toString(), storeContext);
    } catch (URISyntaxException e) {
      LOG.warn("could not build url for image segment " + segmentWithLeadingSlash, e.getMessage());
      return null;
    }
  }

  @NonNull
  private String resolveUrlFromWCS(String url) {
    checkState(isNotBlank(cmsHost), "Wrong configuration of the cms host of the asset url provider: " + cmsHost);

    //if the url contains http:// or https:// make the url start with them
    String resolvedUrl = url;
    int i = url.indexOf("http://");
    if (i < 0) {
      i = url.indexOf("https://");
    }
    if (i >= 0) {
      resolvedUrl = url.substring(i);
    }

    //replace [cmsHost]
    resolvedUrl = resolvedUrl.replace(CMS_HOST_PLACEHOLDER, cmsHost);

    StoreContext storeContext = CurrentStoreContext.find().orElse(null);
    if (storeContext == null) {
      return resolvedUrl;
    }

    //replace [storeId]
    resolvedUrl = resolvedUrl.replace(STORE_ID_PLACEHOLDER, storeContext.getStoreId());
    //replace [catalogId]
    resolvedUrl = resolvedUrl.replace(CATALOG_ID_PLACEHOLDER, storeContext.getCatalogId().get().value());

    return resolvedUrl;
  }

  @NonNull
  private static String removeTrailingSlash(@NonNull String url) {
    char lastCharacter = url.charAt(url.length() - 1);
    if (lastCharacter == '/') {
      return url.substring(0, url.lastIndexOf("/"));
    }

    return url;
  }

  @NonNull
  private static String ensureLeadingSlash(@NonNull String url) {
    if (url.startsWith("/")) {
      return url;
    }

    return "/" + url;
  }

  @NonNull
  private String prependStorefrontCatalog(@NonNull String url) {
    if (url.startsWith("http") || url.startsWith("/")) {
      // with search based REST handler active, WCS sends server-relative URLs already containing the
      // catalog asset store. Unfortunately, also different in normal and preview mode
      return url;
    }

    String leadingSlashPath = ensureLeadingSlash(url);
    String storefrontAssetPathPrefix = removeTrailingSlash(this.storefrontAssetPathPrefix);
    if (leadingSlashPath.contains(storefrontAssetPathPrefix)) {
      return leadingSlashPath;
    }

    return storefrontAssetPathPrefix.concat(leadingSlashPath);
  }

  @Override
  @Required
  public void setCommercePreviewUrl(@NonNull String commercePreviewUrl) {
    this.commercePreviewUrl = commercePreviewUrl;
  }

  @Override
  @Required
  public void setCommerceProductionUrl(@NonNull String commerceProductionUrl) {
    this.commerceProductionUrl = commerceProductionUrl;
  }

  @Required
  public void setCmsHost(@NonNull String cmsHost) {
    this.cmsHost = cmsHost;
  }

  @Override
  @Required
  public void setCatalogPathPrefix(String catalogPathPrefix) {
    this.storefrontAssetPathPrefix = catalogPathPrefix;
  }
}
