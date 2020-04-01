package com.coremedia.livecontext.asset.studio {
import com.coremedia.cap.content.impl.ContentImpl;
import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewConstants;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewContainer;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewExtension;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewManagerInternal;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewModel;
import com.coremedia.cms.editor.sdk.collectionview.tree.LibraryTree;
import com.coremedia.cms.editor.sdk.desktop.ComponentBasedEntityWorkAreaTabType;
import com.coremedia.cms.editor.sdk.desktop.sidepanel.SidePanelManagerImpl;
import com.coremedia.cms.editor.sdk.desktop.sidepanel.SidePanelStudioPlugin;
import com.coremedia.cms.editor.sdk.desktop.sidepanel.sidePanelManager;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ecommerce.studio.ECommerceStudioPlugin;
import com.coremedia.ecommerce.studio.components.repository.CatalogRepositoryContextMenu;
import com.coremedia.ecommerce.studio.components.repository.CatalogRepositoryList;
import com.coremedia.ecommerce.studio.components.repository.CatalogRepositoryListContainer;
import com.coremedia.ecommerce.studio.components.tree.impl.CatalogTreeDragDropModel;
import com.coremedia.ecommerce.studio.components.tree.impl.CatalogTreeModel;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.library.ECommerceCollectionViewExtension;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.livecontext.studio.library.LivecontextCollectionViewActionsPlugin;
import com.coremedia.livecontext.studio.library.LivecontextCollectionViewExtension;
import com.coremedia.ui.components.SwitchingContainer;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.test.Step;
import com.coremedia.ui.util.ContextMenuEventAdapter;
import com.coremedia.ui.util.TableUtil;

import ext.Component;
import ext.ComponentManager;
import ext.Ext;
import ext.button.Button;
import ext.container.Container;
import ext.data.Model;
import ext.data.NodeInterface;
import ext.grid.GridPanel;
import ext.menu.Item;
import ext.selection.TreeSelectionModel;
import ext.toolbar.Toolbar;

import js.HTMLElement;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.ecommerce.studio.ECommerceStudioPlugin')]
public class SearchProductImagesTest extends AbstractCatalogAssetTest {

  private var viewport:SearchProductImagesTestView;
  private var testling:CollectionView;
  private var catalogTree:LibraryTree;
  private var searchProductPicturesContextMenuItem:Item;

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
    new LivecontextAssetStudioPlugin();


    // For the sake of the test, let's assume everything can be opened in a tab.
    // Cleaner alternative: Register all tab types.
    ComponentBasedEntityWorkAreaTabType.canBeOpenedInTab = function ():Boolean {return true};
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

    viewport = new SearchProductImagesTestView();

    var cvContainer:CollectionViewContainer = viewport.getComponent(CollectionViewContainer.ID) as CollectionViewContainer;
    SidePanelManagerImpl(sidePanelManager).registerItem(cvContainer);
    testling = cvContainer.getComponent(CollectionView.COLLECTION_VIEW_ID) as CollectionView;

    new LivecontextCollectionViewActionsPlugin();

    catalogTree = getTree();
  }

  private function getRepositoryList():CatalogRepositoryList {
    var repositorySwitchingContainer:SwitchingContainer = getRepositorySwitchingContainer();
    return CatalogRepositoryList(repositorySwitchingContainer.getComponent(CollectionViewConstants.LIST_VIEW)) as CatalogRepositoryList;
  }



  private function getRepositorySwitchingContainer():SwitchingContainer {
    var myCatalogRepositoryContainer:Container = Container(getCollectionModesContainer().getComponent(CollectionViewModel.REPOSITORY_MODE));
    var listViewSwitchingContainer:Container = Container(myCatalogRepositoryContainer.getComponent("listViewSwitchingContainer"));
    var repositorySwitchingContainer:SwitchingContainer = SwitchingContainer(listViewSwitchingContainer.getComponent(CatalogRepositoryListContainer.VIEW_CONTAINER_ITEM_ID));
    return repositorySwitchingContainer;
  }

  private function getCollectionModesContainer():SwitchingContainer {
    return SwitchingContainer(testling.getComponent(CollectionView.COLLECTION_MODES_CONTAINER_ITEM_ID));
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


  private function getTree():LibraryTree {
    var myCatalogRepositoryContainer:Container = Container(getCollectionModesContainer().getComponent(CollectionViewModel.REPOSITORY_MODE));
    return myCatalogRepositoryContainer.getComponent(CollectionView.TREE_ITEM_ID) as LibraryTree;
  }

  override public function tearDown():void {
    super.tearDown();
    editorContext.getSitesService().getPreferredSiteId = getPreferredSite;
  }

  //noinspection JSUnusedGlobalSymbols
  /**
   * Test the product images search
   */
  public function testSearchProductImages():void {
    chain(
            initStore(),
            loadContentRepository(),
            waitForContentRepositoryLoaded(),
            loadContentTypes(),
            waitForContentTypesLoaded(),
            createTestlingStep(),
            selectStore(),
            waitUntilStoreIsSelected(),
            selectNextCatalogTreeNode(),
            waitUntilMarketingSpotsAreSelected(),
            selectNextCatalogTreeNode(),
            waitUntilProductCatalogIsSelected(),
            //wait for the product catalog node to be expanded
            waitUntilSelectedTreeNodeIsExpanded(),
            selectNextCatalogTreeNode(),
            //wait for the Apparel node to be expanded
            waitUntilSelectedTreeNodeIsExpanded(),
            selectNextCatalogTreeNode(),
            //wait for the women node to be expanded
            waitUntilSelectedTreeNodeIsExpanded(),
            selectNextCatalogTreeNode(),
            //wait for the women node to be expanded
            waitUntilSelectedTreeNodeIsExpanded(),
            selectNextCatalogTreeNode(),
            //wait for the women node to be expanded
            waitUntilSelectedTreeNodeIsExpanded(),
            selectNextCatalogTreeNode(),

            //now the Dresses node is selected
            waitUntilProductIsLoadedInRepositoryList(),
            waitUntilSearchProductPicturesToolbarButtonIsInvisible(),
            openContextMenuOnFirstItemOfRepositoryList(),
            waitUntilRepositoryListContextMenuOpened(),
            waitUntilSearchProductPicturesToolbarButtonIsEnabled(),
            waitUntilSearchProductPicturesContextMenuIsEnabled(),
            searchProductPicturesUsingContextMenu(),
            waitUntilSearchModeIsActive(),
            waitUntilSearchTextIsPartnumber(),
            waitUntilSearchTypeIsPicture(),
            waitUntilSearchFolderIsRoot()
    );
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

  private function getRepositoryContainer():CatalogRepositoryList {
    var repositoryContainer:Container = Container(getCollectionModesContainer().getComponent(CollectionViewModel.REPOSITORY_MODE));
    var repositorySwitch:SwitchingContainer = SwitchingContainer(Container(repositoryContainer.getComponent("listViewSwitchingContainer")));
    var repositoryListContainer:CatalogRepositoryListContainer = CatalogRepositoryListContainer(repositorySwitch.getComponent(CatalogRepositoryListContainer.VIEW_CONTAINER_ITEM_ID));
    //ensure type cast!!!! there are other list views too
    return repositoryListContainer.getComponent(CollectionViewConstants.LIST_VIEW) as CatalogRepositoryList;
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

  private function waitUntilStoreIsSelected():Step {
    return new Step("catalog tree should select the store",
            function ():Boolean {
              var selection:Array = catalogTree.getSelection();
              return selection.length === 1 && Model(selection[0]).get("text") === "PerfectChefESite";
            },
            function ():void {
              var selectionModel:TreeSelectionModel = TreeSelectionModel(catalogTree.getSelectionModel());
              selectionModel.getSelection()[0]['expand']();
            });
  }

  private function selectNextCatalogTreeNode():Step {
    return new Step("selecting next catalog tree node",
            function ():Boolean {
              return true;

            },
            function ():void {
              var selectionModel:TreeSelectionModel = TreeSelectionModel(catalogTree.getSelectionModel());
              selectionModel.selectNext();
              selectionModel.getSelection()[0]['expand']();
            });
  }

  private function waitUntilMarketingSpotsAreSelected():Step {
    return new Step("catalog tree should select the marketing root",
            function ():Boolean {
              return catalogTree.getSelection().length > 0 &&
                      ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'StoreTree_marketing_root')
                      === Model(catalogTree.getSelection()[0]).get('text');

            });
  }

  private function waitUntilProductCatalogIsSelected():Step {
    return new Step("catalog tree should select the product catalog",
            function ():Boolean {
              return catalogTree.getSelection().length > 0 &&
                      ("Product Catalog" === Model(catalogTree.getSelection()[0]).get("text") ||
                      "Produktkatalog" === Model(catalogTree.getSelection()[0]).get("text"));

            },
            function ():void {
              TreeSelectionModel(catalogTree.getSelectionModel()).selectNext();
            });
  }


  private function waitUntilRepositoryListContextMenuOpened():Step {
    return new Step("Wait for the context menu on the repository list to be opened",
            function ():Boolean {
              return findCatalogRepositoryContextMenu();
            }
    );
  }

  private function getProductPicturesSearchButton():Button {
    return Button(getActiveToolbar().queryById(LivecontextAssetStudioPlugin.SEARCH_PRODUCT_PICTURES_BUTTON_ITEM_ID));
  }

  private function waitUntilSearchModeIsActive():Step {
    return new Step("Search Mode should be active",
            function ():Boolean {
              return getCollectionModesContainer().getActiveItemValue() === CollectionViewModel.SEARCH_MODE;
            }
    );
  }

  private function waitUntilSearchTextIsPartnumber():Step {
    return new Step("Search Text should be the part number of the product",
            function ():Boolean {
              var mainStateBean:Bean = testling.getCollectionViewModel().getMainStateBean();
              return mainStateBean.get(CollectionViewModel.SEARCH_TEXT_PROPERTY) === "AuroraWMDRS-1";
            }
    );
  }

  private function waitUntilSearchTypeIsPicture():Step {
    return new Step("Search Type should be CMPicture",
            function ():Boolean {
              var mainStateBean:Bean = testling.getCollectionViewModel().getMainStateBean();
              return mainStateBean.get(CollectionViewModel.CONTENT_TYPE_PROPERTY) === "CMPicture";
            }
    );
  }

  private function waitUntilSearchFolderIsRoot():Step {
    return new Step("Search Folder should be root",
            function ():Boolean {
              var mainStateBean:Bean = testling.getCollectionViewModel().getMainStateBean();
              var folder:ContentImpl = mainStateBean.get(CollectionViewModel.FOLDER_PROPERTY);
              return folder.getPath() === "/";
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

  private function waitUntilSelectedTreeNodeIsExpanded():Step {
    return new Step("Wait for the selected node of the catalog tree to be expanded",
            function ():Boolean {
              var selectionModel:TreeSelectionModel = TreeSelectionModel(catalogTree.getSelectionModel());
              var selection:Array = selectionModel.getSelection();
              return selection.length === 1 && NodeInterface(selection[0]).isExpanded();
            }
    );
  }

  private function waitUntilSearchProductPicturesToolbarButtonIsInvisible():Step {
    return new Step("Wait for the product pictures search toolbar button is invisible",
            function ():Boolean {
              return getProductPicturesSearchButton().hidden;
            }
    )
  }

  private function waitUntilSearchProductPicturesToolbarButtonIsEnabled():Step {
    return new Step("Wait for the product pictures search toolbar button is enabled",
            function ():Boolean {
              return !getProductPicturesSearchButton().disabled;
            }
    )
  }

  private function waitUntilSearchProductPicturesContextMenuIsEnabled():Step {
    return new Step("Wait for the product pictures search context menu item is enabled",
            function ():Boolean {
              return !searchProductPicturesContextMenuItem.disabled;
            }
    )
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

  private function searchProductPicturesUsingContextMenu():Step {
    return new Step("Search Product Pictures using the context menu",
            function ():Boolean {
              return true;
            },
            function ():void {
              searchProductPicturesContextMenuItem.baseAction.execute();
            }
    );

  }

  private function openContextMenu(grid:GridPanel, row:Number):void {
    var event:Object = {
      getXY: function ():Array {
        return TableUtil.getCell(grid, row, 1).getXY();
      },
      preventDefault: function ():void {
        //do nothing
      },
      getTarget: function():HTMLElement {
        return TableUtil.getCellAsDom(grid, row, 1);
      },
      type: ContextMenuEventAdapter.EVENT_NAME
    };
    grid.fireEvent("rowcontextmenu", grid, null, null, row, event);
  }

  private function findCatalogRepositoryContextMenu():CatalogRepositoryContextMenu {
    var contextMenu:CatalogRepositoryContextMenu = ComponentManager.getAll().filter(function (component:Component):Boolean {
              return !component.up() && !component.hidden && component.isXType(CatalogRepositoryContextMenu.xtype);
            })[0] as CatalogRepositoryContextMenu;
    if (contextMenu) {
      searchProductPicturesContextMenuItem = contextMenu.getComponent(LivecontextAssetStudioPlugin.SEARCH_PRODUCT_PICTURES_MENU_ITEM_ID) as Item;
    }

    return  contextMenu;
  }

}
}
