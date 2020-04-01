package com.coremedia.ecommerce.studio {

import com.coremedia.blueprint.base.components.util.UserUtil;
import com.coremedia.blueprint.base.pagegrid.PageGridUtil;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewManagerInternal;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.ecommerce.studio.augmentation.augmentationService;
import com.coremedia.ecommerce.studio.components.preferences.CatalogPreferencesBase;
import com.coremedia.ecommerce.studio.components.tree.impl.CatalogTreeDragDropModel;
import com.coremedia.ecommerce.studio.components.tree.impl.CatalogTreeModel;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.helper.StoreUtil;
import com.coremedia.ecommerce.studio.library.ECommerceCollectionViewExtension;
import com.coremedia.ecommerce.studio.model.Catalog;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.CatalogObjectPropertyNames;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class ECommerceStudioPluginBase extends StudioPlugin {

  private static const EXTERNAL_CHANNEL_TYPE:String = "CMExternalChannel";

  public function ECommerceStudioPluginBase(config:ECommerceStudioPlugin = null) {
    super(config)
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);

    var collectionViewManagerInternal:CollectionViewManagerInternal =
            ((editorContext.getCollectionViewManager()) as CollectionViewManagerInternal);
    editorContext.getCollectionViewExtender().addExtension(new ECommerceCollectionViewExtension());

    var catalogTreeModel:CatalogTreeModel = new CatalogTreeModel();
    collectionViewManagerInternal.addTreeModel(catalogTreeModel,
            new CatalogTreeDragDropModel(catalogTreeModel));

    initCatalogPreferences();

    // customize the initialization of page layouts
    PageGridUtil.defaultLayoutResolver = getDefaultLayoutFromCatalogRoot;
  }

  internal function getShopExpression():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():String {
      var store:Store = Store(CatalogHelper.getInstance().getActiveStoreExpression().getValue());
      return store && store.getName();
    });
  }

  /**
   * We have to force a reload if the catalog view settings are changed.
   * Maybe this is possible without a Studio reload in the future, but this is the easiest way to apply the setting.
   */
  private function initCatalogPreferences():void {
    //load the catalog view settings and apply it to the tree model
    var showCatalogContentPref:Boolean = editorContext.getPreferences().get(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY);
    if(showCatalogContentPref === undefined) {
      showCatalogContentPref = false;
    }

    applySearchSettings(showCatalogContentPref);

    //add change listener to the catalog view settings
    var preferencesVE:ValueExpression = ValueExpressionFactory.create(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY, editorContext.getPreferences());
    preferencesVE.addChangeListener(function (ve:ValueExpression):void {
      var doShow:Boolean = ve.getValue() || false;
      applySearchSettings(doShow);

      //re-initialize the selection to update the search filter combo, etc.
      var home:Content = UserUtil.getHome();
      var cmInternal:CollectionViewManagerInternal = editorContext.getCollectionViewManager() as CollectionViewManagerInternal;
      var selection:Content = cmInternal.getCollectionView().getSelectedFolderValueExpression().getValue();
      cmInternal.getCollectionView().getSelectedFolderValueExpression().setValue(home);
      cmInternal.getCollectionView().getSelectedFolderValueExpression().setValue(selection);
    });
  }

  private static function applySearchSettings(showCatalogContent:Boolean):void {
    if(!showCatalogContent) {
      // remove the commerce doctypes from the search result by default
      excludeFromSearch(CatalogHelper.CONTENT_TYPE_CM_CATEGORY);
      excludeFromSearch(CatalogHelper.CONTENT_TYPE_CM_PRODUCT);
      excludeFromSearch(CatalogHelper.CONTENT_TYPE_CM_ABSTRACT_CATEGORY);
      excludeFromSearch(CatalogHelper.CONTENT_TYPE_CM_EXTERNAL_CHANNEL);
      excludeFromSearch(CatalogHelper.CONTENT_TYPE_CM_EXTERNAL_PRODUCT);

      //remove the commerce doctypes from the search filter by default
      excludeFromSearchResult(CatalogHelper.CONTENT_TYPE_CM_CATEGORY);
      excludeFromSearchResult(CatalogHelper.CONTENT_TYPE_CM_PRODUCT);
      excludeFromSearchResult(CatalogHelper.CONTENT_TYPE_CM_ABSTRACT_CATEGORY);
      excludeFromSearchResult(CatalogHelper.CONTENT_TYPE_CM_EXTERNAL_CHANNEL);
      excludeFromSearchResult(CatalogHelper.CONTENT_TYPE_CM_EXTERNAL_PRODUCT);
    }
    else {
      addToSearchResult(CatalogHelper.CONTENT_TYPE_CM_CATEGORY);
      addToSearchResult(CatalogHelper.CONTENT_TYPE_CM_PRODUCT);
      addToSearchResult(CatalogHelper.CONTENT_TYPE_CM_ABSTRACT_CATEGORY);
      addToSearchResult(CatalogHelper.CONTENT_TYPE_CM_EXTERNAL_CHANNEL);
      addToSearchResult(CatalogHelper.CONTENT_TYPE_CM_EXTERNAL_PRODUCT);
    }
  }

  private static function excludeFromSearch(contentTypeName:String):void {
    for (var i:int = 0; i < editorContext.getContentTypesExcludedFromSearch().length; i++) {
      if (editorContext.getContentTypesExcludedFromSearch()[i] === contentTypeName) {
        return;
      }
    }
    editorContext.getContentTypesExcludedFromSearch().push(contentTypeName);
  }

  private static function excludeFromSearchResult(contentTypeName:String):void {
    for (var i:int = 0; i < editorContext.getContentTypesExcludedFromSearch().length; i++) {
      if (editorContext.getContentTypesExcludedFromSearchResult()[i] === contentTypeName) {
        return;
      }
    }
    editorContext.getContentTypesExcludedFromSearchResult().push(contentTypeName);
  }

  private static function addToSearchResult(contentTypeName:String):void {
    for (var i:int = 0; i < editorContext.getContentTypesExcludedFromSearch().length; i++) {
      if (editorContext.getContentTypesExcludedFromSearch()[i] === contentTypeName) {
        editorContext.getContentTypesExcludedFromSearch().splice(i, 1);
        break;
      }
    }

    for (var j:int = 0; j < editorContext.getContentTypesExcludedFromSearchResult().length; j++) {
      if (editorContext.getContentTypesExcludedFromSearchResult()[j] === contentTypeName) {
        editorContext.getContentTypesExcludedFromSearchResult().splice(j, 1);
        break;
      }
    }
  }

  /**
   * A default layout resolver that can read the initial layout of a new created augmented category
   * from the current settings in catalog root. If the page is not an augmented category it returns
   * null. That means the default method is called.
   */
  private static function getDefaultLayoutFromCatalogRoot(content:Content, pagegridProperty:String):Content {
    // only content of CMExternalChannel (Augmented Categories) is affected
    if (!content) {
      return undefined;
    }
    var ct:ContentType = content.getType();
    if (ct === undefined) {
      return undefined;
    }
    if (ct.isSubtypeOf(EXTERNAL_CHANNEL_TYPE)) {
      var category:Category = augmentationService.getCategory(content);
      if (!category) {
        return undefined;
      }

      var catalog:Catalog = category.get(CatalogObjectPropertyNames.CATALOG);
      if (!catalog) {
        return undefined;
      }
      var rootCategory:Category = catalog.getRootCategory();

      if (rootCategory === undefined) {
        return undefined;
      }
      if (rootCategory) {
        var rootCategoryContent:Content = augmentationService.getContent(rootCategory);
        if (rootCategoryContent === undefined) {
          return undefined;
        }
        var layout:Content = PageGridUtil.getLayoutWithoutDefault(rootCategoryContent, pagegridProperty);
        if (layout === undefined) {
          return undefined;
        }
        return layout;
      }
    }
    return null;
  }

}
}
