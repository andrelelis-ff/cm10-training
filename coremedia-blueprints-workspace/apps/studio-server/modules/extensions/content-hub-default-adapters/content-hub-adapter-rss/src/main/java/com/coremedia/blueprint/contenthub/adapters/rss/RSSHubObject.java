package com.coremedia.blueprint.contenthub.adapters.rss;


import com.coremedia.contenthub.api.ContentHubObject;
import com.coremedia.contenthub.api.ContentHubObjectId;
import com.rometools.rome.feed.synd.SyndFeed;
import edu.umd.cs.findbugs.annotations.NonNull;

abstract class RSSHubObject implements ContentHubObject {

  private ContentHubObjectId hubId;
  private SyndFeed feed;
  private String name;

  RSSHubObject(ContentHubObjectId hubId, SyndFeed feed) {
    this.hubId = hubId;
    this.name = feed.getTitle();
    this.feed = feed;
  }

  @NonNull
  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @NonNull
  @Override
  public String getDisplayName() {
    return getName();
  }

  @NonNull
  @Override
  public ContentHubObjectId getId() {
    return hubId;
  }

  SyndFeed getFeed() {
    return feed;
  }
}
