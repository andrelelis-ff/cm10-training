package com.coremedia.blueprint.common.layout;

import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.objectserver.beans.ContentBean;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * ContentBeans containing a PageGrid-Property need to implement this IF.
 * @cm.template.api
 */
public interface HasPageGrid extends ContentBean {
  /**
   * Returns the merged PageGrid coming from the current bean and regarding inheritance along
   * its navigation path.
   * @return the merged PageGrid
   */
  @NonNull
  PageGrid getPageGrid();


  /**
   * Return the navigation context regarding pagegrid inheritance.
   * @return the navigation context or null
   */
  @Nullable
  CMNavigation getContext();
}
