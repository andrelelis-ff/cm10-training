package com.coremedia.blueprint.contenthub.adapters.youtube;

import com.coremedia.contenthub.api.ContentHubAdapter;
import com.coremedia.contenthub.api.ContentHubContext;
import com.coremedia.contenthub.api.ContentHubObject;
import com.coremedia.contenthub.api.ContentHubObjectId;
import com.coremedia.contenthub.api.ContentHubTransformer;
import com.coremedia.contenthub.api.ContentHubType;
import com.coremedia.contenthub.api.Folder;
import com.coremedia.contenthub.api.Item;
import com.coremedia.contenthub.api.column.ColumnProvider;
import com.coremedia.contenthub.api.exception.ContentHubException;
import com.coremedia.contenthub.api.search.ContentHubSearchResult;
import com.coremedia.contenthub.api.search.ContentHubSearchService;
import com.coremedia.contenthub.api.search.Sort;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class YouTubeContentHubAdapter implements ContentHubAdapter, ContentHubSearchService {
  private static final Logger LOGGER = LoggerFactory.getLogger(YouTubeContentHubAdapter.class);
  private static final String HTTPS_WWW_GOOGLEAPIS_COM_AUTH_YOUTUBE_FORCE_SSL = "https://www.googleapis.com/auth/youtube.force-ssl";
  private static final List<ContentHubType> SEARCH_TYPES = Collections.singletonList(new ContentHubType(YouTubeTypes.ITEM));

  private final YouTubeContentHubSettings settings;
  private final String connectionId;
  private final YouTubeColumnProvider columnProvider;
  private final YouTubeConnector youTubeConnector;
  private final ContentHubObjectId rootId;

  YouTubeContentHubAdapter(YouTubeContentHubSettings settings, String connectionId) {
    this.settings = settings;
    this.connectionId = connectionId;
    rootId = new ContentHubObjectId(connectionId, connectionId);
    columnProvider = new YouTubeColumnProvider();

    try {
      List<String> scopes = Lists.newArrayList(HTTPS_WWW_GOOGLEAPIS_COM_AUTH_YOUTUBE_FORCE_SSL);
      String credentialsJson = null;
      if (settings != null) {
        credentialsJson = settings.getCredentialsJson();
      }
      if (credentialsJson == null || credentialsJson.length() == 0) {
        throw new ContentHubException("No credentialsJson found for youtube adapter '" + connectionId + "'");
      }

      GoogleCredential credential = GoogleCredential.fromStream(new ByteArrayInputStream(credentialsJson.getBytes()));
      if (credential.createScopedRequired()) {
        credential = credential.createScoped(scopes);
      }
      YouTube youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName("youtubeProvider").build();
      youTubeConnector = new YouTubeConnector(youTube);
    } catch (ContentHubException che) {
      throw che;
    } catch (Exception e) {
      LOGGER.error("Error initializing youtube adapter '" + connectionId + "': " + e.getMessage(), e);
      throw new ContentHubException(e);
    }
  }


  // --- ContentHubAdapter ------------------------------------------

  @NonNull
  @Override
  public Folder getRootFolder(@NonNull ContentHubContext context) throws ContentHubException {
    return new YouTubeFolder(rootId, getChannelDisplayName());
  }

  @Nullable
  @Override
  public Item getItem(@NonNull ContentHubContext context, @NonNull ContentHubObjectId id) throws ContentHubException {
    try {
      return item(id);
    } catch (Exception e) {
      throw new ContentHubException("Cannot get item by id " + id, e);
    }
  }

  @Nullable
  @Override
  public Folder getFolder(@NonNull ContentHubContext context, @NonNull ContentHubObjectId id) throws ContentHubException {
    return getRootFolder(context);
  }

  @NonNull
  @Override
  public List<Item> getItems(@NonNull ContentHubContext context, @NonNull Folder folder) throws ContentHubException {
    if (rootId.equals(folder.getId())) {
      if (settings != null) {
        String channelId = settings.getChannelId();
        if (!StringUtils.isEmpty(channelId)) {
          return youTubeConnector.getVideos(channelId).stream()
                  .map(this::item)
                  .collect(Collectors.toUnmodifiableList());
        }
      }
      throw new IllegalStateException("No channelId to fetch items for");
    } else {
      String playlistId = folder.getId().getExternalId();
      return youTubeConnector.getPlaylistItems(playlistId).stream()
              .map(this::item)
              .collect(Collectors.toUnmodifiableList());
    }
  }

  @NonNull
  @Override
  public List<Folder> getSubFolders(@NonNull ContentHubContext context, @NonNull Folder folder) throws ContentHubException {
    List<Folder> result = new ArrayList<>();

    if (rootId.equals(folder.getId())) {
      List<Playlist> playLists = getPlaylists();
      int counter = 0;
      for (Playlist list : playLists) {
        //in order to prevent performance issues, only deliver 1000 items max!
        if (counter == 1000) {
          break;
        }
        ContentHubObjectId categoryId = new ContentHubObjectId(connectionId, list.getId());
        YouTubeFolder channel = new YouTubeFolder(categoryId, list);
        result.add(channel);
        counter++;
      }
    }

    return result;
  }

  @Nullable
  @Override
  public Folder getParent(@NonNull ContentHubContext context, @NonNull ContentHubObject contentHubObject) throws ContentHubException {
    return rootId.equals(contentHubObject.getId()) ? null : getRootFolder(context);
  }

  @Override
  @NonNull
  public ContentHubTransformer transformer() {
    return new YouTubeContentHubTransformer();
  }

  @NonNull
  @Override
  public Optional<ContentHubSearchService> searchService() {
    return Optional.of(this);
  }


  // --- ContentHubSearchService ------------------------------------

  @Override
  @NonNull
  public Collection<ContentHubType> supportedTypes() {
    return SEARCH_TYPES;
  }

  @Override
  @NonNull
  public Set<Sort> supportedSortCriteria() {
    return ChannelSearchResult.YOUTUBE_VIDEO_ORDERS.keySet();
  }

  @Override
  public int supportedLimit() {
    return ChannelSearchResult.MAX_LIMIT;
  }

  @Override
  @NonNull
  public ContentHubSearchResult search(@NonNull String query,
                                       @Nullable Folder belowFolder,
                                       @Nullable ContentHubType type,
                                       @NonNull Collection<String> filterQueries,
                                       @NonNull List<Sort> sortCriteria,
                                       int limit) {
    if (belowFolder != null) {
      throw new IllegalArgumentException("Search below folder is not supported");
    }
    if (type != null && !supportedTypes().contains(type)) {
      throw new IllegalArgumentException("Unsupported search type " + type);
    }
    if (limit < -1) {
      throw new IllegalArgumentException("limit must be >= -1, as specified by ContentHubSearchService.search.");
    }
    if (limit == 0) {
      LOGGER.debug("YouTube does not support total hits, result is useless.");
      return new ContentHubSearchResult(Collections.emptyList());
    }
    if (limit > supportedLimit()) {
      // Be gentle for now, since we have not decided yet how to deal with the
      // limit in the UI.  Maybe, we throw an IllegalArgumentException later.
      LOGGER.debug("{} is greater than the supported limit of {}, result may be misleading", limit, supportedLimit());
    }
    if (!filterQueries.isEmpty()) {
      LOGGER.debug("filterQueries are not supported, ignore.");
    }

    try {
      List<SearchResult> videos = youTubeConnector.searchVideos(settings.getChannelId(), query, sortCriteria, limit);
      List<Item> items = videos.stream().map(this::item).collect(Collectors.toUnmodifiableList());
      return new ContentHubSearchResult(items);
    } catch (Exception e) {
      throw new ContentHubException("YouTube search failed", e);
    }
  }


  //------------------------ Helper ------------------------------------------------------------------------------------

  private Item item(@NonNull ContentHubObjectId id) {
    Video video = youTubeConnector.getVideo(id.getExternalId());
    return new YouTubeItem(id, video);
  }

  private Item item(PlaylistItem playlistItem) {
    String videoId = playlistItem.getSnippet().getResourceId().getVideoId();
    ContentHubObjectId objectId = new ContentHubObjectId(connectionId, videoId);
    return new YouTubeItem(objectId, playlistItem);
  }

  private Item item(SearchResult searchResult) {
    String videoId = searchResult.getId().getVideoId();
    ContentHubObjectId objectId = new ContentHubObjectId(connectionId, videoId);
    return new YouTubeItem(objectId, searchResult);
  }

  private String getChannelDisplayName() {
    String name = settings.getDisplayName();
    if (StringUtils.isEmpty(name)) {
      LOGGER.warn("No display name set for YouTube adapter '{}'", connectionId);
      name = connectionId;
    }
    return name;
  }

  private List<Playlist> getPlaylists() {
    String channelId = settings.getChannelId();
    if (!StringUtils.isEmpty(channelId)) {
      return youTubeConnector.getPlaylistsByChannel(channelId);
    }

    String user = settings.getUser();
    if (!StringUtils.isEmpty(user)) {
      return youTubeConnector.getPlaylistsByUser(user);
    }

    return Collections.emptyList();
  }

  @NonNull
  @Override
  public ColumnProvider columnProvider() {
    return columnProvider;
  }
}
