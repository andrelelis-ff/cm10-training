package com.coremedia.blueprint.cae.search;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Service to find an indexed document by context ID and segment.
 *
 * @since 7.5.13
 */
public interface SegmentResolver {

  /**
   * Searches for an indexed document with given context ID and segment.
   *
   * <p>The returned object was indexed with given context ID in field
   * {@link com.coremedia.blueprint.cae.search.SearchConstants.FIELDS#CONTEXTS} and given segment in field
   * {@link com.coremedia.blueprint.cae.search.SearchConstants.FIELDS#SEGMENT}.
   *
   * <p>This method assumes that there is only one result with given context ID, segment of the given resultClass.
   * If more objects match, only the first will be returned.
   *
   * @param contextId context ID
   * @param segment segment
   * @param resultClass result class
   * @param <T> type of result
   * @return result or null if none found
   */
  @Nullable
  <T> T resolveSegment(int contextId, @NonNull String segment, @NonNull Class<T> resultClass);

}
