package com.coremedia.ecommerce.studio.library {
import com.coremedia.cap.content.ContentTypeNames;
import com.coremedia.cap.content.search.SearchParameters;
import com.coremedia.cms.editor.sdk.ContentTreeRelation;
import com.coremedia.cms.editor.sdk.collectionview.*;
import com.coremedia.cms.editor.sdk.collectionview.sort.RepositoryListSorter;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.upload.UploadSettings;
import com.coremedia.ecommerce.studio.*;
import com.coremedia.ecommerce.studio.components.repository.CatalogRepositoryListContainer;
import com.coremedia.ecommerce.studio.components.repository.CatalogRepositoryToolbarContainer;
import com.coremedia.ecommerce.studio.components.search.CatalogSearchListContainer;
import com.coremedia.ecommerce.studio.components.search.CatalogSearchToolbarContainer;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Marketing;
import com.coremedia.ecommerce.studio.model.MarketingSpot;
import com.coremedia.ecommerce.studio.model.Product;
import com.coremedia.ecommerce.studio.model.SearchResult;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ecommerce.studio.tree.categoryTreeRelation;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;
import com.coremedia.ui.util.ObjectUtils;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.ecommerce.studio.ECommerceStudioPlugin')]
public class ECommerceCollectionViewExtension implements CollectionViewExtension {

  protected static const DEFAULT_TYPE_PRODUCT_RECORD:Object = {
    name: ContentTypeNames.CONTENT,
    label: ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'Product_label'),
    icon: ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'Product_icon')
  };

  /**
   * Since this is a common extension to be extended, it is not applicable, only subclasses of it.
   */
  public function isApplicable(model:Object):Boolean {
    return false;
  }

  /**
   * Not based on content, therefore no ContentTreeRelation.
   * @return
   */
  public function getContentTreeRelation():ContentTreeRelation {
    return null;
  }

  public function search(searchParameters:SearchParameters, callback:Function):void {
    var store:Store = CatalogHelper.getInstance().getActiveStoreExpression().getValue();

    if(store) {
      var catalogSearch:RemoteServiceMethod = new RemoteServiceMethod("livecontext/search/" + store.getSiteId(), "GET");

      //to object conversion
      var searchParams:Object = ObjectUtils.getPublicProperties(searchParameters);
      searchParams = ObjectUtils.removeUndefinedOrNullProperties(searchParams);

      catalogSearch.request(searchParams,
              function (response:RemoteServiceMethodResponse):void {
                var searchResult:SearchResult = new SearchResult();
                var responseObject:Object = response.getResponseJSON();
                searchResult.setHits(responseObject['hits']);
                searchResult.setTotal(responseObject['total']);
                callback.call(null, searchResult);
              });
    }
  }

  public function getSearchOrSearchSuggestionsParameters(filters:Object, mainStateBean:Bean):SearchParameters {
    var searchText:String = mainStateBean.get(CollectionViewModel.SEARCH_TEXT_PROPERTY);
    var catalogType:String = mainStateBean.get(CollectionViewModel.CONTENT_TYPE_PROPERTY);

    var searchParameters:SearchParameters = SearchParameters({});
    delete searchParameters['xclass'];

    var catalogObject:CatalogObject = mainStateBean.get(CollectionViewModel.FOLDER_PROPERTY);

    if (catalogObject is Category) {
      searchParameters['category'] = catalogObject.getExternalTechId() || catalogObject.getExternalId();
      searchParameters['catalogAlias'] = CatalogHelper.getInstance().getCatalogAliasFromId(catalogObject.getId());
    }

    searchParameters.query = searchText || "*";


    if (!catalogType || catalogType === ContentTypeNames.CONTENT) {
      // Cannot search in 'All' catalog objects, so fall back to guessed type depending on catalogObject type:
      catalogType = (catalogObject is Marketing) ? CatalogModel.TYPE_MARKETING_SPOT : CatalogModel.TYPE_PRODUCT;
    }

    searchParameters['searchType'] = catalogType;
    searchParameters['siteId'] = editorContext.getSitesService().getPreferredSiteId();
    searchParameters['workspaceId'] = CatalogHelper.getInstance().getExtractedWorkspaceId();
    return searchParameters;
  }

  public function getSearchSuggestionsUrl():String {
    return "api/livecontext/suggestions";
  }

  public function getSearchViewItemId():String {
    return CatalogSearchListContainer.VIEW_CONTAINER_ITEM_ID;
  }

  public function getAvailableSearchTypes(folder:Object):Array {
    return [DEFAULT_TYPE_PRODUCT_RECORD];
  }

  public function getRepositoryToolbarItemId():String {
    return CatalogRepositoryToolbarContainer.CATALOG_REPOSITORY_TOOLBAR_ITEM_ID;
  }

  public function getRepositoryListSorter():RepositoryListSorter {
    return null;
  }

  public function isSearchable():Boolean {
    return true;
  }

  public function isUploadDisabledFor(folder:Object):Boolean {
    return true;
  }

  public function upload(files:Array, folder:Object, settings:UploadSettings):void {
  }


  public function getSearchToolbarItemId():String {
    return CatalogSearchToolbarContainer.CATALOG_SEARCH_TOOLBAR_ITEM_ID;
  }

  public function getEnabledSearchFilterIds(model:Object):Array {
    return null;
  }

  public function getFolderContainerItemId():String {
    return CatalogRepositoryListContainer.VIEW_CONTAINER_ITEM_ID
  }

  public function getPathInfo(model:Object):String {
    var catalogObject:CatalogObject = model as CatalogObject;
    if (!catalogObject) {
      return "";
    }
    var namePath:Array = [];
    var store:Store = catalogObject.getStore();
    var isSingleRootCategory:Boolean = !store || !store.getCatalogs() ||
            store.getCatalogs().length <= 1;
    while (catalogObject) {
      //when multi-catalog is not configured there will be only one root category. then the root category should called
      // 'Product Catalog' for the sake of backward compatibility.
      namePath.push(isSingleRootCategory && catalogObject is Category && categoryTreeRelation.isRoot(catalogObject) ?
              ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'StoreTree_root_category') : catalogHelper.getDecoratedName(catalogObject));
      if (catalogObject is Product) {
        catalogObject = (catalogObject as Product).getCategory();
      } else if (catalogObject is Category) {
        catalogObject = (catalogObject as Category).getParent();
      } else if (catalogObject is MarketingSpot) {
        catalogObject = (catalogObject as MarketingSpot).getMarketing();
      } else {
        break;
      }
    }
    namePath.push(store.getName());
    return '/' + namePath.reverse().join('/');
  }
}
}
