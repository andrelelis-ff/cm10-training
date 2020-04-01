package com.coremedia.blueprint.common.feeds;

import java.util.List;

/**
 * Interface for all classes that want to be used for RSS feed
 */
public interface FeedSource<T> {

  /**
   * Returns the items to include in this feed.
   *
   * @return the items to include in this feed
   */
  List<? extends T> getFeedItems();

  /**
   * Returns the items retrieved by #getItems but flattens the result list.
   *
   * @return the items retrieved by #getItems but flattens the result list.
   */
  List<? extends T> getItemsFlattened();

  /**
   * Returns the format of the feed.
   *
   * @return the format of the feed
   */
  FeedFormat getFeedFormat();

  /**
   * Returns the title of the feed.
   *
   * @return the title of the feed
   */
  String getFeedTitle();

  /**
   * Returns the description for the feed.
   *
   * @return the description for the feed
   */
  String getFeedDescription();

}