package com.coremedia.catalog.studio {

import com.coremedia.blueprint.base.components.util.UserUtil;
import com.coremedia.blueprint.studio.util.ContentInitializer;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.ContentTypeNames;
import com.coremedia.catalog.studio.collectionview.search.LostandfoundFilterPanel;
import com.coremedia.catalog.studio.library.CatalogCollectionViewExtension;
import com.coremedia.catalog.studio.library.CatalogTreeRelation;
import com.coremedia.catalog.studio.library.RepositoryCatalogTreeModel;
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewConstants;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewExtension;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewManagerInternal;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewModel;
import com.coremedia.cms.editor.sdk.collectionview.SearchState;
import com.coremedia.cms.editor.sdk.collectionview.tree.RepositoryTreeDragDropModel;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.preferences.PreferenceWindow;
import com.coremedia.cms.editor.sdk.premular.ReferrerListPanel;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtilInternal;
import com.coremedia.cms.editor.sdk.util.MetaStyleService;
import com.coremedia.ecommerce.studio.catalogHelper;
import com.coremedia.ecommerce.studio.components.preferences.CatalogPreferencesBase;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.helper.StoreUtil;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.util.EventUtil;

import ext.Ext;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.ecommerce.studio.ECommerceStudioPlugin')]
[ResourceBundle('com.coremedia.cms.editor.Editor')]
[ResourceBundle('com.coremedia.catalog.studio.CatalogStudioPlugin')]
public class CatalogStudioPluginBase extends StudioPlugin {

  public function CatalogStudioPluginBase(config:CatalogStudioPlugin = null) {
    super(config);
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);

    //apply defaults
    editorContext.registerContentInitializer(CatalogTreeRelation.CONTENT_TYPE_CATEGORY, ContentInitializer.initChannel);

    addCatalogTreeModel();

    initCatalogPreferences();

    addSearchFilters(editorContext);

    // Colorful Studio styles
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_TURQUOISE, [CatalogTreeRelation.CONTENT_TYPE_PRODUCT]);
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_PURPLE, [CatalogTreeRelation.CONTENT_TYPE_CATEGORY]);
  }

  /**
   * Registers the catalog tree model and its dnd model.
   */
  private static function addCatalogTreeModel():void {
    var collectionViewManagerInternal:CollectionViewManagerInternal =
            ((editorContext.getCollectionViewManager()) as CollectionViewManagerInternal);

    var treeModel:RepositoryCatalogTreeModel = new RepositoryCatalogTreeModel();
    collectionViewManagerInternal.addTreeModel(treeModel, new RepositoryTreeDragDropModel(treeModel));

    //add extension for custom search document types
    editorContext.getCollectionViewExtender().addExtension(new CatalogCollectionViewExtension(), 799);
  }

  /**
   * We have to force a reload if the catalog view settings are changed.
   * Maybe this is possible without a Studio reload in the future, but this is the easiest way to apply the setting.
   */
  private function initCatalogPreferences():void {
    //load the catalog view settings and apply it to the tree model
    var showCatalogContentPref:Boolean = editorContext.getPreferences().get(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY);
    if (showCatalogContentPref === undefined) {
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

  private static function addSearchFilters(editorContext:IEditorContext):void {
    editorContext.getEnabledSearchFilterIds().push(LostandfoundFilterPanel.FILTER_ID);
  }

  private static function applySearchSettings(showCatalogContent:Boolean):void {
    if (!showCatalogContent) {
      //remove the corporate catalog doctypes from the search result by default
      addToSearch(CatalogTreeRelation.CONTENT_TYPE_CATEGORY);
      addToSearch(CatalogTreeRelation.CONTENT_TYPE_PRODUCT);

      //remove the corporate catalog doctypes from the search filter by default
      addToSearchResult(CatalogTreeRelation.CONTENT_TYPE_CATEGORY);
      addToSearchResult(CatalogTreeRelation.CONTENT_TYPE_PRODUCT);
    }
    else {
      removeFromSearchResult(CatalogTreeRelation.CONTENT_TYPE_CATEGORY);
      removeFromSearchResult(CatalogTreeRelation.CONTENT_TYPE_PRODUCT);
    }
  }

  private static function addToSearch(contentTypeName:String):void {
    for (var i:int = 0; i < editorContext.getContentTypesExcludedFromSearch().length; i++) {
      if (editorContext.getContentTypesExcludedFromSearch()[i] === contentTypeName) {
        return;
      }
    }
    editorContext.getContentTypesExcludedFromSearch().push(contentTypeName);
  }

  private static function addToSearchResult(contentTypeName:String):void {
    for (var i:int = 0; i < editorContext.getContentTypesExcludedFromSearchResult().length; i++) {
      if (editorContext.getContentTypesExcludedFromSearchResult()[i] === contentTypeName) {
        return;
      }
    }
    editorContext.getContentTypesExcludedFromSearchResult().push(contentTypeName);
  }

  private static function removeFromSearchResult(contentTypeName:String):void {
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

  public static function getShopExpression(config:ReferrerListPanel):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():String {
      var store:Store = Store(CatalogHelper.getInstance().getStoreForContentExpression(config.bindTo).getValue());
      return store && store.getName() && CatalogHelper.getInstance().isCoreMediaStore(store);
    });
  }

  /**
   * Custom search handler for catalog link lists.
   */
  public static function openCatalogSearch(linkListTargetType:ContentType, sourceContent:Content):void {
    var preferencesVE:ValueExpression = ValueExpressionFactory.create(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY, editorContext.getPreferences());
    var showCatalogContent:Boolean = preferencesVE.getValue();
    var preferredSiteId:String = editorContext.getSitesService().getPreferredSiteId();
    var contentSiteId:String = editorContext.getSitesService().getSiteIdFor(sourceContent);
    var contentSite:Site = editorContext.getSitesService().getSiteFor(sourceContent);

    var searchType:String = linkListTargetType.getName();
    //default supertype to all documents since we are searching inside the catalog
    if (searchType === 'CMLinkable') {
      searchType = ContentTypeNames.DOCUMENT;
    }

    //open the regular catalog search if the sites are matching
    if (preferredSiteId === contentSiteId) {
      openSearch(contentSite, searchType, true);
    }
    else if (showCatalogContent) {
      openSearch(contentSite, searchType, false);
    }
    else {
      var msg:String = ResourceManager.getInstance().getString('com.coremedia.catalog.studio.CatalogStudioPlugin', 'Catalog_show_search_fails_for_Content');
      var buttons:Object = {
        no: ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'Catalog_show_preferences_button_text'),
        yes: ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'Catalog_show_switch_site_button_text'),
        cancel: ResourceManager.getInstance().getString('com.coremedia.cms.editor.Editor', 'dialog_defaultCancelButton_text')
      };

      MessageBoxUtilInternal.show(ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'Catalog_show_in_tree_fails_title'), msg, null, buttons, getButtonCallback(searchType, contentSite));
    }
  }

  /**
   * Somehow the open in search mode for the CollectionView does not work properly
   * when it has never been opened before. The only working solution is to set the search state and search
   * afterwards.
   * //TODO fix the library search and change this to 'editorContext.getCollectionViewManager().openSearchForType(searchType, null, catalogRoot);'
   * @param contentSite
   * @param searchType
   * @param catalogSearch
   */
  private static function openSearch(contentSite:Site, searchType:String, catalogSearch:Boolean):void {
    ValueExpressionFactory.createFromFunction(function ():Content {
      return getCatalogRootForSite(contentSite);
    }).loadValue(function (catalogRoot:Content):void {
      var collectionViewModel:CollectionViewModel = EditorContextImpl(editorContext).getCollectionViewModel();
      var state:SearchState = new SearchState();
      state.contentType = searchType;

      if (catalogSearch) {
        state.folder = catalogRoot;
        collectionViewModel.setSearchState(true, CollectionViewConstants.LIST_VIEW, state, catalogRoot);
        editorContext.getCollectionViewManager().openSearch(state, false, CollectionViewConstants.LIST_VIEW);
      }
      else {
        state.folder = catalogRoot.getParent();
        collectionViewModel.setSearchState(true, CollectionViewConstants.LIST_VIEW, state, catalogRoot);
        editorContext.getCollectionViewManager().openSearch(state, false, CollectionViewConstants.LIST_VIEW);
      }
    });
  }

  private static function getButtonCallback(linkListType:String, contentSite:Site):Function {
    return function (btn:String):void {
      if (btn === 'yes') {
        switchSite(contentSite, function (cRoot:Content):void {
          var state:SearchState = new SearchState();
          state.folder = cRoot;
          state.contentType = linkListType;

          var collectionViewModel:CollectionViewModel = EditorContextImpl(editorContext).getCollectionViewModel();
          collectionViewModel.setSearchState(true, CollectionViewConstants.LIST_VIEW, state, cRoot);

          editorContext.getCollectionViewManager().openSearch(state, false, CollectionViewConstants.LIST_VIEW);
        });
      }
      else if (btn === 'no') {
        //show preferences
        var prefWindow:PreferenceWindow = Ext.create(PreferenceWindow, {selectedTabItemId: 'contentCatalogPreferences'});
        prefWindow.show();
        //open the content in library if the user enable the show as content contentCatalogPreferences
        prefWindow.on('close', function ():void {
          var preferencesVE:ValueExpression = ValueExpressionFactory.create(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY, editorContext.getPreferences());
          var showCatalogContent:Boolean = preferencesVE.getValue();
          if (showCatalogContent) {
            openSearch(contentSite, linkListType, false);
          }
        });
      }
    };
  }

  /**
   * Why this hell of a method? When the studio is opened with a catalog item
   * of another site, the library has not been opened yet and the user clicks on a linklist to open
   * the catalog search for another site (assuming the user has clicked the 'Switch Site' option), then
   * the whole library has be be initialized first.
   * This means that catalog settings may not have been loaded yet.
   * @param site the new preferred site
   * @param callback the callback called when the site initialization has been finished.
   */
  public static function switchSite(site:Site, callback:Function):void {
    //switch site
    editorContext.getSitesService().getPreferredSiteIdExpression().setValue(site.getId());

    //load all mandatory content before switching the view afterwards to repo or search mode
    EventUtil.invokeLater(function ():void {
      ValueExpressionFactory.createFromFunction(function ():Content {
        var catalogRoot:Content = getCatalogRootForSite(site);
        if (!catalogRoot) {
          return catalogRoot;
        }
        if (!catalogRoot.getPath()) {
          return undefined;
        }
        return catalogRoot;
      }).loadValue(callback);
    });
  }

  internal static function getCatalogRootForSite(site:Site):Content {
    var storeForContentExpression:ValueExpression = StoreUtil.getStoreForSiteExpression(site);
    return getCatalogRootForStore(storeForContentExpression);
  }

  public static function getCatalogRootForStore(storeExpression:ValueExpression):Content {
    var activeStore:Store = storeExpression.getValue();
    if (undefined === activeStore) {
      return undefined;
    }
    if (activeStore is Store) {
      var rootCategory:Category = activeStore.getRootCategory();
      if (undefined === rootCategory) {
        return undefined;
      }
      if (catalogHelper.isCoreMediaStore(activeStore)) {
        var externalTechId:String = rootCategory.getExternalTechId();
        if (undefined === externalTechId) {
          return undefined;
        }
        return beanFactory.getRemoteBean("content/" + externalTechId) as Content;
      }
    }
    return null;
  }

  public static function findCoreMediaStores():Array {
    var sites:Array = editorContext.getSitesService().getSites();
    var result:Array = [];
    for each (var site:Site in sites) {
      var store:Store = StoreUtil.getStoreForSiteExpression(site).getValue();
      var isCoreMediaStore:Boolean = catalogHelper.isCoreMediaStore(store);
      if (undefined === isCoreMediaStore) {
        return undefined;
      }
      if (isCoreMediaStore) {
        result.push(store);
      }
    }
    return result;
  }

  internal function mayCreate(selection:Content, contentType:ContentType):Boolean {
    if (!selection.getType().getName()) {
      return undefined;
    }

    var extension:CollectionViewExtension = editorContext.getCollectionViewExtender().getExtension(selection);
    if (extension === undefined) {
      return undefined;
    }

    if (extension is CatalogCollectionViewExtension) {
      return true;
    }

    var site:Site = editorContext.getSitesService().getSiteFor(selection);
    if (!site) {
      return false;
    }

    var store:Store = getStoreForSite(site);

    var cmCatalog:Boolean = CatalogHelper.getInstance().isCoreMediaStore(store);
    if (!cmCatalog) {
      return false;
    }

    return showCatalogAsContent();
  }

  internal static function getStoreForSite(site:Site):Store {
    return StoreUtil.getStoreForSiteExpression(site).getValue();
  }

  internal static function showCatalogAsContent():Boolean {
    var preferencesVE:ValueExpression = ValueExpressionFactory.create(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY, editorContext.getPreferences());
    var showCatalogContent:Boolean = preferencesVE.getValue();
    return showCatalogContent;
  }
}
}
