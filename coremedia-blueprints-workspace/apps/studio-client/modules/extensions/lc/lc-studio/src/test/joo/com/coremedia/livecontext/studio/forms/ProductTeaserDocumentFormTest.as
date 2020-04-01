package com.coremedia.livecontext.studio.forms {
import com.coremedia.livecontext.studio.AbstractProductTeaserComponentsTest;
import com.coremedia.ui.data.test.Step;

import ext.container.Viewport;
import ext.form.field.TextArea;
import ext.form.field.TextField;
import ext.tip.QuickTipManager;

public class ProductTeaserDocumentFormTest extends AbstractProductTeaserComponentsTest{
  private var viewPort:Viewport;
  private var productTeaserTitleField:TextField;
  private var productTeaserTextArea:TextArea;

  override public function setUp():void {
    super.setUp();
    QuickTipManager.init(true);
  }

  override public function tearDown():void {
    super.tearDown();
    viewPort && viewPort.destroy();
  }

  //noinspection JSUnusedGlobalSymbols
  public function testProductTeaserDocumentForm():void {
    chain(
            loadContentRepository(),
            waitForContentRepositoryLoaded(),
            loadContentTypes(),
            waitForContentTypesLoaded(),
            loadProductTeaser(),
            waitForProductTeaserToBeLoaded(),
            waitForProductTeaserContentTypeToBeLoaded(),
            createTestlingStep(),
            waitForTeaserTitleFieldValue(ORANGES_NAME),
            waitForTeaserTextAreaValue(ORANGES_SHORT_DESC)
    )
  }

  private function waitForTeaserTitleFieldValue(value:String):Step {
    return new Step("Wait for the product teaser title field to be " + value,
            function ():Boolean {
              return productTeaserTitleField.getValue() === value;
            }
    );
  }

  private function waitForTeaserTextAreaValue(value:String):Step {
    return new Step("Wait for the product teaser text area to be " + value,
            function ():Boolean {
              return productTeaserTextArea.getValue() && productTeaserTextArea.getValue().indexOf(value) >= 0;
            }
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

  private function createTestling():void {
    var config:ProductTeaserDocumentFormTestView = ProductTeaserDocumentFormTestView({});
    config.bindTo = getBindTo();
    viewPort = new ProductTeaserDocumentFormTestView(config);
    productTeaserTitleField = viewPort.queryById('stringPropertyField') as TextField;
    productTeaserTextArea = viewPort.queryById('textAreaPropertyField') as TextArea;
  }
}
}