package com.coremedia.blueprint.contenthub.adapters.youtube;

import com.coremedia.contenthub.api.search.Sort;
import com.coremedia.contenthub.api.search.SortDirection;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ChannelSearchResult {
  private static final Logger LOG = LoggerFactory.getLogger(ChannelSearchResult.class);

  // Origin of these magic constants: https://developers.google.com/youtube/v3/docs
  private static final String YOUTUBE_TYPE_VIDEO = "video";
  private static final String YOUTUBE_LIST_PART_SNIPPET = "snippet";

  // The YouTube API supports exactly these orders.
  // S. https://developers.google.com/youtube/v3/docs/search/list # Optional parameters # order
  private static final String YOUTUBE_ORDER_DATE = "date"; // Descending
  private static final String YOUTUBE_ORDER_TITLE = "title"; // Ascending
  // Currently unused, but possible.  Just for completeness.
  //private static final String YOUTUBE_ORDER_RATING = "rating"; // Descending
  //private static final String YOUTUBE_ORDER_RELEVANCE = "relevance"; // Descending
  //private static final String YOUTUBE_ORDER_VIEWCOUNT = "viewCount"; // Descending

  // The Studio client is not aware of the YouTube wording, but understands
  // the ColumnModelProvider terms.
  private static final Sort STUDIO_SORT_NAME = new Sort(YouTubeColumnProvider.DATA_INDEX_NAME_COLUMN, SortDirection.ASCENDING);
  private static final Sort STUDIO_SORT_LASTMODIFIED = new Sort(YouTubeColumnProvider.DATA_INDEX_LAST_MODIFIED, SortDirection.DESCENDING);

  /**
   * Map the Studio client's column names to the YouTube orders.
   * <p>
   * The Studio client has no access to the unmapped YouTube orders.
   */
  static final Map<Sort, String> YOUTUBE_VIDEO_ORDERS = Map.of(
          STUDIO_SORT_NAME, YOUTUBE_ORDER_TITLE,
          STUDIO_SORT_LASTMODIFIED, YOUTUBE_ORDER_DATE
  );

  /**
   * The maximum number of search hits supported by the YouTube API.
   * <p>
   * S. https://developers.google.com/youtube/v3/docs/search/list # Optional parameters # channelId
   * <p>
   * Do not confuse with {@link YouTubeConnector#MAX_RESULTS}, which is the
   * paging chunk size.
   */
  static final int MAX_LIMIT = 500;

  private final YouTube youTube;
  private final String channelId;
  private final String term;
  private final String order;
  private final int limit;

  /**
   * @implNote We need the nullable term to fetch the "videos of the root folder".
   * Since YouTube has no folder model, this means all videos of the channel, and
   * there is no cheaper YouTube API way but search to obtain them.
   */
  ChannelSearchResult(@NonNull YouTube youTube,
                      @NonNull String channelId,
                      @Nullable String term,
                      int limit,
                      @NonNull List<Sort> sortCriteria) {
    this.youTube = youTube;
    this.channelId = channelId;
    this.term = StringUtils.isEmpty(term) ? null : term;
    this.order = studioSortToYoutubeOrder(sortCriteria);
    this.limit = limitToLimit(limit);
  }

  @NonNull
  List<SearchResult> channelSearchResult() throws IOException {
    List<SearchResult> result = new ArrayList<>();
    fetchSearchListResponse(result, null);
    return result;
  }


  // --- internal ---------------------------------------------------

  /**
   * Fetch the results.
   *
   * @implNote <ul>
   *   <li>YouTube supports no limit, must count ourselves (s. limit)</li>
   *   <li>Responses are paginated, method is recursive (s. token)</li>
   * </ul>
   */
  private void fetchSearchListResponse(List<SearchResult> searchResults, String token) throws IOException {
    LOG.debug("YouTube.search channel: {}, term: {}, order: {}, limit: {}{}", channelId, term, order, limit, (token==null ? "" : " (paging)"));
    YouTube.Search.List list = youTube.search()
            .list(YOUTUBE_LIST_PART_SNIPPET)
            .setMaxResults(YouTubeConnector.MAX_RESULTS)
            .setChannelId(channelId)
            .setType(YOUTUBE_TYPE_VIDEO);
    if (term != null) {
      list.setQ(term);
    }
    if (order != null) {
      list.setOrder(order);
    }
    if (token != null) {
      list.setPageToken(token);
    }
    SearchListResponse response = list.execute();

    List<SearchResult> items = response.getItems();
    if (items != null) {
      int missing = limit==-1 ? Integer.MAX_VALUE : limit-searchResults.size();
      searchResults.addAll(items.subList(0, Math.min(missing, items.size())));
    }

    if (limit==-1 || searchResults.size() < limit) {
      String nextPageToken = response.getNextPageToken();
      if (nextPageToken != null) {
        fetchSearchListResponse(searchResults, nextPageToken);
      }
    }
  }

  private static String studioSortToYoutubeOrder(List<Sort> sortCriteria) {
    if (sortCriteria.isEmpty()) {
      return null;
    }
    Sort studioSort = sortCriteria.get(0);
    if (sortCriteria.size() > 1) {
      // (Almost) silently ignore this illegal argument.
      // The Studio user can only select one order anyway, so he will not
      // notice ineffective fallback Sorts added by the Studio client.
      LOG.debug("YouTube does not support cascaded sorting.  Only {} is effective.", studioSort);
    }
    String youtubeOrder = YOUTUBE_VIDEO_ORDERS.get(studioSort);
    if (youtubeOrder == null) {
      throw new IllegalArgumentException("No YouTube video order for: " + studioSort);
    }
    return youtubeOrder;
  }

  private static int limitToLimit(int limit) {
    if (limit > MAX_LIMIT) {
      LOG.debug("Limit {} is overridden by YouTube's limit of 500.", limit);
    }
    // Preserve our value, anyway.  Code can cope with it.
    return limit;
  }
}
