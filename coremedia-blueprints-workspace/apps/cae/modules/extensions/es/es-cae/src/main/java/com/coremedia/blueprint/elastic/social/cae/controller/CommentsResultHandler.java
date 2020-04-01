package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.base.elastic.common.ImageHelper;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.user.User;
import com.coremedia.elastic.core.api.blobs.Blob;
import com.coremedia.elastic.core.api.blobs.BlobService;
import com.coremedia.elastic.social.api.ModerationType;
import com.coremedia.elastic.social.api.comments.Comment;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriTemplate;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.TARGETVIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_FRAGMENT;
import static com.coremedia.blueprint.base.links.UriConstants.Views.VIEW_FRAGMENT;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Link
@RequestMapping
public class CommentsResultHandler extends ElasticContentHandler<CommentsResult> {

  private static final int maxNumberOfAttachments = 10;
  private static final int maxFileSize = 5000000;

  private static final String COMMENTS_PREFIX = "comments";

  @Inject
  private BlobService blobService;

  @Inject
  private MessageUtil messageUtil;

  /**
   * URI pattern, for URIs like "/dynamic/fragment/comments/{segment}/{contextId}/{id}"
   */
  public static final String DYNAMIC_PATTERN_COMMENTS = "/" + PREFIX_DYNAMIC +
    "/" + SEGMENTS_FRAGMENT +
    "/" + COMMENTS_PREFIX +
    "/{" + ROOT_SEGMENT + "}" +
    "/{" + CONTEXT_ID + "}" +
    "/{" + ID + "}";


  @GetMapping(value = DYNAMIC_PATTERN_COMMENTS)
  public ModelAndView getComments(@PathVariable("contextId") String contextId,
                                  @PathVariable("id") String targetId,
                                  @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                  HttpServletRequest request) {
    Navigation navigation = getNavigation(contextId);

    Site site = SiteHelper.getSiteFromRequest(request);
    if (site == null) {
      return HandlerHelper.notFound();
    }
    Object contributionTarget = getContributionTarget(targetId, site);
    if (contributionTarget == null) {
      return HandlerHelper.notFound();
    }
    Object[] beans = getBeansForSettings(contributionTarget, navigation).toArray();

    ElasticSocialConfiguration elasticSocialConfiguration = getElasticSocialConfiguration(beans);
    CommentsResult commentsResult = new CommentsResult(contributionTarget, getElasticSocialUserHelper().getCurrentUser(),
            getElasticSocialService(), elasticSocialConfiguration.isFeedbackEnabled(), elasticSocialConfiguration.getCommentType());

    ModelAndView modelWithView = HandlerHelper.createModelWithView(commentsResult, view);
    NavigationLinkSupport.setNavigation(modelWithView, navigation);

    return modelWithView;
  }

  /**
   * Handler to create comments for the currently logged in user
   * @param contextId     the context for a comment
   * @param targetId    the target for a comment
   * @param text        the text for a comment
   * @param authorName  the author name for anonymous comments (if allowed)
   * @param replyToId   if the comment is the reply to another comment
   * @return the newly created comment
   */
  @PostMapping(value= DYNAMIC_PATTERN_COMMENTS)
  public ModelAndView createComment(@PathVariable("contextId") String contextId,
                                    @PathVariable("id") String targetId,
                                    @RequestParam(value = "comment", required = false) String text,
                                    @RequestParam(value = "authorName", required = false) String authorName,
                                    @RequestParam(value = "replyTo", required = false) String replyToId,
                                    HttpServletRequest request) {
    Navigation navigation = getNavigation(contextId);

    Site site = SiteHelper.getSiteFromRequest(request);
    if (site == null) {
      return HandlerHelper.notFound();
    }
    Object contributionTarget = getContributionTarget(targetId, site);
    if( contributionTarget == null ) {
      return HandlerHelper.notFound();
    }
    Object[] beans = getBeansForSettings(contributionTarget, navigation).toArray();

    ElasticSocialConfiguration elasticSocialConfiguration = getElasticSocialConfiguration(beans);
    // workaround to prevent creating anonymous users when no comment can be written because of validation errors etc.
    CommunityUser author = getElasticSocialUserHelper().getCurrentUser();

    HandlerInfo result = new HandlerInfo();
    User developer = UserVariantHelper.getUser(request);
    validateInput(result, author, text, navigation, developer, beans);

    if (result.isSuccess()) {
      ModerationType moderation = elasticSocialConfiguration.getCommentModerationType();
      try {
        String adjustedAuthorName = authorName;
        if (author == null) {
          author = getElasticSocialUserHelper().getAnonymousUser();
        } else if (!author.isAnonymous()) {
          // For non-anonymous users we do not need and do not want to store the authorname directly within the
          // comment. This is especially important for data deletion requests.
          adjustedAuthorName = null;
        }
        List<Blob> blobs = extractBlobs(request, result, beans, maxFileSize, maxNumberOfAttachments);
        Comment comment = getElasticSocialService().createComment(author, adjustedAuthorName, contributionTarget,
                navigation, text, moderation, replyToId, blobs);
        result.setModel(comment);
        if (moderation.equals(ModerationType.PRE_MODERATION)) {
          result.addMessage(SUCCESS_MESSAGE, null, getMessage(navigation, developer, ContributionMessageKeys.COMMENT_FORM_SUCCESS_PREMODERATION));
        } else {
          result.addMessage(SUCCESS_MESSAGE, null, getMessage(navigation, developer, ContributionMessageKeys.COMMENT_FORM_SUCCESS));
        }
      } catch (Exception e) {
        LOG.error("Could not write a comment", e);
        addErrorMessage(result, null, navigation, developer, ContributionMessageKeys.COMMENT_FORM_ERROR);
      }
    }

    return HandlerHelper.createModel(result);
  }

