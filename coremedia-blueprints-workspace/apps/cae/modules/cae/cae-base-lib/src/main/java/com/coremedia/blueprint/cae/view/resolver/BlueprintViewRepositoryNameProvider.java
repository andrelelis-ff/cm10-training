package com.coremedia.blueprint.cae.view.resolver;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMTheme;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.user.User;
import com.coremedia.objectserver.view.resolver.ViewRepositoryNameProvider;
import com.coremedia.objectserver.web.UserVariantHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provide names of {@link com.coremedia.objectserver.view.ViewRepository viewRepositories}. This implementation
 * provides a combined list of names from two sources: names explicitly configured on the instance of this class
 * (i.e. common view repositories that should be used for any view lookup) and names which are specific to the
 * current request.
 * <p/>
 * The request specific view repositories are retrieved from the setting {@link #VIEW_REPOSITORY_NAMES} (a list of
 * Strings ordered by decreasing priority) of the current context.
 */
public class BlueprintViewRepositoryNameProvider implements ViewRepositoryNameProvider {

  private static final Logger LOG = LoggerFactory.getLogger(BlueprintViewRepositoryNameProvider.class);

  /**
   * The list of view repositories does not change within the same request. We compute it only once and store
   * it as a request attribute for multiple usages.
   */
  private static final String VIEW_REPOSITORY_NAMES_ATTR = "com.coremedia.blueprint.viewrepositorynames";

  /**
   * Name of the struct element that is looked up in the root navigation
   */
  private static final String VIEW_REPOSITORY_NAMES = "viewRepositoryNames";

  /**
   * Common view repositories that are stored in a list
   */
  private List<String> commonViewRepositoryNames;

  private SettingsService settingsService;
  private ThemeTemplateViewRepositoryProvider themeTemplateViewRepositoryProvider;

  // --- ViewRepositoryNameProvider ---------------------------------

  @NonNull
  @Override
  public List<String> getViewRepositoryNames(String viewName, Map model, Locale locale, HttpServletRequest request) {
    @SuppressWarnings("unchecked")
    List<String> viewRepositoryNames = (List<String>) request.getAttribute(VIEW_REPOSITORY_NAMES_ATTR); //NOSONAR

    if (viewRepositoryNames == null) {
      viewRepositoryNames = doGetViewRepositoryNames(model, UserVariantHelper.getUser(request));
      request.setAttribute(VIEW_REPOSITORY_NAMES_ATTR, viewRepositoryNames);
    }

    return viewRepositoryNames;
  }

  // --- internal ---------------------------------------------------

  /**
   * Combines view repository names configured for the instance of this class with view repository names loaded from
   * the current context's setting {@link #VIEW_REPOSITORY_NAMES} giving the context's view repositories precedence
   * over the statically configured ones.
   *
   * @param model Used to determine the current context
   * @param developer Refer to the developer's work in progress templates
   * @return a list of view repositories ordered by decreasing priority
   */
  @NonNull
  private List<String> doGetViewRepositoryNames(@NonNull Map model, @Nullable User developer) {
    List<String> result = new ArrayList<>();

    // 1. From current request: get the view repositories configured on the current context
    Navigation navigation = NavigationLinkSupport.getNavigation(model);
    if (navigation != null) {

      // 1a. view repository names from theme, developer specific
      CMTheme theme = navigation.getTheme(developer);
      if (theme != null) {
        result.addAll(themeTemplateViewRepositoryProvider.viewRepositoryNames(theme.getContent(), developer));
      }

      // 1b. view repository names by setting
      List<? extends String> vrNames = settingsService.settingAsList(VIEW_REPOSITORY_NAMES, String.class, navigation);
      result.addAll(vrNames);
    }

    // 2. From configuration: get list of the basic configured repositories valid for all contexts
    result.addAll(commonViewRepositoryNames);
    LOG.debug("Found view repository names: {}", result);

    return result;
  }

  // --- configure --------------------------------------------------

  /**
   * @param commonViewRepositoryNames list of
   *                                  {@link #getViewRepositoryNames(String, java.util.Map, java.util.Locale, javax.servlet.http.HttpServletRequest)}.
   */
  public void setCommonViewRepositoryNames(List<String> commonViewRepositoryNames) {
    this.commonViewRepositoryNames = commonViewRepositoryNames;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setThemeTemplateViewRepositoryProvider(ThemeTemplateViewRepositoryProvider themeTemplateViewRepositoryProvider) {
    this.themeTemplateViewRepositoryProvider = themeTemplateViewRepositoryProvider;
  }
}
