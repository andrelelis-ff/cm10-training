package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.user.User;
import com.coremedia.elastic.core.cms.ContentWithSite;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.web.UserVariantHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class ElasticContentHandler<T extends ContributionResult> extends ElasticHandler<T> {

  private ContentBeanFactory contentBeanFactory;
  private ContentRepository contentRepository;
  private ContextHelper contextHelper;

  @Inject
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  @Inject
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  @Inject
  public void setContextHelper(ContextHelper contextHelper) {
    this.contextHelper = contextHelper;
  }

  public ContextHelper getContextHelper() {
    return contextHelper;
  }

  @Override
  protected UriComponentsBuilder getUriComponentsBuilder(Site site, T result, UriTemplate uriTemplate) {
    CMLinkable contentBean = getTargetAsContentBean(result);
    CMNavigation context = contextHelper.contextFor(contentBean);

    return getUriComponentsBuilder(uriTemplate, context, IdHelper.parseContentId(contentBean.getContent().getId()));
  }

  protected CMLinkable getTargetAsContentBean(T contributionResult) {
    ContentBean contentBean;
    Content content = null;

    if (contributionResult.getTarget() instanceof Content) {
      content = (Content) contributionResult.getTarget();
    } else if (contributionResult.getTarget() instanceof ContentWithSite) {
      content = ((ContentWithSite) contributionResult.getTarget()).getContent();
    }

    if (content != null) {
      contentBean = contentBeanFactory.createBeanFor(content, ContentBean.class);
    } else if (contributionResult.getTarget() instanceof ContentBean) {
      contentBean = (ContentBean) contributionResult.getTarget();
    } else {
      throw new IllegalArgumentException("Cannot handle comments target " + contributionResult.getTarget());
    }

    if (!(contentBean instanceof CMLinkable)) {
      throw new IllegalArgumentException("Cannot handle content beans that are not linkables: " + contentBean);
    }
    return (CMLinkable) contentBean;
  }


  protected Content getContent(String targetId) {
    if (StringUtils.isBlank(targetId)) {
      return null;
    }
    return contentRepository.getContent(IdHelper.formatContentId(targetId));
  }

  protected Object getContributionTarget(String targetId, Site site) {
    return getContentWithSite(targetId, site);
  }

  protected ContentWithSite getContentWithSite(String targetId, Site site) {
    Content content = getContent(targetId);
    return content == null ? null : new ContentWithSite(content, site);
  }

  /**
   * Provides a {@link com.coremedia.blueprint.common.contentbeans.CMNavigation} from a navigation context id.
   */
  protected Navigation getNavigation(String contextId) {
    final Content content = getContent(contextId);
    final ContentBean navigation = contentBeanFactory.createBeanFor(content, ContentBean.class);
    if (navigation instanceof Navigation) {
      return (Navigation) navigation;
    } else {
      throw new IllegalArgumentException("Content is not navigation " + content.getId());
    }
  }

  protected Object fetchContributionTarget(HttpServletRequest request, String targetId) {
    return SiteHelper.findSite(request)
            .map(site -> getContributionTarget(targetId, site))
            .orElse(null);
  }

  protected HandlerInfo createResult(HttpServletRequest request, Navigation navigation, CommunityUser author, Object contributionTarget) {
    Object[] beans = getBeansForSettings(contributionTarget, navigation).toArray();
    HandlerInfo result = new HandlerInfo();
    User developer = UserVariantHelper.getUser(request);
    validateEnabled(result, author, navigation, developer, beans);
    return result;
  }

  /**
   * Hook method, invoked by
   * {@link #createResult(HttpServletRequest, Navigation, CommunityUser, Object)}
   * <p>
   * Does nothing.  To be overridden if needed.
   *
   * @param user The community user that this is all about
   * @param developer A Blueprint developer whose work in progress may be considered by particular features
   */
  protected void validateEnabled(HandlerInfo handlerInfo, CommunityUser user, Navigation navigation, @Nullable User developer, Object... beans) {
    // To be overridden if needed.
  }
}
