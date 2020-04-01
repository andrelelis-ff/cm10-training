package com.coremedia.blueprint.contenthub.adapters.rss;


import com.coremedia.contenthub.api.ContentHubObjectId;
import com.coremedia.contenthub.api.ContentHubType;
import com.coremedia.contenthub.api.Folder;
import com.rometools.rome.feed.synd.SyndFeed;
import edu.umd.cs.findbugs.annotations.NonNull;

class RSSFolder extends RSSHubObject implements Folder {

  RSSFolder(ContentHubObjectId id, SyndFeed feed, String name) {
    super(id, feed);
    setName(name);
  }

  @NonNull
  @Override
  public ContentHubType getContentHubType() {
    return new ContentHubType("feed");
  }
}
