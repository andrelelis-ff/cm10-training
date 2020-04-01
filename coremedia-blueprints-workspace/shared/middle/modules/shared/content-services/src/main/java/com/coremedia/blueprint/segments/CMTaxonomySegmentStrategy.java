package com.coremedia.blueprint.segments;

import com.coremedia.blueprint.base.links.ContentSegmentStrategy;
import com.coremedia.cap.content.Content;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * ContentSegmentStrategy for CMTaxonomy
 */
public class CMTaxonomySegmentStrategy implements ContentSegmentStrategy {
  private static final String VALUE = "value";

  /**
   * Returns the taxonomy's value.
   */
  @Override
  @NonNull
  public String segment(@NonNull Content content) {
    return content.getString(VALUE);
  }
}
