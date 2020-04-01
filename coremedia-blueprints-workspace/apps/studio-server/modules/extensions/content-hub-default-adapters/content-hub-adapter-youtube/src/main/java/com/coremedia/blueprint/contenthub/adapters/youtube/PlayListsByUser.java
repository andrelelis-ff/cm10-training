package com.coremedia.blueprint.contenthub.adapters.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Playlist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PlayListsByUser {
  private static final Logger LOG = LoggerFactory.getLogger(PlayListsByUser.class);
  private static final String REQUEST_TYPE_BY_USER = "snippet,contentDetails";

  private final YouTube youTube;
  private final String user;

  PlayListsByUser(YouTube youTube, String user) {
    this.youTube = youTube;
    this.user = user;
  }

  List<Playlist> playlists() throws IOException {
    List<Playlist> playlists = new ArrayList<>();
    List<Channel> channels = new ArrayList<>();
    fetchChannelList(channels, user, null);
    for (Channel channel : channels) {
      PlayLists.fetchPlayListResponse(youTube, playlists, channel.getId());
    }
    return playlists;
  }

  private void fetchChannelList(List<Channel> channels, String user, String token) throws IOException {
    LOG.debug("YouTube.Channels user: {}{}", user, (token==null ? "" : " (paging)"));
    YouTube.Channels.List list = youTube.channels()
            .list(REQUEST_TYPE_BY_USER)
            .setMaxResults(YouTubeConnector.MAX_RESULTS)
            .setForUsername(user);

    if(token != null) {
      list.setPageToken(token);
    }

    ChannelListResponse response = list.execute();
    List<Channel> items = response.getItems();
    if(items != null) {
      channels.addAll(items);
    }

    String nextPageToken = response.getNextPageToken();
    if(nextPageToken != null) {
      fetchChannelList(channels, user, nextPageToken);
    }
  }
}
