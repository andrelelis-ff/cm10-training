package com.coremedia.blueprint.studio.taxonomy.forms {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyUtil;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.premular.DocumentTabPanel;
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
import com.coremedia.cms.editor.sdk.premular.fields.StringPropertyField;
import com.coremedia.ui.util.createComponentSelector;

import ext.Component;
import ext.Plugin;
import ext.form.field.TextField;
import ext.panel.Panel;

import mx.resources.ResourceManager;

public class TaxonomyChangePluginBase implements Plugin {

  private var form:DocumentTabPanel;
  private var nameField:TextField;

  public function TaxonomyChangePluginBase(config:TaxonomyChangePlugin = null) {
  }

  public function init(component:Component):void {
    this.form = component as DocumentTabPanel;
    this.form.addListener('afterrender', addNameFieldListener);
    this.form.addListener('destroy', onTabDestroy);
  }

  private function addNameFieldListener():void {
    var fieldId:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.TaxonomyStudioPluginSettings', 'taxonomy_display_property');

    var collapsable:Panel = form.query(createComponentSelector()._xtype(PropertyFieldGroup.xtype).build())[0] as Panel;
    var stringPropertyFields:Array = collapsable.query(createComponentSelector()._xtype(StringPropertyField.xtype).build());

    for each(var field:StringPropertyField in stringPropertyFields) {
      if (field.propertyName === fieldId) {
        nameField = field.query(createComponentSelector()._xtype("textfield").build())[0] as TextField;
        nameField.addListener('blur', commitNode);
      }
    }
  }

  private function commitNode():void {
    var node:TaxonomyNode = TaxonomyUtil.getLatestSelection();
    var content:Content = SESSION.getConnection().getContentRepository().getContent(node.getRef());
    content.invalidate(function ():void {
      var defaultName:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.TaxonomyStudioPluginSettings', 'taxonomy_default_name');
      if(TaxonomyUtil.decodeHTML(node.getName()) === defaultName) {
        setBusy(false);
      }
      else if (TaxonomyUtil.escapeHTML(content.getName()) !== node.getName() || !content.isPublished()) {
        node.commitNode(function ():void {
          content.invalidate(function ():void {
            setBusy(false);
          });
        });
      }
      else {
        setBusy(false);
      }
    });
  }

  private function setBusy(busy:Boolean):void {
    //no visual update yet
  }


  private function onTabDestroy():void {
    if (nameField) {
      nameField.removeListener('blur', commitNode);
    }
  }
}
}
