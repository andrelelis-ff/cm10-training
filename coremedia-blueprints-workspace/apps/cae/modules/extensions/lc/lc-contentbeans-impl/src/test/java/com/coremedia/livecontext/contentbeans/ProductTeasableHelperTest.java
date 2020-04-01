package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.teaserOverlay.TeaserOverlaySettings;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import com.coremedia.xml.MarkupUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.CATALOG_TEASER_TEXT;
import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.CATALOG_TEASER_TEXT_MARKUP;
import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.CATALOG_TEASER_TITLE;
import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.CONTENT_DETAIL_TEXT;
import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.CONTENT_DETAIL_TEXT_PROPERTY;
import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.CONTENT_TEASER_TEXT;
import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.CONTENT_TEASER_TEXT_MARKUP;
import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.CONTENT_TEASER_TEXT_PROPERTY;
import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.CONTENT_TEASER_TITLE;
import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.CONTENT_TEASER_TITLE_PROPERTY;
import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.CONTENT_TITLE;
import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.CONTENT_TITLE_PROPERTY;
import static com.coremedia.livecontext.contentbeans.CMProductTeaserImplTest.EMPTY_MARKUP;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProductTeasableHelperTest {

  @Mock
  private Content content;

  @Mock
  private LiveContextProductTeasable productTeasable;

  @Mock
  private Product product;

  @Mock
  private SettingsService settingsService;

  @Mock
  private TeaserOverlaySettings teaserOverlaySettings;

  private ProductTeasableHelper testling;

  @Before
  public void setUp() {
    initMocks(this);

    when(teaserOverlaySettings.isEnabled()).thenReturn(false);

    when(settingsService.settingAsMap(CMTeasable.TEASER_OVERLAY_SETTINGS_STRUCT_NAME, String.class, Object.class, Content.class))
            .thenReturn(emptyMap());
    when(settingsService.createProxy(TeaserOverlaySettings.class, Object.class)).thenReturn(teaserOverlaySettings);

    when(productTeasable.getContent()).thenReturn(content);
    when(productTeasable.getExternalId()).thenReturn("vendor://catalog/product/id");
    when(productTeasable.getProduct()).thenReturn(product);

    testling = new ProductTeasableHelper();
    testling.setSettingsService(settingsService);
  }

  @Test
  public void testGetTeaserValuesFromContent() {
    String teaserTitle = testling.getTeaserTitleInternal(productTeasable, CONTENT_TEASER_TITLE);
    assertThat(teaserTitle).isEqualTo(CONTENT_TEASER_TITLE);

    Markup teaserText = testling.getTeaserTextInternal(productTeasable, createMarkup(CONTENT_TEASER_TEXT));
    assertThat(teaserText).isEqualTo(CONTENT_TEASER_TEXT_MARKUP);
  }

  @Test
  public void testGetTeaserValuesFromCatalogWhenPropertyEmpty() {
    when(product.getName()).thenReturn(CATALOG_TEASER_TITLE);
    when(product.getShortDescription()).thenReturn(createMarkup(CATALOG_TEASER_TEXT));

    String teaserTitle = testling.getTeaserTitleInternal(productTeasable, "");
    assertThat(teaserTitle).isEqualTo(CATALOG_TEASER_TITLE);

    Markup teaserText = testling.getTeaserTextInternal(productTeasable, EMPTY_MARKUP);
    assertThat(teaserText).isEqualTo(CATALOG_TEASER_TEXT_MARKUP);
  }

  @Test
  public void testGetTeaserValuesIfContentAndCatalogEmpty() {
    when(product.getName()).thenReturn("");
    when(product.getShortDescription()).thenReturn(null);

    when(product.getName()).thenReturn("");
    assertThat(testling.getTeaserTitleInternal(productTeasable, "")).isEmpty();

    when(product.getShortDescription()).thenReturn(EMPTY_MARKUP);
    assertThat(testling.getTeaserTextInternal(productTeasable, EMPTY_MARKUP)).isEqualTo(EMPTY_MARKUP);
  }

  @Test
  public void testGetTeaserValuesFromCatalog() {
    when(product.getName()).thenReturn(CATALOG_TEASER_TITLE);
    when(product.getShortDescription()).thenReturn(createMarkup(CATALOG_TEASER_TEXT));

    String teaserTitle = testling.getTeaserTitleInternal(productTeasable, null);
    assertThat(teaserTitle).isEqualTo(CATALOG_TEASER_TITLE);

    Markup teaserText = testling.getTeaserTextInternal(productTeasable, null);
    assertThat(teaserText).isEqualTo(CATALOG_TEASER_TEXT_MARKUP);
  }

  @Test
  public void testGetTeaserValuesWithCommerceException() {
    when(product.getName()).thenThrow(CommerceException.class);
    when(product.getShortDescription()).thenReturn(null);

    String teaserTitle = testling.getTeaserTitleInternal(productTeasable, CONTENT_TITLE);
    assertThat(teaserTitle).isEqualTo(CONTENT_TITLE);

    Markup teaserText = testling.getTeaserTextInternal(productTeasable, createMarkup(CONTENT_DETAIL_TEXT));
    assertThat(MarkupUtil.asPlainText(teaserText).trim()).isEqualTo(CONTENT_DETAIL_TEXT);
  }

  @Test
  public void testGetTeaserValuesWithNoProduct() {
    when(content.getString(CONTENT_TEASER_TITLE_PROPERTY)).thenReturn("");
    when(content.getMarkup(CONTENT_TEASER_TEXT_PROPERTY)).thenReturn(EMPTY_MARKUP);
    when(content.getString(CONTENT_TITLE_PROPERTY)).thenReturn(CONTENT_TITLE);
    when(content.getMarkup(CONTENT_DETAIL_TEXT_PROPERTY)).thenReturn(createMarkup(CONTENT_DETAIL_TEXT));

    when(productTeasable.getProduct()).thenReturn(null);

    String teaserTitle = testling.getTeaserTitleInternal(productTeasable, null);
    Markup teaserText = testling.getTeaserTextInternal(productTeasable, null);

    assertThat(teaserTitle).isNull();
    assertThat(teaserText).isNull();
  }

  static Markup createMarkup(String value) {
    String markupData
            = "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">"
            + "<p>"
            + value
            + "</p>"
            + "</div>";
    return MarkupFactory.fromString(markupData);
  }
}
