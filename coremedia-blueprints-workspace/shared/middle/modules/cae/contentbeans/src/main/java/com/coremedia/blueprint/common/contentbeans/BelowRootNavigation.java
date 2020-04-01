package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.blueprint.common.navigation.Navigation;

import java.util.Collection;

/**
 * Beans which have root navigations.
 */
public interface BelowRootNavigation {

  /**
   * Returns a collection with root navigations.
   *
   * @return root navigations
   */
  Collection<? extends Navigation> getRootNavigations();

}
