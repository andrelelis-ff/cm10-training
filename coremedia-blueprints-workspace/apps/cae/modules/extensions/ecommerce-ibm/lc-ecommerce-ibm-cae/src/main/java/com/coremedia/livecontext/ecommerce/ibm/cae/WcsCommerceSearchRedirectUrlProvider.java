package com.coremedia.livecontext.ecommerce.ibm.cae;

import com.coremedia.livecontext.ecommerce.common.ForVendor;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.handler.CommerceSearchRedirectUrlProvider;
import com.coremedia.livecontext.handler.LiveContextPageHandlerBase;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ForVendor("ibm")
public class WcsCommerceSearchRedirectUrlProvider implements CommerceSearchRedirectUrlProvider {

  private final WcsUrlProvider wcsUrlProvider;

  public WcsCommerceSearchRedirectUrlProvider(WcsUrlProvider wcsUrlProvider) {
    this.wcsUrlProvider = wcsUrlProvider;
  }

  @NonNull
  @Override
  public Optional<UriComponents> provideRedirectUrl(@Nullable String term, @NonNull HttpServletRequest request,
                                                    @NonNull StoreContext storeContext) {
    Map<String, Object> params = new HashMap<>();
    boolean studioPreviewRequest = LiveContextPageHandlerBase.isStudioPreviewRequest(request);
    params.put(LiveContextPageHandlerBase.URL_PROVIDER_IS_STUDIO_PREVIEW, studioPreviewRequest);
    params.put(LiveContextPageHandlerBase.URL_PROVIDER_SEARCH_TERM, term);

    return wcsUrlProvider.provideValue(params, request, storeContext);
  }
}
