package com.coremedia.livecontext.studio.action {
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.cms.editor.sdk.actions.CollectionViewModelAction;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewModel;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.livecontext.studio.AbstractLiveContextStudioTest;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.test.Step;

import ext.data.Store;

public class CollectionViewModelActionTest extends AbstractLiveContextStudioTest {
  private static const MODE_PROPERTY:String = CollectionViewModel.MODE_PROPERTY;
  private static const REPOSITORY_MODE:String = CollectionViewModel.REPOSITORY_MODE;
  private static const SEARCH_MODE:String = CollectionViewModel.SEARCH_MODE;

  private var repositoryAction:CollectionViewModelAction;
  private var searchAction:CollectionViewModelAction;

  private var getPreferredSite:Function;
  private var preferredSiteExpression:ValueExpression;

  override public function setUp():void {
    super.setUp();

    preferredSiteExpression = ValueExpressionFactory.create('site', beanFactory.createLocalBean({site: 'HeliosSiteId'}));
    getPreferredSite = editorContext.getSitesService().getPreferredSiteId;
    editorContext.getSitesService().getPreferredSiteId = function ():String {
      return preferredSiteExpression.getValue();
    };

    repositoryAction = new CollectionViewModelAction(
      CollectionViewModelAction({property: CollectionViewModel.MODE_PROPERTY,
                                 value: REPOSITORY_MODE}));

    searchAction = new CollectionViewModelAction(
      CollectionViewModelAction({property: MODE_PROPERTY,
                                 value: SEARCH_MODE}));
  }

  override public function tearDown():void {
    super.tearDown();
    editorContext.getSitesService().getPreferredSiteId = getPreferredSite;
  }

  //noinspection JSUnusedGlobalSymbols
  public function testDefault():void {
    chain(waitForDefault());
  }

  //noinspection JSUnusedGlobalSymbols
  public function testInCmsToSearch(): void {
    chain(switchToSearch(),
          waitForCmsSearch());
  }

  //noinspection JSUnusedGlobalSymbols
  public function testInCmsToRepository(): void {
    chain(switchToSearch(),
          waitForCmsSearch(),
          switchToRepository(),
          waitForDefault());
  }

  //noinspection JSUnusedGlobalSymbols
  public function testInCatalogToSearch(): void {
    chain(switchToCatalog(),
          waitForCatalogRepository(),
          switchToSearch(),
          waitForCatalogSearch());
  }

  //noinspection JSUnusedGlobalSymbols
  public function testInCatalogToRepository(): void {
    chain(switchToCatalog(),
          switchToSearch(),
          waitForCatalogSearch(),
          switchToRepository(),
          waitForCatalogRepository());
  }

  //noinspection JSUnusedGlobalSymbols
  public function testInRepositoryToCatalog():void {
    chain(switchToCatalog(),
          waitForCatalogRepository());
  }

  //noinspection JSUnusedGlobalSymbols
  public function testInRepositoryToCms():void {
    chain(switchToCatalog(),
          waitForCatalogRepository(),
          waitForDefault());
  }

  //noinspection JSUnusedGlobalSymbols
  public function testInSearchToCatalog():void {
    chain(switchToSearch(),
          waitForCmsSearch(),
          switchToCatalog(),
          waitForCatalogSearch());
  }

  //noinspection JSUnusedGlobalSymbols
  public function testInSearchToCms():void {
    chain(switchToSearch(),
          switchToCatalog(),
          waitForCatalogSearch(),
          waitForCmsSearch());
  }

  /**
   *  Waiting and Testing Steps
   */

  private function waitForDefault():Step {
    return new Step("wait for default repository cms and default mode repository.",
      function():Boolean {
        return getCollectionViewState().get(MODE_PROPERTY) === REPOSITORY_MODE;
      },
      function():void {
        assertTrue(repositoryAction.isPressed());
        assertFalse(searchAction.isPressed());
      });
  }

  private function waitForCmsSearch():Step {
    return new Step("wait for repository cms and search mode.",
      function():Boolean {
        return getCollectionViewState().get(MODE_PROPERTY) === SEARCH_MODE;
      },
      function():void {
        assertFalse(repositoryAction.isPressed());
        assertTrue(searchAction.isPressed());
      });
  }

  private function waitForCatalogRepository():Step {
    return new Step("wait for repository catalog and mode repository.",
      function():Boolean {
        return getCollectionViewState().get(MODE_PROPERTY) === REPOSITORY_MODE;
      },
      function():void {
        assertTrue(repositoryAction.isPressed());
        assertFalse(searchAction.isPressed());
      });
  }

  private function waitForCatalogSearch():Step {
    return new Step("wait for repository catalog and mode search.",
      function():Boolean {
        return getCollectionViewState().get(MODE_PROPERTY) === SEARCH_MODE;
      },
      function():void {
        assertFalse(repositoryAction.isPressed());
        assertTrue(searchAction.isPressed());
      });
  }

  /**
   * Action Steps
   */

  private function switchToRepository():Step {
    return new Step("switch to repository.",
      function():Boolean {
        return true;
      },
      function():void {
        repositoryAction.execute();
      });
  }

  private function switchToSearch():Step {
    return new Step("switch to search.",
      function():Boolean {
        return true;
      },
      function():void {
        searchAction.execute();
      });
  }

  private function switchToCatalog():Step {
    return new Step("switch to catalog.",
      function():Boolean {
        return true;
      },
      function():void {
        var store:Store = CatalogHelper.getInstance().getActiveStoreExpression().getValue();
        getCollectionViewState().set(CollectionViewModel.FOLDER_PROPERTY, store);
      });
  }


  internal static function getCollectionViewState():Bean {
    return EditorContextImpl(editorContext).getCollectionViewModel().getMainStateBean();
  }


}
}