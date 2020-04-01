package com.coremedia.blueprint.cae.view.resolver;

import com.coremedia.dispatch.Type;
import com.coremedia.objectserver.view.View;
import com.coremedia.objectserver.view.ViewRepository;
import com.coremedia.objectserver.view.resolver.RepositoriesFirstViewLookupTraversal;

import java.util.List;

/**
 * A view lookup strategy that takes in account the viewtype and walks through all repositories first before walking through the types hierarchy.
 */
public class BlueprintViewLookupTraversal extends RepositoriesFirstViewLookupTraversal {

  private static final String VIEWTYPE_START_CHARACTER = "[";

  @Override
  public View lookup(List<ViewRepository> repositories, Type type, String viewName) {

    View returnView;

    //make view lookup regardless whether viewName contains a viewtype or not
    returnView = super.lookup(repositories,type,viewName);

    //no view found. If viewName contains viewtype (e.g. the view would be CMAction.[search] or CMAction.asHeaderItem[search]), make view lookup again without one.
    if(returnView == null && viewName != null && viewName.contains(VIEWTYPE_START_CHARACTER)) {

        //Remove viewtype.
        String viewNameWithoutViewType = viewName.substring(0, viewName.indexOf(VIEWTYPE_START_CHARACTER));

        if(viewNameWithoutViewType.isEmpty()) {
          //viewName consisted only of a viewtype (e.g. [search]), there was no viewName (e.g. asHeaderItem[search]).
          //The ViewRepository expects null instead of an empty string if there is no viewName.
          viewNameWithoutViewType = null;
        }
        returnView = super.lookup(repositories,type,viewNameWithoutViewType);
    }

    return returnView;

  }

}
