package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Locale;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * REST Handler to resolve Settings from the Content Managed System.
 * <p>
 * This REST Handler provides the information if the CMS can provide some parts of a page for an augmentation scenario
 * in LiveContext.
 */
@Controller
public class ManagedPagePartsRestHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ManagedPagePartsRestHandler.class);

  /**
   * settings name in the CMS which indicates if the navigation can be provided.
   */
  static final String MANAGED_NAVIGATION_KEY = "livecontext.manageNavigation";

  /**
   * settings name in the CMS which indicates if the header can be provided.
   */
  static final String MANAGED_HEADER_KEY = "livecontext.manageHeader";

  /**
   * settings name in the CMS which indicates if the footer can be provided.
   */
  static final String MANAGED_FOOTER_KEY = "livecontext.manageFooter";

  /**
   * settings name in the CMS which indicates if the footer navigation can be provided.
   */
  static final String MANAGED_FOOTER_NAVIGATION_KEY = "livecontext.manageFooterNavigation";

  private static final String PATH = "service/lcsettings/{storeId}/{locale}/managedPageParts";
  private static final boolean DEFAULT_VALUE = Boolean.FALSE;

  private LiveContextSiteResolver siteResolver;
  private SettingsService settingsService;

  public ManagedPagePartsRestHandler(@NonNull LiveContextSiteResolver siteResolver,
                                     @NonNull SettingsService settingsService) {
    requireNonNull(siteResolver);
    requireNonNull(settingsService);

    this.siteResolver = siteResolver;
    this.settingsService = settingsService;
  }

  @GetMapping(value = PATH, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ManagedPagePartsSettings> managedPagePartsHandler(@PathVariable("storeId") String storeId,
                                                                          @PathVariable("locale") Locale locale) {
    requireNonNull(storeId);
    requireNonNull(locale);

    Optional<Site> site = siteResolver.findSiteFor(storeId, locale);

    if (!site.isPresent()) {
      LOG.info("No site found for storeId '{}' and locale '{}'.", storeId, locale);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    Content siteRootDocument = site.get().getSiteRootDocument();

    boolean managedNavigation = getBooleanSetting(MANAGED_NAVIGATION_KEY, siteRootDocument);
    boolean managedHeader = getBooleanSetting(MANAGED_HEADER_KEY, siteRootDocument);
    boolean managedFooter = getBooleanSetting(MANAGED_FOOTER_KEY, siteRootDocument);
    boolean managedFooterNavigation = getBooleanSetting(MANAGED_FOOTER_NAVIGATION_KEY, siteRootDocument);

    ManagedPagePartsSettings settings = new ManagedPagePartsSettings();
    settings.setManagedFooter(managedFooter);
    settings.setManagedHeader(managedHeader);
    settings.setManagedNavigation(managedNavigation);
    settings.setManagedFooterNavigation(managedFooterNavigation);
    return new ResponseEntity<>(settings, HttpStatus.OK);
  }

  private boolean getBooleanSetting(@NonNull String name, @NonNull Content siteRootDocument) {
    return settingsService.getSetting(name, Boolean.class, siteRootDocument).orElse(DEFAULT_VALUE);
  }
}
