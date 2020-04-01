package com.coremedia.blueprint.elastic.social.rest;

import com.coremedia.blueprint.base.elastic.common.BlobConverter;
import com.coremedia.blueprint.base.elastic.common.ImageHelper;
import com.coremedia.blueprint.elastic.social.util.BbCodeToCoreMediaRichtextTransformer;
import com.coremedia.blueprint.elastic.social.util.RepositoryFileNameHelper;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.elastic.core.api.blobs.Blob;
import com.coremedia.elastic.core.api.models.ModelException;
import com.coremedia.elastic.social.api.comments.Comment;
import com.coremedia.elastic.social.api.comments.CommentService;
import com.coremedia.elastic.social.api.reviews.ReviewService;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.rest.linking.AbstractLinkingResource;
import com.coremedia.xml.Markup;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.common.logging.BaseMarker.UNCLASSIFIED_PERSONAL_DATA;
import static com.coremedia.elastic.social.rest.api.ElasticSocialRestConstants.ELASTIC_SOCIAL_REST_PREFIX;

/**
 * Copies {@link Comment Comments} and associated image attachements from Elastic Social into {@link Content}
 * in the {@link com.coremedia.cap.common.CapRepository repository}.
 */
@Named
@RestController
@RequestMapping(value = CuratedTransferResource.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class CuratedTransferResource extends AbstractLinkingResource {
  public static final String PATH = ELASTIC_SOCIAL_REST_PREFIX + "/curate";
  private static final Logger LOG = LoggerFactory.getLogger(CuratedTransferResource.class);
  private static final String LINEBREAK = "\r\n";

  private static final String CONTENT_PROPERTY_TO_COPY_TO = "detailText";
  private static final String CONTENT_PROPERTY_TITLE = "title";
  private static final String COMMENTS_SEPARATOR_REGEX = ";";

  /**
   * name of comment model property which holds the list of curated contents created from it
   */
  static final String COMMENT_PROPERTY_CURATED_CONTENTS = "curatedContents";

  private static final String COMMENT_DATE_FORMAT_STRING = "dd.MM.yyyy | HH:mm";
  private static final ThreadLocal<SimpleDateFormat> COMMENT_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
    @Override
    protected synchronized SimpleDateFormat initialValue() {
      return new SimpleDateFormat(COMMENT_DATE_FORMAT_STRING);
    }
  };

  private static final String GALLERY_DOCUMENT_TYPE = "CMPicture";
  private static final String GALLERY_PROPERTY_TITLE = "title";
  private static final String GALLERY_PROPERTY_LINKLIST = "items";
  private static final String GALLERY_PROPERTY_TEASER_LINKLIST = "pictures";

  private static final String IMAGE_PROPERTY_TITLE = "title";
  private static final String IMAGE_PROPERTY_BLOB = "data";

  @Inject
  private ContentRepository contentRepository;

  @Inject
  private CommentService commentService;

  @Inject
  private ReviewService reviewService;

  @Inject
  private BlobConverter blobConverter;

  /**
   * <p>Copies {@link Comment comments} into a single {@link Content}.</p>
   *
   * @param capId      ID of the {@link Content} the {@link Comment Comments} will be copied to
   * @param commentIds numeric IDs of the {@link Comment comments} that will be copied, separated by ';'
   * @return ID of the {@link Content} the {@link Comment Comments} will be copied to
   * @throws IllegalArgumentException if a supplied argument is NULL
   */
  @PostMapping(value = "comments", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public String postProcess(@RequestParam("capId") String capId,
                            @RequestParam("commentIds") String commentIds) {
    validateContentId(capId);
    validateCommentIds(commentIds);

    final Content contentToCopyTo = fetchContent(capId);
    final List<Comment> comments = commentsFromIds(commentIds);

    if (!comments.isEmpty()) {
      copyCommentsTextTo(contentToCopyTo, comments);
      contentToCopyTo.set(CONTENT_PROPERTY_TITLE, contentToCopyTo.getName());
    }

    return capId;
  }

  /**
   * Copies image attachments of {@link Comment comments} into a single {@link Content}.
   *
   * @param capId      ID of the {@link Content} the {@link Comment Comments} will be copied to
   * @param commentIds numeric IDs of the {@link Comment comments} that will be copied, separated by ';'
   * @return ID of the {@link Content} the {@link Comment Comments} will be copied to
   * @throws IllegalArgumentException if a supplied argument is NULL
   */
  @PostMapping(value = "images", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public String postProcessImages(@RequestParam("capId") String capId,
                                  @RequestParam("commentIds") String commentIds) {
    validateContentId(capId);
    validateCommentIds(commentIds);

    final Content contentToCopyTo = fetchContent(capId);
    final List<Comment> comments = commentsFromIds(commentIds);

    if (!comments.isEmpty()) {
      copyImagesOfCommentsTo(contentToCopyTo, comments);
    }

    return capId;
  }

  private Content fetchContent(final String capId) {
    return contentRepository.getContent(capId);
  }

  private void copyCommentsTextTo(Content contentToCopyTo, final List<Comment> comments) {
    final StringBuilder bbCodeBuilder = new StringBuilder();
    for (Comment comment : comments) {
      if (bbCodeBuilder.length() > 0) {
        bbCodeBuilder.append(LINEBREAK);
      }
      // Suppress warning about assigning @PersonalData result from #formatComment to non-annotated variable
      // This class is designed to store personal data from Elastic Social comments in Content.
      @SuppressWarnings("PersonalData")
      String formattedContent = formatComment(comment);
      bbCodeBuilder.append(formattedContent);

      linkCuratedContentAtComment(comment, contentToCopyTo);
    }

    if (bbCodeBuilder.length() > 0) {
      writeCommentsAsCoremediaRichtextTo(contentToCopyTo, bbCodeBuilder);
    }
  }

  /**
   * Adds the given curated content created for the given comment to the comment's list property
   * {@link #COMMENT_PROPERTY_CURATED_CONTENTS}.
   *
   * @param comment comment
   * @param content curated content created from comment
   */
  private static void linkCuratedContentAtComment(Comment comment, Content content) {
    // the list of created curated contents from a comment is not personal data
    @SuppressWarnings("PersonalData")
    List<?> existingCurated = comment.getProperty(COMMENT_PROPERTY_CURATED_CONTENTS, List.class);
    List<Object> curated = existingCurated == null
            ? ImmutableList.of(content)
            : ImmutableList.builder().addAll(existingCurated).add(content).build();
    comment.setProperty(COMMENT_PROPERTY_CURATED_CONTENTS, curated);
    comment.save();
  }

  private void copyImagesOfCommentsTo(Content imageGallery, final List<Comment> comments) {
    final List<Content> galleryImages = new ArrayList<>();
    final RepositoryFileNameHelper repositoryFileNameHelper = new RepositoryFileNameHelper(contentRepository, imageGallery.getParent());

    for (Comment comment : comments) {
      final List<Blob> attachments = comment.getAttachments();
      if (attachments != null && !attachments.isEmpty()) {
        for (Blob attachment : attachments) {
          if (ImageHelper.isSupportedMimeType(attachment.getContentType())) {
            final String fileNameWithoutType = attachment.getFileName().substring(0, attachment.getFileName().lastIndexOf('.'));
            final String uniqueFileName = repositoryFileNameHelper.uniqueFileNameFor(fileNameWithoutType);
            final Content image = createImageFromAttachment(imageGallery, attachment, uniqueFileName);
            if (image != null) {
              galleryImages.add(image);
            }
          }
        }
      }
    }

    if (!galleryImages.isEmpty()) {
      final List<Content> teaserImages = Collections.singletonList(galleryImages.get(0));
      imageGallery.set(GALLERY_PROPERTY_LINKLIST, galleryImages);
      imageGallery.set(GALLERY_PROPERTY_TEASER_LINKLIST, teaserImages);
    }
    copyCommentsTextTo(imageGallery, comments);
    imageGallery.set(GALLERY_PROPERTY_TITLE, imageGallery.getName());
    imageGallery.checkIn();
  }

  private Content createImageFromAttachment(Content imageGallery, Blob imageAttachment, String uniqueFileName) {
    Content createdPicture = null;
    Content galleryFolder = imageGallery.getParent();
    if (galleryFolder != null) {
      com.coremedia.cap.common.Blob capBlob = blobConverter.capBlobFrom(imageAttachment);

      if (capBlob.getSize() != 0) {
        Map<String, Object> imageProperties = new HashMap<>();

        imageProperties.put(IMAGE_PROPERTY_TITLE, uniqueFileName);
        imageProperties.put(IMAGE_PROPERTY_BLOB, capBlob);
        createdPicture = contentRepository.createChild(galleryFolder, uniqueFileName, GALLERY_DOCUMENT_TYPE, imageProperties);
        createdPicture.checkIn();
      }
    } else {
      LOG.warn("Cannot find parent folder for content {}. ", imageGallery);
    }
    return createdPicture;
  }

  // Suppress warning about adding @PersonalData values to StringBuilder. Okay, the result is returned as @PersonalData
  @SuppressWarnings("PersonalData")
  private @PersonalData String formatComment(Comment comment) {
    String formattedDateString = COMMENT_DATE_FORMAT.get().format(comment.getCreationDate());

    StringBuilder result = new StringBuilder();
    result.append("[i]");
    result.append(getAuthorName(comment));
    result.append("[/i], ");
    result.append(formattedDateString);
    result.append(":");
    result.append("[cmQuote]");
    result.append(comment.getText());
    result.append("[/cmQuote]");
    return result.toString();
  }

  private @PersonalData String getAuthorName(Comment comment) {
    @PersonalData String name = comment.getAuthorName();

    CommunityUser author = comment.getAuthor();
    if (author != null) {
      boolean anonymous = true;
      try {
        anonymous = author.isAnonymous();
      } catch (ModelException e) {
        LOG.warn(UNCLASSIFIED_PERSONAL_DATA, "Could not resolve reference from comment/review {} to author: {}", comment.getId(), e.getMessage());
      }
      if (!anonymous) {
        name = author.getName();
      }
    }
    return StringUtils.isBlank(name) ? "anonymous" : name;
  }

  private void writeCommentsAsCoremediaRichtextTo(Content contentToCopyTo, final StringBuilder textBuilder) {
    final String commentsBbCode = textBuilder.toString();
    final Markup commentsAsRichtext = BbCodeToCoreMediaRichtextTransformer.newInstance().transform(commentsBbCode);
    contentToCopyTo.set(CONTENT_PROPERTY_TO_COPY_TO, commentsAsRichtext);
  }

  private List<Comment> commentsFromIds(final String commentIds) {
    final List<Comment> comments = new ArrayList<>();

    for (final String commentId : commentIds.split(COMMENTS_SEPARATOR_REGEX)) {
      try {
        Comment comment = commentService.getComment(commentId);
        if (comment == null) {
          comment = reviewService.getReview(commentId);
        }
        if (comment != null) {
          comments.add(comment);
        } else {
          LOG.info("Could not create comment/review for ID {}. Skipping.", commentId);
        }
      } catch (RuntimeException ex) {
        LOG.error("Error creating comment/review for ID {}. Skipping.", commentId, ex);
      }
    }

    return comments;
  }

  private static void validateContentId(final String capId) {
    if (!IdHelper.isContentId(capId)) {
      throw new IllegalArgumentException(String.format("'%s' is not a valid ContentId for argument 'capId'.", capId));
    }
  }

  private static void validateCommentIds(String commentIds) {
    if (commentIds == null) {
      throw new IllegalArgumentException("Argument 'commentIds' must not be null.");
    }
  }
}
