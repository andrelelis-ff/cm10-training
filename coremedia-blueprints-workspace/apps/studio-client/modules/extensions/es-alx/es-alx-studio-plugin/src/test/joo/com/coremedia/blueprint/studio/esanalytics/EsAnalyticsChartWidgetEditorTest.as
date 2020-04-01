package com.coremedia.blueprint.studio.esanalytics {

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.SiteImpl;
import com.coremedia.cms.editor.sdk.sites.SitesService;
import com.coremedia.ui.components.LocalComboBox;
import com.coremedia.ui.data.Locale;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.util.createComponentSelector;

import ext.ComponentManager;
import ext.Ext;
import ext.data.AbstractStore;
import ext.form.field.ComboBox;

import flexunit.framework.TestCase;

public class EsAnalyticsChartWidgetEditorTest extends TestCase {
  private static const FIELD_LABEL:String = "Site";
  private static const VALUE_FIELD:String = "id";
  private static const ID_1:String = "test-1";
  private static const NAME_1:String = "test-1-" + new Date();
  private static const ID_2:String = "test-2";
  private static const NAME_2:String = "test-2-" + new Date();

  private var site1:SiteImpl;
  private var site2:SiteImpl;

  override public function setUp():void {
    super.setUp();
    EditorContextImpl.initEditorContext();
    mockSites();
    editorContext['getSitesService'] = getSitesService;
  }

  public function testWidgetEditMode():void {

    // create widget instance rendered to document body
    // rendering is necessary because bindPlugins load stores only when component is rendered
    var editorCfg:EsAnalyticsChartWidgetEditor = EsAnalyticsChartWidgetEditor({});
    editorCfg.renderTo = Ext.getBody();
    var esAlxWidgetEditor:EsAnalyticsChartWidgetEditor = ComponentManager.create(editorCfg) as EsAnalyticsChartWidgetEditor;

    // the combobox provides the selection of root channels aka Sites
    var combo:ComboBox = esAlxWidgetEditor.down(createComponentSelector()._xtype(LocalComboBox.xtype).build()) as ComboBox;
    var comboFieldLabel:String = combo.fieldLabel;
    var comboStore:AbstractStore = combo.getStore();


    // assertion of the ALX widget editor, combobox and fields
    assertNotUndefined(esAlxWidgetEditor);
    assertNotUndefined(combo);
    assertNotUndefined(comboFieldLabel);
    assertEquals(FIELD_LABEL, comboFieldLabel);
    assertEquals(VALUE_FIELD, combo.valueField);

    // assertion of the combobox store filled by the local bean properties
    assertNotUndefined(comboStore);
    assertEquals(2, comboStore.getCount());
    assertEquals(0, comboStore.find("id", ID_1));
    assertEquals(0, comboStore.find("value", NAME_1));
    assertEquals(1, comboStore.find("id", ID_2));
    assertEquals(1, comboStore.find("value", NAME_2));

  }

  private function getSitesService():SitesService {
    return SitesService({
      'getSites': function ():Array {
        return [site1, site2];
      }
    });
  }

  private function mockSites():void {
    var contentRepository:ContentRepository = beanFactory.getRemoteBean('content') as ContentRepository;
    site1 = new SiteImpl(ID_1, null, null, null, mockContent({id: ID_1, name: NAME_1}), NAME_1, new Locale({'displayName': 'locale1'}), null, true, false);
    site2 = new SiteImpl(ID_2, null, null, null, mockContent({id: ID_2, name: NAME_2}), NAME_2, new Locale({'displayName': 'locale2'}), null, true, false);
  }

  private static const MOCK_CONTENT_PROTOTYPE:Object = {
    addPropertyChangeListener: Ext.emptyFn,
    "get": function(prop:String):* {
      return this[prop];
    },
    getUriPath: function():String {
      return this.id;
    }
  };
  private static function mockContent(props:Object):Content {
    return Content(Ext.apply(Object.create(MOCK_CONTENT_PROTOTYPE), props));
  }
}
}
