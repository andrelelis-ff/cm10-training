package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceBean;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.ecommerce.studio.rest.model.Store;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.xml.Markup;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;

/**
 * A catalog {@link Product} object as a RESTful resource.
 */
@RestController
@RequestMapping(value = ProductResource.URI_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductResource extends CommerceBeanResource<Product> {

  static final String PATH_TYPE = "product";
  static final String URI_PATH = "livecontext/" + PATH_TYPE + "/{" + PATH_SITE_ID + "}/{" + PATH_CATALOG_ALIAS + "}/{" + PATH_WORKSPACE_ID + "}/{id:.+}";

  @Autowired
  public ProductResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @Override
  protected ProductRepresentation getRepresentation(@NonNull Map<String, String> params) {
    ProductRepresentation representation = new ProductRepresentation();
    fillRepresentation(params, representation);
    return representation;
  }

  protected void fillRepresentation(@NonNull Map<String, String> params, ProductRepresentation representation) {
    Product product = getEntity(params);
    super.fillRepresentation(params, representation);

    representation.setName(product.getName());
    Markup shortDescription = product.getShortDescription();
    if (shortDescription != null) {
      representation.setShortDescription(shortDescription.asXml());
    }
    Markup longDescription = product.getLongDescription();
    if (longDescription != null) {
      representation.setLongDescription(longDescription.asXml());
    }
    String thumbnailUrl = product.getThumbnailUrl();
    representation.setThumbnailUrl(
            RepresentationHelper.modifyAssetImageUrl(thumbnailUrl, getContentRepositoryResource().getContentRepository()));
    representation.setCategory(product.getCategory());
    representation.setStore(new Store(product.getContext()));
    AbstractCommerceBean.getCatalog(product).ifPresent(representation::setCatalog);
    representation.setOfferPrice(product.getOfferPrice());
    representation.setListPrice(product.getListPrice());

    Currency currency = product.getCurrency();
    Locale locale = product.getLocale();
    if (currency != null && locale != null) {
      representation.setCurrency(currency.getSymbol(locale));
    }

    representation.setVariants(product.getVariants());
    representation.setPictures(product.getPictures());
    representation.setDownloads(product.getDownloads());
    representation.setDescribingAttributes(product.getDescribingAttributes());
    representation.setContent(getContent(params));
  }

  @Override
  protected Product doGetEntity(@NonNull Map<String, String> params) {
    StoreContext storeContext = getStoreContext(params)
            .orElseThrow(() -> new IllegalArgumentException("No store context available."));
    CommerceConnection connection = storeContext.getConnection();

    CommerceId commerceId = connection.getIdProvider()
            .formatProductId(storeContext.getCatalogAlias(), params.get(PATH_ID));

    return connection.getCatalogService()
            .findProductById(commerceId, storeContext);
  }

  @Autowired(required = false)
  @Qualifier("productAugmentationService")
  @Override
  public void setAugmentationService(AugmentationService augmentationService) {
    super.setAugmentationService(augmentationService);
  }
}
