package com.coremedia.blueprint.image.transformation;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.coderesources.ThemeService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.transform.VariantsStructResolver;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Optional;

import static java.util.Optional.empty;

/**
 * Responsible for reading the struct with the image variants from the root channel.
 */
class VariantsStructResolverImpl implements VariantsStructResolver {

  private SettingsService settingsService;
  private ContentRepository contentRepository;
  private ThemeService themeService;

  private static final String VARIANTS_STRUCT_NAME = "responsiveImageSettings";
  private static final String SETTINGS_PROPERTY = "settings";
  private static final String SETTINGS_DOCTYPE = "CMSettings";

  private static final String GLOBAL_VARIANTS_SETTINGS = "/Settings/Options/Settings/Responsive Image Settings";

  VariantsStructResolverImpl(@NonNull SettingsService settingsService,
                             @NonNull ContentRepository contentRepository,
                             @NonNull ThemeService themeService) {
    this.settingsService = settingsService;
    this.contentRepository = contentRepository;
    this.themeService = themeService;
  }

  @Nullable
  @Override
  public Struct getVariantsForSite(@NonNull Site site) {
    Content theme = null;
    Content siteRootDocument = site.getSiteRootDocument();
    // since 1907 themes may also contain responsive image settings
    if (siteRootDocument != null) {
      theme = themeService.theme(siteRootDocument, null);
    }
    Struct setting = settingsService.setting(VARIANTS_STRUCT_NAME, Struct.class, site, theme);

    if (setting == null) {
      setting = getGlobalVariants().orElse(null);
    }

    return setting;
  }

  @NonNull
  private Optional<Struct> getGlobalVariants() {
    Content settings = contentRepository.getChild(GLOBAL_VARIANTS_SETTINGS);

    if (settings == null) {
      return empty();
    }

    return getStruct(settings);
  }

  @NonNull
  private static Optional<Struct> getStruct(@NonNull Content setting) {
    if (setting.getType().isSubtypeOf(SETTINGS_DOCTYPE)) {
      Struct subStruct = setting.getStruct(SETTINGS_PROPERTY);

      // Find settings document that contains the struct with the configured name.
      if (subStruct.toNestedMaps().containsKey(VARIANTS_STRUCT_NAME)) {
        Struct variantsStruct = subStruct.getStruct(VARIANTS_STRUCT_NAME);
        return Optional.ofNullable(variantsStruct);
      }
    }

    return empty();
  }
}
