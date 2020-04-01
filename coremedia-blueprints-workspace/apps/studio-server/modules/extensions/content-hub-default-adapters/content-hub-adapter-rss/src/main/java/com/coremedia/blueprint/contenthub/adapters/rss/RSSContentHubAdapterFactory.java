package com.coremedia.blueprint.contenthub.adapters.rss;

import com.coremedia.contenthub.api.ContentHubAdapter;
import com.coremedia.contenthub.api.ContentHubAdapterFactory;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 *
 */
class RSSContentHubAdapterFactory implements ContentHubAdapterFactory<RSSContentHubSettings> {

  @Override
  @NonNull
  public String getId() {
    return "rss";
  }

  @NonNull
  @Override
  public ContentHubAdapter createAdapter(@NonNull RSSContentHubSettings settings,
                                         @NonNull String connectionId) {
    return new RSSContentHubAdapter(settings, connectionId);
  }

}
