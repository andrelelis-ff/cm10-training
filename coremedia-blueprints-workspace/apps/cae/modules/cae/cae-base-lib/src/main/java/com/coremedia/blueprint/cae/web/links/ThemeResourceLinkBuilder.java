package com.coremedia.blueprint.cae.web.links;

import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMTheme;
import com.coremedia.blueprint.common.services.context.CurrentContextService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.user.User;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This link handler enables frontend developers to create links to theme resources from within templates in the
 * frontend workspace by means of their paths in the repository.
 * <p>The developer is not allowed to access resources outside of the theme directory. Consequently using relative path
 * segments is not allowed. The given absolute path is interpreted relative to the root directory of the theme which
 * also is the root directory of any references brick.</p>
 * <p><strong>The actual link building only works for links that do not require a "view" parameter. This link handler
 * only delegates to the actual link handlers defined for the type of resource located at the given path.</strong></p>
 */
public class ThemeResourceLinkBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(ThemeResourceLinkBuilder.class);

  private  boolean useLocalResources = false;

  private LinkFormatter linkFormatter;
  private CurrentContextService contextService;
  private ContentRepository repository;
  private ContentBeanFactory contentBeanFactory;
  private DataViewFactory dataViewFactory;

  public void setUseLocalResources(boolean useLocalResources) {
    this.useLocalResources = useLocalResources;
  }

  @Required
  public void setLinkFormatter(LinkFormatter linkFormatter) {
    this.linkFormatter = linkFormatter;
  }

  @Required
  public void setContextService(CurrentContextService contextService) {
    this.contextService = contextService;
  }

  @Required
  public void setRepository(ContentRepository repository) {
    this.repository = repository;
  }

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  @Required
  public void setDataViewFactory(DataViewFactory dataViewFactory) {
    this.dataViewFactory = dataViewFactory;
  }

  /**
   * Determines the URL that belongs to a theme resource (image, webfont, etc.) defined by its path within the
   * theme folder. The path is relative to the root directory of theme. The path must not contain any <strong>..</strong>
   * descending path segments.
   *
   * @param path path to the resource within the theme folder
   * @param request current request (required by the linkFormatter)
   * @param response  current response (required by the linkFormatter)
   * @return the URL path that belongs to a theme resource or an empty link
   */
  public String getLinkToThemeResource(String path, @NonNull HttpServletRequest request,
                                       @NonNull HttpServletResponse response) {
    // sanitize static URL
    if (path == null || path.startsWith("../") || path.contains("/../")) {
      LOG.error("Path to theme resource has to be absolute but is \"{}\".", path);
      throw new IllegalArgumentException("Path to theme resource has to be absolute but is \"" + path + "\".");
    }

    String normalizedPath = path.startsWith("/") ? path : "/" + path;

    CMTheme theme = getThemeFromCurrentContext(request);
    if (theme == null) {
      return StringUtils.EMPTY;
    }

    Content themeFolder = getThemeFolder(theme);
    if (themeFolder == null) {
      LOG.error("Theme is either deleted or theme folder does not match conventions: {}. Returning empty link.", theme.getContent().getPath());
      return StringUtils.EMPTY;
    }

    String pathToThemeResource;
    if (useLocalResources) { // this path in only relevant for development with a local CAE
      String themeName = getThemeName(themeFolder);
      pathToThemeResource = request.getContextPath() + "/themes/" + themeName + normalizedPath;
      LOG.debug("Created link to local theme resource: {}", pathToThemeResource);
      return pathToThemeResource;
    }

    pathToThemeResource = themeFolder.getPath() + normalizedPath;
    ContentBean themeResource = getThemeResourceAt(pathToThemeResource);
    if (themeResource == null) {
      return StringUtils.EMPTY;
    }

    LOG.debug("Rendering link to theme resource at repository path\"{}\"", pathToThemeResource);
    return linkFormatter.formatLink(themeResource, null, request, response, false);

  }

  @VisibleForTesting
  @NonNull
  String getLocalResourcePath(@NonNull HttpServletRequest request, String themeName, String path) {
    return request.getContextPath() + "/themes/" + themeName + path;
  }

  @VisibleForTesting
  @Nullable
  Content getThemeFolder(@NonNull CMTheme theme) {
    // the folder of the theme document is the only content that still contains the original name of the theme
    Content themeContent = theme.getContent();
    Content themeFolder = themeContent.getParent();
    if (themeFolder != null // the folder is only null of the theme was the root folder (not possible) or if the theme is deleted
            && themeContent.getName().toLowerCase().startsWith(getThemeName(themeFolder).toLowerCase())) { // Make sure that the theme document was not renamed or moved to some other folder. this would break the theme chooser as well but just to make sure...
      return themeFolder;
    }
    return null;
  }

  @VisibleForTesting
  @Nullable
  CMTheme getThemeFromCurrentContext(@NonNull HttpServletRequest request) {
    CMContext context = contextService.getContext();
    if (context == null) {
      LOG.error("Cannot create link to theme resource without a navigation context. Returning empty link.");
      return null;
    }

    CMTheme theme = context.getTheme(getDeveloperUser(request));
    if (theme == null) {
      LOG.error("Cannot create URL to theme resource because there is no theme defined for context {}", context.getContentId());
      return null;
    }
    return theme;
  }

  @VisibleForTesting
  @Nullable
  ContentBean getThemeResourceAt(@NonNull String pathToThemeResource) {
    Content themeResourceContent = repository.getChild(pathToThemeResource);
    if (themeResourceContent != null) {
      ContentBean themeResourceBean = contentBeanFactory.createBeanFor(themeResourceContent, ContentBean.class);
      return dataViewFactory.loadCached(themeResourceBean, null);
    }
    LOG.error("Could not find theme resource at \"{}\". Neither the theme document nor other resource in the content repository may be moved to a different location.", pathToThemeResource);
    return null;
  }

  @VisibleForTesting
  @Nullable
  User getDeveloperUser(@NonNull HttpServletRequest request) {
    return UserVariantHelper.getUser(request);
  }

  /**
   * Derives the theme name from the location of the theme document in the content repository.
   */
  @NonNull
  private String getThemeName(@NonNull Content themeFolder) {
    // the folder of the theme document is the only content that still contains the original name of the theme
    return themeFolder.getName();
  }

}
