package com.coremedia.livecontext.asset;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.livecontext.ecommerce.asset.AssetService;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_EXTENSION;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_EXTENSION;
import static com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService.DEFAULT_CATALOG_ALIAS;

@RequestMapping
public class CategoryCatalogPictureHandler extends CatalogPictureHandlerBase {

  /**
   * URI Pattern for transformed blobs for categories
   * e.g. /catalogimage/category/10202/en_US/full/PC_Deli.jpg
   */
  public static final String IMAGE_URI_PATTERN =
          "/" + AssetService.CATEGORY_URI_PREFIX +
                  "/{" + STORE_ID + "}" +
                  "/{" + LOCALE + "}" +
                  "/{" + FORMAT_NAME + "}" +
                  "/{" + PART_NUMBER + "}" +
                  ".{" + SEGMENT_EXTENSION + ":" + PATTERN_EXTENSION + "}";

  /**
   * e.g. /catalogimage/category/[storeId]/en_US/[catalogId]/full/PC_Deli.jpg
   */
  public static final String IMAGE_URI_PATTERN_FOR_CATALOG =
          "/" + AssetService.CATEGORY_URI_PREFIX +
                  "/{" + STORE_ID + "}" +
                  "/{" + LOCALE + "}" +
                  "/{" + CATALOG_ID + "}" +
                  "/{" + FORMAT_NAME + "}" +
                  "/{" + PART_NUMBER + "}" +
                  ".{" + SEGMENT_EXTENSION + ":" + PATTERN_EXTENSION + "}";

  @GetMapping(value = IMAGE_URI_PATTERN_FOR_CATALOG)
  public ModelAndView handleRequestWidthHeightForCategoryWithCatalog(@PathVariable(STORE_ID) String storeId,
                                                                     @PathVariable(LOCALE) String locale,
                                                                     @PathVariable(FORMAT_NAME) String formatName,
                                                                     @PathVariable(PART_NUMBER) String partNumber,
                                                                     @PathVariable(SEGMENT_EXTENSION) String extension,
                                                                     @PathVariable(CATALOG_ID) String catalogId,
                                                                     WebRequest request) {
    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection connection = storeContext.getConnection();

    CatalogAlias catalogAlias = resolveCatalogAliasFromId(CatalogId.of(catalogId), storeContext);
    CommerceId id = connection.getIdProvider().formatCategoryId(catalogAlias, partNumber);

    return handleRequestWidthHeight(storeId, locale, formatName, id, extension, request);
  }

  @GetMapping(value = IMAGE_URI_PATTERN)
  public ModelAndView handleRequestWidthHeightForCategory(@PathVariable(STORE_ID) String storeId,
                                                          @PathVariable(LOCALE) String locale,
                                                          @PathVariable(FORMAT_NAME) String formatName,
                                                          @PathVariable(PART_NUMBER) String partNumber,
                                                          @PathVariable(SEGMENT_EXTENSION) String extension,
                                                          WebRequest request) {
    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection connection = storeContext.getConnection();

    CommerceId id = connection.getIdProvider().formatCategoryId(DEFAULT_CATALOG_ALIAS, partNumber);

    return handleRequestWidthHeight(storeId, locale, formatName, id, extension, request);
  }
}
