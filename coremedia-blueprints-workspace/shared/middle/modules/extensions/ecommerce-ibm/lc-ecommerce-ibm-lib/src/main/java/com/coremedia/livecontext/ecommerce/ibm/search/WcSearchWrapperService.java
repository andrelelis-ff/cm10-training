package com.coremedia.livecontext.ecommerce.ibm.search;

import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractWcWrapperService;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestServiceMethod;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.http.HttpMethod;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper.getStoreId;
import static java.util.Arrays.asList;

/**
 * Wrapper query and result format of the IBM rest search service.
 */
public class WcSearchWrapperService extends AbstractWcWrapperService {

  private static final WcRestServiceMethod<WcSuggestionViews, Void> GET_KEYWORD_SUGGESTIONS = WcRestServiceMethod
          .builderForSearch(HttpMethod.GET, "store/{storeId}/sitecontent/keywordSuggestionsByTerm/{term}",
                  WcSuggestionViews.class)
          .previewSupport(true)
          .build();

  @NonNull
  public List<WcSuggestion> getKeywordSuggestionsByTerm(String term, @NonNull StoreContext storeContext) {
    try {
      String storeId = getStoreId(storeContext);

      List<String> variableValues = asList(storeId, term);

      Map<String, String[]> parametersMap = buildParameterMap()
              .withCurrency(storeContext)
              .withLanguageId(storeContext)
              .build();

      storeContext.getCatalogId()
              .ifPresent(catalogId -> parametersMap.put("catalogId", new String[]{catalogId.value()}));

      Optional<WcSuggestionViews> suggestionViews = getRestConnector()
              .callService(GET_KEYWORD_SUGGESTIONS, variableValues, parametersMap, null, storeContext, null);

      return suggestionViews
              .map(WcSuggestionViews::getSuggestionView)
              .flatMap(WcSearchWrapperService::getSuggestions)
              .orElseGet(Collections::emptyList);
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }

  @NonNull
  private static Optional<List<WcSuggestion>> getSuggestions(List<WcSuggestionView> suggestionViews) {
    return suggestionViews.stream()
            .findFirst()
            .map(WcSuggestionView::getEntry);
  }
}
