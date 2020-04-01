package com.coremedia.livecontext.studio.collectionview {

import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewConstants;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewContainer;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewExtension;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewManagerInternal;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewModel;
import com.coremedia.cms.editor.sdk.collectionview.SearchState;
import com.coremedia.cms.editor.sdk.collectionview.search.SearchArea;
import com.coremedia.cms.editor.sdk.desktop.ComponentBasedEntityWorkAreaTabType;
import com.coremedia.cms.editor.sdk.desktop.sidepanel.SidePanelManagerImpl;
import com.coremedia.cms.editor.sdk.desktop.sidepanel.SidePanelStudioPlugin;
import com.coremedia.cms.editor.sdk.desktop.sidepanel.sidePanelManager;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.ThumbnailResolverFactory;
import com.coremedia.ecommerce.studio.CatalogModel;
import com.coremedia.ecommerce.studio.ECommerceStudioPlugin;
import com.coremedia.ecommerce.studio.components.repository.CatalogRepositoryContextMenu;
import com.coremedia.ecommerce.studio.components.repository.CatalogRepositoryList;
import com.coremedia.ecommerce.studio.components.repository.CatalogRepositoryListContainer;
import com.coremedia.ecommerce.studio.components.search.CatalogSearchContextMenu;
import com.coremedia.ecommerce.studio.components.search.CatalogSearchList;
import com.coremedia.ecommerce.studio.components.search.CatalogSearchListContainer;
import com.coremedia.ecommerce.studio.components.tree.impl.CatalogTreeDragDropModel;
import com.coremedia.ecommerce.studio.components.tree.impl.CatalogTreeModel;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.library.ECommerceCollectionViewExtension;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.livecontext.studio.AbstractLiveContextStudioTest;
import com.coremedia.livecontext.studio.CatalogTeaserThumbnailResolver;
import com.coremedia.livecontext.studio.CatalogThumbnailResolver;
import com.coremedia.livecontext.studio.LivecontextStudioPlugin;
import com.coremedia.livecontext.studio.library.LivecontextCollectionViewActionsPlugin;
import com.coremedia.livecontext.studio.library.LivecontextCollectionViewExtension;
import com.coremedia.ui.components.SwitchingContainer;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.test.ActionStep;
import com.coremedia.ui.data.test.Step;
import com.coremedia.ui.store.BeanRecord;
import com.coremedia.ui.util.ContextMenuEventAdapter;
import com.coremedia.ui.util.QtipUtil;
import com.coremedia.ui.util.TableUtil;

import ext.Component;
import ext.ComponentManager;
import ext.Ext;
import ext.button.Button;
import ext.container.Container;
import ext.container.Viewport;
import ext.event.Event;
import ext.grid.GridPanel;
import ext.menu.Item;
import ext.selection.RowSelectionModel;
import ext.toolbar.TextItem;
import ext.toolbar.Toolbar;

import js.HTMLElement;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.ecommerce.studio.ECommerceStudioPlugin')]
public class CatalogCollectionViewTest extends AbstractLiveContextStudioTest {
  private static const CATALOG_REPOSITORY_CONTAINER:String = CatalogRepositoryListContainer.VIEW_CONTAINER_ITEM_ID;
  private static const CATALOG_SEARCH_LIST_CONTAINER:String = CatalogSearchListContainer.VIEW_CONTAINER_ITEM_ID;

  private var testling:CollectionView;
  private var searchProductVariantsContextMenuItem:Item;

  private var getPreferredSite:Function;
  private var preferredSiteExpression:ValueExpression;

  override public function setUp():void {
    super.setUp();
    preferredSiteExpression = ValueExpressionFactory.create('site', beanFactory.createLocalBean({site: 'HeliosSiteId'}));
    getPreferredSite = editorContext.getSitesService().getPreferredSiteId;
    editorContext.getSitesService().getPreferredSiteId = function ():String {
      return preferredSiteExpression.getValue();
    };
    //use SidePanelStudioPlugin to register the CollectionViewContainer
    var plugin:SidePanelStudioPlugin = Ext.create(SidePanelStudioPlugin, {});
    plugin.init(editorContext);
    //use ECommerceStudioPlugin to add CatalogRepositoryListContainer, CatalogSearchListContainer etc.
    new ECommerceStudioPlugin();
    new LivecontextCollectionViewActionsPlugin();

    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_CHANNEL));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_PAGE));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(LivecontextStudioPlugin.CONTENT_TYPE_MARKETING_SPOT));
    editorContext.registerThumbnailResolver(new CatalogTeaserThumbnailResolver(LivecontextStudioPlugin.CONTENT_TYPE_PRODUCT_TEASER));
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create(LivecontextStudioPlugin.CONTENT_TYPE_PRODUCT_TEASER, "pictures"));


    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(CatalogModel.TYPE_CATEGORY));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(CatalogModel.TYPE_MARKETING));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(CatalogModel.TYPE_MARKETING_SPOT));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(CatalogModel.TYPE_PRODUCT));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(CatalogModel.TYPE_PRODUCT_VARIANT));

    // For the sake of the test, let's assume everything can be opened in a tab.
    // Cleaner alternative: Register all tab types.
    ComponentBasedEntityWorkAreaTabType.canBeOpenedInTab = function ():Boolean {return true};

    QtipUtil.registerQtipFormatter();
  }

  public function testCatalogLibrary():void {
    chain(
            //initialize the catalog library
            initStore(),
            loadContentRepository(),
            waitForContentRepositoryLoaded(),
            loadContentTypes(),
            waitForContentTypesLoaded(),
            createTestlingStep(),

            //test catalog repository thumbnail view
            selectNode("livecontext/category/HeliosSiteId/catalog/NO_WS/Women"),
            waitUntilSwitchToListButtonIsPressed(),
            switchToThumbnailView(),
            waitUntilSwitchToListButtonIsUnpressed(),
            waitUntilThumbnailViewIsActive(),

            //test context menu on the repository list and thumbnail view
            switchToListView(),
            waitUntilListViewIsActive(),
            selectNode("livecontext/category/HeliosSiteId/catalog/NO_WS/Dresses"),
            waitUntilProductIsLoadedInRepositoryList(),
            waitUntilSearchProductVariantToolbarButtonIsHidden(),
            openContextMenuOnFirstItemOfRepositoryList(),
            waitUntilRepositoryListContextMenuOpened(),
            waitUntilSearchProductVariantToolbarButtonIsEnabled(),
            waitUntilSearchProductVariantContextMenuIsEnabled(),
            searchProductVariantsUsingContextMenu(),
            waitUntilSearchModeIsActive(),
            waitUntilProductVariantIsLoadedInSearchList(),
            waitUntilCatalogSearchListIsLoadedAndNotEmpty(2, HERMITAGE_RUCHED_BODICE_COCKTAIL_DRESS),
            //now test that the variant search is hidden on product variants themselves
            selectFirstItemOfSearchList(),
            openContextMenuOnFirstItemOfSearchList(),
            waitUntilSearchListContextMenuOpened(),
            waitUntilSearchProductVariantToolbarButtonIsHidden(),
            waitUntilSearchProductVariantContextMenuIsHidden(),

            // test marketing spots
            switchToRepositoryMode(),
            selectNode("livecontext/marketing/HeliosSiteId/NO_WS"),
            waitUntilSwitchToListButtonIsPressed(),
            switchToThumbnailView(),
            waitUntilSwitchToListButtonIsUnpressed(),
            waitUntilThumbnailViewIsActive(),

            //test product search
            selectStore(),
            triggerSearch("Oranges", CatalogModel.TYPE_PRODUCT),
            waitUntilSearchModeIsActive(),
            waitUntilSwitchToListButtonIsPressed(),
            waitUntilCatalogSearchListIsLoadedAndNotEmpty(2, ORANGES_NAME),
            waitUntilCatalogSearchListAndLabelIsLoadedAndFooterShowsTotalHits(2),
            switchToThumbnailView(),
            waitUntilSwitchToListButtonIsUnpressed(),
            waitUntilThumbnailViewIsActive(),

            //test product variant search
            switchToListView(),
            waitUntilListViewIsActive(),
            triggerSearch("Oranges", CatalogModel.TYPE_PRODUCT_VARIANT),
            waitUntilCatalogSearchListIsLoadedAndNotEmpty(3, ORANGES_SKU_NAME),
            waitUntilCatalogSearchListAndLabelIsLoadedAndFooterShowsTotalHits(3),
            switchToThumbnailView(),
            waitUntilSwitchToListButtonIsUnpressed(),
            waitUntilThumbnailViewIsActive()
  );
  }

  override public function tearDown():void {
    super.tearDown();
    editorContext.getSitesService().getPreferredSiteId = getPreferredSite;
    //we have to reset the items of the side panel manager so that it creates CollectionViewContainer anew.
    SidePanelManagerImpl(sidePanelManager).resetItems();
  }


  private function createTestling():void {
    var collectionViewManagerInternal:CollectionViewManagerInternal =
            ((editorContext.getCollectionViewManager()) as CollectionViewManagerInternal);

    var catalogTreeModel:CatalogTreeModel = new CatalogTreeModel();
    collectionViewManagerInternal.addTreeModel(catalogTreeModel, new CatalogTreeDragDropModel(catalogTreeModel));

    var extension:CollectionViewExtension = new ECommerceCollectionViewExtension();
    editorContext.getCollectionViewExtender().addExtension(extension);

    var lcExtension:CollectionViewExtension = new LivecontextCollectionViewExtension();
    editorContext.getCollectionViewExtender().addExtension(lcExtension);

    var cvContainer:CollectionViewContainer = sidePanelManager.getOrCreateComponent(CollectionViewContainer.ID) as CollectionViewContainer;
    var viewportConfig:Viewport = Viewport({});
    viewportConfig.items = [cvContainer];
    var myViewPort:Viewport = new Viewport(viewportConfig);
    myViewPort.show();
    testling = cvContainer.getComponent(CollectionView.COLLECTION_VIEW_ID) as CollectionView;
  }

  private function getSearchArea():SearchArea {
    return SearchArea(testling.getComponent(CollectionView.SEARCH_AREA_ITEM_ID));
  }



  private function getSearchList():CatalogSearchList {
    var catalogSearch:Container = Container(getCollectionModesContainer().getComponent(CollectionViewModel.SEARCH_MODE));
    var searchList:SwitchingContainer = SwitchingContainer(Container(catalogSearch.getComponent("searchSwitchingContainer")));
    var searchContainer:CatalogSearchListContainer = CatalogSearchListContainer(searchList.getComponent(CATALOG_SEARCH_LIST_CONTAINER));
    //ensure type cast!!!! there are other list views too
    return searchContainer.getComponent(CollectionViewConstants.LIST_VIEW) as CatalogSearchList;
  }

  private function getRepositoryContainer():CatalogRepositoryList {
    var repositoryContainer:Container = Container(getCollectionModesContainer().getComponent(CollectionViewModel.REPOSITORY_MODE));
    var repositorySwitch:SwitchingContainer = SwitchingContainer(Container(repositoryContainer.getComponent("listViewSwitchingContainer")));
    var repositoryListContainer:CatalogRepositoryListContainer = CatalogRepositoryListContainer(repositorySwitch.getComponent(CATALOG_REPOSITORY_CONTAINER));
    //ensure type cast!!!! there are other list views too
    return repositoryListContainer.getComponent(CollectionViewConstants.LIST_VIEW) as CatalogRepositoryList;
  }

  private function getRepositorySwitchingContainer():SwitchingContainer {
    var myCatalogRepositoryContainer:Container = Container(getCollectionModesContainer().getComponent(CollectionViewModel.REPOSITORY_MODE));
    var listViewSwitchingContainer:Container = Container(myCatalogRepositoryContainer.getComponent("listViewSwitchingContainer"));
    var repositorySwitchingContainer:SwitchingContainer = SwitchingContainer(listViewSwitchingContainer.getComponent(CATALOG_REPOSITORY_CONTAINER));
    return repositorySwitchingContainer;
  }

  private function getRepositoryList():CatalogRepositoryList {
    var repositorySwitchingContainer:SwitchingContainer = getRepositorySwitchingContainer();
    return CatalogRepositoryList(repositorySwitchingContainer.getComponent(CollectionViewConstants.LIST_VIEW)) as CatalogRepositoryList;
  }

  private function getCollectionModesContainer():SwitchingContainer {
    return SwitchingContainer(testling.getComponent(CollectionView.COLLECTION_MODES_CONTAINER_ITEM_ID));
  }

  private function getFooter():Toolbar {
    return Toolbar(testling.getComponent(CollectionView.FOOTER_INFO_ITEM_ID));
  }

  private function getFooterTotalHitsLabel():TextItem {
    return TextItem(getFooter().getComponent("totalHitsLabel"));
  }

  private function createTestlingStep():Step {
    return new Step("Create the testling",
            function ():Boolean {
              return true;
            },
            createTestling
    );
  }

  private function initStore():Step {
    return new Step("Load Store Data",
            function ():Boolean {
              var store:Store = CatalogHelper.getInstance().getActiveStoreExpression().getValue();
              return store !== null && store !== undefined;
            },
            CatalogHelper.getInstance().getActiveStoreExpression().getValue()
    );
  }

  private function selectStore():Step {
    return new Step("Select Store Node",
            function ():Boolean {
              var store:Store = CatalogHelper.getInstance().getActiveStoreExpression().getValue();
              testling.setOpenPath(store);
              return getRepositoryContainer() &&  getRepositoryContainer().rendered && getRepositoryContainer().getStore();
            }
    );
  }

  private function selectNode(path:String):Step {
    return new ActionStep("selecting '" + path +"' tree node",
            function ():void {
              testling.setOpenPath(beanFactory.getRemoteBean(path));
            });
  }

  private function triggerSearch(searchTerm:String, searchType:String):Step {
    return new Step("trigger catalog search",
            function ():Boolean {
              return true;
            },
            function ():void {
              setSearchStateAndTriggerSearch(searchTerm, searchType);
            });
  }

  private function waitUntilSwitchToListButtonIsPressed():Step {
    return new Step("Switch to List Button should be pressed",
            function ():Boolean {
              return getSwitchToListViewButton() && getSwitchToListViewButton().pressed;
            });
  }

  private function waitUntilRepositoryListContextMenuOpened():Step {
    return new Step("Wait for the context menu on the repository list to be opened",
            function ():Boolean {
              return findCatalogRepositoryContextMenu();
            }
    );
  }

  private function waitUntilSearchListContextMenuOpened():Step {
    return new Step("Wait for the context menu on the search list to be opened",
            function ():Boolean {
              return findCatalogSearchListContextMenu();
            }
    );
  }

  private function getSwitchToRepositoryModeButton():Button {
    return Button(getSearchArea().queryById(SearchArea.SWITCH_TO_REPOSITORY_BUTTON_ITEM_ID));
  }

  private function getSwitchToListViewButton():Button {
    return Button(getActiveToolbarViewSwitch().queryById("list"));
  }

  private function getSwitchToThumbnailViewButton():Button {
    return Button(getActiveToolbarViewSwitch().queryById("thumb"));
  }

  private function getProductVariantSearchButton():Button {
    return Button(getActiveToolbar().queryById(LivecontextStudioPlugin.SEARCH_PRODUCT_VARIANTS_BUTTON_ITEM_ID));
  }

  private function getActiveToolbarViewSwitch():Container {
    var itemId:String = getCollectionModesContainer().getActiveItem().getItemId();
    var container:Container = undefined;
    if(itemId === "repository") {
      container = Container(testling.queryById("toolbarSwitchingContainer")).queryById("catalogRepositoryToolbar") as Container;
    }
    else {
      container = Container(testling.queryById("searchToolbar")).queryById("searchToolbarSwitchingContainer") as Container;
    }

    return container.queryById("switchButtonsContainer") as Container;
  }

  private function getActiveToolbar():Toolbar {
    var itemId:String = getCollectionModesContainer().getActiveItem().getItemId();
    if(itemId === "repository") {
      var repoContainer:Container = Container(testling.queryById("toolbarSwitchingContainer")).queryById("catalogRepositoryToolbar") as Container;
      return repoContainer.queryById("commerceToolbar") as Toolbar;
    }

    var searchContainer:Container = Container(testling.queryById("searchToolbar")).queryById("searchToolbarSwitchingContainer") as Container;
    return searchContainer.queryById("commerceToolbar") as Toolbar;
  }

  private function waitUntilSwitchToListButtonIsUnpressed():Step {
    return new Step("Switch to List Button should be unpressed",
            function ():Boolean {
              return !getSwitchToListViewButton().pressed;
            },
            function ():void {
              //nothing to do
            });
  }

  private function waitUntilListViewIsActive():Step {
    return new Step("List View should be active",
            function ():Boolean {
              return getRepositorySwitchingContainer().getActiveItemValue() === CollectionViewConstants.LIST_VIEW;
            });
  }

  private function waitUntilThumbnailViewIsActive():Step {
    return new Step("Thumbnail View should be active",
            function ():Boolean {
              return getRepositorySwitchingContainer().getActiveItemValue() === CollectionViewConstants.THUMBNAILS_VIEW;
            });
  }

  private function waitUntilSearchModeIsActive():Step {
    return new Step("Search Mode should be active",
            function ():Boolean {
              return getCollectionModesContainer().getActiveItemValue() === CollectionViewModel.SEARCH_MODE;
            }
    );
  }

  private function waitUntilProductIsLoadedInRepositoryList():Step {
    return new Step("Wait for the repository list to be loaded with products",
            function ():Boolean {
              return getRepositoryList().getStore().getCount() > 0 &&
                      Ext.get(TableUtil.getCellAsDom(getRepositoryList(), 0,0)).query("[aria-label]")[0].getAttribute("aria-label") === ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'Product_label');
            }
    );
  }

  private function waitUntilProductVariantIsLoadedInSearchList():Step {
    return new Step("Wait for the search list to be loaded with product variants",
            function ():Boolean {
              return getSearchList().getStore().getCount() > 0 &&
                      Ext.get(TableUtil.getCellAsDom(getSearchList(), 0,0)).query("[aria-label]")[0].getAttribute("aria-label") === ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'ProductVariant_label');
            }
    );
  }

  private function waitUntilSearchProductVariantToolbarButtonIsHidden():Step {
    return new Step("Wait for the product variant search toolbar button is hidden",
            function ():Boolean {
              return getProductVariantSearchButton().hidden;
            }
    )
  }

  private function waitUntilSearchProductVariantToolbarButtonIsEnabled():Step {
    return new Step("Wait for the product variant search toolbar button is enabled",
            function ():Boolean {
              return !getProductVariantSearchButton().disabled;
            }
    )
  }

  private function waitUntilSearchProductVariantContextMenuIsEnabled():Step {
    return new Step("Wait for the product variant search context menu item is enabled",
            function ():Boolean {
              return !searchProductVariantsContextMenuItem.disabled;
            }
    )
  }

  private function waitUntilSearchProductVariantContextMenuIsHidden():Step {
    return new Step("Wait for the product variant search context menu item is hidden",
            function ():Boolean {
              return searchProductVariantsContextMenuItem.hidden;
            }
    )
  }

  private function switchToListView():Step {
    return new ActionStep("Switch to list view",
            function ():void {
              getSwitchToListViewButton().initialConfig.handler();
            });
  }

  private function switchToThumbnailView():Step {
    return new ActionStep("Switch to thumbnail view",
            function ():void {
              getSwitchToThumbnailViewButton().initialConfig.handler();
            });
  }

  private function switchToRepositoryMode():Step {
    return new ActionStep("Switch to repository mode",
            function ():void {
              getSwitchToRepositoryModeButton().initialConfig.handler();
            });
  }

  private function waitUntilCatalogSearchListIsLoadedAndNotEmpty(expectedResultCount:int, firstItemName:String):Step {
    return new Step("Wait for the catalog search list to be loaded and the search items to be " + expectedResultCount +
            " and the first item to be " + firstItemName,
            function ():Boolean {
              if (getSearchList().getStore() && getSearchList().getStore().getCount() <= 0) {
                return false;
              }
              var name:String = CatalogObject(BeanRecord(getSearchList().getStore().getAt(0)).getBean()).getName();
              return firstItemName === name && expectedResultCount === getSearchList().getStore().getCount();
            }
    );
  }

  private function waitUntilCatalogSearchListAndLabelIsLoadedAndFooterShowsTotalHits(expectedResultCount:int):Step {
    return new Step("footer and catalog search list should be loaded and must not be empty",
            function ():Boolean {
              var footerTotalHitsLabel:TextItem = getFooterTotalHitsLabel();
              var searchList:CatalogSearchList = getSearchList();
              return footerTotalHitsLabel && searchList.getStore() && searchList.getStore().getCount() > 0 &&
                      footerTotalHitsLabel.html && footerTotalHitsLabel.html.indexOf(String(expectedResultCount)) === 0;

            },
            function ():void {
              //nothing to do
            });
  }

  private function openContextMenuOnFirstItemOfRepositoryList():Step {
    return new Step("Open Context Menu on the first item of the repository list",
            function ():Boolean {
              return true;
            },
            function ():void {
              openContextMenu(getRepositoryList(), 0);
            }
    );

  }

  private function selectFirstItemOfSearchList():Step {
    return new Step("Open Context Menu on the first item of the searhc list",
            function ():Boolean {
              return true;
            },
            function ():void {
              var sm:RowSelectionModel = getSearchList().getSelectionModel() as RowSelectionModel;
              sm.select(0);
            }
    );

  }

  private function openContextMenuOnFirstItemOfSearchList():Step {
    return new Step("Open Context Menu on the first item of the searhc list",
            function ():Boolean {
              var contextMenu:CatalogSearchContextMenu = ComponentManager.getAll().filter(function (component:Component):Boolean {
                return component.isXType(CatalogSearchContextMenu.xtype);
              })[0] as CatalogSearchContextMenu;
              return contextMenu.itemCollection.getRange().some(function (item:Item):Boolean {
                return !!item.baseAction && !item.isHidden();
              })
            },
            function ():void {
              openContextMenu(getSearchList(), 0);
            }
    );

  }

  private function searchProductVariantsUsingContextMenu():Step {
    return new Step("Search Product Variants using the context menu",
            function ():Boolean {
              return true;
            },
            function ():void {
              searchProductVariantsContextMenuItem.initialConfig.handler();
            }
    );

  }

  private function openContextMenu(grid:GridPanel, row:Number):void {
    var event:Event = Event({
      getXY: function():Array {
        return Ext.fly(event.getTarget()).getXY();
      },
      preventDefault : function():void{
        //do nothing
      },
      getTarget: function():HTMLElement {
        return TableUtil.getCellAsDom(grid, row, 1);
      },
      type: ContextMenuEventAdapter.EVENT_NAME
    });
    grid.fireEvent("rowcontextmenu", grid, null, null, row, event);
  }

  private function findCatalogRepositoryContextMenu():CatalogRepositoryContextMenu {
    var contextMenu:CatalogRepositoryContextMenu = ComponentManager.getAll().filter(function (component:Component):Boolean {
              return !component.up() && !component.hidden && component.isXType(CatalogRepositoryContextMenu.xtype);
            })[0] as CatalogRepositoryContextMenu;
    if (contextMenu) {
      searchProductVariantsContextMenuItem = contextMenu.getComponent(LivecontextStudioPlugin.SEARCH_PRODUCT_VARIANTS_MENU_ITEM_ID) as Item;
    }

    return contextMenu;
  }

  private function findCatalogSearchListContextMenu():CatalogSearchContextMenu {
    var contextMenu:CatalogSearchContextMenu = ComponentManager.getAll().filter(function (component:Component):Boolean {
              return !component.up() && !component.hidden && component.isXType(CatalogSearchContextMenu.xtype);
            })[0] as CatalogSearchContextMenu;
    if (contextMenu) {
      searchProductVariantsContextMenuItem = contextMenu.getComponent(LivecontextStudioPlugin.SEARCH_PRODUCT_VARIANTS_MENU_ITEM_ID) as Item;
    }

    return  contextMenu;
  }

  private static function setSearchStateAndTriggerSearch(searchTerm:String, searchType:String):void {
    var searchState:SearchState = new SearchState();
    searchState.searchText = searchTerm;
    searchState.contentType = searchType;
    searchState.folder = CatalogHelper.getInstance().getActiveStoreExpression().getValue();

    editorContext.getCollectionViewManager().openSearch(searchState, true, CollectionViewConstants.LIST_VIEW);
  }

}
}
