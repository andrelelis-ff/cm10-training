package com.coremedia.blueprint.contenthub.adapters.rss;


import com.coremedia.common.util.WordAbbreviator;
import com.coremedia.contenthub.api.ContentHubBlob;
import com.coremedia.contenthub.api.ContentHubObjectId;
import com.coremedia.contenthub.api.ContentHubType;
import com.coremedia.contenthub.api.Item;
import com.coremedia.contenthub.api.UrlBlobBuilder;
import com.coremedia.contenthub.api.preview.DetailsElement;
import com.coremedia.contenthub.api.preview.DetailsSection;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.web.util.HtmlUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class RSSItem extends RSSHubObject implements Item {
  private static final WordAbbreviator ABBREVIATOR = new WordAbbreviator();
  private static final int BLOB_SIZE_LIMIT = 10000000;
  private final SyndEntry rssEntry;

  RSSItem(ContentHubObjectId id, SyndFeed feed, @NonNull SyndEntry rssEntry) {
    super(id, feed);
    this.rssEntry = rssEntry;
  }

  SyndEntry getRssEntry() {
    return rssEntry;
  }

  @NonNull
  @Override
  public ContentHubType getContentHubType() {
    return new ContentHubType("rss");
  }

  @NonNull
  @Override
  public String getName() {
    return rssEntry.getTitle();
  }

  @Nullable
  @Override
  public String getDescription() {
    if (rssEntry.getDescription() != null) {
      return HtmlUtils.htmlUnescape(rssEntry.getDescription().getValue());
    }
    return null;
  }

  @NonNull
  @Override
  public String getCoreMediaContentType() {
    return "CMArticle";
  }

  @NonNull
  @Override
  public List<DetailsSection> getDetails() {
    ContentHubBlob imageRef = firstImageReference();
    boolean showPicture = imageRef!=null && imageRef.getLength() < BLOB_SIZE_LIMIT;
    return List.of(
            new DetailsSection("main", List.of(
                    new DetailsElement<>(rssEntry.getTitle(), false, showPicture ? imageRef : SHOW_TYPE_ICON)
            ), false, false, false),
            new DetailsSection("metadata", List.of(
                    new DetailsElement<>("text", formatPreviewString(getDescription())),
                    new DetailsElement<>("author", rssEntry.getAuthor()),
                    new DetailsElement<>("published", formatPreviewDate(rssEntry.getPublishedDate())),
                    new DetailsElement<>("lastModified", formatPreviewDate(rssEntry.getUpdatedDate())),
                    new DetailsElement<>("link", rssEntry.getLink())
            ).stream().filter(p -> Objects.nonNull(p.getValue())).collect(Collectors.toUnmodifiableList())));
  }


  @Nullable
  @Override
  public ContentHubBlob getBlob(String classifier) {
    List<String> imageUrls = getImageUrls();
    return imageUrls.isEmpty() ?
            null :
            new UrlBlobBuilder(this, classifier).withUrl(imageUrls.get(0)).withEtag().build();
  }

  @Nullable
  private ContentHubBlob firstImageReference() {
    List<String> imageUrls = getImageUrls();
    // The particular classifier value is irrelevant here, since #getBlob does not consider it.
    return imageUrls.isEmpty() ?
            null :
            new UrlBlobBuilder(this, "classifier").withUrl(imageUrls.get(0)).withEtag().build();
  }

  @NonNull
  private List<String> getImageUrls() {
    return FeedImageExtractor.extractImageUrls(rssEntry);
  }

  @Nullable
  private String formatPreviewString(@Nullable String str) {
    return str == null ? null : ABBREVIATOR.abbreviateString(str, 240);
  }

  @Nullable
  private Calendar formatPreviewDate(@Nullable Date date) {
    if (date == null) {
      return null;
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar;
  }
}
