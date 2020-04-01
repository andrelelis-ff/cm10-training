package com.coremedia.blueprint.cae.feeds.impl;

import com.coremedia.blueprint.cae.feeds.FeedItemDataProvider;
import com.coremedia.blueprint.cae.handlers.TransformedBlobHandler;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.contentbeans.CMVideo;
import com.coremedia.blueprint.common.contentbeans.CMVisual;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.ContentType;
import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.coremedia.xml.MarkupUtil;
import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.MediaEntryModuleImpl;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.modules.mediarss.types.Metadata;
import com.rometools.modules.mediarss.types.Thumbnail;
import com.rometools.modules.mediarss.types.UrlReference;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.feed.synd.SyndPersonImpl;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.activation.MimeType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;
import static java.util.stream.Collectors.toList;

public class TeasableFeedItemDataProvider implements FeedItemDataProvider {

  private static final Logger LOG = LoggerFactory.getLogger(TeasableFeedItemDataProvider.class);

  private static final String AUTHOR_NAME = "";
  private static final String NEW_ITEM = "New Item";
  private static final String IMAGE_RATIO = "landscape_ratio4x3";
  private static final int IMAGE_WIDTH = 400;
  private static final int IMAGE_HEIGHT = 300;
  private static final int THUMBNAIL_WIDTH = 100;
  private static final int THUMBNAIL_HEIGHT = 75;

  private LinkFormatter linkFormatter;

  // --- configure --------------------------------------------------

  @Required
  public void setLinkFormatter(LinkFormatter linkFormatter) {
    this.linkFormatter = linkFormatter;
  }

  // Only for Java Bean compliance, not supposed to be used.
  public LinkFormatter getLinkFormatter() {
    return linkFormatter;
  }

  // --- FeedItemDataProvider ---------------------------------------

  @Override
  public boolean isSupported(Object item) {
    return (item != null && CMTeasable.class.isAssignableFrom(item.getClass()));
  }

  @NonNull
  @Override
  public SyndEntry getSyndEntry(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object bean) {
    CMTeasable teasable = (CMTeasable) bean;

    SyndPerson syndPerson = new SyndPersonImpl();
    syndPerson.setName(AUTHOR_NAME);

    SyndEntry entry = new SyndEntryImpl();
    entry.setAuthor(AUTHOR_NAME);
    entry.setAuthors(Collections.singletonList(syndPerson));
    entry.getModules().add(createMediaEntryModule(request, response, teasable)); //NOSONAR
    entry.setCategories(Collections.emptyList());
    entry.setTitle(getTitle(request, response, teasable));
    entry.setPublishedDate(teasable.getContent().getCreationDate().getTime());
    entry.setUpdatedDate(teasable.getContent().getModificationDate().getTime());
    entry.setLink(getLink(request, response, teasable, null));
    entry.setDescription(createSyndContent(teasable));

    return entry;
  }

  // --- overridable ------------------------------------------------

