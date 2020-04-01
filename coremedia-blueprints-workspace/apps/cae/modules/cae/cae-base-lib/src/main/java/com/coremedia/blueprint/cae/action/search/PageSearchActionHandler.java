package com.coremedia.blueprint.cae.action.search;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.handlers.PageHandlerBase;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.searchsuggestion.Suggestion;
import com.coremedia.blueprint.cae.searchsuggestion.Suggestions;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.view.substitution.Substitution;
import com.coremedia.objectserver.view.substitution.SubstitutionRegistry;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.VIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ROOT;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;
import static com.coremedia.objectserver.web.HandlerHelper.notFound;
import static java.util.Arrays.asList;

/**
 * Handler responsible for rendering search links and handling search requests
 */
@RequestMapping
@Link
public class PageSearchActionHandler extends PageHandlerBase {

  static final String ACTION_NAME = "search";
  static final String ACTION_ID = "search";
  static final String PARAMETER_ROOT_NAVIGATION_ID = "rootNavigationId";
  static final String PARAMETER_QUERY = "query";
  static final String CONTENT_TYPE_JSON = "application/json";

  //used for filtering doctypes when search is executed
  static final String DOCTYPE_SELECT = "searchDoctypeSelect";
  static final String TOPICS_DOCTYPE_SELECT = "search.topicsdoctypeselect";
  /**
   * setting with list of index field names with IDs of taxonomy documents of types in {@link #TOPICS_DOCTYPE_SELECT}
   */
  static final String TOPICS_INDEX_FIELDS = "search.topicsindexfields";

  static final int DEFAULT_MINIMAL_SEARCH_QUERY_LENGTH = 3;

  /**
   * e.g. /service/media/2236/search
   */
  private static final String URI_PATTERN =
          '/' + PREFIX_SERVICE +
                  "/"+ACTION_NAME +
                  "/{" + SEGMENT_ROOT + "}" +
                  "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}";
  private static final String SEARCH_CHANNEL_SETTING = "searchChannel";

  private SearchService searchService;
  private SettingsService settingsService;
  private int minimalSearchQueryLength = DEFAULT_MINIMAL_SEARCH_QUERY_LENGTH;

  @Required
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Substitution(ACTION_ID)
  @SuppressWarnings("unused")
  public SearchActionState createActionState(CMAction representative, HttpServletRequest request) {
    return new SearchActionState(representative, minimalSearchQueryLength);
  }

  /**
   * Performs site search
   *
   * @see "SearchActionState.ftl"
   */
  @GetMapping(value = URI_PATTERN)
  public ModelAndView handleSearchAction(@PathVariable(SEGMENT_ID) CMAction action,
                                   @PathVariable(SEGMENT_ROOT) String context,
                                   @ModelAttribute() SearchFormBean searchForm,
                                   @RequestParam(value = VIEW_PARAMETER, required = false) String view,
                                   HttpServletRequest request) {
    Navigation navigation = getValidNavigation(action, context, ACTION_NAME);
    if (navigation != null) {
      CMChannel searchChannel = settingsService.setting(SEARCH_CHANNEL_SETTING, CMChannel.class, navigation);
      Page searchResultsPage = asPage(searchChannel, searchChannel, UserVariantHelper.getUser(request));
      //context is always accessed via the page request, so we have to apply it to the
      request.setAttribute(ContextHelper.ATTR_NAME_PAGE, searchResultsPage);

      ModelAndView result;
      SearchActionState actionBean;

      // only search if query is long enough
      if (searchForm.getQuery() != null && searchForm.getQuery().length() >= minimalSearchQueryLength) {
        //regular search result filtered by doctypes given in the Search Settings document
        Collection<String> docTypes = settingsService.settingAsList(DOCTYPE_SELECT, String.class, navigation);
        // fallback to old setting "search.doctypeselect"
        if(docTypes.isEmpty()) {
          docTypes = settingsService.settingAsList("search.doctypeselect", String.class, navigation);
        }
        SearchResultBean searchResult = searchService.search(searchResultsPage, searchForm, docTypes);

        //topics search result filtered by topics doctypes given in the Search Settings document
        Collection<String> topicDocTypes = settingsService.settingAsList(TOPICS_DOCTYPE_SELECT, String.class, navigation);
        Collection<String> topicIndexFields = settingsService.settingAsList(TOPICS_INDEX_FIELDS, String.class, navigation);
        SearchResultBean searchResultTopics = searchService.searchTopics(navigation, searchForm, topicDocTypes, topicIndexFields);

        actionBean = new SearchActionState(action, searchForm, minimalSearchQueryLength, searchResult, searchResultTopics);
      } else if (searchForm.getQuery() == null) {
        actionBean = new SearchActionState(action, searchForm, minimalSearchQueryLength, null, null);
      } else {
        // if no search was executed, write error into SearchActionState.
        actionBean = new SearchActionState(action, searchForm, minimalSearchQueryLength, SearchActionState.ERROR_QUERY_TOO_SHORT);
      }

      // build model with view for results, otherwise the whole search result page
      if (view != null && !view.isEmpty()) {
        result = HandlerHelper.createModelWithView(actionBean, view);
      } else {
        result = HandlerHelper.createModel(searchResultsPage);
        SubstitutionRegistry.register(ACTION_ID, actionBean, result);
      }

      addPageModel(result, searchResultsPage);
      return result;

    }
    return notFound();
  }

