package com.coremedia.livecontext.handler.util;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.multisite.cae.SiteResolver;
import com.coremedia.cap.common.CapObjectDestroyedException;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteDestroyedException;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.fragment.FragmentParameters;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toSet;

/**
 * Utility class for resolving a site from an URL.
 */
public class LiveContextSiteResolverImpl implements LiveContextSiteResolver {

  private static final Logger LOG = LoggerFactory.getLogger(LiveContextSiteResolverImpl.class);

  private SiteResolver delegate;
  private SitesService sitesService;
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @NonNull
  @Override
  public Optional<Site> findSiteFor(@NonNull FragmentParameters fragmentParameters) {
    Optional<Site> site = findSiteForEnvironment(fragmentParameters.getLocale(), fragmentParameters.getEnvironment());
    if (site.isPresent()) {
      return site;
    }

    return findSiteFor(fragmentParameters.getStoreId(), fragmentParameters.getLocale());
  }

  @NonNull
  @Override
  public Optional<Site> findSiteFor(@NonNull String storeId, @NonNull Locale locale) {
    Set<Site> matchingSites = sitesService.getSites().stream()
            .filter(site -> localeMatchesSite(site, locale))
            .filter(site -> siteHasStore(site, storeId))
            .collect(toSet());

    int matchingSitesCount = matchingSites.size();

    if (matchingSitesCount > 1) {
      throw new IllegalStateException("Found more than one site for store.id: " + storeId + " and locale: " + locale);
    }

    if (matchingSitesCount == 0) {
      LOG.warn("No site found with store.id={} and locale={}", storeId, locale);
      return Optional.empty();
    }

    Site site = matchingSites.iterator().next();
    LOG.debug("Found site {}({}) for store.id={} and locale={}", site.getName(), site.getLocale(), storeId, locale);
    return Optional.of(site);
  }

  // --- internal ---------------------------------------------------

  private boolean siteHasStore(@NonNull Site site, @NonNull String storeId) {
    StoreContext storeContext;

    try {
      Optional<CommerceConnection> commerceConnection = commerceConnectionInitializer.findConnectionForSite(site);

      if (!commerceConnection.isPresent()) {
        LOG.debug("Site '{}' has no commerce connection.", site.getName());
        return false;
      }

      storeContext = commerceConnection.get().getStoreContext();
    } catch (CommerceException e) {
      LOG.debug("Could not retrieve store context for site '{}'.", site.getName(), e);
      return false;
    }

    return storeId.equalsIgnoreCase(String.valueOf(storeContext.getStoreId()));
  }

  private static boolean localeMatchesSite(@NonNull Site site, @NonNull Locale locale) {
    Locale siteLocale = site.getLocale();
    return locale.equals(siteLocale) ||
            (isNullOrEmpty(siteLocale.getCountry()) && locale.getLanguage().equals(siteLocale.getLanguage()));
  }

  /**
   * Extracts the site name out of the environment parameter String, e.g. site:siteName
   *
   * @param locale      The locale passed for the fragment request.
   * @param environment The name of the environment which contains the site name to use.
   * @return The site that was resolved by the environment (name matching by default).
   */
  @NonNull
  private Optional<Site> findSiteForEnvironment(@NonNull Locale locale, @Nullable String environment) {
    if (environment == null) {
      return Optional.empty();
    }

    String siteName = extractSiteNameFromEnvironment(environment);
    if (siteName == null) {
      return Optional.empty();
    }

    return sitesService.getSites().stream()
            .filter(matchesNameAndLocale(siteName, locale))
            .findFirst();
  }

  private static Predicate<Site> matchesNameAndLocale(String siteName, Locale locale) {
    return site -> {
      try {
        return Objects.equals(siteName, site.getName()) && Objects.equals(locale, site.getLocale());
      } catch (CapObjectDestroyedException | SiteDestroyedException e) {
        LOG.debug("ignoring destroyed site '{}'", site.getId(), e);
        return false;
      }
    };
  }

  @Nullable
  private static String extractSiteNameFromEnvironment(@NonNull String environment) {
    if (isNullOrEmpty(environment) || !environment.startsWith("site:")) {
      return null;
    }

    return environment.split(":")[1];
  }

  // -------------- Defaults ------------------------------

  @Override
  public Site findSiteByPath(String normalizedPath) {
    return delegate.findSiteByPath(normalizedPath);
  }

  @Override
  public Site findSiteBySegment(String siteSegment) {
    return delegate.findSiteBySegment(siteSegment);
  }

  @Override
  public Site findSiteForPathWithContentId(String normalizedPath) {
    return delegate.findSiteForPathWithContentId(normalizedPath);
  }

  @Override
  public Site findSiteForContentId(int contentId) {
    return delegate.findSiteForContentId(contentId);
  }

  // --- configuration ----------------------------------------------

  @Required
  public void setDelegate(SiteResolver delegate) {
    this.delegate = delegate;
  }

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Required
  public void setCommerceConnectionInitializer(CommerceConnectionInitializer commerceConnectionInitializer) {
    this.commerceConnectionInitializer = commerceConnectionInitializer;
  }
}
