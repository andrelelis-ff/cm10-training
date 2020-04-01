package com.coremedia.livecontext.ecommerce.ibm.link;

import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractWcWrapperService;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestServiceMethod;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Optional;

import static com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper.getStoreId;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

public class WcPreviewTokenWrapperService extends AbstractWcWrapperService {

  private static final WcRestServiceMethod<WcPreviewToken, WcPreviewTokenParam> PREVIEW_TOKEN = WcRestServiceMethod
          .builder(HttpMethod.POST, "store/{storeId}/previewToken", WcPreviewTokenParam.class, WcPreviewToken.class)
          .secure(true)
          .requiresAuthentication(true)
          .build();

  @NonNull
  public Optional<WcPreviewToken> getPreviewToken(WcPreviewTokenParam bodyData, StoreContext storeContext) {
    try {
      List<String> variableValues = singletonList(getStoreId(storeContext));
      return getRestConnector().callService(PREVIEW_TOKEN, variableValues, emptyMap(), bodyData, storeContext, null);
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }
}
