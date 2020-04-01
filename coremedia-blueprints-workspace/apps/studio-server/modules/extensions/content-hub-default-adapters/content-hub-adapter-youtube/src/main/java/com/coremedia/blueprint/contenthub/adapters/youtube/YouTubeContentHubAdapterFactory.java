package com.coremedia.blueprint.contenthub.adapters.youtube;

import com.coremedia.contenthub.api.ContentHubAdapter;
import com.coremedia.contenthub.api.ContentHubAdapterFactory;
import edu.umd.cs.findbugs.annotations.NonNull;

class YouTubeContentHubAdapterFactory implements ContentHubAdapterFactory<YouTubeContentHubSettings> {

  @Override
  @NonNull
  public String getId() {
    return "youtube";
  }

  @Override
  @NonNull
  public ContentHubAdapter createAdapter(@NonNull YouTubeContentHubSettings settings,
                                         @NonNull String connectionID){
    return new YouTubeContentHubAdapter(settings, connectionID);
  }
}
