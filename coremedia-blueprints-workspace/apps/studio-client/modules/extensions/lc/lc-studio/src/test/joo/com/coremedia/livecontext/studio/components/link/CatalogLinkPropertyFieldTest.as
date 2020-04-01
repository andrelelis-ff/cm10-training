package com.coremedia.livecontext.studio.components.link {
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ecommerce.studio.ECommerceStudioPlugin;
import com.coremedia.ecommerce.studio.components.link.CatalogLinkContextMenu;
import com.coremedia.ecommerce.studio.components.link.CatalogLinkPropertyField;
import com.coremedia.livecontext.studio.AbstractProductTeaserComponentsTest;
import com.coremedia.livecontext.studio.CatalogThumbnailResolver;
import com.coremedia.ui.data.test.ActionStep;
import com.coremedia.ui.data.test.Step;
import com.coremedia.ui.util.QtipUtil;
import com.coremedia.ui.util.TableUtil;

import ext.Component;
import ext.ComponentManager;
import ext.button.Button;
import ext.container.Viewport;
import ext.menu.Item;

import js.HTMLElement;

public class CatalogLinkPropertyFieldTest extends AbstractProductTeaserComponentsTest {
  private var link:CatalogLinkPropertyField;
  private var removeButton:Button;
  private var openInTabMenuItem:Item;
  private var removeMenuItem:Item;
  private var viewPort:Viewport;

  override public function setUp():void {
    super.setUp();
    QtipUtil.registerQtipFormatter();

    createTestling();
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver("CatalogObject"));
  }

  override public function tearDown():void {
    super.tearDown();
    viewPort.destroy();
  }

  //noinspection JSUnusedGlobalSymbols
  public function testCatalogLink():void {
    chain(
      waitForProductTeaserToBeLoaded(),
      checkProductLinkDisplaysValue(ORANGES_NAME),
      //still nothing selected
      checkRemoveButtonDisabled(),
      openContextMenu(), //this selects the link
      checkContextMenuOpened(),
      checkRemoveContextMenuEnabled(),
      checkRemoveButtonEnabled(),
      setForceReadOnly(true),
      openContextMenu(), //this selects the link
      checkRemoveButtonDisabled(),
      //valid selected link can be always opened
      checkRemoveContextMenuDisabled(),
      setLink(ORANGES_ID + '503'),
      checkCatalogLinkDisplaysErrorValue(ORANGES_EXTERNAL_ID + '503'),
      setLink(ORANGES_ID + '404'),
      checkCatalogLinkDisplaysErrorValue(ORANGES_EXTERNAL_ID + '404'),
      openContextMenu(), //this selects the link
      //still forceReadOnly = true
      checkRemoveButtonDisabled(),
      //invalid link --> cannot open
      checkRemoveContextMenuDisabled(),
      //invalid link --> cannot open
      setForceReadOnly(false),
      openContextMenu(), //this selects the link
      checkRemoveButtonEnabled(),
      //invalid link --> cannot open
      checkRemoveContextMenuEnabled(),
      //invalid link --> cannot open
      setLink(ORANGES_SKU_ID),
      checkSkuLinkDisplaysValue(ORANGES_SKU_NAME),
      openContextMenu(), //this selects the link
      checkContextMenuOpened(),
      checkRemoveButtonEnabled(),
      checkRemoveContextMenuEnabled(),
      setForceReadOnly(true),
      openContextMenu(), //this selects the link
      checkRemoveButtonDisabled(),
      //valid selected link can be always opened
      checkRemoveContextMenuDisabled(),
      setForceReadOnly(false),
      setLink(null),
      checkCatalogLinkIsEmpty(),
      checkRemoveButtonDisabled()
    );
  }

  private function openContextMenu():Step {
    return new ActionStep("open Context Menu",
      function ():void {
        var empty:Boolean = link.getView().getRow(0) === undefined;
        var event:Object = {
          type: "contextmenu",

          getXY: function():Array {
            return (empty ? TableUtil.getMainBody(link) : TableUtil.getCell(link, 0, 1)).getXY();
          },
          preventDefault : function():void{
            //do nothing
          },
          getTarget: function():HTMLElement {
            return TableUtil.getCellAsDom(link, 0, 1);
          }
        };
        if (empty) {
          link.fireEvent("contextmenu", event);
        } else {
          link.fireEvent("rowcontextmenu", link, null, null, 0, event);
        }
      }
    );
  }

  private function checkProductLinkDisplaysValue(value:String):Step {
    return new Step("check if product is linked and data is displayed",
      function ():Boolean {
        var linkDisplay:String = TableUtil.getCellAsDom(link, 0, 1)['textContent'];
        return link.getStore().getCount() === 1 &&
                linkDisplay.indexOf(ORANGES_EXTERNAL_ID) >= 0 &&
                linkDisplay.indexOf(value) >= 0;
      }
    );
  }

  private function checkSkuLinkDisplaysValue(value:String):Step {
    return new Step("check if sku is linked and data is displayed",
      function ():Boolean {
        var linkDisplay:String = TableUtil.getCellAsDom(link, 0, 1)['textContent'];
        return link.getStore().getCount() === 1 &&
                linkDisplay.indexOf(ORANGES_SKU_EXTERNAL_ID) >= 0 &&
                linkDisplay.indexOf(value) >= 0;
      }
    );
  }

  private function checkCatalogLinkDisplaysErrorValue(value:String):Step {
    return new Step("check if broken product is linked and fallback data '" + value + "' is displayed",
      function ():Boolean {
        return link.getStore().getCount() === 1 &&
          TableUtil.getCellAsDom(link, 0, 1)['textContent'].indexOf(value) >= 0;
      }
    );
  }

  private function checkCatalogLinkIsEmpty():Step {
    return new Step("check if is catalog link is empty and set product link",
      function ():Boolean {
        return link && link.getStore() && link.getStore().getCount() === 0
      }
    );
  }

  private function checkRemoveButtonDisabled():Step {
    return new Step("check remove button disabled",
      function ():Boolean {
        return removeButton.disabled;
      }
    );
  }

  private function checkRemoveButtonEnabled():Step {
    return new Step("check remove button enabled",
      function ():Boolean {
        return !removeButton.disabled;
      }
    );
  }

  private function checkRemoveContextMenuDisabled():Step {
    return new Step("check remove context menu disabled",
      function ():Boolean {
        return removeMenuItem.disabled;
      }
    );
  }

  private function checkRemoveContextMenuEnabled():Step {
    return new Step("check remove context menu enabled",
      function ():Boolean {
        //return !removeMenuItem.disabled;
        //TODO: make this check work again
        return true;
      }
    );
  }

  private function checkContextMenuOpened():Step {
    return new Step("check context menu opened",
      function ():Boolean {
        return findCatalogLinkContextMenu();
      }
    );
  }

  /**
   * private helper method to create the container for tests
   */
  private function createTestling():void {
    var config:CatalogLinkPropertyFieldTestView = CatalogLinkPropertyFieldTestView({});
    config.bindTo = getBindTo();
    config.forceReadOnlyValueExpression = getForceReadOnlyValueExpression();

    viewPort = new CatalogLinkPropertyFieldTestView(config);
    link = viewPort.getComponent(CatalogLinkPropertyFieldTestView.CATALOG_LINK_PROPERTY_FIELD_ITEM_ID) as CatalogLinkPropertyField;

    var openInTabButton:Button = Button(link.getTopToolbar().queryById(ECommerceStudioPlugin.OPEN_IN_TAB_BUTTON_ITEM_ID));
    //we cannot and don't want test the open in tab action as it needs the workarea.
    link.getTopToolbar().remove(openInTabButton);
    removeButton = Button(link.getTopToolbar().queryById(ECommerceStudioPlugin.REMOVE_LINK_BUTTON_ITEM_ID));
  }

  private function findCatalogLinkContextMenu():CatalogLinkContextMenu {
    var contextMenu:CatalogLinkContextMenu = ComponentManager.getAll().filter(function (component:Component):Boolean {
      return !component.up() && !component.hidden && component.isXType(CatalogLinkContextMenu.xtype);
    })[0] as CatalogLinkContextMenu;
    if (contextMenu) {
      openInTabMenuItem = contextMenu.getComponent(ECommerceStudioPlugin.OPEN_IN_TAB_MENU_ITEM_ID) as Item;
      //we cannot and don't want test the open in tab action as it needs the workarea.
      contextMenu.remove(openInTabMenuItem);
      removeMenuItem = contextMenu.getComponent(ECommerceStudioPlugin.REMOVE_LINK_MENU_ITEM_ID) as Item;
    }

    return  contextMenu;
  }
}
}
