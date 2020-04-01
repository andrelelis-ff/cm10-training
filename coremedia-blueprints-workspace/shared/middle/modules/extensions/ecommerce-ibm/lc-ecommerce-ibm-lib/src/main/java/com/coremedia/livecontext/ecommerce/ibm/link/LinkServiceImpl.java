package com.coremedia.livecontext.ecommerce.ibm.link;

import com.coremedia.blueprint.base.links.UrlPrefixResolver;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommercePropertyHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.link.GenericStorefrontRefBuilder;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.link.LinkService;
import com.coremedia.livecontext.ecommerce.link.QueryParam;
import com.coremedia.livecontext.ecommerce.link.StorefrontRef;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Provides commerce storefront links
 */
@DefaultAnnotation(NonNull.class)
public class LinkServiceImpl implements LinkService, InitializingBean {

  private static final Logger LOG = LoggerFactory.getLogger(LinkServiceImpl.class);

  private static final String PREVIEW_URL_PATTERN = "/preview/";
  private static final String CMS_HOST_PLACEHOLDER = "[cmsHost]";
  private static final String STORE_ID_PLACEHOLDER = "[storeId]";
  private static final String CATALOG_ID_PLACEHOLDER = "[catalogId]";
  private static final String DEFAULT_CAE_BASE_URI_PATH = "/blueprint/servlet";

  @Nullable
  private String commercePreviewUrl;

  @Nullable
  private String commerceProductionUrl;

  @Nullable
  private String storefrontAssetPathPrefix;

  @Nullable
  private String cmsHost;

  @Nullable
  private String studioPreviewUrlPrefix;

  @Nullable
  private UrlPrefixResolver urlPrefixResolver;

  private String caeBaseUriPath = DEFAULT_CAE_BASE_URI_PATH;

  @Override
  public void afterPropertiesSet() {
    if (StringUtils.isBlank(cmsHost) && StringUtils.isNotBlank(studioPreviewUrlPrefix)) {
      cmsHost = extractCmsHost(studioPreviewUrlPrefix);
    }
  }

  @Override
  public Optional<String> getImageUrl(String imageSegment, StoreContext storeContext) {
    return getImageUrl(imageSegment, storeContext, false);
  }

  Optional<String> getImageUrl(String imageSegment, StoreContext storeContext, boolean prependCatalogPath) {
    return buildUrl(imageSegment, storeContext, prependCatalogPath);
  }

  @Override
  public StorefrontRef getCategoryLink(Category category, List<QueryParam> linkParameters, HttpServletRequest request) {
    Map<String, Object> replacements = new HashMap<>(category.getContext().getReplacements());

    List<Category> breadcrumbPath = category.getBreadcrumb();
    int level = breadcrumbPath.size();
    if (level > 3) {
      level = 3;
    }

    Category parent = category.getParent();
    if (level == 3 && parent != null) {
      replacements.put("categoryId", category.getExternalTechId());
      replacements.put("parentCategoryId", parent.getExternalTechId());
      replacements.put("topCategoryId", breadcrumbPath.get(0).getExternalTechId());
      replacements.put("level", level + "");
    } else if (level >= 2) {
      replacements.put("categoryId", category.getExternalTechId());
      replacements.put("topCategoryId", breadcrumbPath.get(0).getExternalTechId());
      replacements.put("level", level + "");
    } else {
      replacements.put("categoryId", category.getExternalTechId());
      replacements.put("level", level + "");
    }

    String linkTemplate = "<!--CM {"
            + "\"parentCategoryId\":\"{parentCategoryId}\","
            + "\"topCategoryId\":\"{topCategoryId}\","
            + "\"level\":{level},\"renderType\":\"url\","
            + "\"categoryId\":\"{categoryId}\","
            + "\"objectType\":\"category\""
            + "} CM-->";

    return GenericStorefrontRefBuilder.fromTemplate(linkTemplate, replacements).expand(linkParameters);
  }

  @Override
  public StorefrontRef getProductLink(Product product, @Nullable Category alternativeCategory,
                                      List<QueryParam> linkParameters, HttpServletRequest request) {
    Map<String, Object> replacements = new HashMap<>(product.getContext().getReplacements());

    Category category = alternativeCategory != null ? alternativeCategory : product.getCategory();
    replacements.put("productId", product.getExternalTechId());
    replacements.put("categoryId", category.getExternalTechId());

    String linkTemplate = "<!--CM {"
            + "\"productId\":\"{productId}\","
            + "\"renderType\":\"url\","
            + "\"categoryId\":\"{categoryId}\","
            + "\"objectType\":\"product\""
            + "} CM-->";

    return GenericStorefrontRefBuilder.fromTemplate(linkTemplate, replacements).expand(linkParameters);
  }

