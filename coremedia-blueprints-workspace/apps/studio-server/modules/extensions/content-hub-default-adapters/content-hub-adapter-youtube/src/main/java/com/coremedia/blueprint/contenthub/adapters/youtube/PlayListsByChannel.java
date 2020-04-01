package com.coremedia.blueprint.contenthub.adapters.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PlayListsByChannel {
  private final YouTube youTube;
  private final String channelId;

  PlayListsByChannel(YouTube youTube, String channelId) {
    this.youTube = youTube;
    this.channelId = channelId;
  }

  List<Playlist> playlistsByChannel() throws IOException {
    List<Playlist> playlists = new ArrayList<>();
    PlayLists.fetchPlayListResponse(youTube, playlists, channelId);
    return playlists;
  }
}
