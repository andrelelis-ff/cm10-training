package com.coremedia.blueprint.elastic.social.rest;

import com.coremedia.blueprint.base.elastic.common.BlobConverter;
import com.coremedia.blueprint.elastic.social.util.BbCodeToCoreMediaRichtextTransformer;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentException;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.elastic.core.api.blobs.Blob;
import com.coremedia.elastic.core.api.models.ModelException;
import com.coremedia.elastic.social.api.comments.Comment;
import com.coremedia.elastic.social.api.comments.CommentService;
import com.coremedia.elastic.social.api.reviews.Review;
import com.coremedia.elastic.social.api.reviews.ReviewService;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.xml.Markup;
import com.google.common.base.Preconditions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.coremedia.elastic.core.test.Injection.inject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CuratedTransferResourceTest {
  private static final String VALID_CONTENT_ID = "coremedia:///cap/content/42";
  private static final String IMAGE_GALLERY_DEFAULT_PARENTFOLDER_CONTENT_ID = "coremedia:///cap/content/666";
  private static final String VALID_COMMENT_IDS = "42,666";
  private static final String CONTENT_PROPERTY_TO_COPY_TO = "detailText";
  private static final String CMARTICLE_PROPERTY_TITLE = "title";
  private static final String CMGALLERY_PROPERTY_TO_COPY_TO = "items";
  private static final String CMGALLERY_PROPERTY_TITLE = "title";
  private static final String VALID_IMAGE_MIME_TYPE = "image/jpeg";
  private static final int DEFAULT_BLOB_SIZE = 42;
  private static final String GALLERY_DOCUMENTTYPE = "CMPicture";
  private static final String DEFAULT_DATE_STRING = "10.10.2010-12:42";
  private static final String IMAGE_PROPERTY_BLOB = "data";

  private CuratedTransferResource curatedTransferResource;

  @Before
  public void setUp() throws Exception {
    curatedTransferResource = new CuratedTransferResource();
  }

  // --- CuratedTransfer: Comments -------------------------------------------------------------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void postProcess_createArticleFromComments_capIdMustNotBeNull() throws Exception {
    curatedTransferResource.postProcess(null, VALID_COMMENT_IDS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void postProcess_invalidContendId() {
    curatedTransferResource.postProcess("fooBar42", VALID_COMMENT_IDS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void postProcess_commentIdsMustNotBeNull() throws Exception {
    curatedTransferResource.postProcess(VALID_CONTENT_ID, null);
  }

  @Test(expected = ContentException.class)
  public void postProcess_articleToCopyToDoesNotExist() throws Exception {
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);
    when(contentRepository.getContent(VALID_CONTENT_ID)).thenThrow(ContentException.class);

    curatedTransferResource.postProcess(VALID_CONTENT_ID, VALID_COMMENT_IDS);
  }

  @Test
  public void postProcess_copyFromOneComment() throws ParseException {
    String articleContentId = VALID_CONTENT_ID;
    String commentId = "42";
    String commentText = "fooBar42";
    final String commentAuthorName = "Dilbert";
    String articleName = "test";

    // Mock services
    CommentService commentService = mockAndInjectInto(CommentService.class, curatedTransferResource);
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);

    Content articleToCopyTo = mockContent();
    when(articleToCopyTo.getName()).thenReturn(articleName);
    when(contentRepository.getContent(articleContentId)).thenReturn(articleToCopyTo);

    Comment comment = mockComment(commentId, commentText, commentAuthorName, dateFromString("21.09.2012-16:23"));
    when(commentService.getComment(comment.getId())).thenReturn(comment);

    // Actual computation
    curatedTransferResource.postProcess(articleContentId, commentId);

    String expectedContentAsBbCode = "[i]" + commentAuthorName + "[/i], " + "21.09.2012 | 16:23:" + "[cmQuote]" + commentText + "[/cmQuote]";
    Markup expectedContentAsRichtext = BbCodeToCoreMediaRichtextTransformer.newInstance().transform(expectedContentAsBbCode);
    verify(articleToCopyTo).set(CONTENT_PROPERTY_TO_COPY_TO, expectedContentAsRichtext);
    verify(articleToCopyTo).set(CMARTICLE_PROPERTY_TITLE, articleName);
  }

  @Test
  public void postProcess_copyFromOneReview() throws ParseException {
    String articleContentId = VALID_CONTENT_ID;
    String reviewId = "42";
    String reviewText = "fooBar42";
    final String reviewAuthorName = "Dilbert";
    String articleName = "test";

    // Mock services
    CommentService commentService = mockAndInjectInto(CommentService.class, curatedTransferResource);
    ReviewService reviewService = mockAndInjectInto(ReviewService.class, curatedTransferResource);
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);

    Content articleToCopyTo = mockContent();
    when(articleToCopyTo.getName()).thenReturn(articleName);
    when(contentRepository.getContent(articleContentId)).thenReturn(articleToCopyTo);

    Review review = mockReview(reviewId, reviewText, reviewAuthorName, dateFromString("21.09.2012-16:23"));
    when(commentService.getComment(review.getId())).thenReturn(review);
    when(reviewService.getReview(review.getId())).thenReturn(review);

    // Actual computation
    curatedTransferResource.postProcess(articleContentId, reviewId);

    String expectedContentAsBbCode = "[i]" + reviewAuthorName + "[/i], " + "21.09.2012 | 16:23:" + "[cmQuote]" + reviewText + "[/cmQuote]";
    Markup expectedContentAsRichtext = BbCodeToCoreMediaRichtextTransformer.newInstance().transform(expectedContentAsBbCode);
    verify(articleToCopyTo).set(CONTENT_PROPERTY_TO_COPY_TO, expectedContentAsRichtext);
    verify(articleToCopyTo).set(CMARTICLE_PROPERTY_TITLE, articleName);
  }

  @Test
  public void postProcess_copyFromOneAnonymousComment() throws ParseException {
    String articleContentId = VALID_CONTENT_ID;
    String commentId = "42";
    String commentText = "fooBar42";
    final String commentAuthorName = null;
    String articleName = "test";

    // Mock services
    CommentService commentService = mockAndInjectInto(CommentService.class, curatedTransferResource);
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);

    Content articleToCopyTo = mockContent();
    when(articleToCopyTo.getName()).thenReturn(articleName);
    when(contentRepository.getContent(articleContentId)).thenReturn(articleToCopyTo);

    Comment comment = mockComment(commentId, commentText, commentAuthorName, dateFromString("21.09.2012-16:23"));
    when(commentService.getComment(comment.getId())).thenReturn(comment);

    // Actual computation
    curatedTransferResource.postProcess(articleContentId, commentId);

    String expectedContentAsBbCode = "[i]anonymous[/i], " + "21.09.2012 | 16:23:" + "[cmQuote]" + commentText + "[/cmQuote]";
    Markup expectedContentAsRichtext = BbCodeToCoreMediaRichtextTransformer.newInstance().transform(expectedContentAsBbCode);
    verify(articleToCopyTo).set(CONTENT_PROPERTY_TO_COPY_TO, expectedContentAsRichtext);
    verify(articleToCopyTo).set(CMARTICLE_PROPERTY_TITLE, articleName);
  }

  @Test
  public void postProcess_copyFromOneUserComment() throws ParseException {
    String articleContentId = VALID_CONTENT_ID;
    String commentId = "42";
    String commentText = "fooBar42";
    final String commentAuthorName = "dilbert";
    String articleName = "test";

    CommunityUser author = mock(CommunityUser.class);
    when(author.isAnonymous()).thenReturn(false);
    when(author.getName()).thenReturn(commentAuthorName);

    // Mock services
    CommentService commentService = mockAndInjectInto(CommentService.class, curatedTransferResource);
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);

    Content articleToCopyTo = mockContent();
    when(articleToCopyTo.getName()).thenReturn(articleName);
    when(contentRepository.getContent(articleContentId)).thenReturn(articleToCopyTo);

    Comment comment = mockComment(commentId, commentText, "", dateFromString("21.09.2012-16:23"));
    when(comment.getAuthor()).thenReturn(author);
    when(commentService.getComment(comment.getId())).thenReturn(comment);

    // Actual computation
    curatedTransferResource.postProcess(articleContentId, commentId);

    String expectedContentAsBbCode = "[i]" + commentAuthorName + "[/i], " + "21.09.2012 | 16:23:" + "[cmQuote]" + commentText + "[/cmQuote]";
    Markup expectedContentAsRichtext = BbCodeToCoreMediaRichtextTransformer.newInstance().transform(expectedContentAsBbCode);
    verify(articleToCopyTo).set(CONTENT_PROPERTY_TO_COPY_TO, expectedContentAsRichtext);
    verify(articleToCopyTo).set(CMARTICLE_PROPERTY_TITLE, articleName);
  }

  @Test
  public void postProcess_copyFromTwoComments() throws ParseException {
    String articleContentId = VALID_CONTENT_ID;
    String commentId = "42";
    String secondCommentId = "555";
    final String commentAuthorName = "Dilbert";
    final String secondCommentAuthorName = "Hobbes";
    String commentText = "fooBar42";
    String secondCommentText = "3000";
    String articleName = "test";

    // Mock services
    CommentService commentService = mockAndInjectInto(CommentService.class, curatedTransferResource);
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);

    Content articleToCopyTo = mockContent();
    when(articleToCopyTo.getName()).thenReturn(articleName);
    when(contentRepository.getContent(articleContentId)).thenReturn(articleToCopyTo);

    Comment comment01 = mockComment(commentId, commentText, commentAuthorName, dateFromString("21.09.2012-16:23"));
    when(commentService.getComment(comment01.getId())).thenReturn(comment01);

    Comment comment02 = mockComment(secondCommentId, secondCommentText, secondCommentAuthorName, dateFromString("23.09.2012-09:04"));
    when(commentService.getComment(comment02.getId())).thenReturn(comment02);

    // Actual computation
    final String commentIds = commentId + ";" + secondCommentId;
    curatedTransferResource.postProcess(articleContentId, commentIds);

    String lineBreak = "\r\n";
    String commentBbCode = "[i]" + commentAuthorName + "[/i], " + "21.09.2012 | 16:23:" + "[cmQuote]" + commentText + "[/cmQuote]";
    String secondCommentBbCode = "[i]" + secondCommentAuthorName + "[/i], " + "23.09.2012 | 09:04:" + "[cmQuote]" + secondCommentText + "[/cmQuote]";

    final String mergedCommentsAsBbCode = commentBbCode + lineBreak + secondCommentBbCode;
    final Markup expectedRichtextContent = BbCodeToCoreMediaRichtextTransformer.newInstance().transform(mergedCommentsAsBbCode);
    verify(articleToCopyTo).set(CONTENT_PROPERTY_TO_COPY_TO, expectedRichtextContent);
    verify(articleToCopyTo).set(CMARTICLE_PROPERTY_TITLE, articleName);
  }

  @Test
  public void postProcess_commentNotFound() throws ParseException {
    String articleContentId = VALID_CONTENT_ID;
    String commentId = "42";
    String articleName = "test";

    // Mock services
    CommentService commentService = mockAndInjectInto(CommentService.class, curatedTransferResource);
    ReviewService reviewService = mockAndInjectInto(ReviewService.class, curatedTransferResource);
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);

    Content articleToCopyTo = mockContent();
    when(articleToCopyTo.getName()).thenReturn(articleName);
    when(contentRepository.getContent(articleContentId)).thenReturn(articleToCopyTo);

    //No comment or review found
    when(commentService.getComment(commentId)).thenReturn(null);
    when(reviewService.getReview(commentId)).thenReturn(null);

    // Actual computation
    curatedTransferResource.postProcess(articleContentId, commentId);

    verify(articleToCopyTo, never()).set(eq(CONTENT_PROPERTY_TO_COPY_TO), anyString());
    verify(articleToCopyTo, never()).set(eq(CMARTICLE_PROPERTY_TITLE), anyString());
  }

  // --- CuratedTransfer: Image attachments ----------------------------------------------------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void postProcessImages_capIdMustNotBeNull() {
    curatedTransferResource.postProcessImages(null, VALID_COMMENT_IDS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void postProcessImages_commentIdsMustNotBeNull() throws Exception {
    curatedTransferResource.postProcess(VALID_CONTENT_ID, null);
  }

  @Test(expected = ContentException.class)
  public void postProcessImages_galleryToCopyToDoesNotExist() throws Exception {
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);
    when(contentRepository.getContent(VALID_CONTENT_ID)).thenThrow(ContentException.class);

    curatedTransferResource.postProcess(VALID_CONTENT_ID, VALID_COMMENT_IDS);
  }

  @Test
  public void postProcessImages_copyFromOneCommentWithoutImageAttachment() throws ParseException {
    String imageGalleryParentContentId = IMAGE_GALLERY_DEFAULT_PARENTFOLDER_CONTENT_ID;
    String imageGalleryContentId = VALID_CONTENT_ID;
    String imageGalleryName = "test";
    String commentId = "42";

    // Mock services
    CommentService commentService = mockAndInjectInto(CommentService.class, curatedTransferResource);
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);

    // Mock comment
    Comment commentWithoutImageAttachment = mockComment(commentId, "dummy", "dummy", createDefaultDate());
    when(commentService.getComment(commentWithoutImageAttachment.getId())).thenReturn(commentWithoutImageAttachment);

    // Mock image gallery (including the containing folder)
    Content imageGalleryFolder = mockFolder();

    when(contentRepository.getContent(imageGalleryParentContentId)).thenReturn(imageGalleryFolder);
    Content imageGallery = mockContent();
    when(imageGallery.getParent()).thenReturn(imageGalleryFolder);
    when(imageGallery.getName()).thenReturn(imageGalleryName);
    when(contentRepository.getContent(imageGalleryContentId)).thenReturn(imageGallery);

    curatedTransferResource.postProcessImages(imageGalleryContentId, commentId);

    verify(contentRepository, never()).createChild(
            eq(imageGalleryFolder),
            anyString(),
            eq(GALLERY_DOCUMENTTYPE),
            anyMap()
    );

    verify(imageGallery).set(CMGALLERY_PROPERTY_TITLE, imageGalleryName);
    verify(imageGallery).set(eq(CONTENT_PROPERTY_TO_COPY_TO), any(Markup.class));
  }

  @Test
  public void postProcessImages_copyOneImageAttachmentFromOneComment() throws ParseException {
    String imageGalleryParentContentId = IMAGE_GALLERY_DEFAULT_PARENTFOLDER_CONTENT_ID;
    String imageGalleryContentId = VALID_CONTENT_ID;
    String imageGalleryName = "my-gallery";
    String imageAttachmentFileName = "attachment-42.jpg";
    String imageAttachmentFileNameWithoutType = "attachment-42";
    String commentId = "42";
    final String commentText = "I am a very nice dummy text!";

    // Mock services
    BlobConverter blobConverter = mockAndInjectInto(BlobConverter.class, curatedTransferResource);
    CommentService commentService = mockAndInjectInto(CommentService.class, curatedTransferResource);
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);

    // Mock image gallery (including the containing folder)
    Content imageGalleryFolder = mockFolder();
    when(contentRepository.getContent(imageGalleryParentContentId)).thenReturn(imageGalleryFolder);
    Content imageGallery = mockContent();
    when(imageGallery.getName()).thenReturn(imageGalleryName);
    when(imageGallery.getParent()).thenReturn(imageGalleryFolder);
    when(contentRepository.getContent(imageGalleryContentId)).thenReturn(imageGallery);

    // Mock comment
    List<Blob> imageAttachments = Arrays.asList(mockImageBlob(imageAttachmentFileName));
    Comment commentWithOneImageAttachment = mockCommentWithImageAttachment(commentId, imageAttachments);
    when(commentWithOneImageAttachment.getText()).thenReturn(commentText);
    when(commentService.getComment(commentWithOneImageAttachment.getId())).thenReturn(commentWithOneImageAttachment);

    // Fake image attachment
    final com.coremedia.cap.common.Blob imageAttachmentAsCapBlob = mockCapBlob();
    Blob imageAttachment = commentWithOneImageAttachment.getAttachments().get(0);
    when(blobConverter.capBlobFrom(imageAttachment)).thenReturn(imageAttachmentAsCapBlob);

    // Fake CMPicture (created from image attachment)
    Map<String, Object> expectedPictureProperties = Collections.<String, Object>singletonMap(IMAGE_PROPERTY_BLOB, imageAttachmentAsCapBlob);
    Content cmPicture = mockContent();
    when(contentRepository.createChild(
            eq(imageGalleryFolder),
            eq(imageAttachmentFileNameWithoutType),
            eq(GALLERY_DOCUMENTTYPE),
            argThat(isMapContaining(expectedPictureProperties)))
    ).thenReturn(cmPicture);

    // Actual computation
    curatedTransferResource.postProcessImages(imageGalleryContentId, commentId);

    // corresponding CMPicture created?
    verify(contentRepository, times(1)).createChild(
            eq(imageGalleryFolder),
            eq(imageAttachmentFileNameWithoutType),
            eq(GALLERY_DOCUMENTTYPE),
            argThat(isMapContaining(expectedPictureProperties))
    );

    // Created CMPicture linked to gallery?
    verify(imageGallery, times(1)).set(CMGALLERY_PROPERTY_TO_COPY_TO, Arrays.asList(cmPicture));
    verify(imageGallery).set(CMGALLERY_PROPERTY_TITLE, imageGalleryName);
    verify(imageGallery).set(eq(CONTENT_PROPERTY_TO_COPY_TO), any(Markup.class));
  }

  @Test
  public void postProcessImages_copyTwoImageAttachmentsWithSameFileNameFromOneComment() throws ParseException {
    String imageGalleryParentContentId = IMAGE_GALLERY_DEFAULT_PARENTFOLDER_CONTENT_ID;
    String imageGalleryContentId = VALID_CONTENT_ID;
    String commentId = "42";
    String attachmentFileName = "attachment-42.jpg";

    // Mock services
    BlobConverter blobConverter = mockAndInjectInto(BlobConverter.class, curatedTransferResource);
    CommentService commentService = mockAndInjectInto(CommentService.class, curatedTransferResource);
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);

    // Mock image gallery (including the containing folder)
    Content imageGalleryFolder = mockFolder();
    when(contentRepository.getContent(imageGalleryParentContentId)).thenReturn(imageGalleryFolder);
    Content imageGallery = mockContent();
    when(imageGallery.getParent()).thenReturn(imageGalleryFolder);
    when(contentRepository.getContent(imageGalleryContentId)).thenReturn(imageGallery);

    // Fake image attachments
    Blob firstAttachment = mockImageBlob(attachmentFileName);
    Blob secondAttachment = mockImageBlob(attachmentFileName);
    List<Blob> imageAttachments = Arrays.asList(firstAttachment, secondAttachment);
    final com.coremedia.cap.common.Blob dummyCapBlob = mockCapBlob();
    when(blobConverter.capBlobFrom(firstAttachment)).thenReturn(dummyCapBlob);
    when(blobConverter.capBlobFrom(secondAttachment)).thenReturn(dummyCapBlob);

    // Fake CMPicture (created from image attachment)
    Map<String, Object> expectedPictureProperties = Collections.<String,Object>singletonMap(IMAGE_PROPERTY_BLOB, dummyCapBlob);
    Content cmPicture = mockContent();
    when(contentRepository.createChild(
            eq(imageGalleryFolder),
            anyString(),
            eq(GALLERY_DOCUMENTTYPE),
            argThat(isMapContaining(expectedPictureProperties)))
    ).thenReturn(cmPicture);

    // Mock comments
    Comment firstComment = mockCommentWithImageAttachment(commentId, imageAttachments);
    when(commentService.getComment(firstComment.getId())).thenReturn(firstComment);

    // Actual computation
    curatedTransferResource.postProcessImages(imageGalleryContentId, commentId);

    verify(contentRepository, times(1)).createChild(
            eq(imageGalleryFolder),
            eq("attachment-42"),
            eq(GALLERY_DOCUMENTTYPE),
            anyMap()
    );

    verify(contentRepository, times(1)).createChild(
            eq(imageGalleryFolder),
            eq("attachment-42(1)"),
            eq(GALLERY_DOCUMENTTYPE),
            anyMap()
    );
  }

  @Test
  public void postProcessImages_copyOneImageAttachmentWhoseFilePathExistsInTheRepository() throws ParseException {
    String imageGalleryParentContentId = IMAGE_GALLERY_DEFAULT_PARENTFOLDER_CONTENT_ID;
    String imageGalleryParentPath = "/root/imageGalleries";
    String imageGalleryContentId = VALID_CONTENT_ID;
    String commentId = "42";
    String attachmentFileName = "attachment-42.jpg";
    String attachmentFileNameWithoutType = "attachment-42";


    // Mock services
    BlobConverter blobConverter = mockAndInjectInto(BlobConverter.class, curatedTransferResource);
    CommentService commentService = mockAndInjectInto(CommentService.class, curatedTransferResource);
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);

    // Mock image gallery (including the containing folder)
    Content imageGalleryFolder = mockFolder();
    when(imageGalleryFolder.getPath()).thenReturn(imageGalleryParentPath);
    when(contentRepository.getContent(imageGalleryParentContentId)).thenReturn(imageGalleryFolder);
    Content imageGallery = mockContent();
    when(imageGallery.getParent()).thenReturn(imageGalleryFolder);
    when(contentRepository.getContent(imageGalleryContentId)).thenReturn(imageGallery);

    // fake existing content in repository
    String identicalFilePath = imageGalleryParentPath + "/" + attachmentFileNameWithoutType;
    when(contentRepository.getChild(identicalFilePath)).thenReturn(mockContent());

    // Fake image attachments
    Blob imageAttachment = mockImageBlob(attachmentFileName);
    final com.coremedia.cap.common.Blob dummyCapBlob = mockCapBlob();
    when(blobConverter.capBlobFrom(imageAttachment)).thenReturn(dummyCapBlob);

    // Fake CMPicture (created from image attachment)
    Map<String, Object> expectedPictureProperties = Collections.<String, Object>singletonMap(IMAGE_PROPERTY_BLOB, dummyCapBlob);
    Content cmPicture = mockContent();
    when(contentRepository.createChild(
            eq(imageGalleryFolder),
            anyString(),
            eq(GALLERY_DOCUMENTTYPE),
            argThat(isMapContaining(expectedPictureProperties)))
    ).thenReturn(cmPicture);

    // Mock comments
    Comment firstComment = mockCommentWithImageAttachment(commentId, Arrays.asList(imageAttachment));
    when(commentService.getComment(firstComment.getId())).thenReturn(firstComment);

    // Actual computation
    curatedTransferResource.postProcessImages(imageGalleryContentId, commentId);

    verify(contentRepository, times(1)).createChild(
            eq(imageGalleryFolder),
            eq("attachment-42(1)"),
            eq(GALLERY_DOCUMENTTYPE),
            anyMap()
    );
  }

  @Test
  public void postProcess_commentWithInvalidUser() throws ParseException {
    String articleContentId = VALID_CONTENT_ID;
    String commentId = "42";
    String commentText = "fooBar42";
    CommunityUser author = mock(CommunityUser.class);
    when(author.isAnonymous()).thenThrow(new ModelException("No delegate for model"));

    // Mock services
    CommentService commentService = mockAndInjectInto(CommentService.class, curatedTransferResource);
    ContentRepository contentRepository = mockAndInjectInto(ContentRepository.class, curatedTransferResource);

    Content articleToCopyTo = mockContent();
    when(contentRepository.getContent(articleContentId)).thenReturn(articleToCopyTo);

    Comment comment = mockComment(commentId, commentText, "", dateFromString("21.09.2012-16:23"));
    when(comment.getAuthor()).thenReturn(author);
    when(commentService.getComment(comment.getId())).thenReturn(comment);

    // Actual computation
    curatedTransferResource.postProcess(articleContentId, commentId);

    String expectedContentAsBbCode = "[i]" + "anonymous" + "[/i], " + "21.09.2012 | 16:23:" + "[cmQuote]" + commentText + "[/cmQuote]";
    Markup expectedContentAsRichtext = BbCodeToCoreMediaRichtextTransformer.newInstance().transform(expectedContentAsBbCode);
    verify(articleToCopyTo).set(CONTENT_PROPERTY_TO_COPY_TO, expectedContentAsRichtext);
  }


  private com.coremedia.cap.common.Blob mockCapBlob() {
    com.coremedia.cap.common.Blob capBlob = mock(com.coremedia.cap.common.Blob.class);
    when(capBlob.getSize()).thenReturn(DEFAULT_BLOB_SIZE);
    return capBlob;
  }

  // --- Helper methods ------------------------------------------------------------------------------------------------

  private Content mockFolder() {
    Content folder = mockContent();
    when(folder.isFolder()).thenReturn(true);
    return folder;
  }

  private Content mockContent() {
    return mock(Content.class);
  }

  private <T> T mockAndInjectInto(Class<T> targetClass, Object injectionTarget) {
    T blobConverter = mock(targetClass);
    inject(injectionTarget, blobConverter);
    return blobConverter;
  }

  private Comment mockComment(String commentId, String commentText, String commentAuthorName, Date commentDate) throws ParseException {
    Date date = (commentDate != null) ? commentDate : createDefaultDate();

    Comment comment = mock(Comment.class);
    when(comment.getId()).thenReturn(commentId);
    when(comment.getText()).thenReturn(commentText);
    when(comment.getAuthorName()).thenReturn(commentAuthorName);
    when(comment.getCreationDate()).thenReturn(date);

    return comment;
  }

  private Review mockReview(String reviewId, String reviewText, String reviewAuthorName, Date reviewDate) throws ParseException {
    Date date = (reviewDate != null) ? reviewDate : createDefaultDate();

    Review review = mock(Review.class);
    when(review.getId()).thenReturn(reviewId);
    when(review.getText()).thenReturn(reviewText);
    when(review.getAuthorName()).thenReturn(reviewAuthorName);
    when(review.getCreationDate()).thenReturn(date);

    return review;
  }

  private Comment mockCommentWithImageAttachment(String commentId, List<Blob> imageAttachments) throws ParseException {
    Comment comment = mockComment(commentId, "COMMENT_WITH_ATTACHMENT", "JOHN_DOE", createDefaultDate());
    when(comment.getAttachments()).thenReturn(imageAttachments);
    return comment;
  }

  private Blob mockImageBlob(String attachmentFileName) {
    Blob imageBlob = mock(Blob.class);
    when(imageBlob.getContentType()).thenReturn(VALID_IMAGE_MIME_TYPE);
    when(imageBlob.getFileName()).thenReturn(attachmentFileName);
    return imageBlob;
  }

  @SuppressWarnings("all")
  private ArgumentMatcher<Map<String, Object>> isMapContaining(final Map<String, Object> expectedProperties) {
    return new ArgumentMatcher<Map<String, Object>>() {
      public boolean matches(Map<String, Object> argument) {
        Preconditions.checkArgument(expectedProperties.size() > 0, "Cannot compare with empty property map.");

        Map<String, Object> actualProperties = (Map<String, Object>) argument;
        boolean allPropertiesContained = true;
        for (Map.Entry<String, Object> propEntry : expectedProperties.entrySet()) {
          allPropertiesContained &= actualProperties.containsKey(propEntry.getKey())
                  && actualProperties.get(propEntry.getKey()).equals(propEntry.getValue());
        }

        return allPropertiesContained;
      }
    };
  }

  private static Date dateFromString(String germanDateString) throws ParseException {
    return new SimpleDateFormat("dd.MM.yyyy-HH:mm").parse(germanDateString);
  }

  private static Date createDefaultDate() throws ParseException {
    return dateFromString(DEFAULT_DATE_STRING);
  }
}
