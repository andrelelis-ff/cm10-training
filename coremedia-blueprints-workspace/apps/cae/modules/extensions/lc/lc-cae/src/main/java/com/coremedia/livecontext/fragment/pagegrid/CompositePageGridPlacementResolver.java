package com.coremedia.livecontext.fragment.pagegrid;

import com.coremedia.blueprint.common.layout.HasPageGrid;
import com.coremedia.blueprint.common.layout.PageGridPlacement;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * A pagegrid resolver that will delegate to its list of resolvers.
 */
public class CompositePageGridPlacementResolver implements PageGridPlacementResolver {

  private DataViewFactory dataViewFactory;
  private List<PageGridPlacementResolver> resolvers;

  @Required
  public void setDataViewFactory(DataViewFactory dataViewFactory) {
    this.dataViewFactory = dataViewFactory;
  }

  @Required
  public void setResolvers(List<PageGridPlacementResolver> resolvers) {
    this.resolvers = resolvers;
  }

  @Nullable
  @Override
  public PageGridPlacement resolvePageGridPlacement(@NonNull HasPageGrid context, @NonNull String placementName) {
    for (PageGridPlacementResolver resolver: resolvers) {
      PageGridPlacement result = resolver.resolvePageGridPlacement(context, placementName);
      if (result != null) {
        // Wrap in data view (CMS-2545)
        result = dataViewFactory.loadCached(result, null);
        return result;
      }
    }
    return null;
  }
}
