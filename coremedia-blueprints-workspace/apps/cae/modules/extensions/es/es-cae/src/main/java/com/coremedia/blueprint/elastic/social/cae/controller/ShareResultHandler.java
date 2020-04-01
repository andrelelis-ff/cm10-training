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

@Link
@RequestMapping
public class ShareResultHandler extends ElasticContentHandler<ShareResult> {

  private static final String SHARE_PREFIX = "share";
  public static final String PROVIDER_PARAMETER = "provider";
  /**
   * URI pattern, for URIs share "/dynamic/fragment/shares/{segment}/{contextId}/{id}"
   */
  public static final String DYNAMIC_PATTERN_SHARE = "/" + PREFIX_DYNAMIC +
          "/" + SEGMENTS_FRAGMENT +
          "/" + SHARE_PREFIX +
          "/{" + ROOT_SEGMENT + "}" +
          "/{" + CONTEXT_ID + "}" +
          "/{" + ID + "}";

  @GetMapping(value = DYNAMIC_PATTERN_SHARE)
  public ModelAndView getShareResult(@PathVariable(CONTEXT_ID) String contextId,
                                         @PathVariable(ID) String id,
                                         @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                         HttpServletRequest request) {

    Navigation navigation = getNavigation(contextId);

    Object contributionTarget = fetchContributionTarget(request, id);
    if (contributionTarget == null) {
      return HandlerHelper.notFound();
    }

    Object[] beans = getBeansForSettings(contributionTarget, navigation).toArray();
    ElasticSocialConfiguration elasticSocialConfiguration = getElasticSocialConfiguration(beans);
    CommunityUser currentUser = getElasticSocialUserHelper().getCurrentUser();
    ShareResult shareResult = new ShareResult(contributionTarget, currentUser,
            getElasticSocialService(), elasticSocialConfiguration.isFeedbackEnabled(), elasticSocialConfiguration.getSharingType());

    ModelAndView modelWithView = HandlerHelper.createModelWithView(shareResult, view);
    NavigationLinkSupport.setNavigation(modelWithView, navigation);

    return modelWithView;
  }

  @PostMapping(value = DYNAMIC_PATTERN_SHARE)
  public ModelAndView createShare(@PathVariable(CONTEXT_ID) String contextId,
                                      @PathVariable(ID) String targetId,
                                      @RequestParam(value = PROVIDER_PARAMETER, required = true) String provider,
                                      @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                      HttpServletRequest request) {

    Navigation navigation = getNavigation(contextId);

    Object contributionTarget = fetchContributionTarget(request, targetId);
    if( contributionTarget == null ) {
      return HandlerHelper.notFound();
    }

    // workaround to prevent creating anonymous users when no share can be written because of validation errors etc.
    CommunityUser author = getElasticSocialUserHelper().getCurrentOrAnonymousUser();
    HandlerInfo result = createResult(request, navigation, author, contributionTarget);
    if (result.isSuccess()) {
      getElasticSocialService().share(author, contributionTarget, navigation.getContext(), provider);
    }

    return HandlerHelper.createModel(result);
  }

  @Override
  protected void validateEnabled(HandlerInfo handlerInfo, CommunityUser user, Navigation navigation, @Nullable User developer, Object... beans) {
    ElasticSocialConfiguration elasticSocialConfiguration = getElasticSocialConfiguration(beans);
    if (!elasticSocialConfiguration.isSharingEnabled()) {
      addErrorMessage(handlerInfo, null, navigation, developer, ContributionMessageKeys.SHARE_FORM_ERROR_NOT_ENABLED);
    } else if ((user == null || user.isAnonymous()) && !elasticSocialConfiguration.isAnonymousSharingEnabled()) {
      addErrorMessage(handlerInfo, null, navigation, developer, ContributionMessageKeys.SHARE_FORM_ERROR_NOT_LOGGED_IN);
    }
  }


  // ---------------------- building links ---------------------------------------------------------------------
  @Link(type = ShareResult.class, view = VIEW_FRAGMENT, uri = DYNAMIC_PATTERN_SHARE)
  public UriComponents buildFragmentLink(ShareResult shareResult,
                                         UriTemplate uriTemplate,
                                         Map<String, Object> linkParameters,
                                         HttpServletRequest request) {
    return buildFragmentUri(SiteHelper.getSiteFromRequest(request), shareResult, uriTemplate, linkParameters);
  }

  @Link(type = ShareResult.class, uri = DYNAMIC_PATTERN_SHARE)
  public UriComponents buildInfoLink(ShareResult shareResult, UriTemplate uriTemplate,HttpServletRequest request) {
    return getUriComponentsBuilder(SiteHelper.getSiteFromRequest(request), shareResult, uriTemplate).build();
  }
}
