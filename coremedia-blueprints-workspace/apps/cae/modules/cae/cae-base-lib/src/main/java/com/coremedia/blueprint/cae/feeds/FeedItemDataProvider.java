
package com.coremedia.blueprint.cae.feeds;

import com.rometools.rome.feed.synd.SyndEntry;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base interface for FeedItemDataProvider
 */
public interface FeedItemDataProvider {

  /**
   * Determines if the provider is suitable for the given content bean.
   *
   * @param item the content bean to check for
   * @return true if the provider is suitable, false if not
   */
  boolean isSupported(Object item);

  /**
   * Creates a ROME SyndEntry for the given linkable Object.
   *
   * @param request  the HTTP request of the user
   * @param response the HTTP response of the user
   * @param bean     the linkable to generate the SyndEntry for
   * @return the generated SyndEntry itselves
   */
  @NonNull
  SyndEntry getSyndEntry(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                         @NonNull Object bean);
}
