package com.coremedia.blueprint.contenthub.adapters.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PlayListItems {
  private static final Logger LOG = LoggerFactory.getLogger(PlayListItems.class);
  private static final String REQUEST_TYPE_SNIPPET = "snippet";

  private final YouTube youTube;
  private final String playlistId;

  PlayListItems(YouTube youTube, String playlistId) {
    this.youTube = youTube;
    this.playlistId = playlistId;
  }

  List<PlaylistItem> playlistItems() throws IOException {
    List<PlaylistItem> result = new ArrayList<>();
    fetchPlayListItems(result, null);
    return result;
  }

  private void fetchPlayListItems(List<PlaylistItem> items, @Nullable String token) throws IOException {
    LOG.debug("YouTube.PlaylistItems id: {}{}", playlistId, (token==null ? "" : " (paging)"));
    YouTube.PlaylistItems.List list = youTube.playlistItems()
            .list(REQUEST_TYPE_SNIPPET)
            .setMaxResults(YouTubeConnector.MAX_RESULTS)
            .setPlaylistId(playlistId);

    if (token != null) {
      list.setPageToken(token);
    }

    PlaylistItemListResponse response = list.execute();
    List<PlaylistItem> responseItems= response.getItems();
    if (responseItems != null) {
      items.addAll(responseItems);
    }

    String nextPageToken = response.getNextPageToken();
    if(nextPageToken != null) {
      fetchPlayListItems(items, nextPageToken);
    }
  }
}
