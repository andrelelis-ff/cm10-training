package com.coremedia.livecontext.handler;

import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Provider for search URLs pointing to the commerce system.
 */
@DefaultAnnotation(NonNull.class)
public interface CommerceSearchRedirectUrlProvider {

  /**
   * Provide the commerce search URL for redirect.
   *
   * @param term         the optional search term
   * @param request      the current request
   * @param storeContext the current store context
   * @return a commerce search URL
   */
  Optional<UriComponents> provideRedirectUrl(@Nullable String term,
                                             HttpServletRequest request,
                                             StoreContext storeContext);
}
