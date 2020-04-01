package com.coremedia.blueprint.ecommerce.segments;

import com.coremedia.blueprint.segments.CMLinkableSegmentStrategy;
import com.coremedia.cap.content.Content;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * ContentSegmentStrategy for CMProduct
 */
public class CMProductSegmentStrategy extends CMLinkableSegmentStrategy {
  private static final String PRODUCT_NAME = "productName";

  /**
   * Use segment, productName, title or id as segment.
   */
  @NonNull
  @Override
  public String segment(@NonNull Content content) {
    return getSomeString(content, true, SEGMENT, PRODUCT_NAME, TITLE);
  }
}