  // ---------------------- building links ---------------------------------------------------------------------
  @Link(type = CommentsResult.class, view = VIEW_FRAGMENT, uri = DYNAMIC_PATTERN_COMMENTS)
  public UriComponents buildFragmentLink(CommentsResult commentsResult,
                                         UriTemplate uriTemplate,
                                         Map<String, Object> linkParameters,
                                         HttpServletRequest request) {
    return super.buildFragmentUri(SiteHelper.getSiteFromRequest(request), commentsResult, uriTemplate, linkParameters);
  }

  @Link(type = CommentsResult.class, uri = DYNAMIC_PATTERN_COMMENTS)
  public UriComponents buildCommentInfoLink(CommentsResult commentsResult,
                                            UriTemplate uriTemplate,
                                            Map<String, Object> linkParameters,
                                            HttpServletRequest request) {
    return getUriComponentsBuilder(SiteHelper.getSiteFromRequest(request), commentsResult, uriTemplate).build();
  }



  private void validateInput(HandlerInfo handlerInfo, CommunityUser user, String text, Navigation navigation, User developer, Object... beans) {
    // user == null was not allowed in previous versions, removed because user filter handling not fix
    validateCommentsEnabled(handlerInfo, user, navigation, developer, beans);
    if (isBlank(text)) {
      addErrorMessage(handlerInfo, "comment", navigation, developer, ContributionMessageKeys.COMMENT_FORM_ERROR_COMMENT_BLANK);
    }
  }

  private void validateCommentsEnabled(HandlerInfo handlerInfo, CommunityUser user, Navigation navigation, User developer, Object... beans) {
    ElasticSocialConfiguration elasticSocialConfiguration = getElasticSocialConfiguration(beans);
    // user == null was not allowed in previous versions, removed because user filter handling not fix
    if (!elasticSocialConfiguration.isWritingCommentsEnabled()) {
      addErrorMessage(handlerInfo, null, navigation, developer, ContributionMessageKeys.COMMENT_FORM_ERROR_NOT_ENABLED);
    }
    if ((user == null || user.isAnonymous()) && !elasticSocialConfiguration.isAnonymousCommentingEnabled()) {
      addErrorMessage(handlerInfo, null, navigation, developer, ContributionMessageKeys.COMMENT_FORM_NOT_LOGGED_IN);
    }
  }

  private List<Blob> extractBlobs(HttpServletRequest request, HandlerInfo result, Object[] beans, int maxImageFileSize, int maxNumberOfAttachments) {
    User developer = UserVariantHelper.getUser(request);
    List<Blob> blobs= new ArrayList<>();
    if (request instanceof DefaultMultipartHttpServletRequest) {
      DefaultMultipartHttpServletRequest defaultMultipartHttpServletRequest = (DefaultMultipartHttpServletRequest) request;
      MultiValueMap<String, MultipartFile> files = defaultMultipartHttpServletRequest.getMultiFileMap();
      if (files.entrySet().size() > maxNumberOfAttachments) {
          addError("commentForm-too-many-files", result, null, developer, beans, maxNumberOfAttachments);
          return null;
      }
      for (Map.Entry<String, List<MultipartFile>> fileEntry : files.entrySet()) {
        List<MultipartFile> fileList = fileEntry.getValue();
        for (MultipartFile file : fileList) {
          if (file.getSize() != 0) {
            if (file.getSize() > maxImageFileSize) {
              addError("commentForm-file-too-large", result, null, developer, beans, file.getOriginalFilename(), ImageHelper.getBytesAsKBString(maxImageFileSize));
              return null;
            }
            if (!ImageHelper.isSupportedMimeType(file.getContentType())) {
              addError("commentForm-file-unsupported-content-type", result, null, developer, beans, file.getOriginalFilename(), ImageHelper.getSupportedMimeTypesString());
              return null;
            }
            try {
              blobs.add(blobService.put(file.getInputStream(), file.getContentType(), file.getOriginalFilename()));
            } catch (IOException e) {
              addError("commentForm-file-upload-error", result, null, developer, beans, file.getOriginalFilename());
              return null;
            }
          }
        }
      }
    }
    return blobs;
  }

  private void addError(String messageKey, HandlerInfo result, String path, @Nullable User developer, Object[] beans, Object... args) {
    String message = getMessage(messageKey, developer, beans);
    MessageFormat messageFormat = new MessageFormat(message);
    result.addMessage(ERROR_MESSAGE, path, messageFormat.format(args));
  }

}
