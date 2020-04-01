package com.coremedia.blueprint.studio.analytics {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.test.AbstractRemoteTest;

import ext.Ext;
import ext.container.Viewport;

import joo.getOrCreatePackage;

public class OpenAnalyticsDeepLinkUrlButtonTest extends AbstractRemoteTest {

  private static const DRILLDOWN_URL:String = "http://host.domain.net/my/drilldown";
  private static const MY_ID:Number = 4711;

  private var button:OpenAnalyticsDeepLinkUrlButton;
  private var window_open:Function;
  private var args:Object;

  override public function setUp():void {
    super.setUp();
    window_open = window.open;
    window.open = function (... myArgs) : void { args = myArgs;};

    delete getOrCreatePackage("com.coremedia.cms.editor.sdk")['editorContext'];
    EditorContextImpl.initEditorContext();

    button = Ext.create(OpenAnalyticsDeepLinkUrlButton, {
      serviceName: "googleAnalytics",
      contentExpression: ValueExpressionFactory.createFromValue()
    });
    Ext.create(Viewport, {
      items: [button]
    });
  }

  override public function tearDown():void {
    super.tearDown();
    button = null;
    window.open = window_open;
    args = null;
  }

  public function testButtonDisabled():void {
    assertTrue(button.disabled);
  }

  public function testDeepLinkReportUrl():void {
    button.setContent(Content({
      getNumericId: function ():int {
        return MY_ID;
      },
      "get": function(prop:String):* {
        if (prop === "type") {
          return {name: 'typeWithPreview'};
        }
      }
    }));
    waitUntil("button still disabled",
            function ():Boolean {
              return !button.disabled
            },
            button['handler'] // simulate click
    );
    waitUntil("no window opened",
            function ():Boolean {
              return args !== null && args[0] == DRILLDOWN_URL;
            }
    )
  }

  protected override function getMockCalls():Array {
    return [
      {
        "request": { "uri": "alxservice/" + MY_ID },
        "response": { "body": {
          "googleAnalytics": DRILLDOWN_URL
        }}
      }
    ];
  }

}
}