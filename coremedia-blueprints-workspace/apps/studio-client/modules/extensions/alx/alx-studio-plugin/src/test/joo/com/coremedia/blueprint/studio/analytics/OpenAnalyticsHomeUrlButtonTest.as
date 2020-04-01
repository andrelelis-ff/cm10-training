package com.coremedia.blueprint.studio.analytics {
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.impl.BeanFactoryImpl;
import com.coremedia.ui.data.test.AbstractRemoteTest;

import ext.Ext;

import ext.container.Viewport;

import joo.getOrCreatePackage;
import joo.getQualifiedObject;

public class OpenAnalyticsHomeUrlButtonTest extends AbstractRemoteTest {

  private var button:OpenAnalyticsHomeUrlButton;
  private var args:Object;

  private var window_open:Function;
  private var is_sub_object:Function;
  private var beanImplPrototype:*;

  static function isSubObject(value:*, propertyPath:*):Boolean {
    // Plain objects represent sub-beans.
    return value.constructor === Object;
  }

  override public function setUp():void {
    super.setUp();

    BeanFactoryImpl.initBeanFactory();

    delete getOrCreatePackage("com.coremedia.cms.editor.sdk")['editorContext'];
    EditorContextImpl.initEditorContext();

    AnalyticsStudioPluginBase['SETTINGS'] = ValueExpressionFactory.create('testSettings');
    AnalyticsStudioPluginBase.SETTINGS.setValue(beanFactory.createLocalBean({
      properties: {
        settings: {
          testService: {}
        }
      }
    }));

    beanImplPrototype = getQualifiedObject("com.coremedia.ui.data.impl.BeanImpl")['prototype'];
    is_sub_object = beanImplPrototype.isSubObject;
    beanImplPrototype.isSubObject = isSubObject;
    window_open = window.open;
    window.open = function (... myArgs) : void { args = myArgs;};
    button = Ext.create(OpenAnalyticsHomeUrlButton, { serviceName: 'testService'});
    // show buttons
    Ext.create(Viewport, {
      items: [ button ]
    });
  }

  override public function tearDown():void {
    setHomeUrlValue(null);
    window.open = window_open;
    args = null;
    beanImplPrototype.isSubObject = is_sub_object;
    super.tearDown();
  }

  static function setHomeUrlValue(value:String):void {
    AnalyticsStudioPluginBase.SETTINGS.extendBy("properties.settings.testService.homeUrl").setValue(value);
  }

  public function testInitialState():void {
    assertTrue(button.disabled);
  }

  public function testSetHomeUrls():void {
    setHomeUrlValue('http://fake.url');
    waitUntil("button still disabled",
            function():Boolean {
              return !button.disabled
            },
            button['handler'] // simulate click
    );
    waitUntil("home url not opened",
            function():Boolean {return args !== null && args[0] == 'http://fake.url';},
            function():void {
              setHomeUrlValue('yetAnotherInvalidUrl')
            }
    );
    waitUntil("button still enabled",
            function():Boolean {
              return button.disabled
            }
    );
  }

}
}
