package com.coremedia.blueprint.contenthub.adapters.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

class PlayLists {
  private static final Logger LOG = LoggerFactory.getLogger(PlayLists.class);
  private static final String REQUEST_TYPE_SNIPPET = "snippet";

  static void fetchPlayListResponse(YouTube youTube, List<Playlist> playlists, String channelId) throws IOException {
    recFetchPlayListResponse(youTube, playlists, channelId, null);
  }

  private static void recFetchPlayListResponse(YouTube youTube, List<Playlist> playlists, String channelId, String token) throws IOException {
    LOG.debug("YouTube.Playlists channel: {}{}", channelId, (token==null ? "" : " (paging)"));
    YouTube.Playlists.List list = youTube.playlists()
            .list(REQUEST_TYPE_SNIPPET)
            .setMaxResults(YouTubeConnector.MAX_RESULTS)
            .setChannelId(channelId);

    if(token != null) {
      list.setPageToken(token);
    }

    PlaylistListResponse response = list.execute();
    List<Playlist> items = response.getItems();
    if(items != null) {
      playlists.addAll(items);
    }

    String nextPageToken = response.getNextPageToken();
    if(nextPageToken != null) {
      recFetchPlayListResponse(youTube, playlists, channelId, nextPageToken);
    }
  }
}
