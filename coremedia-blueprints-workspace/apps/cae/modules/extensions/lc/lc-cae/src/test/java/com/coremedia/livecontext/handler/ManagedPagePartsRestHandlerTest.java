package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;
import java.util.Optional;

import static com.coremedia.livecontext.handler.ManagedPagePartsRestHandler.MANAGED_FOOTER_KEY;
import static com.coremedia.livecontext.handler.ManagedPagePartsRestHandler.MANAGED_FOOTER_NAVIGATION_KEY;
import static com.coremedia.livecontext.handler.ManagedPagePartsRestHandler.MANAGED_HEADER_KEY;
import static com.coremedia.livecontext.handler.ManagedPagePartsRestHandler.MANAGED_NAVIGATION_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ManagedPagePartsRestHandlerTest {

  private static final String ANY_VALID_STOREID = "mystoreid";
  private static final Locale VALID_LOCALE = Locale.US;
  private static final String ANY_NON_EXISTENT = "anyNonExistent";

  @Mock
  private LiveContextSiteResolver siteResolver;

  @Mock
  private SettingsService settingsService;

  @InjectMocks
  private ManagedPagePartsRestHandler testling;

  @Test
  public void managedPagePartsHandler() throws Exception {
    Content siteRootDocument = mockSiteRootDocument(ANY_VALID_STOREID, VALID_LOCALE);

    when(settingsService.getSetting(MANAGED_FOOTER_KEY, Boolean.class, siteRootDocument)).thenReturn(Optional.of(true));
    when(settingsService.getSetting(MANAGED_HEADER_KEY, Boolean.class, siteRootDocument)).thenReturn(Optional.of(true));
    when(settingsService.getSetting(MANAGED_NAVIGATION_KEY, Boolean.class, siteRootDocument)).thenReturn(Optional.of(true));
    when(settingsService.getSetting(MANAGED_FOOTER_NAVIGATION_KEY, Boolean.class, siteRootDocument)).thenReturn(Optional.of(true));

    ResponseEntity<ManagedPagePartsSettings> actual = testling.managedPagePartsHandler(ANY_VALID_STOREID, VALID_LOCALE);

    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(actual.getBody().isManagedFooter()).isTrue();
    assertThat(actual.getBody().isManagedHeader()).isTrue();
    assertThat(actual.getBody().isManagedNavigation()).isTrue();
    assertThat(actual.getBody().isManagedFooterNavigation()).isTrue();
  }

  @Test
  public void managedPagePartsHandlerNoSiteFound() throws Exception {
    when(siteResolver.findSiteFor(ANY_NON_EXISTENT, VALID_LOCALE)).thenReturn(Optional.empty());

    ResponseEntity<ManagedPagePartsSettings> actual = testling.managedPagePartsHandler(ANY_NON_EXISTENT, VALID_LOCALE);

    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void managedPagePartsHandlerHandleNullFromSettingsService() throws Exception {
    Content siteRootDocument = mockSiteRootDocument(ANY_VALID_STOREID, VALID_LOCALE);
    when(settingsService.getSetting(MANAGED_FOOTER_KEY, Boolean.class, siteRootDocument)).thenReturn(Optional.empty());
    when(settingsService.getSetting(MANAGED_HEADER_KEY, Boolean.class, siteRootDocument)).thenReturn(Optional.empty());
    when(settingsService.getSetting(MANAGED_NAVIGATION_KEY, Boolean.class, siteRootDocument)).thenReturn(Optional.empty());
    when(settingsService.getSetting(MANAGED_FOOTER_NAVIGATION_KEY, Boolean.class, siteRootDocument)).thenReturn(Optional.empty());

    ResponseEntity<ManagedPagePartsSettings> actual = testling.managedPagePartsHandler(ANY_VALID_STOREID, VALID_LOCALE);

    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(actual.getBody().isManagedFooter()).isFalse();
    assertThat(actual.getBody().isManagedHeader()).isFalse();
    assertThat(actual.getBody().isManagedNavigation()).isFalse();
    assertThat(actual.getBody().isManagedFooterNavigation()).isFalse();
  }

  private Content mockSiteRootDocument(String storeId, Locale locale) {
    Site site = mockSite();
    Content siteRootDocument = mock(Content.class);
    when(site.getSiteRootDocument()).thenReturn(siteRootDocument);
    when(siteResolver.findSiteFor(storeId, locale)).thenReturn(Optional.of(site));
    return siteRootDocument;
  }

  private Site mockSite() {
    return mock(Site.class);
  }
}
