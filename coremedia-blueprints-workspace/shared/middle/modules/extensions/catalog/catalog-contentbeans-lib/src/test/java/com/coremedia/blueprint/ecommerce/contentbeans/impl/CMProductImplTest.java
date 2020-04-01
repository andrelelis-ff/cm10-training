package com.coremedia.blueprint.ecommerce.contentbeans.impl;

import com.coremedia.blueprint.base.ecommerce.catalog.CmsCatalogService;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.blueprint.ecommerce.contentbeans.CMProduct;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.ecommerce.asset.CatalogPicture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CMProductImplTest {

  @Mock
  private Content contentPic1;

  @Mock
  private Content contentPic2;

  @Mock
  private Content productPic1;

  @Mock
  private Content productPic2;

  @Mock
  private CMPicture contentPic1Bean;

  @Mock
  private CMPicture contentPic2Bean;

  @Mock
  private CMPicture productPic1Bean;

  @Mock
  private CMPicture productPic2Bean;

  @Mock
  private CmsCatalogService catalogService;


  @SuppressWarnings("Duplicates")
  @Test
  public void testGetProductPictureFromContent() {
    when(contentPic1Bean.getContent()).thenReturn(contentPic1);
    when(contentPic2Bean.getContent()).thenReturn(contentPic2);

    CMProduct testling = createTestling();
    contentPictures(testling, contentPic1Bean, contentPic2Bean);

    List<CatalogPicture> productPictures = testling.getProductPictures();
    assertSamePictures(productPictures, contentPic1, contentPic2);

    CatalogPicture productPicture = testling.getProductPicture();
    assertEquals(contentPic1, productPicture.getPicture());
  }

  @SuppressWarnings("Duplicates")
  @Test
  public void testGetProductPictureFromProduct() {
    when(productPic1Bean.getContent()).thenReturn(productPic1);
    when(productPic2Bean.getContent()).thenReturn(productPic2);

    CMProduct testling = createTestling();
    contentPictures(testling, productPic1Bean, productPic2Bean);

    List<CatalogPicture> productPictures = testling.getProductPictures();
    assertSamePictures(productPictures, productPic1, productPic2);

    CatalogPicture productPicture = testling.getProductPicture();
    assertEquals(productPic1, productPicture.getPicture());
  }

  @Test
  public void testGetProductPictureNoProductFound() {
    CMProduct testling = createTestling();
    contentPictures(testling);

    List<CatalogPicture> productPictures = testling.getProductPictures();

    assertNotNull(productPictures);
    assertThat(productPictures.isEmpty(), is(true));
  }

  public CMProduct createTestling() {
    CMProductImpl testling = Mockito.spy(new CMProductImpl());
    testling.setCatalogService(catalogService);
    return testling;
  }

  private static void assertSamePictures(List<CatalogPicture> actualArgs, Content... expectedArgs) {
    List<Content> expected = Arrays.asList(expectedArgs);
    List<Content> actual = actualArgs.stream().map(CatalogPicture::getPicture).collect(Collectors.toList());

    assertEquals(actual, expected);
  }

  private void contentPictures(CMProduct testling, CMPicture... contentPictures) {
    Mockito.doReturn(Arrays.asList(contentPictures)).when(testling).getPictures();  // Teasable.getPictures()
  }

}
