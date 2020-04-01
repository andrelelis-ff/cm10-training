package com.coremedia.livecontext.ecommerce.ibm.cae.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.contentbeans.CMExternalPage;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.cae.WcsUrlProvider;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * Link Scheme for ibm content led scenario in live cae.
 */
@Link
@DefaultAnnotation(NonNull.class)
@Deprecated
public class ExternalPageLinkScheme {

  private final WcsUrlProvider wcsUrlProvider;
  private final CommerceConnectionSupplier commerceConnectionSupplier;

  public ExternalPageLinkScheme(WcsUrlProvider wcsUrlProvider, CommerceConnectionSupplier commerceConnectionSupplier) {
    this.wcsUrlProvider = wcsUrlProvider;
    this.commerceConnectionSupplier = commerceConnectionSupplier;
  }

  @Link(type = CMExternalPage.class, order = 2)
  @Nullable
  public UriComponents buildLinkForExternalPage(CMExternalPage externalPage, Map<String, Object> linkParameters,
                                                HttpServletRequest request) {

    StoreContext storeContext = StoreContextHelper.findStoreContext(request)
            .orElseGet(() -> findCommerceStoreContext(externalPage.getContent()).orElse(null));

    if (storeContext == null) {
      return null;
    }

    UriComponentsBuilder ucb = wcsUrlProvider.buildPageLink(externalPage, linkParameters, request, storeContext).orElse(null);
    return ucb==null ? null : ucb.build();
  }

  private Optional<StoreContext> findCommerceStoreContext(Content content) {
    return commerceConnectionSupplier.findConnection(content)
            .map(CommerceConnection::getStoreContext);
  }
}