  /**
   * Performs suggestion search and provides a JSON object containing the suggestions.
   *
   */
  @ResponseBody
  @GetMapping(value = URI_PATTERN, params = {PARAMETER_ROOT_NAVIGATION_ID, PARAMETER_QUERY}, produces = CONTENT_TYPE_JSON)
  public List<Suggestion> handleSearchSuggestionAction(@PathVariable(SEGMENT_ID) CMAction action,
                                               @PathVariable(SEGMENT_ROOT) String context,
                                               @RequestParam(value = PARAMETER_ROOT_NAVIGATION_ID) String rootNavigationId,
                                               @RequestParam(value = PARAMETER_QUERY) String term) {
    Navigation navigation = getValidNavigation(action, context, ACTION_NAME);
     if (navigation != null) {
       //regular search result filtered by doctypes given in the Search Settings document
       Collection<String> docTypes = settingsService.settingAsList(DOCTYPE_SELECT, String.class, navigation);
       // fallback to old setting "search.doctypeselect"
       if(docTypes.isEmpty()) {
         docTypes = settingsService.settingAsList("search.doctypeselect", String.class, navigation);
       }
       Suggestions suggestions = searchService.getAutocompleteSuggestions(rootNavigationId, term, docTypes);

      return suggestions.delegate();
    }
    throw new IllegalArgumentException("Could not resolve navigation for content "+action.getContent().getId());
  }


  @Link(type = SearchActionState.class, uri = URI_PATTERN)
  public UriComponents buildSearchActionLink(SearchActionState action, UriTemplate uriTemplate, Map<String, Object> linkParameters, HttpServletRequest request) {
    Page page = (Page) linkParameters.get("page");
    if (page == null) {
      throw new IllegalArgumentException("Missing 'page' parameter when building link for "+action);
    }
    UriComponentsBuilder uri = UriComponentsBuilder.fromPath(uriTemplate.toString());
    UriComponentsBuilder result = addLinkParametersAsQueryParameters(uri, linkParameters);
    return result.buildAndExpand(ImmutableMap.of(
            SEGMENT_ID, getId(action.getAction()),
            SEGMENT_ROOT, getPathSegments(page.getNavigation()).get(0)));
  }


  // ==============

  /**
   * Provides the CMNavigation that belongs to the addressed bean
   * @param bean The action bean
   * @param rootSegmentName The name of the root segment
   * @param actionName The action name
   * @return The navigation or null, if the bean and/or the segment name are invalid. This means that it is
   *  an invalid request
   */
  protected Navigation getValidNavigation(ContentBean bean, String rootSegmentName, String actionName) {
    if( !(bean instanceof CMAction) ) {
      // not an action
      return null;
    }
    else if( !actionName.equals(((CMAction) bean).getId())) {
      // the action name is invalid
      return null;
    }

    return getNavigation(asList(rootSegmentName));
  }

  public void setMinimalSearchQueryLength(int minimalSearchQueryLength) {
    this.minimalSearchQueryLength = minimalSearchQueryLength;
  }

}
