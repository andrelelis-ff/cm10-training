package com.coremedia.blueprint.taxonomies.semantic;

import com.coremedia.blueprint.taxonomies.TaxonomyUtil;
import com.coremedia.cap.content.Content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper class for the result of a suggestion query.
 */
public class Suggestions {

  private Map<String, Suggestion> suggestions = new HashMap<>();

  public void addSuggestion(Content content, float weight) {
    Suggestion suggestion = new Suggestion(content, weight);
    String id = TaxonomyUtil.getRestIdFromCapId(content.getId());
    suggestions.put(id, suggestion);
  }

  public List<Suggestion> asList(int max) {
    //finally sort and return the result list
    List<Suggestion> resultList = new ArrayList<>(suggestions.values());
    Collections.sort(resultList);


    //limit the amount of hits
    if (max > 0 && suggestions.size() > max) {
      resultList = resultList.subList(0, max);
    }
    return resultList;
  }

  public boolean contains(String id) {
    return suggestions.containsKey(id);
  }

  public int size() {
    return suggestions.size();
  }
}
