package com.coremedia.livecontext.ecommerce.ibm.link;

import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.link.PreviewUrlService;
import com.coremedia.livecontext.ecommerce.link.QueryParam;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singleton;

@DefaultAnnotation(NonNull.class)
public class PreviewUrlServiceImpl implements PreviewUrlService {

  private static final String PREVIEW_TOKEN_PARAM = "previewToken";

  private final PreviewTokenService previewTokenService;
  private final WcsUrlProvider wcsUrlProvider;

  private String contentUrlKeyword = "cm";

  public PreviewUrlServiceImpl(PreviewTokenService previewTokenService, WcsUrlProvider wcsUrlProvider) {
    this.previewTokenService = previewTokenService;
    this.wcsUrlProvider = wcsUrlProvider;
  }

  @Override
  public UriComponents getCategoryUrl(Category category, StoreContext storeContext, List<QueryParam> linkParameters, HttpServletRequest request) {
    UriComponentsBuilder builder = wcsUrlProvider.buildCategoryLink(category, linkParameters, true)
            .orElseThrow(() -> new CommerceException(
                    "Could not build preview URL for category '" + category.getId() + "'."));

    return setPreviewTokenParam(builder, storeContext);
  }

  @Override
  public UriComponents getProductUrl(Product product, @Nullable Category category, StoreContext storeContext, List<QueryParam> linkParameters,
                                     HttpServletRequest request) {
    UriComponentsBuilder builder = wcsUrlProvider.buildProductLink(product, linkParameters, true)
            .orElseThrow(() -> new CommerceException(
                    "Could not build preview URL for product '" + product.getId() + "'."));

    return setPreviewTokenParam(builder, storeContext);
  }

  @Override
  public UriComponents getExternalPageSeoUrl(@Nullable String seoPath, StoreContext storeContext,
                                             List<QueryParam> linkParameters, HttpServletRequest request) {
    UriComponentsBuilder builder = wcsUrlProvider
            .buildExternalPageSeoLink(seoPath, linkParameters, true, storeContext)
            .orElseThrow(() -> new CommerceException(
                    "Could not build preview URL for external page with SEO path '" + seoPath + "'."));

    return setPreviewTokenParam(builder, storeContext);
  }

  @Override
  public UriComponents getExternalPageNonSeoUrl(String nonSeoPath, StoreContext storeContext,
                                                          List<QueryParam> linkParameters, HttpServletRequest request) {
    UriComponentsBuilder builder = wcsUrlProvider
            .buildExternalPageNonSeoLink(nonSeoPath, linkParameters, true, storeContext)
            .orElseThrow(() -> new CommerceException(
                    "Could not build preview URL for external page with non-SEO path '" + nonSeoPath + "'."));

    return setPreviewTokenParam(builder, storeContext);
  }

  @Override
  public UriComponents getContentUrl(@Nullable String seoPath, StoreContext storeContext,
                                     List<QueryParam> linkParameters, HttpServletRequest request) {
    String seoSegments = seoPath != null ? contentUrlKeyword + "/" + seoPath : "";

    UriComponentsBuilder builder = wcsUrlProvider.buildShopLink(seoSegments, linkParameters, true, storeContext)
            .orElseThrow(() -> new CommerceException(
                    "Could not build preview URL for content (SEO path: '" + seoPath + "')."));

    return setPreviewTokenParam(builder, storeContext);
  }

  private UriComponents setPreviewTokenParam(UriComponentsBuilder builder, StoreContext storeContext) {
    String previewToken = previewTokenService.getPreviewToken(storeContext);

    return builder
            .queryParam(PREVIEW_TOKEN_PARAM, previewToken)
            .build();
  }

  @Override
  public Set<String> getParameterNames() {
    return singleton(PREVIEW_TOKEN_PARAM);
  }

  public void setContentUrlKeyword(String contentUrlKeyword) {
    this.contentUrlKeyword = contentUrlKeyword;
  }
}
