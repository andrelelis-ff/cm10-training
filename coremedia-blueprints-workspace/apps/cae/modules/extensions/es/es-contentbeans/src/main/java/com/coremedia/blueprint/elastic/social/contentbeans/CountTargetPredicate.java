package com.coremedia.blueprint.elastic.social.contentbeans;

import com.google.common.base.Predicate;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Used to filter {@link com.coremedia.blueprint.elastic.social.contentbeans.Count#getTarget()} in a ESDynamicList.
 *
 * @param <T> The target type the Predicate applies to
 */
public interface CountTargetPredicate<T> extends Predicate<T> {

  /**
   * The source type as class object so that {@link com.coremedia.blueprint.elastic.social.contentbeans.ESDynamicListImpl}
   * can check if a predicate can be called with a given target.
   * @return the source type as class object
   */
  @NonNull
  Class<T> getType();
}
