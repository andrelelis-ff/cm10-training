package com.coremedia.livecontext.asset;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.transform.TransformImageService;
import com.coremedia.livecontext.ecommerce.asset.AssetService;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import com.coremedia.objectserver.web.HttpError;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService.DEFAULT_CATALOG_ALIAS;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CMCatalogPictureHandlerTest {

  @InjectMocks
  private ProductCatalogPictureHandler testling = new ProductCatalogPictureHandler();

  @Mock
  private LiveContextSiteResolver siteResolver;

  @Mock
  private Site site;

  @Mock
  private AssetService assetService;

  @Mock
  private TransformImageService transformImageService;

  @Mock
  private Content pictureContent;

  @Mock
  private CatalogAliasTranslationService catalogAliasTranslationService;

  private static final CommerceId PRODUCT_REFERENCE = CommerceIdParserHelper
          .parseCommerceIdOrThrow("vendor:///catalog/product/PC_SUMMER_DRESS");

  @Before
  public void setUp() {
    Map<String, String> pictureFormats = ImmutableMap.<String, String>builder()
            .put("thumbnail", "portrait_ratio20x31/200/310")
            .put("full", "portrait_ratio20x31/646/1000")
            .build();
    testling.setPictureFormats(pictureFormats);

    testling.setTransformImageService(transformImageService);

    testling.setCatalogAliasTranslationService(catalogAliasTranslationService);
  }

  @Test
  public void testHandleRequestWithSiteNull() {
    when(siteResolver.findSiteFor(anyString(), any(Locale.class))).thenReturn(Optional.empty());

    ModelAndView result = testling.handleRequestWidthHeight(
            "10201", "en_US", "full", PRODUCT_REFERENCE, "jpg", mock(WebRequest.class)
    );

    assert404(result);
  }

  @Test
  public void testHandleRequestWithPictureFormatsEmpty() {
    when(siteResolver.findSiteFor(anyString(), any(Locale.class))).thenReturn(Optional.of(site));
    testling.setPictureFormats(Collections.emptyMap());

    ModelAndView result = testling.handleRequestWidthHeight(
            "10201", "en_US", "full", PRODUCT_REFERENCE, "jpg", mock(WebRequest.class)
    );

    assert404(result);
  }

  @Test
  public void testHandleRequestNoPictureFound() {
    when(siteResolver.findSiteFor(anyString(), any(Locale.class))).thenReturn(Optional.of(site));

    ModelAndView result = testling.handleRequestWidthHeight(
            "10201", "en_US", "full", PRODUCT_REFERENCE, "jpg", mock(WebRequest.class)
    );

    assert404(result);
  }

  @Test
  public void testHandleRequestSuccess() {
    prepareSuccessRequest();

    ModelAndView result = testling.handleRequestWidthHeight(
            "10201", "en_US", "full", PRODUCT_REFERENCE, "jpg", mock(WebRequest.class)
    );

    assert200(result);
  }

  @Test
  public void testHandleRequestSuccessCached() {
    WebRequest request = mock(WebRequest.class);
    when(request.checkNotModified(nullable(String.class))).thenReturn(true);

    prepareSuccessRequest();

    ModelAndView result = testling.handleRequestWidthHeight(
            "10201", "en_US", "full", PRODUCT_REFERENCE, "jpg", request
    );

    assert304(result);
  }

  @Test
  public void testResolveCatalogAliasFromId() {
    StoreContext storeContext = mock(StoreContext.class);
    when(storeContext.getCatalogAlias()).thenReturn(DEFAULT_CATALOG_ALIAS);
    when(catalogAliasTranslationService.getCatalogAliasForId(CatalogId.of("catalogId"), storeContext ))
            .thenReturn(Optional.of(CatalogAlias.of("catalogAlias")));

    CatalogAlias catalogAlias = testling.resolveCatalogAliasFromId(CatalogId.of("catalogId"), storeContext);
    assertThat(catalogAlias).isEqualTo(CatalogAlias.of("catalogAlias"));

    CatalogAlias catalogAliasNotFound = testling.resolveCatalogAliasFromId(CatalogId.of("unknownId"), storeContext);
    assertThat(catalogAliasNotFound).isEqualTo(DEFAULT_CATALOG_ALIAS);
  }

  private void prepareSuccessRequest() {
    when(siteResolver.findSiteFor(anyString(), any(Locale.class))).thenReturn(Optional.of(site));
    when(assetService.findPictures(PRODUCT_REFERENCE, true)).thenReturn(newArrayList(pictureContent));
    when(transformImageService.transformWithDimensions(any(Content.class), eq("data"), anyString(), anyInt(), anyInt()))
            .thenReturn(Optional.of(mock(Blob.class)));
  }

  private void assert404(ModelAndView result) {
    Object self = result.getModel().get("self");
    assertThat(self).isInstanceOf(HttpError.class);

    HttpError error = (HttpError) self;
    assertThat(error.getErrorCode()).isEqualTo(404);
  }

  private void assert304(ModelAndView result) {
    assertThat(result).isNull();
  }

  private void assert200(ModelAndView result) {
    Object self = result.getModel().get("self");
    assertThat(self).isInstanceOf(Blob.class);
  }
}
