package com.coremedia.blueprint.contenthub.adapters.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

class VideoById {
  private static final Logger LOG = LoggerFactory.getLogger(VideoById.class);
  private static final String REQUEST_TYPE_SNIPPET = "snippet";

  private final YouTube youTube;
  private final String videoId;

  VideoById(YouTube youTube, String videoId) {
    this.youTube = youTube;
    this.videoId = videoId;
  }

  Video video() throws IOException {
    LOG.debug("YouTube.videos id: {}", videoId);
    VideoListResponse videoListResponse = youTube.videos()
            .list(REQUEST_TYPE_SNIPPET)
            .setMaxResults(1L)
            .setId(videoId)
            .execute();

    List<Video> videos = videoListResponse.getItems();
    return videos!=null && !videos.isEmpty() ? videos.get(0) : null;
  }
}
