package com.coremedia.livecontext.asset;

import com.coremedia.blueprint.base.livecontext.util.LocaleHelper;
import com.coremedia.blueprint.ecommerce.cae.AbstractCommerceContextInterceptor;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Optional;

/**
 * Initialize Context for asset urls (e.g. {@link ProductCatalogPictureHandler#IMAGE_URI_PATTERN}
 * and {@link ProductCatalogPictureHandler#IMAGE_URI_PATTERN_FOR_CATALOG}
 */
public class AssetCommerceContextInterceptor extends AbstractCommerceContextInterceptor {

  LiveContextSiteResolver liveContextSiteResolver;

  @NonNull
  @Override
  protected Optional<Site> findSite(HttpServletRequest request, String normalizedPath) {
    String storeId = extractStoreId(normalizedPath);
    Locale locale = extractLocale(normalizedPath);

    return liveContextSiteResolver.findSiteFor(storeId, locale);
  }

  private static String extractStoreId(@NonNull String path) {
    return extractToken(path, 3);
  }

  @Nullable
  private static Locale extractLocale(@NonNull String path) {
    String localeToken = extractToken(path, 4);
    return LocaleHelper.parseLocaleFromString(localeToken).orElse(null);
  }

  private static String extractToken(@NonNull String path, int index) {
    String[] split = path.split("/");
    //length == 7 no catalogId
    //length == 8 with catalogId
    if (split.length == 7 || split.length == 8) {
      return split[index];
    }
    throw new IllegalArgumentException("Cannot handle path " + path);
  }

  @Required
  public void setLiveContextSiteResolver(LiveContextSiteResolver liveContextSiteResolver) {
    this.liveContextSiteResolver = liveContextSiteResolver;
  }
}
