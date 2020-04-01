package com.coremedia.livecontext.ibm.studio {
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ecommerce.studio.ECommerceStudioPlugin;
import com.coremedia.ecommerce.studio.components.link.CatalogLinkContextMenu;
import com.coremedia.ecommerce.studio.components.link.CatalogLinkPropertyField;
import com.coremedia.livecontext.ibm.studio.mgmtcenter.ManagementCenterUtil;
import com.coremedia.livecontext.studio.AbstractProductTeaserComponentsTest;
import com.coremedia.livecontext.studio.CatalogThumbnailResolver;
import com.coremedia.livecontext.studio.components.link.*;
import com.coremedia.ui.data.test.Step;
import com.coremedia.ui.util.QtipUtil;
import com.coremedia.ui.util.TableUtil;

import ext.Component;
import ext.ComponentManager;
import ext.button.Button;
import ext.container.Viewport;
import ext.menu.Item;

import js.HTMLElement;

public class EcommerceIbmCatalogLinkPropertyFieldTest extends AbstractProductTeaserComponentsTest {
  private var link:CatalogLinkPropertyField;
  private var openWcsButton:Button;
  private var openInTabMenuItem:Item;
  private var openWcsMenuItem:Item;
  private var viewPort:Viewport;

  override public function setUp():void {
    super.setUp();
    QtipUtil.registerQtipFormatter();
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver("CatalogObject"));

    var conf:CatalogLinkPropertyFieldTestView = CatalogLinkPropertyFieldTestView({});
    conf.bindTo = getBindTo();
    conf.forceReadOnlyValueExpression = getForceReadOnlyValueExpression();

    createTestling(conf);
  }

  override public function tearDown():void {
    super.tearDown();
    viewPort.destroy();
  }

  override protected function createPlugin():void {
    new EcommerceIbmStudioPlugin();
  }

  //noinspection JSUnusedGlobalSymbols
  public function testCatalogLink():void {
    chain(
      waitForProductTeaserToBeLoaded(),
      //still nothing selected
      checkOpenWcsButtonDisabled(),
      openContextMenu(), //this selects the link
      checkOpenWcsButtonEnabled(),
      checkOpenWcsContextMenuEnabled(),
      setForceReadOnly(true),
      openContextMenu(), //this selects the link
      //valid selected link can be always opened
      checkOpenWcsButtonEnabled(),
      checkOpenWcsContextMenuEnabled(),
      setLink(ORANGES_ID + '503'),
      setLink(ORANGES_ID + '404'),
      openContextMenu(), //this selects the link
      //invalid link --> cannot open
      checkOpenWcsButtonDisabled(),
      //invalid link --> cannot open
      checkOpenWcsContextMenuDisabled(),
      setForceReadOnly(false),
      openContextMenu(), //this selects the link
      //invalid link --> cannot open
      checkOpenWcsButtonDisabled(),
      //invalid link --> cannot open
      checkOpenWcsContextMenuDisabled(),
      setLink(ORANGES_SKU_ID),
      openContextMenu(), //this selects the link
      checkOpenWcsButtonEnabled(),
      checkOpenWcsContextMenuEnabled(),
      setForceReadOnly(true),
      openContextMenu(), //this selects the link
      //valid selected link can be always opened
      checkOpenWcsButtonEnabled(),
      checkOpenWcsContextMenuEnabled(),
      setForceReadOnly(false),
      setLink(null),
      checkOpenWcsButtonDisabled()
    );
  }

  private function openContextMenu():Step {
    return new Step("open Context Menu",
      function ():Boolean {
        return true;
      },
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


  private function checkOpenWcsButtonDisabled():Step {
    return new Step("check open Wcs button disabled",
      function ():Boolean {
        return openWcsButton.disabled;
      }
    );
  }

  private function checkOpenWcsButtonEnabled():Step {
    return new Step("check open Wcs button enabled",
      function ():Boolean {
        return !ManagementCenterUtil.isSupportedBrowser() || !openWcsButton.disabled;
      }
    );
  }

  private function checkOpenWcsContextMenuDisabled():Step {
    return new Step("check open Wcs context menu disabled",
      function ():Boolean {
        return !ManagementCenterUtil.isSupportedBrowser() || openWcsMenuItem.disabled;
      }
    );
  }

  private function checkOpenWcsContextMenuEnabled():Step {
    return new Step("check open Wcs context menu enabled",
    function ():Boolean {
      return !ManagementCenterUtil.isSupportedBrowser() || !openWcsMenuItem.disabled;
    }
    );
  }

  /**
   * private helper method to create the container for tests
   */
  private function createTestling(config:CatalogLinkPropertyFieldTestView):void {
    viewPort = new CatalogLinkPropertyFieldTestView(config);
    link = viewPort.getComponent(CatalogLinkPropertyFieldTestView.CATALOG_LINK_PROPERTY_FIELD_ITEM_ID) as CatalogLinkPropertyField;
    var openInTabButton:Button = Button(link.getTopToolbar().find('itemId', ECommerceStudioPlugin.OPEN_IN_TAB_BUTTON_ITEM_ID)[0]);
    //we cannot and don't want test the open in tab action as it needs the workarea.
    link.getTopToolbar().remove(openInTabButton);
    openWcsButton = Button(link.getTopToolbar().find('itemId', EcommerceIbmStudioPlugin.OPEN_IN_MCENTER_BUTTON_ITEM_ID)[0]);
  }

  private function findCatalogLinkContextMenu():CatalogLinkContextMenu {
    var contextMenu:CatalogLinkContextMenu = ComponentManager.getAll().filter(function (component:Component):Boolean {
      return !component.up() && !component.hidden && component.isXType(CatalogLinkContextMenu.xtype);
    })[0] as CatalogLinkContextMenu;
    if (contextMenu) {
      openInTabMenuItem = contextMenu.getComponent(ECommerceStudioPlugin.OPEN_IN_TAB_MENU_ITEM_ID) as Item;
      //we cannot and don't want test the open in tab action as it needs the workarea.
      contextMenu.remove(openInTabMenuItem);
      openWcsMenuItem = contextMenu.getComponent(EcommerceIbmStudioPlugin.OPEN_IN_MCENTER_MENU_ITEM_ID) as Item;
    }

    return  contextMenu;
  }
}
}
