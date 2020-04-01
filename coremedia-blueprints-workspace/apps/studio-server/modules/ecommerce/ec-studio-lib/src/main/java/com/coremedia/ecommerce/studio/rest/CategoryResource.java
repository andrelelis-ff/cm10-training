package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceBean;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.ecommerce.studio.rest.model.ChildRepresentation;
import com.coremedia.ecommerce.studio.rest.model.Facets;
import com.coremedia.ecommerce.studio.rest.model.Store;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A catalog {@link Category} object as a RESTful resource.
 */
@RestController
@RequestMapping(value = CategoryResource.URI_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryResource extends CommerceBeanResource<Category> {

  static final String PATH_TYPE = "category";
  static final String URI_PATH
          = "livecontext/" + PATH_TYPE + "/{" + PATH_SITE_ID + "}/{" + PATH_CATALOG_ALIAS + "}/{" + PATH_WORKSPACE_ID + "}/{id:.+}";

  @Autowired
  public CategoryResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @Override
  protected CategoryRepresentation getRepresentation(@NonNull Map<String, String> params) {
    CategoryRepresentation categoryRepresentation = new CategoryRepresentation();
    fillRepresentation(params, categoryRepresentation);
    return categoryRepresentation;
  }

  protected void fillRepresentation(@NonNull Map<String, String> params, CategoryRepresentation representation) {
    super.fillRepresentation(params, representation);
    Category entity = getEntity(params);
    representation.setName(entity.getName());
    Markup shortDescription = entity.getShortDescription();
    if (shortDescription != null) {
      representation.setShortDescription(shortDescription.asXml());
    }
    Markup longDescription = entity.getLongDescription();
    if (longDescription != null) {
      representation.setLongDescription(longDescription.asXml());
    }
    representation.setThumbnailUrl(RepresentationHelper.modifyAssetImageUrl(entity.getThumbnailUrl(), getContentRepositoryResource().getContentRepository()));
    representation.setParent(entity.getParent());
    representation.setSubCategories(entity.getChildren());
    representation.setProducts(entity.getProducts());
    representation.setStore(new Store(entity.getContext()));
    AbstractCommerceBean.getCatalog(entity).ifPresent(representation::setCatalog);
    representation.setDisplayName(entity.getDisplayName());

    List<CommerceBean> children = new ArrayList<>();
    children.addAll(representation.getSubCategories());
    children.addAll(representation.getProducts());
    representation.setChildren(children);
    representation.setPictures(entity.getPictures());
    representation.setDownloads(entity.getDownloads());

    Map<String, ChildRepresentation> result = new LinkedHashMap<>();
    for (CommerceBean child : children) {
      ChildRepresentation childRepresentation = new ChildRepresentation();
      childRepresentation.setChild(child);
      childRepresentation.setDisplayName(child.getExternalId());
      result.put(CommerceIdFormatterHelper.format(child.getId()), childRepresentation);
    }
    representation.setChildrenByName(result);

    representation.setContent(getContent(params));

    Facets facets = new Facets(entity.getContext());
    facets.setId(entity.getExternalId());
    representation.setFacets(facets);
  }

  @Override
  protected Category doGetEntity(@NonNull Map<String, String> params) {
    Optional<StoreContext> storeContextOptional = getStoreContext(params);
    if (!storeContextOptional.isPresent()) {
      return null;
    }

    StoreContext storeContext = storeContextOptional.get();
    CommerceConnection commerceConnection = storeContext.getConnection();

    CatalogAlias catalogAlias = storeContext.getCatalogAlias();
    CommerceId commerceId = commerceConnection.getIdProvider().formatCategoryId(catalogAlias, params.get(PATH_ID));
    return commerceConnection.getCatalogService().findCategoryById(commerceId, storeContext);
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Category category) {
    Map<String, String> params = super.getPathVariables(category);
    params.put(PATH_ID, category.getExternalId());
    return params;
  }

  @Override
  @Autowired(required = false)
  @Qualifier("categoryAugmentationService")
  public void setAugmentationService(AugmentationService augmentationService) {
    super.setAugmentationService(augmentationService);
  }
}
