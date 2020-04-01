package com.coremedia.livecontext.studio.forms {
import com.coremedia.livecontext.studio.AbstractProductTeaserComponentsTest;
import com.coremedia.livecontext.studio.components.product.ViewSettingsRadioGroup;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.test.Step;

import ext.Ext;
import ext.container.Viewport;
import ext.tip.QuickTipManager;

public class ProductTeaserSettingsFormTest extends AbstractProductTeaserComponentsTest {
  private var viewPort:Viewport;
  private var viewSettings:ViewSettingsRadioGroup;

  override public function setUp():void {
    super.setUp();
    QuickTipManager.init(true);
  }

  override public function tearDown():void {
    super.tearDown();
    viewPort && viewPort.destroy();
    viewPort = null;
  }

  public function testProductTeaserSettingsForm():void {
    chain(
            loadContentRepository(),
            waitForContentRepositoryLoaded(),
            loadContentTypes(),
            waitForContentTypesLoaded(),
            loadProductTeaser(),
            waitForProductTeaserToBeLoaded(),
            createTestlingStep(),
            //inherit is the default
            waitForRadioToBeChecked(ViewSettingsRadioGroup.INHERITED_SETTING),
            checkRadio(ViewSettingsRadioGroup.ENABLED_SETTING),
            waitForRadioToBeChecked(ViewSettingsRadioGroup.ENABLED_SETTING),
            checkRadio(ViewSettingsRadioGroup.DISABLED_SETTING),
            waitForRadioToBeChecked(ViewSettingsRadioGroup.DISABLED_SETTING),
            checkRadio(ViewSettingsRadioGroup.INHERITED_SETTING),
            waitForRadioToBeChecked(ViewSettingsRadioGroup.INHERITED_SETTING)
    )
  }

  private function checkRadio(value:String):Step {
    return new Step("change view settings to " + value,
            function ():Boolean {return true;},
            function ():void {
              var valueObject:Object = {};
              valueObject[viewSettings.radioButtonFormName] = value;
              viewSettings.setValue(valueObject);
            }
    );
  }

  private function waitForRadioToBeChecked(itemId:String):Step {
    return new Step("Wait for radio button " + itemId + " to be checked",
            function ():Boolean {
              var value:Object = viewSettings.getValue();
              return value[viewSettings.radioButtonFormName] === itemId;
            }
    );
  }

  private function createTestlingStep():Step {
    var ve:ValueExpression;
    return new Step("Create the testling",
            function ():Boolean {
              if(!viewPort) {
                // create only once
                var config:ProductTeaserSettingsFormTestView = ProductTeaserSettingsFormTestView({});
                config.bindTo = getBindTo();
                viewPort = new ProductTeaserSettingsFormTestView(ProductTeaserSettingsFormTestView(Ext.apply({}, config)));
                viewSettings = viewPort.queryById('viewSettingsPropertyField') as ViewSettingsRadioGroup;
                ve = Object(viewSettings).getInheritOptionVisibleExpression(config.bindTo);
              }
              // but wait for inherit option to initialize
              return ve.getValue();
            }
    );
  }

}
}