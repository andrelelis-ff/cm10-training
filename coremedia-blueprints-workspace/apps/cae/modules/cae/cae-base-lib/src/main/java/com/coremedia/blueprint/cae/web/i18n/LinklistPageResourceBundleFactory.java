package com.coremedia.blueprint.cae.web.i18n;

import com.coremedia.cap.util.PairCacheKey;
import com.coremedia.cap.util.StructUtil;
import com.coremedia.blueprint.coderesources.ThemeService;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.localization.LocalizationService;
import com.coremedia.cache.Cache;
import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * An implementation of PageResourceBundleFactory which is backed by the
 * resource bundles of the page's navigation and its theme.
 */
public class LinklistPageResourceBundleFactory implements PageResourceBundleFactory {
  private static final Logger LOG = LoggerFactory.getLogger(LinklistPageResourceBundleFactory.class);

  private static final String LINKABLE_RESOURCEBUNDLES = "resourceBundles2";  // CMLinkable#resourceBundles
  private static final String THEME_RESOURCEBUNDLES = "resourceBundles";  // CMTheme#resourceBundles
  private static final String RESOURCEBUNDLE_LOCALIZATIONS = "localizations";  // CMResourceBundle#localizations

  private Cache cache = null;
  private boolean useLocalresources = false;

  private SitesService sitesService;
  private LocalizationService localizationService;
  private ThemeService themeService;


  // --- configure --------------------------------------------------

  /**
   * Usage of a cache is strongly recommended for production use.
   */
  public void setCache(Cache cache) {
    this.cache = cache;
  }

  /**
   * Only for frontend development.
   * <p>
   * MUST NOT be set in production instances, because it disables caching of
   * resource bundles.
   */
  public void setUseLocalresources(boolean useLocalresources) {
    this.useLocalresources = useLocalresources;
  }

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Required
  public void setLocalizationService(LocalizationService localizationService) {
    this.localizationService = localizationService;
  }

  @Required
  public void setThemeService(ThemeService themeService) {
    this.themeService = themeService;
  }


  // --- PageResourceBundleFactory ----------------------------------

  @Override
  public ResourceBundle resourceBundle(Page page, User developer) {
    // For performance and cache size reasons this implementation supports
    // resource bundles only for the page's navigation.  If you really need
    // resource bundles at content level, you can include the page content's
    // resourceBundles here.
    return resourceBundle(page.getNavigation(), developer);
  }

  @Override
  public ResourceBundle resourceBundle(Navigation navigation, User developer) {
    if (useLocalresources || cache==null) {
      if (!useLocalresources) {
        LOG.warn("Using " + getClass().getName() + " without cache.  Ok for testing, too expensive for production.");
      }
      return resourceBundleUncached(navigation, developer);
    } else {
      return cache.get(new NavigationCacheKey(navigation, developer));
    }
  }


  // --- internal ---------------------------------------------------

  /**
   * Returns the navigation's resource bundle.
   * <p>
   * Considers the developer's work in progress resource bundles.
   */
  private ResourceBundle resourceBundleUncached(Navigation navigation, @Nullable User developer) {
    Struct struct = hierarchicalMergedResourceBundles(navigation, developer);
    return struct == null ? EmptyResourceBundle.emptyResourceBundle() : CapStructHelper.asResourceBundle(struct);
  }

  private Struct hierarchicalMergedResourceBundles(Navigation navigation, User developer) {
    Struct bundle = null;
    // Naive idea: the navigation's locale.
    // Just more complicated because there may be navigations w/o locale.
    Locale mostSpecificLocale = null;
    // Precedence:
    // 1. child navigation over parent navigation
    // 2. linked resource bundle over theme
    for (Navigation nav = navigation; nav!=null; nav = nav.getParentNavigation()) {
      if (nav instanceof CMNavigation) {
        Content navContent = ((CMNavigation)nav).getContent();
        // Watch out for a locale along the channel path.
        // Normally, the given navigation itself should have one.
        if (mostSpecificLocale == null) {
          mostSpecificLocale = sitesService.getContentSiteAspect(navContent).getLocale();
        }
        bundle = StructUtil.mergeStructs(bundle, mergedResourceBundles(navContent));
        // Must stick to the original locale here, not this navContent's one.
        bundle = StructUtil.mergeStructs(bundle, mergedResourceBundlesFromTheme(navContent, mostSpecificLocale, developer));
      }
    }
    return bundle;
  }

  private static Struct mergedResourceBundles(Content linkable) {
    List<Content> bundles = linkable.getLinks(LINKABLE_RESOURCEBUNDLES);
    List<Struct> localizations = new ArrayList<>();
    for (Content bundle : bundles) {
      localizations.add(bundle.getStruct(RESOURCEBUNDLE_LOCALIZATIONS));
    }
    return StructUtil.mergeStructList(localizations);
  }

  /**
   * Instead of doing a simple lookup of just the cmNavigation's locale, this method will merge resourcebundles by
   * 1. language, country and variant
   * 2. language and country
   * 3. or just a country.
   * <p>
   * Therefore an editor will only need to link one resource bundle if all resource bundles are linked via the master linklist.
   * In case of a translation there is no need to link one specific bundle anymore.
   *
   * @param cmNavigation the navigation containing the theme and the locale
   * @param locale the locale of the original navigation
   * @param developer considers the developer's work in progress resource bundles
   * @return a Struct containing the localizations.
   */
  private Struct mergedResourceBundlesFromTheme(Content cmNavigation, Locale locale, @Nullable User developer) {
    List<Struct> structs = new ArrayList<>();
    // Consider only the direct theme here.
    // In case of channels A/B/C, theme inheritance from A
    // would break the precedence of B's linked resource bundles.
    Content theme = themeService.directTheme(Collections.singletonList(cmNavigation), developer);
    if (theme != null) {
      List<Content> bundles = theme.getLinks(THEME_RESOURCEBUNDLES);
      // always fallback to the linked resource bundles of a theme (e.g. if no explicit resource bundles exist for the
      // given localization)
      structs.add(localizationService.resources(bundles, locale, bundles));
    }
    return StructUtil.mergeStructList(structs);
  }


  // --- caching ----------------------------------------------------

  private class NavigationCacheKey extends PairCacheKey<Navigation, User, ResourceBundle> {
    NavigationCacheKey(@NonNull Navigation navigation, User user) {
      super(navigation, user);
    }

    @Override
    protected ResourceBundle evaluate(Cache cache, Navigation key1, User key2) {
      return resourceBundleUncached(key1, key2);
    }
  }
}