  @Override
  public StorefrontRef getExternalPageLink(@Nullable String seoPath, @Nullable String alternativePath,
                                           StoreContext storeContext, List<QueryParam> linkParameters,
                                           HttpServletRequest request) {
    Map<String, Object> replacements = new HashMap<>(storeContext.getReplacements());

    if (alternativePath != null) {
      replacements.put("alternativePath", alternativePath);
    } else {
      replacements.put("externalSeoSegment", seoPath != null ? seoPath : "");
    }

    String linkTemplate = "<!--CM {"
            + "\"externalSeoSegment\":\"{externalSeoSegment}\","
            + "\"externalUriPath\":\"{externalUriPath}\","
            + "\"renderType\":\"url\","
            + "\"objectType\":\"page\""
            + "} CM-->";

    return GenericStorefrontRefBuilder.fromTemplate(linkTemplate, replacements).expand(linkParameters);
  }

  @Override
  public StorefrontRef getContentLink(@Nullable String seoPath, StoreContext storeContext,
                                      List<QueryParam> linkParameters, HttpServletRequest request) {
    Map<String, Object> replacements = new HashMap<>(storeContext.getReplacements());

    replacements.put("externalSeoSegment", seoPath != null ? seoPath : "");

    String linkTemplate = "<!--CM {"
            + "\"externalSeoSegment\":\"{externalSeoSegment}\","
            + "\"renderType\":\"url\","
            + "\"objectType\":\"content\""
            + "} CM-->";

    return GenericStorefrontRefBuilder.fromTemplate(linkTemplate, replacements).expand(linkParameters);
  }

  @Override
  public StorefrontRef getAjaxLink(String url, StoreContext storeContext, HttpServletRequest request) {
    Map<String, Object> replacements = new HashMap<>(storeContext.getReplacements());

    replacements.put("url", url);

    String linkTemplate = "<!--CM {"
            + "\"url\":\"{url}\","
            + "\"renderType\":\"url\","
            + "\"objectType\":\"ajax\""
            + "} CM-->";

    return GenericStorefrontRefBuilder.fromTemplate(linkTemplate, replacements);
  }

  //--- private ---

  private Optional<String> buildUrl(String segment, StoreContext storeContext, boolean prependCatalogPath) {
    checkState(isNotBlank(commercePreviewUrl),
            "Wrong configuration of the commerce preview host of the asset URL provider: %s", commercePreviewUrl);
    checkState(isNotBlank(commerceProductionUrl),
            "Wrong configuration of the commerce production host of the asset URL provider: %s", commerceProductionUrl);
    if (isNullOrEmpty(segment)) {
      return Optional.empty();
    }

    //TODO: workaround for asset management urls from WCS:
    // they can look like /wcsstore/ExtendedSitesCatalogAssetStore/http://[cmsHost]/blueprint/servlet/product/catalogimage/10202/en_US/thumbnail/PC_FRENCH_PRESS.jpg
    String url = resolveUrlFromWCS(segment, storeContext);

    //if segment is a fully qualified url, do not prepend host
    if (url.startsWith("http") || url.startsWith("//")) {
      //make absolute url scheme relative
      url = url.startsWith("http") ? url.substring(url.indexOf("//")) : url;
      return Optional.of(url);
    }

    if (prependCatalogPath) {
      checkState(storefrontAssetPathPrefix != null,
              "Storefront asset path prefix must not be null if it shall be appended.");
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

      String urlWithTokensReplaced = CommercePropertyHelper.replaceTokens(uri.toString(), storeContext);
      return Optional.ofNullable(urlWithTokensReplaced);
    } catch (URISyntaxException e) {
      LOG.warn("Could not build URL for image segment '{}'.", segmentWithLeadingSlash, e);
      return Optional.empty();
    }
  }

