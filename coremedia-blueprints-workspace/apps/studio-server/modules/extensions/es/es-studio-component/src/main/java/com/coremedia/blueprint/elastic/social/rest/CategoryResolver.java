package com.coremedia.blueprint.elastic.social.rest;

import com.coremedia.cap.content.Content;
import com.coremedia.elastic.social.rest.api.CategoryKeyAndDisplay;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Implementations of this interface convert a {@link com.coremedia.cap.content.Content}
 * to a {@link com.coremedia.elastic.social.rest.api.CategoryKeyAndDisplay}.
 * Return <code>null</code> if it cannot handle the given {@link com.coremedia.cap.content.Content}.
 */
public interface CategoryResolver {

  /**
   * Returns a {@link com.coremedia.elastic.social.rest.api.CategoryKeyAndDisplay} for the given {@link com.coremedia.cap.content.Content}
   * or <code>null</code> if it cannot handle the given {@link com.coremedia.cap.content.Content}.
   * @param content a {@link com.coremedia.cap.content.Content} to resolve
   * @return a {@link com.coremedia.elastic.social.rest.api.CategoryKeyAndDisplay} for the given {@link com.coremedia.cap.content.Content}
   * or <code>null</code> if it cannot handle the given {@link com.coremedia.cap.content.Content}
   */
  CategoryKeyAndDisplay resolve(@NonNull Content content);
}
