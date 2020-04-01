package com.coremedia.blueprint.studio.analytics {
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.ui.components.IconButton;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.test.AbstractRemoteTest;

import ext.button.Button;
import ext.menu.Item;

import joo.getOrCreatePackage;

public class AnalyticsDeepLinkButtonContainerTest extends AbstractRemoteTest {

  private static const URL_1:String = "http://host.domain.net/drilldown";
  private static const URL_2:String = "http://my.url";
  private static const MY_ID:Number = 666;

  private static const content:Object = {
    getNumericId: function ():int {
      return MY_ID;
    },
    type: {
      getNumericId: function ():int {
        return MY_ID;
      },
      name: 'typeWithPreview'}
  };

  private var window_open:Function;
  private var args:Object;

  override public function setUp():void {
    super.setUp();
    window_open = window.open;
    window.open = function (... myArgs) : void { args = myArgs;};

    delete getOrCreatePackage("com.coremedia.cms.editor.sdk")['editorContext'];
    EditorContextImpl.initEditorContext();
  }

  override public function tearDown():void {
    super.tearDown();
    window.open = window_open;
    args = null;
  }

  public function testAnalyticsDeepLinkButtonContainerSingleView():void {
    var testView:SingleAnalyticsUrlButtonTestView = new SingleAnalyticsUrlButtonTestView(
            SingleAnalyticsUrlButtonTestView({contentValueExpression: ValueExpressionFactory.createFromValue()}));

    var contentContainer:ContentProvidingTestContainerBase = ContentProvidingTestContainerBase(testView.getComponent("contentContainer"));
    var container:AnalyticsDeepLinkButtonContainer = AnalyticsDeepLinkButtonContainer(contentContainer.getComponent("alxDeepLinkButtonContainer"));
    var item:Button = Button(container.itemCollection.get(0));

    assertTrue(item is OpenAnalyticsDeepLinkUrlButton);
    assertTrue(item.disabled);

    contentContainer.setContent(content);

    waitUntil("button still disabled",
            function ():Boolean {
              return !item.disabled;
            },
            function():void {
              // invoke handler on enabled buttons:
              item['handler']();
              assertNotNull(args);
              assertEquals(URL_1, args[0]);
            }
    );

  }

  public function testAnalyticsDeepLinkButtonContainerMultiView():void {
    var testView:MultipleAnalyticsUrlButtonsTestView = new MultipleAnalyticsUrlButtonsTestView(
            MultipleAnalyticsUrlButtonsTestView({contentValueExpression: ValueExpressionFactory.createFromValue()}));
    var contentContainer:ContentProvidingTestContainerBase = ContentProvidingTestContainerBase(testView.getComponent("contentContainer"));
    var container:AnalyticsDeepLinkButtonContainer = AnalyticsDeepLinkButtonContainer(contentContainer.getComponent("alxDeepLinkButtonContainer"));

    // the menu button should be disabled initially
    var item:Button = Button(container.itemCollection.get(3));

    assertTrue(item is IconButton);
    assertNotNull(item.menu);
    assertTrue("menu button should be initially disabled", item.disabled);

    var items:Array = item.menu.itemCollection.getRange();
    assertEquals(3, items.length);
    items.forEach(function (item:Item, index:int):void {
      assertTrue("item " + index + " should be disabled",item.disabled);
    });

    contentContainer.setContent(content);

    waitUntil("button still disabled",
            function ():Boolean {
              return !item.disabled;
            },
            function():void {
              // state of menu items:
              assertTrue("first menu item should be disabled", items[0].disabled);
              assertFalse("second menu item should be enabled", items[1].disabled);
              assertFalse("third menu item should be enabled", items[2].disabled);

              // invoke handler on enabled menu items:
              items[1].handler();
              assertNotNull(args);
              assertEquals(URL_1, args[0]);
              args = null;
              items[2].handler();
              assertNotNull(args);
              assertEquals(URL_2, args[0]);
            }
    );
  }

  protected override function getMockCalls():Array {
    return [
      {
        "request": { "uri": "alxservice/" + MY_ID},
        "response": { "body": {
          "testService1": 'invalidUrl',
          "testService2": URL_1,
          "testService3": URL_2
        }}
      }
    ];
  }

}
}
