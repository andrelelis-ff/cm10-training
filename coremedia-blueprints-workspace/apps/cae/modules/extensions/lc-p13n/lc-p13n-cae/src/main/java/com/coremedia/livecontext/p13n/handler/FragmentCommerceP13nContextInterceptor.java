package com.coremedia.livecontext.p13n.handler;

import com.coremedia.blueprint.ecommerce.cae.AbstractCommerceContextInterceptor;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.fragment.FragmentContextProvider;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.livecontext.fragment.links.context.Context;
import com.coremedia.livecontext.fragment.links.context.LiveContextContextHelper;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import com.coremedia.personalization.preview.PreviewPersonalizationHandlerInterceptor;
import com.coremedia.personalization.preview.TestContextSource;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Suitable for URLs whose second segment denotes the store, e.g. /fragment/10001/...
 * It sets p13n-related request attributes out of the store context.
 */
public class FragmentCommerceP13nContextInterceptor extends AbstractCommerceContextInterceptor {

  private LiveContextSiteResolver liveContextSiteResolver;

  @Required
  public void setLiveContextSiteResolver(LiveContextSiteResolver liveContextSiteResolver) {
    this.liveContextSiteResolver = liveContextSiteResolver;
  }

  @Override
  @NonNull
  protected Optional<CommerceConnection> getCommerceConnectionWithConfiguredStoreContext(
          @NonNull Site site, @NonNull HttpServletRequest request) {
    Optional<CommerceConnection> connection = super.getCommerceConnectionWithConfiguredStoreContext(site, request);

    Context context = LiveContextContextHelper.findContext(request).orElse(null);
    if (context != null && isPreview()) {
      String testContextFlag = (String) context.get(PreviewPersonalizationHandlerInterceptor.QUERY_PARAMETER_TESTCONTEXT);
      String testContextId = (String) context.get(TestContextSource.QUERY_PARAMETER_TESTCONTEXTID);

      if ("true".equals(testContextFlag) && testContextId != null && !testContextId.equals("0")) {
        request.setAttribute(PreviewPersonalizationHandlerInterceptor.QUERY_PARAMETER_TESTCONTEXT, testContextFlag);
        request.setAttribute(TestContextSource.QUERY_PARAMETER_TESTCONTEXTID, testContextId);
      }
    }

    return connection;
  }

  @NonNull
  @Override
  protected Optional<Site> findSite(HttpServletRequest request, String normalizedPath) {
    FragmentParameters parameters = FragmentContextProvider.getFragmentContext(request).getParameters();
    return Optional.ofNullable(parameters).flatMap(liveContextSiteResolver::findSiteFor);
  }
}