  @NonNull
  private String resolveUrlFromWCS(@NonNull String url, @NonNull StoreContext storeContext) {
    // If the URL contains `http://` or `https://` make the URL start with it.
    String resolvedUrl = url;
    int i = url.indexOf("http://");
    if (i < 0) {
      i = url.indexOf("https://");
    }

    if (i >= 0) {
      resolvedUrl = url.substring(i);
    }

    // Replace CMS host placeholder.
    String cmsHostTmp = getCmsHost(storeContext);
    if (cmsHostTmp != null) {
      resolvedUrl = resolvedUrl.replace(CMS_HOST_PLACEHOLDER, cmsHostTmp);
    }

    // Replace store ID placeholder.
    resolvedUrl = resolvedUrl.replace(STORE_ID_PLACEHOLDER, storeContext.getStoreId());
    // Replace catalog ID placeholder.
    resolvedUrl = resolvedUrl.replace(CATALOG_ID_PLACEHOLDER, storeContext.getCatalogId().get().value());

    return resolvedUrl;
  }

  private static String removeTrailingSlash(String url) {
    char lastCharacter = url.charAt(url.length() - 1);
    if (lastCharacter == '/') {
      return url.substring(0, url.lastIndexOf("/"));
    }

    return url;
  }

  private static String ensureLeadingSlash(String url) {
    if (url.startsWith("/")) {
      return url;
    }

    return "/" + url;
  }

  private String prependStorefrontCatalog(String url) {
    if (url.startsWith("http") || url.startsWith("/")) {
      // With search-based REST handler active, WCS sends server-relative
      // URLs already containing the catalog asset store. Unfortunately,
      // also different in normal and preview mode.
      return url;
    }

    String leadingSlashPath = ensureLeadingSlash(url);
    String storefrontAssetPathPrefix = removeTrailingSlash(this.storefrontAssetPathPrefix);
    if (leadingSlashPath.contains(storefrontAssetPathPrefix)) {
      return leadingSlashPath;
    }

    return storefrontAssetPathPrefix.concat(leadingSlashPath);
  }

  @Nullable
  private String extractCmsHost(@NonNull String urlPrefix) {
    try {
      UriComponents uriComponents = UriComponentsBuilder.fromUriString(urlPrefix).build();
      String endpoint = uriComponents.getHost();
      int port = uriComponents.getPort();
      if (port > 0) {
        endpoint += ":" + port;
        endpoint += Objects.requireNonNullElse(caeBaseUriPath, DEFAULT_CAE_BASE_URI_PATH);
      }
      return endpoint;
    } catch (IllegalArgumentException e) {
      LOG.warn("cms host '{}' derived from urlPrefix is not an url.", urlPrefix, e);
      return null;
    }
  }

  @Nullable
  private String getCmsHost(@NonNull StoreContext storeContext) {
    if (cmsHost == null) {
      if (urlPrefixResolver == null) {
        LOG.warn("Required urlPrefixResolver not found.");
        return null;
      }
      String siteId = storeContext.getSiteId();
      String urlPrefix = urlPrefixResolver.getUrlPrefix(siteId, null, null);
      if (urlPrefix == null) {
        LOG.warn("Unable to derive URL prefix from site '{}'.", siteId);
        return null;
      }
      cmsHost = extractCmsHost(urlPrefix);
    }
    return cmsHost;
  }

  //--- wiring ---

  @Required
  public void setCommercePreviewUrl(String commercePreviewUrl) {
    this.commercePreviewUrl = commercePreviewUrl;
  }

  @Required
  public void setCommerceProductionUrl(String commerceProductionUrl) {
    this.commerceProductionUrl = commerceProductionUrl;
  }

  @Required
  public void setCatalogPathPrefix(String catalogPathPrefix) {
    this.storefrontAssetPathPrefix = catalogPathPrefix;
  }

  public void setCmsHost(String cmsHost) {
    this.cmsHost = cmsHost;
  }

  @Autowired(required = false)
  public void setUrlPrefixResolver(UrlPrefixResolver urlPrefixResolver) {
    this.urlPrefixResolver = urlPrefixResolver;
  }

  @Value("${studio.previewUrlPrefix:}")
  public void setStudioPreviewUrlPrefix(String studioPreviewUrlPrefix) {
    this.studioPreviewUrlPrefix = studioPreviewUrlPrefix;
  }

  public void setCaeBaseUriPath(String caeBaseUriPath) {
    this.caeBaseUriPath = caeBaseUriPath;
  }
}
