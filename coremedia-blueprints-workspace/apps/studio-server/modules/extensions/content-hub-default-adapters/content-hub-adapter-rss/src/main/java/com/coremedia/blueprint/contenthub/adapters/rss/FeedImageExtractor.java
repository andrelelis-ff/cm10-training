package com.coremedia.blueprint.contenthub.adapters.rss;


import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.modules.mediarss.types.MediaGroup;
import com.rometools.modules.mediarss.types.Metadata;
import com.rometools.modules.mediarss.types.Thumbnail;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to extract all image URLs from a feed entry.
 */
class FeedImageExtractor {
  // static class
  private FeedImageExtractor() {}

  /**
   * Evaluates the HTML and the metadata for the RSS entry to find images
   *
   * @param entry the RSS entry to evaluate
   * @return the list of image URLs
   */
  @NonNull
  static List<String> extractImageUrls(@NonNull SyndEntry entry) {
    List<String> result = new ArrayList<>();

    extractFromEnclosures(entry, result);

    for (Object module : entry.getModules()) {
      if (module instanceof MediaEntryModule) {
        extractFromMediaModule((MediaEntryModule) module, result);
      }
    }

    extractFromContents(entry, result);
    return result;
  }

  /**
   * Searches the HTML contents of the feed for image references
   * and extracts them via regular expression.
   * @param entry the feed entry
   * @param result the list of extracted images
   */
  private static void extractFromContents(@NonNull SyndEntry entry, List<String> result) {
    StringBuilder text = new StringBuilder(entry.getDescription().getValue());
    if (entry.getContents() != null && !entry.getContents().isEmpty()) {
      for (SyndContent content : entry.getContents()) {
        text.append(content.getValue());
      }
    }

    String imgRegex = "src\\s*=\\s*([\\\"'])?([^ \\\"']*)";
    Pattern p = Pattern.compile(imgRegex, Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(text);
    while (m.find()) {
      String imageUrl = m.group(2);
      if (imageUrl.contains(".png") || imageUrl.contains(".jpg") || imageUrl.contains(".jpeg")) {
        addImage(result, imageUrl);
      }
    }
  }

  /**
   * Searches the media entries of the feed for thumbnail references
   * @param module the media module that may contain image references
   * @param result the extracted images
   */
  private static void extractFromMediaModule(MediaEntryModule module, List<String> result) {
    //images inside the metadata
    for (Thumbnail thumb : module.getMetadata().getThumbnail()) {
      String url = thumb.getUrl().toString();
      addImage(result, url);
    }

    //images inside the media contents
    MediaContent[] mediaContents = module.getMediaContents();
    if (mediaContents != null) {
      for (MediaContent mediaContent : mediaContents) {
        Metadata metadata = mediaContent.getMetadata();
        if (metadata == null || metadata.getThumbnail() == null || metadata.getThumbnail().length == 0) {
          continue;
        }

        Thumbnail thumbnail = metadata.getThumbnail()[0];
        String url = thumbnail.getUrl().toString();
        addImage(result, url);
      }
    }

    //images inside the media group contents
    for (MediaGroup group : module.getMediaGroups()) {
      MediaContent[] contents = group.getContents();
      for (MediaContent content : contents) {
        if (content.getType().startsWith("image") && content.getReference() != null) {
          String url = content.getReference().toString();
          addImage(result, url);
        }
      }
    }
  }

  /**
   * Extracts images of the enclosure element from the feed entry
   * @param entry the entry to extract the enclosures from
   * @param result the extracted images
   */
  private static void extractFromEnclosures(@NonNull SyndEntry entry, List<String> result) {
    List<SyndEnclosure> enclosures = entry.getEnclosures();
    if (enclosures != null) {
      for (SyndEnclosure enclosure : enclosures) {
        String type = enclosure.getType();
        if (type.contains("image")) {
          String url = enclosure.getUrl();
          addImage(result, url);
        }
      }
    }
  }

  private static void addImage(List<String> result, String url) {
    if (!result.contains(url)) {
      result.add(url);
    }
  }
}
