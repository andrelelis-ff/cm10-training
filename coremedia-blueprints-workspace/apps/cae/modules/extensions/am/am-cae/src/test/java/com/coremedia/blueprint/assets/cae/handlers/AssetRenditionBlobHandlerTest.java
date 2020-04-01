package com.coremedia.blueprint.assets.cae.handlers;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.blueprint.cae.handlers.CapBlobHandler;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.web.HandlerHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionBlobHandlerTest {

  @InjectMocks
  private AssetRenditionBlobHandler assetRenditionBlobHandler;

  @Mock
  private CapBlobHandler capBlobHandler;

  @Mock
  private AMAsset asset;

  @Mock
  private AMAssetRendition amAssetRendition;

  @Mock
  private AMAssetRendition amAssetRenditionWithoutBlob;

  @Mock
  private HttpServletResponse response;

  @Mock
  private ModelAndView modelAndView;

  @Before
  public void setUp() {
    when(capBlobHandler.handleRequest(any(ContentBean.class), nullable(String.class), anyString(), nullable(String.class), nullable(WebRequest.class), any())).thenReturn(modelAndView);
  }


  @Test
  public void handleAssetRenditionRequest_validRendition_delegatedToCapBlobHandler() {
    String requestedRendition = "web";

    when(asset.getPublishedRenditions()).thenReturn(Collections.singletonList(amAssetRendition));
    when(amAssetRendition.getName()).thenReturn(requestedRendition);

    ModelAndView result = assetRenditionBlobHandler.handleAssetRenditionRequest(asset, null, requestedRendition, null, null, response);

    assertNotNull("modelAndView should never be null for a valid request", modelAndView);
    assertEquals("modelAndView should be the expected mock", modelAndView, result);
  }

  @Test
  public void handleAssetRenditionRequest_invalidRendition_returnsNotFound() {
    String requestedRendition = "web";

    when(asset.getPublishedRenditions()).thenReturn(Collections.singletonList(amAssetRendition));
    when(amAssetRendition.getName()).thenReturn("print");

    ModelAndView result = assetRenditionBlobHandler.handleAssetRenditionRequest(asset, null, requestedRendition, null, null, response);

    assertNotNull("modelAndView should never be null", modelAndView);
    assertEquals("modelAndView should be the notFound-ModelAndView", HandlerHelper.notFound().getModel(), result.getModel());
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildLink_validBean_assetWithoutBlob() {
    assetRenditionBlobHandler.buildRenditionLink(amAssetRenditionWithoutBlob);
  }

}
