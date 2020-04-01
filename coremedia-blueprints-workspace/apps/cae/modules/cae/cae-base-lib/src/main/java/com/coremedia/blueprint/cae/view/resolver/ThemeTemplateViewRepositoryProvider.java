package com.coremedia.blueprint.cae.view.resolver;

import com.coremedia.cap.util.PairCacheKey;
import com.coremedia.blueprint.coderesources.ThemeService;
import com.coremedia.cache.Cache;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.user.User;
import com.coremedia.cap.util.JarBlobResourceLoader;
import com.coremedia.objectserver.view.ViewRepository;
import com.coremedia.objectserver.view.resolver.AbstractTemplateViewRepositoryProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Collections2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ThemeTemplateViewRepositoryProvider extends AbstractTemplateViewRepositoryProvider {
  private static final Logger LOG = LoggerFactory.getLogger(ThemeTemplateViewRepositoryProvider.class);

  private static final String THEME_VIEW_REPOSITORY_NAME_PREFIX = "theme:";
  private static final String CM_TEMPLATESET_ARCHIVE = "archive";
  private static final String TEMPLATES_PATH_PREFIX = "META-INF/resources/WEB-INF/templates";

  private Cache cache;

  private CapConnection capConnection;
  private ThemeService themeService;
  private JarBlobResourceLoader jarBlobResourceLoader;
  private boolean useLocalResources = false;


  // --- construct and configure ------------------------------------

  public void setCache(Cache cache) {
    this.cache = cache;
  }

  @Required
  public void setThemeService(ThemeService themeService) {
    this.themeService = themeService;
  }

  @Required
  public void setCapConnection(CapConnection capConnection) {
    this.capConnection = capConnection;
  }

  @Required
  public void setJarBlobResourceLoader(JarBlobResourceLoader jarBlobResourceLoader) {
    this.jarBlobResourceLoader = jarBlobResourceLoader;
  }

  public void setUseLocalResources(boolean useLocalResources) {
    this.useLocalResources = useLocalResources;
  }


  // --- ViewRepositoryProvider -------------------------------------

  /**
   * Get the view repository for the given name.
   *
   * @return A view repository or null if this ViewRepositoryProvider is not responsible
   */
  @Override
  public ViewRepository getViewRepository(String name) {
    if (!isThemeViewRepository(name)) {
      // We encounter this case for alien repository names, but also
      // for our own #viewRepositoryNames(Content theme), if
      // * useLocalResources is set, or
      // * a theme uses the deprecated viewRepositoryName property rather than
      //   bringing its own templates.
      return null;
    }
    return createViewRepository(templateLocations(name));
  }


  // --- more features ----------------------------------------------

  /**
   * Returns the view repository names of the theme.
   */
  List<String> viewRepositoryNames(Content theme, @Nullable User developer) {
    if (cache!=null) {
      return cache.get(new ViewRepositoryNamesCacheKey(theme, developer));
    } else {
      LOG.warn("No cache. Ok for development/test, too slow for production.");
      return viewRepositoryNamesUncached(theme, developer);
    }
  }


  // --- internal ---------------------------------------------------

  /**
   * Returns the template locations for the given view repository name.
   * <p>
   * The format is suitable for the {@link JarBlobResourceLoader}.
   *
   * @return the templates locations of the theme
   */
  @VisibleForTesting
  List<String> templateLocations(String themeViewRepositoryName) {
    if (!isThemeViewRepository(themeViewRepositoryName)) {
      throw new IllegalArgumentException(themeViewRepositoryName + " is not a theme backed view repository.");
    }
    ThemeViewRepositoryName tvrn = new ThemeViewRepositoryName(capConnection, themeViewRepositoryName);
    if (cache!=null) {
      return cache.get(new TemplateLocationsCacheKey(tvrn.theme(), tvrn.developer()));
    } else {
      LOG.warn("No cache. Ok for development/test, too slow for production.");
      return templateLocationsUncached(tvrn.theme(), tvrn.developer());
    }
  }

  private List<String> viewRepositoryNamesUncached(Content theme, @Nullable User developer) {
    List<String> locations = new ArrayList<>();
    for (Content templateSet : themeService.templateSets(theme, developer)) {
      locations.addAll(viewRepositoryNamesFromJar(templateSet).stream().map(vrn -> viewRepositoryName(theme, developer, vrn)).collect(toList()));
    }
    if (!locations.isEmpty()) {
      return locations;
    }

    // Legacy fallback:
    // The theme does not bring its own templates but assumes a certain
    // view repository to exist.  Leave the name as is, it won't be served by
    // #getViewRepository but hopefully by some other ViewRepositoryProvider.
    String legacyVRN = themeService.viewRepositoryName(theme, developer);
    return legacyVRN==null ? Collections.emptyList() : Collections.singletonList(legacyVRN);
  }

  private String viewRepositoryName(Content theme, @Nullable User developer, String vrn) {
    if (useLocalResources) {
      // suitable to match tomcat-contexts.xml#Resources#/=${project.basedir}/../../frontend/target/resources
      // e.g. "corporate"
      return vrn;
    } else {
      // suitable to match #templateLocations(String themeViewRepositoryName)
      // e.g. "theme::1234/corporate" or "theme:10:1234/corporate"
      int themeId = IdHelper.parseContentId(theme.getId());
      StringBuilder sb = new StringBuilder(THEME_VIEW_REPOSITORY_NAME_PREFIX);
      if (developer != null) {
        sb.append(IdHelper.parseUserId(developer.getId()));
      }
      sb.append(":");
      sb.append(themeId).append("/").append(vrn);
      return sb.toString();
    }
  }

  /**
   * Check whether the given viewRepositoryName denotes a theme backed
   * view repository.
   */
  private boolean isThemeViewRepository(String viewRepositoryName) {
    return viewRepositoryName.startsWith(THEME_VIEW_REPOSITORY_NAME_PREFIX);
  }

  /**
   * Figure out the view repository names of a template set jar.
   * <p>
   * A template set jar has entries like
   * META-INF/resources/WEB-INF/templates/corporate/com.coremedia.blueprint.common.layout/Container.asGap.ftl
   * or more abstract:
   * prefix/view-repository/package/template
   * In this example "corporate" would be the view repository name.
   */
  private Collection<String> viewRepositoryNamesFromJar(Content templateSet) {
    String location = jarBlobResourceLoader.toLocation(templateSet, CM_TEMPLATESET_ARCHIVE, TEMPLATES_PATH_PREFIX);
    return jarBlobResourceLoader.getChildren(location, true, false, true);
  }

  private List<String> templateLocationsUncached(Content theme, @Nullable User developer) {
    List<String> locations = new ArrayList<>();
    for (Content templateSet : themeService.templateSets(theme, developer)) {
      locations.addAll(templatesRoots(templateSet));
    }
    return locations;
  }

  private Collection<String> templatesRoots(Content templateSet) {
    String location = jarBlobResourceLoader.toLocation(templateSet, CM_TEMPLATESET_ARCHIVE, TEMPLATES_PATH_PREFIX);
    Collection<String> paths = jarBlobResourceLoader.getChildren(location, false, false, true);
    return Collections2.transform(paths, (String s) -> jarBlobResourceLoader.toLocation(templateSet, CM_TEMPLATESET_ARCHIVE, s));
  }


  // --- inner classes ----------------------------------------------

  @VisibleForTesting
  static class ThemeViewRepositoryName {
    private CapConnection capConnection;

    int developerId;
    int themeId;
    String viewRepositoryName;

    /**
     * Parse a themeViewRepositoryName
     * <p>
     * E.g. "theme::1234/corporate" or "theme:10:1234/corporate"
     */
    ThemeViewRepositoryName(CapConnection capConnection, String themeViewRepositoryName) {
      try {
        this.capConnection = capConnection;
        int colon = themeViewRepositoryName.indexOf(':', THEME_VIEW_REPOSITORY_NAME_PREFIX.length());
        int slash = themeViewRepositoryName.indexOf('/');
        String developerIdSubstring = themeViewRepositoryName.substring(THEME_VIEW_REPOSITORY_NAME_PREFIX.length(), colon);
        String themeIdSubstring = themeViewRepositoryName.substring(colon+1, slash);
        developerId = developerIdSubstring.isEmpty() ? -1 : Integer.parseInt(developerIdSubstring);
        themeId = Integer.parseInt(themeIdSubstring);
        viewRepositoryName = themeViewRepositoryName.substring(slash+1);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Cannot parse theme view repository name " + viewRepositoryName, e);
      }
    }

    Content theme() {
      return capConnection.getContentRepository().getContent(IdHelper.formatContentId(themeId));
    }

    @Nullable
    User developer() {
      if (developerId == -1) {
        return null;
      }
      if (capConnection.getUserRepository() == null) {
        // Alternatively, we could return null here for robustness.
        // However, this code is not supposed to be reachable if there is no
        // UserRepository.  If it happens, we'd better get aware and have a look.
        throw new IllegalStateException("No user repository");
      }
      return capConnection.getUserRepository().getUser(IdHelper.formatUserId(developerId));
    }
  }


  // --- caching ----------------------------------------------------

  private class TemplateLocationsCacheKey extends PairCacheKey<Content, User, List<String>> {
    TemplateLocationsCacheKey(Content theme, User developer) {
      super(theme, developer);
    }

    @Override
    public List<String> evaluate(Cache cache, Content theme, User developer) {
      return templateLocationsUncached(theme, developer);
    }
  }

  private class ViewRepositoryNamesCacheKey extends PairCacheKey<Content, User, List<String>> {
    ViewRepositoryNamesCacheKey(Content theme, User developer) {
      super(theme, developer);
    }

    @Override
    public List<String> evaluate(Cache cache, Content theme, User developer) {
      return viewRepositoryNamesUncached(theme, developer);
    }
  }
}
