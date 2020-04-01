package com.coremedia.blueprint.studio.googleanalytics {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.test.AbstractRemoteTest;
import com.coremedia.ui.util.createComponentSelector;

import ext.container.Viewport;

import joo.getOrCreatePackage;

public class GoogleAnalyticsStudioButtonTest extends AbstractRemoteTest {

  private var viewPort:Viewport;

  public function GoogleAnalyticsStudioButtonTest() {
  }

  override public function setUp():void {
    super.setUp();

    delete getOrCreatePackage("com.coremedia.cms.editor.sdk")['editorContext'];
    EditorContextImpl.initEditorContext();

    viewPort = new GoogleAnalyticsStudioButtonTestView(
            GoogleAnalyticsStudioButtonTestView({contentValueExpression: ValueExpressionFactory.createFromValue()}));
  }

  override public function tearDown():void {
    super.tearDown();
  }

  public function testButtonDisabled():void {
    var button:GoogleAnalyticsReportPreviewButton =
            viewPort.down(createComponentSelector()._xtype(GoogleAnalyticsReportPreviewButton.xtype).build()) as GoogleAnalyticsReportPreviewButton;
    assertTrue(button.disabled);
  }

  public function testDeepLinkReportUrl():void {
    var args:Object = undefined;
    window.open = function (... myArgs) : void { args = myArgs;};

    var button:GoogleAnalyticsReportPreviewButton =
            viewPort.down(createComponentSelector()._xtype(GoogleAnalyticsReportPreviewButton.xtype).build()) as GoogleAnalyticsReportPreviewButton;
    button.setContent(Content({
      getNumericId : function():int {return 42;},
      "get": function(prop:String):* {
        if (prop === "type") {
          return {name: 'typeWithPreview'};
        }
      }
    }));
    waitUntil("button still disabled",
            function():Boolean {
              return !button.disabled
            },
            button['handler'] // simulate click
    );
    waitUntil("no window opened",
            function():Boolean {return args !== undefined;}
    )
  }

  private static const DRILLDOWN_URL:String = "http://host.domain.net/gai/drilldown/42";

  protected override function getMockCalls():Array {
    return [
      {
        "request": { "uri": "alxservice/42" },
        "response": { "body": {
          "googleAnalytics": DRILLDOWN_URL
        }}
      }
    ];
  }

}
}
