package com.coremedia.livecontext.asset;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.util.LocaleHelper;
import com.coremedia.blueprint.cae.handlers.HandlerBase;
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
import com.coremedia.objectserver.web.HandlerHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CatalogPictureHandlerBase extends HandlerBase {

  private static final Logger LOG = LoggerFactory.getLogger(CatalogPictureHandlerBase.class);

  public static final String FORMAT_KEY_THUMBNAIL = "thumbnail";
  public static final String FORMAT_KEY_FULL = "full";

  protected AssetService assetService;
  private LiveContextSiteResolver siteResolver;
  private Map<String, String> pictureFormats;
  private TransformImageService transformImageService;
  protected CatalogAliasTranslationService catalogAliasTranslationService;

  protected static final String STORE_ID = "storeId";
  protected static final String CATALOG_ID = "catalogId";
  protected static final String LOCALE = "locale";
  protected static final String PART_NUMBER = "partNumber";
  protected static final String FORMAT_NAME = "formatName";

  /**
   * handle picture request with the given store id, locale, picture format name, reference id and request
   * @param storeId the store id
   * @param locale the locale
   * @param formatName the picture format name
   * @param commerceId the reference id
   * @param extension the mime type extension
   * @param request the request
   */
  @Nullable
  protected ModelAndView handleRequestWidthHeight(@NonNull String storeId,
                                                  @NonNull String locale,
                                                  @NonNull String formatName,
                                                  @NonNull CommerceId commerceId,
                                                  String extension,
                                                  @NonNull WebRequest request) {
    Locale localeObj = LocaleHelper.parseLocaleFromString(locale).orElse(null);
    Optional<Site> site = siteResolver.findSiteFor(storeId, localeObj);
    if (!site.isPresent()) {
      return HandlerHelper.notFound();
    }

    Content catalogPictureObject = findCatalogPictureFor(commerceId, site.get()).orElse(null);
    if (catalogPictureObject == null) {
      //Picture not found
      return HandlerHelper.notFound();
    }

    String pictureFormat = pictureFormats.get(formatName);
    if (pictureFormat == null) {
      //format not found
      return HandlerHelper.notFound();
    }

    //picture format value consists of <transformation segment>/<width>/<height>
    String[] split = pictureFormat.split("/");
    String transformationName = split[0];
    Integer width = Integer.parseInt(split[1]);
    Integer height = Integer.parseInt(split[2]);

    Optional<Blob> transformedBlob = transformImageService.transformWithDimensions(catalogPictureObject, "data", transformationName, width, height);
    if (!transformedBlob.isPresent()) {
      return HandlerHelper.notFound();
    }

    if (request.checkNotModified(transformedBlob.get().getETag())) {
      // shortcut exit - no further processing necessary
      return null;
    }

    return HandlerHelper.createModel(transformedBlob.get());
  }

  @NonNull
  protected CatalogAlias resolveCatalogAliasFromId(@NonNull CatalogId catalogId, @NonNull StoreContext storeContext) {
    return catalogAliasTranslationService.getCatalogAliasForId(catalogId, storeContext)
            .orElseGet(storeContext::getCatalogAlias);
  }

  /**
   * find the catalog picture of the given reference id and site
   * @param id the reference id
   * @param site the given site
   * @return the found catalog picture document
   */
  @NonNull
  private Optional<Content> findCatalogPictureFor(@NonNull CommerceId id, @NonNull Site site) {
    if (assetService == null) {
      return Optional.empty();
    }

    List<Content> pictures = assetService.findPictures(id, true);

    if (pictures.size() > 1) {
      LOG.debug("More than one CMPicture found for the catalog object with the id {} in the site {}", id,
              site.getName());
    }

    return pictures.stream().findFirst();
  }

  @Required
  public void setSiteResolver(LiveContextSiteResolver siteResolver) {
    this.siteResolver = siteResolver;
  }

  @Required
  public void setPictureFormats(Map<String, String> pictureFormats) {
    this.pictureFormats = pictureFormats;
  }

  @Autowired(required = false)
  public void setAssetService(AssetService assetService) {
    this.assetService = assetService;
  }

  @Autowired
  public void setCatalogAliasTranslationService(CatalogAliasTranslationService catalogAliasTranslationService) {
    this.catalogAliasTranslationService = catalogAliasTranslationService;
  }

  @Required
  public void setTransformImageService(TransformImageService transformImageService) {
    this.transformImageService = transformImageService;
  }
}
