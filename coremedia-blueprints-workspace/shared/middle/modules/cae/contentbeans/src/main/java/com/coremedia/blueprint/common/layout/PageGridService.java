package com.coremedia.blueprint.common.layout;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface PageGridService {

  /**
   * Returns a PageGrid for the given bean and its context
   */
  @NonNull
  PageGrid getContentBackedPageGrid(HasPageGrid bean);

}