  protected String getTitle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                            @NonNull CMTeasable teasable) {
    String title = teasable.getTeaserTitle();
    return StringUtils.isEmpty(title) ? NEW_ITEM : title;
  }

  protected String getText(@NonNull CMTeasable teasable) {
    String textPlain = MarkupUtil.asPlainText(teasable.getTeaserText());
    if (StringUtils.isEmpty(textPlain)) {
      textPlain = MarkupUtil.asPlainText(teasable.getDetailText());
    }
    return textPlain;
  }

  @NonNull
  protected List<CMTeasable> getRelatedMediaContents(@NonNull CMTeasable teasable) {
    List<CMTeasable> related = new ArrayList<>();
    related.addAll(teasable.getMedia());
    related.addAll(teasable.getRelated());
    return related;
  }

  // --- utilities --------------------------------------------------

  protected static String getMediaTitle(@NonNull CMTeasable mediaItem) {
    String title = mediaItem.getTeaserTitle();
    return StringUtils.isEmpty(title) ? mediaItem.getContent().getName() : title;
  }

  // --- internal ---------------------------------------------------

  @NonNull
  private List<MediaContent> getMediaContents(@NonNull HttpServletRequest request,
                                              @NonNull HttpServletResponse response, @NonNull CMTeasable teasable) {
    return getRelatedMediaContents(teasable)
            .stream()
            .map(related -> findMediaContent(request, response, related))
            .filter(Objects::nonNull)
            .collect(toList());
  }

  @Nullable
  private MediaContent findMediaContent(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response, @NonNull CMTeasable related) {
    try {
      ContentType contentType = related.getContent().getType();
      if (contentType.isSubtypeOf(CMPicture.NAME)) {
        return createPictureEnclosure(request, response, (CMPicture) related);
      } else if (contentType.isSubtypeOf(CMVideo.NAME)) {
        return createVideoEnclosure(request, response, (CMVideo) related);
      } else {
        return null;
      }
    } catch (URISyntaxException e) {
      LOG.error("Cannot create media content for " + related, e);
      return null;
    }
  }

  @Nullable
  private MediaContent createPictureEnclosure(@NonNull HttpServletRequest request,
                                              @NonNull HttpServletResponse response, @NonNull CMPicture mediaItem)
          throws URISyntaxException {
    request.setAttribute(ABSOLUTE_URI_KEY, true);

    Blob imageBlob = mediaItem.getTransformedData(IMAGE_RATIO);
    if (imageBlob == null) {
      return null;
    }

    String url = createUrlForTransformedBlob(imageBlob, request, response, IMAGE_WIDTH, IMAGE_HEIGHT);
    MimeType mimeType = mediaItem.getData().getContentType();
    MediaContent mediaContent = createMediaContent(request, response, url, mediaItem, mimeType, imageBlob);
    mediaContent.setHeight(IMAGE_HEIGHT);
    mediaContent.setWidth(IMAGE_WIDTH);
    return mediaContent;
  }

  @Nullable
  private MediaContent createVideoEnclosure(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                            @NonNull CMVideo mediaItem) throws URISyntaxException {
    request.setAttribute(ABSOLUTE_URI_KEY, true);

    Blob videoBlob = mediaItem.getData();
    if (videoBlob == null) {
      return null;
    }

    String url = getLinkFormatter().formatLink(videoBlob, null, request, response, false);
    MimeType mimeType = mediaItem.getData().getContentType();

    MediaContent mediaContent = createMediaContent(request, response, url, mediaItem, mimeType, videoBlob);
    mediaContent.setHeight(mediaItem.getHeight());
    mediaContent.setWidth(mediaItem.getWidth());
    return mediaContent;
  }

  @NonNull
  private MediaContent createMediaContent(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                          @NonNull String url, @NonNull CMVisual mediaItem, @NonNull MimeType mimeType,
                                          @NonNull Blob blob)
          throws URISyntaxException {
    MediaContent mediaContent = new MediaContent(new UrlReference(url));
    mediaContent.setFileSize((long) blob.getSize());
    setMimeType(mimeType, mediaContent);
    setMetaData(request, response, mediaItem, mediaContent);
    return mediaContent;
  }

  private void setMetaData(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                           @NonNull CMVisual mediaItem, @NonNull MediaContent mediaContent) throws URISyntaxException {
    String thumbnailUrl = getTumbnailUrl(request, response, mediaItem);
    if (thumbnailUrl == null) {
      return;
    }

    Metadata md = new Metadata();
    md.setThumbnail(new Thumbnail[]{new Thumbnail(new URI(thumbnailUrl))});
    md.setTitle(getMediaTitle(mediaItem));
    mediaContent.setMetadata(md);
  }

  private static void setMimeType(@NonNull MimeType mimeType, @NonNull MediaContent mediaContent) {
    mediaContent.setMedium(mimeType.getPrimaryType());
    mediaContent.setType(mimeType.toString());
  }

  @NonNull
  private SyndContent createSyndContent(@NonNull CMTeasable teasable) {
    String textPlain = getText(teasable);
    SyndContent syndContent = new SyndContentImpl();
    syndContent.setType("text/plain");
    syndContent.setValue(textPlain == null ? "" : textPlain);
    return syndContent;
  }

  @NonNull
  private MediaEntryModule createMediaEntryModule(@NonNull HttpServletRequest request,
                                                  @NonNull HttpServletResponse response, @NonNull CMTeasable teasable) {
    List<MediaContent> contents = getMediaContents(request, response, teasable);
    MediaEntryModuleImpl mediaEntryModule = new MediaEntryModuleImpl();
    mediaEntryModule.setMediaContents(contents.toArray(new MediaContent[contents.size()]));
    return mediaEntryModule;
  }

  @Nullable
  private String getTumbnailUrl(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull CMVisual mediaItem) {
    CMPicture picture = mediaItem.getPicture();
    if (picture == null) {
      return null;
    }

    Blob blob = picture.getTransformedData(IMAGE_RATIO);
    if (blob == null) {
      return null;
    }

    return createUrlForTransformedBlob(blob, request, response, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
  }

  /**
   * Encapsulates the creation of visual enclosures URIs to ensure that the request param {@link ViewUtils#PARAMETERS}
   * will be restored after generating the link.
   *
   * @param blob     The blob which serves the visual enclosure.
   * @param request  The request using to provide attributes.
   * @param response The response.
   * @param width    The with of the enclosure.
   * @param height   The height of the enclosure.
   * @return The URL of the visual enclosure.
   */
  @NonNull
  private String createUrlForTransformedBlob(@NonNull Blob blob, @NonNull HttpServletRequest request,
                                             @NonNull HttpServletResponse response, int width, int height) {
    Object oldParameters = request.getAttribute(ViewUtils.PARAMETERS);

    Map<String, String> params = new HashMap<>();
    params.put(TransformedBlobHandler.WIDTH_SEGMENT, String.valueOf(width));
    params.put(TransformedBlobHandler.HEIGHT_SEGMENT, String.valueOf(height));
    request.setAttribute(ViewUtils.PARAMETERS, params);
    try {
      return getLinkFormatter().formatLink(blob, null, request, response, false);
    } finally {
      request.setAttribute(ViewUtils.PARAMETERS, oldParameters);
    }
  }

  @NonNull
  private String getLink(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                         @NonNull Object bean, @Nullable String view) {
    request.setAttribute(ABSOLUTE_URI_KEY, true);
    return getLinkFormatter().formatLink(bean, view, request, response, true);
  }
}
