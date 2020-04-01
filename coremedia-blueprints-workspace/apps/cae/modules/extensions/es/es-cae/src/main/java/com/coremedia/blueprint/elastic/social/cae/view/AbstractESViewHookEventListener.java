package com.coremedia.blueprint.elastic.social.cae.view;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.blueprint.elastic.social.cae.controller.ContributionResult;
import com.coremedia.objectserver.view.RenderNode;
import com.coremedia.objectserver.view.events.ViewHookEvent;
import com.coremedia.objectserver.view.events.ViewHookEventListener;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static com.coremedia.blueprint.base.cae.web.taglib.ViewHookEventNames.VIEW_HOOK_END;

/**
 * Base class for Elastic-Social-related viewhooks. These view hooks need to check if the respective feature is
 * enabled first.
 */
public abstract class AbstractESViewHookEventListener implements ViewHookEventListener<CMTeasable> {

  @Inject
  private ElasticSocialPlugin elasticSocialPlugin;

  @Inject
  private ContextHelper contextHelper;

  @Override
  public RenderNode onViewHook(ViewHookEvent<CMTeasable> event) {
    if (getSupportedViewHookEventIds().contains(event.getId())) {
      CMTeasable bean = event.getBean();
      String beanType = bean.getContent().getType().getName();
      CMNavigation navigation = contextHelper.contextFor(bean);

      ElasticSocialConfiguration esConfiguration;
      if (navigation == null) { // navigation is not necessarily required, e.g. content is not part of a site
        esConfiguration = elasticSocialPlugin.getElasticSocialConfiguration(bean);
      } else {
        esConfiguration = elasticSocialPlugin.getElasticSocialConfiguration(bean, navigation);
      }

      List<String> whitelist = getWhitelistTypes(esConfiguration);
      if (isEnabled(esConfiguration) && (whitelist == null || whitelist.contains(beanType))) {
        ContributionResult contribution = getContribution(event.getBean());
        if (contribution != null) {
          return new RenderNode(contribution, getView());
        }
      }
    }

    return null;
  }

  //====================================================================================================================

  /**
   * Returns an optional list of document types that are supported by this contribution type. Only concrete types
   * are supported. Inheritance is not considered.
   * @param elasticSocialConfiguration the configuration bean of ES
   * @return a list of concrete document types or <code>null</code> if all types defined by this listener are supported.
   */
  @Nullable
  protected List<String> getWhitelistTypes(@NonNull ElasticSocialConfiguration elasticSocialConfiguration) {
    return null;
  }

  /**
   * Returns a list of supported ViewHookEvent identifiers.
   * @return a list of supported ViewHookEvent identifiers.
   */
  @NonNull
  protected List<String> getSupportedViewHookEventIds() {
    return Collections.singletonList(VIEW_HOOK_END);
  }

  /**
   * Check if the feature that this view hook is related to is currently enabled.
   * @param elasticSocialConfiguration the configuration bean of ES
   * @return <code>true</code> if this view hook is enabled otherwise <code>false</code>
   */
  protected abstract boolean isEnabled(@NonNull ElasticSocialConfiguration elasticSocialConfiguration);

  /**
   * Returns the contribution that belongs to the view hook's target content.
   * @param target the content that this contribution is linked to
   * @return the contribution or <code>null</code> to skip ˝this view hook
   */
  @Nullable
  protected abstract ContributionResult getContribution(@NonNull Object target);

  /**
   * Returns the view name that the contribution result will be rendered with. The default value is <code>null</code>.
   * @return the view name that is passed to the rendering layer.
   */
  @Nullable
  protected String getView() {
    return null;
  }

}
