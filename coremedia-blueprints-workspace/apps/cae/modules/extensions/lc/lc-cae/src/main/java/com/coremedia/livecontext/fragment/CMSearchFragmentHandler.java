package com.coremedia.livecontext.fragment;

import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.action.search.SearchActionState;
import com.coremedia.blueprint.cae.action.search.SearchFormBean;
import com.coremedia.blueprint.cae.action.search.SearchService;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;


public class CMSearchFragmentHandler extends FragmentHandler {

  //used for filtering doctypes when search is executed
  private static final String FILTER_DOCTYPES_SETTING_NAME = "searchDoctypeSelect";
  private static final int DEFAULT_MINIMAL_SEARCH_QUERY_LENGTH = 3;

  private static final String EXTERNAL_REF_FOR_SEARCH_RESULTS_FRAGMENT = "cm-search";
  private static final String SEARCH_TERM_REQUEST_PARAMETER = "searchTerm";

  static final String SEARCH_CHANNEL_SETTING = "searchChannel";
  static final String SEARCH_ACTION_SETTING = "searchAction";

  private SearchService searchService;

  private SettingsService settingsService;

  private int minimalSearchQueryLength = DEFAULT_MINIMAL_SEARCH_QUERY_LENGTH;

  @Nullable
  @Override
  ModelAndView createModelAndView(@NonNull FragmentParameters params, @NonNull HttpServletRequest request) {

    //pick up rootChannel of the site
    Site site = SiteHelper.getSiteFromRequest(request);
    if (site == null) {
      return HandlerHelper.notFound("Cannot derive a site from the request.");
    }

    Content siteRootDocument = site.getSiteRootDocument();
    CMChannel rootChannel = getContentBeanFactory().createBeanFor(siteRootDocument, CMChannel.class);
    if (rootChannel == null) {
      return HandlerHelper.notFound("Site " + site.getName() + " has no root channel");
    }

    //init action, navigation, page
    CMChannel searchChannel = settingsService.setting(SEARCH_CHANNEL_SETTING, CMChannel.class, rootChannel);
    CMAction action = settingsService.setting(SEARCH_ACTION_SETTING, CMAction.class, searchChannel);
    Page searchResultsPage = asPage(searchChannel, action, UserVariantHelper.getUser(request));

    //create searchForm
    SearchFormBean searchForm = new SearchFormBean();
    String searchTerm = request.getParameter(SEARCH_TERM_REQUEST_PARAMETER);
    searchForm.setQuery(searchTerm);

    //perform search
    Collection<String> docTypes = settingsService.settingAsList(FILTER_DOCTYPES_SETTING_NAME, String.class, searchChannel);
    SearchResultBean searchResult = searchService.search(searchResultsPage, searchForm, docTypes);

    //create SearchActionState to use it as model for model and view
    SearchActionState searchActionState = new SearchActionState(action, searchForm, minimalSearchQueryLength, searchResult, null);

    ModelAndView result = HandlerHelper.createModelWithView(searchActionState, params.getView());

    //add cmpage bean to model
    addPageModel(result, searchResultsPage);
    return result;
  }

  @Override
  public boolean include(@NonNull FragmentParameters params) {
    String externalRef = params.getExternalRef();
    return (externalRef != null && externalRef.equals(EXTERNAL_REF_FOR_SEARCH_RESULTS_FRAGMENT));
  }

  // ------------ Config --------------------------------------------
  @Required
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  public void setMinimalSearchQueryLength(int minimalSearchQueryLength) {
    this.minimalSearchQueryLength = minimalSearchQueryLength;
  }

}
