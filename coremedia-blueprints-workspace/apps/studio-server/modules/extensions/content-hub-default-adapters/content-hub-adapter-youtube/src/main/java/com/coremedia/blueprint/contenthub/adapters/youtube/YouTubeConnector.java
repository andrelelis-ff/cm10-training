package com.coremedia.blueprint.contenthub.adapters.youtube;

import com.coremedia.contenthub.api.exception.ContentHubException;
import com.coremedia.contenthub.api.search.Sort;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * The YouTubeConnector is responsible for the connection to youtube.
 */
class YouTubeConnector {

  /**
   * Response paging chunk size
   * <p>
   * Do not confuse with {@link ChannelSearchResult#MAX_LIMIT}, which is the
   * overall limit of search result sizes.
   * <p>
   *
   * @implNote 50 is the max value.  Don't know why we should fetch less and do
   * more roundtrips instead.  However, default is suspiciously low 5.
   * So maybe I understood something wrong, and 50 is not appropriate.
   * S. https://developers.google.com/youtube/v3/docs/search/list # Optional parameters # maxResults
   */
  static final long MAX_RESULTS = 50L;

  private YouTube youTube;

  YouTubeConnector(YouTube youTube) {
    this.youTube = youTube;
  }

  List<Playlist> getPlaylistsByUser(String user) {
    try {
      return new PlayListsByUser(youTube, user).playlists();
    } catch (GoogleJsonResponseException ge) {
      throw processGoogleJsonResponseException("Cannot fetch playlists by user " + user, ge);
    } catch (Exception e) {
      throw new IllegalStateException("Cannot fetch playlists by user " + user, e);
    }
  }

  Playlist getPlayListById(String id) {
    try {
      return new PlayListById(youTube, id).playlist();
    } catch (GoogleJsonResponseException ge) {
      throw processGoogleJsonResponseException("Cannot fetch playlists by id: " + id, ge);
    } catch (Exception e) {
      throw new IllegalStateException("Cannot fetch playlists by id: " + id, e);
    }
  }

  List<Playlist> getPlaylistsByChannel(String channelId) {
    try {
      return new PlayListsByChannel(youTube, channelId).playlistsByChannel();
    } catch (GoogleJsonResponseException ge) {
      throw processGoogleJsonResponseException("Cannot fetch playlists by channel id " + channelId, ge);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot fetch playlists by channel id " + channelId, e);
    }
  }

  Video getVideo(String videoId) {
    try {
      return new VideoById(youTube, videoId).video();
    } catch (GoogleJsonResponseException ge) {
      throw processGoogleJsonResponseException("Cannot fetch video by id " + videoId, ge);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot fetch video by id " + videoId, e);
    }
  }

  @NonNull
  List<SearchResult> getVideos(String channelId) {
    return searchVideos(channelId, null, Collections.emptyList(), -1);
  }

  List<PlaylistItem> getPlaylistItems(String playlistId) {
    try {
      return new PlayListItems(youTube, playlistId).playlistItems();
    } catch (GoogleJsonResponseException ge) {
      throw processGoogleJsonResponseException("Cannot fetch playlist items by id " + playlistId, ge);
    } catch (Exception e) {
      throw new IllegalStateException("Cannot fetch playlist items by id " + playlistId, e);
    }
  }

  @NonNull
  List<SearchResult> searchVideos(@NonNull String channelId, @Nullable String term, @NonNull List<Sort> sortCriteria, int limit) {
    try {
      return new ChannelSearchResult(youTube, channelId, term, limit, sortCriteria).channelSearchResult();
    } catch (GoogleJsonResponseException ge) {
      throw processGoogleJsonResponseException("Cannot fetch videos by channel id " + channelId, ge);
    } catch (Exception e) {
      throw new IllegalStateException("Cannot fetch videos by channel id " + channelId, e);
    }
  }

  private RuntimeException processGoogleJsonResponseException(@NonNull String errorMessage, GoogleJsonResponseException ge) {
    boolean usageLimits = ge.getDetails().getErrors().stream()
            .anyMatch(error -> "dailyLimitExceeded".equals(error.getReason()));

    if (usageLimits) {
      return new ContentHubException("Daily limit exceeded", ge, YouTubeErrorCode.USAGE_LIMIT_EXCEEDED, List.of(ge.getDetails().getMessage()));
    }

    boolean quotaExceeded = ge.getDetails().getErrors().stream()
            .anyMatch(error -> "quotaExceeded".equals(error.getReason()));
    if (quotaExceeded) {
      return new ContentHubException("Quota points exceeded", ge, YouTubeErrorCode.QUOTA_POINTS_EXCEEDED);
    } else {
      return new IllegalStateException(errorMessage, ge);
    }
  }
}
