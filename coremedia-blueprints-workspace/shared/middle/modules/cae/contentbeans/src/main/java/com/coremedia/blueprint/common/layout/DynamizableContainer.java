package com.coremedia.blueprint.common.layout;

import com.coremedia.blueprint.common.contentbeans.CMTeasable;

/**
 * Container that possibly contains dynamic items. Useful to render a dynamic include on the container level.
 */
public interface DynamizableContainer extends Container<CMTeasable> {

  /**
   * Is this a dynamic container? Default implementation returns <code>true</code>.
   * @return true if this is a dynamic container
   */
  default boolean isDynamic() {
    return true;
  }

}
