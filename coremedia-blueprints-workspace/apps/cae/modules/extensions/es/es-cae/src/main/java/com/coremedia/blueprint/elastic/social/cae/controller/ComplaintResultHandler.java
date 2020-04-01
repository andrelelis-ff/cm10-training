package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.user.User;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.TARGETVIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_FRAGMENT;
import static com.coremedia.blueprint.base.links.UriConstants.Views.VIEW_FRAGMENT;
import static com.coremedia.elastic.core.api.users.UserService.USERS_COLLECTION;
import static com.coremedia.elastic.social.api.comments.CommentService.COMMENTS_COLLECTION;

@Link
@RequestMapping
public class ComplaintResultHandler extends ElasticContentHandler<ComplaintResult> {

  private static final String COMPLAINT_PREFIX = "complaint";
  public static final String COLLECTION_PARAMETER = "collection";
  public static final String MODEL_PARAMETER = "model";
  public static final String COMPLAIN_PARAMETER = "complain";

  /**
   * URI pattern, for URIs like "/dynamic/fragment/complaint/{segment}/{contextId}/{id}"
   */
  public static final String DYNAMIC_PATTERN_COMPLAINT = "/" + PREFIX_DYNAMIC +
          "/" + SEGMENTS_FRAGMENT +
          "/" + COMPLAINT_PREFIX +
          "/{" + ROOT_SEGMENT + "}" +
          "/{" + CONTEXT_ID + "}" +
          "/{" + ID + "}";

  @GetMapping(value = DYNAMIC_PATTERN_COMPLAINT)
  public ModelAndView getComplaintResult(@PathVariable(CONTEXT_ID) String contextId,
                                 @PathVariable(ID) String id,
                                 @RequestParam(value = COLLECTION_PARAMETER) String collection,
                                 @RequestParam(value = MODEL_PARAMETER) String modelId,
                                 @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                 HttpServletRequest request) {

    Navigation navigation = getNavigation(contextId);

    Object contributionTarget = fetchContributionTarget(request, id);
    if (contributionTarget == null) {
      return HandlerHelper.notFound();
    }
    Object complaintTarget = getComplaintTarget(collection, id);
    if (complaintTarget == null) {
      return HandlerHelper.notFound();
    }

    Object[] beans = getBeansForSettings(contributionTarget, navigation).toArray();
    ElasticSocialConfiguration elasticSocialConfiguration = getElasticSocialConfiguration(beans);
    ComplaintResult commentsResult = new ComplaintResult(complaintTarget, getElasticSocialUserHelper().getCurrentUser(),
            getElasticSocialService(), elasticSocialConfiguration.isFeedbackEnabled(), elasticSocialConfiguration.getComplaintType());

    ModelAndView modelWithView = HandlerHelper.createModelWithView(commentsResult, view);
    NavigationLinkSupport.setNavigation(modelWithView, navigation);

    return modelWithView;
  }

  public Object getComplaintTarget(String collection, String id) {
    Object realTarget = null;
    if (USERS_COLLECTION.equals(collection)) {
      realTarget = getElasticSocialService().getUser(id);
    } else if (COMMENTS_COLLECTION.equals(collection)) {
      realTarget = getElasticSocialService().getComment(id);
    }
    return realTarget;
  }

  @PostMapping(value = DYNAMIC_PATTERN_COMPLAINT)
  public ModelAndView createComplaint(@PathVariable(CONTEXT_ID) String contextId,
                                   @PathVariable(ID) String targetId,
                                   @RequestParam(value = COMPLAIN_PARAMETER) boolean complain,
                                   @RequestParam(value = COLLECTION_PARAMETER) String collection,
                                   @RequestParam(value = MODEL_PARAMETER) String modelId,
                                   @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                   HttpServletRequest request) {

    Navigation navigation = getNavigation(contextId);

    Object contributionTarget = fetchContributionTarget(request, targetId);
    if (contributionTarget == null) {
      return HandlerHelper.notFound();
    }
    Object complaintTarget = getComplaintTarget(collection, targetId);
    if (complaintTarget == null) {
      return HandlerHelper.notFound();
    }

    // workaround to prevent creating anonymous users when no comment can be written because of validation errors etc.
    CommunityUser author = getElasticSocialUserHelper().getCurrentUser();
    HandlerInfo result = createResult(request, navigation, author, contributionTarget);
    if (result.isSuccess()) {
      getElasticSocialService().updateComplaint(author, complaintTarget, complain);
    }

    return HandlerHelper.createModelWithView(result, view);
  }

  @Override
  protected void validateEnabled(HandlerInfo handlerInfo, CommunityUser user, Navigation navigation, @Nullable User developer, Object... beans) {
    ElasticSocialConfiguration elasticSocialConfiguration = getElasticSocialConfiguration(beans);
    // user == null was not allowed in previous versions, removed because user filter handling not fix
    if (!elasticSocialConfiguration.isComplainingEnabled()) {
      addErrorMessage(handlerInfo, null, navigation, developer, ContributionMessageKeys.COMPLAINT_FORM_ERROR_NOT_ENABLED);
    } else if ((user == null || user.isAnonymous()) && !elasticSocialConfiguration.isAnonymousComplainingEnabled()) {
      addErrorMessage(handlerInfo, null, navigation, developer, ContributionMessageKeys.COMPLAINT_FORM_ERROR_NOT_LOGGED_IN);
    }
  }


  // ---------------------- building links ---------------------------------------------------------------------
  @Link(type = ComplaintResult.class, view = VIEW_FRAGMENT, uri = DYNAMIC_PATTERN_COMPLAINT)
  public UriComponents buildFragmentLink(ComplaintResult complaintResult,
                                         UriTemplate uriTemplate,
                                         Map<String, Object> linkParameters,
                                         HttpServletRequest request) {
    return buildFragmentUri(SiteHelper.getSiteFromRequest(request), complaintResult, uriTemplate, linkParameters);
  }

  @Link(type = ComplaintResult.class, uri = DYNAMIC_PATTERN_COMPLAINT)
  public UriComponents buildInfoLink(ComplaintResult complaintResult, UriTemplate uriTemplate,HttpServletRequest request) {
    return getUriComponentsBuilder(SiteHelper.getSiteFromRequest(request), complaintResult, uriTemplate).build();
  }
}
