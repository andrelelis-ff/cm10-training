package com.coremedia.blueprint.studio.taxonomy.action {

import com.coremedia.blueprint.studio.TaxonomyStudioPlugin;
import com.coremedia.blueprint.studio.TaxonomyStudioPluginBase;
import com.coremedia.blueprint.studio.taxonomy.administration.TaxonomyEditor;
import com.coremedia.blueprint.studio.taxonomy.administration.TaxonomyEditor;
import com.coremedia.cms.editor.sdk.desktop.WorkArea;
import com.coremedia.cms.editor.sdk.desktop.WorkAreaTabType;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.util.EventUtil;

import ext.Action;
import ext.Component;
import ext.Ext;
import ext.button.Button;
import ext.panel.Panel;

import mx.resources.ResourceManager;

/** Opens the TaxonomyEditor **/
[ResourceBundle('com.coremedia.cms.editor.Editor')]
public class OpenTaxonomyEditorActionBase extends Action {
  private var taxonomyId:String;

  internal native function get items():Array;

  public function OpenTaxonomyEditorActionBase(config:OpenTaxonomyEditorAction = null) {
    config.handler = showTaxonomyAdministrationWithLatestSelection;
    super(config);
    taxonomyId = config.taxonomyId;
  }

  override public function addComponent(comp:Component):void {
    super.addComponent(comp);
    TaxonomyStudioPluginBase.isAdministrationEnabled(function(enabled:Boolean):void {
      setDisabled(!enabled);
      updateTooltip(enabled);
    });

    /**
     * Add site selection listener and destroy the editor if the site has
     * changed and the taxonomy manager is still open.
     */
    editorContext.getSitesService().getPreferredSiteIdExpression().addChangeListener(preferredSiteChangedHandler);
  }

  private function preferredSiteChangedHandler():void {
    TaxonomyStudioPluginBase.isAdministrationEnabled(function(enabled:Boolean):void {
      setDisabled(!enabled); //also update the action on site change
      updateTooltip(enabled);
      if(!enabled) {
        var editor:TaxonomyEditor = Ext.getCmp('taxonomyEditor') as TaxonomyEditor;
        if(editor) {
          editor.destroy();
        }
      }
    });
  }

  private static function updateTooltip(enabled:Boolean):void {
    var msg:String = ResourceManager.getInstance().getString('com.coremedia.cms.editor.Editor', 'Button_disabled_insufficient_privileges');
    if(enabled) {
      msg = '';
    }
    var button:Button = (Ext.getCmp(TaxonomyStudioPlugin.TAXONOMY_EDITOR_BUTTON_ID) as Button);
    if(button) {
      button.setTooltip(msg);
    }
  }

  public static function showTaxonomyAdministrationWithLatestSelection():void {
    openTaxonomyAdministration();

    EventUtil.invokeLater(function ():void {
      var taxonomyAdminTab:TaxonomyEditor = Ext.getCmp('taxonomyEditor') as TaxonomyEditor;
      taxonomyAdminTab.showNodeSelectedNode();
    });
  }

  /**
   * Static call to open the taxonomy admin console.
   */
  private static function openTaxonomyAdministration():void {
    var workArea:WorkArea = editorContext.getWorkArea() as WorkArea;
    var taxonomyAdminTab:TaxonomyEditor = Ext.getCmp('taxonomyEditor') as TaxonomyEditor;

    if (!taxonomyAdminTab) {
      var workAreaTabType:WorkAreaTabType = workArea.getTabTypeById(TaxonomyEditor.xtype);
      workAreaTabType.createTab(null, function(tab:Panel):void {
        var editor:TaxonomyEditor = tab as TaxonomyEditor;
        workArea.addTab(workAreaTabType, editor);
        workArea.setActiveTab(editor);
      });
    }
    else {
      workArea.setActiveTab(taxonomyAdminTab);
    }
  }

  override public function removeComponent(comp:Component):void {
    super.removeComponent(comp);
    if (items && items.length === 0) {
      editorContext.getSitesService().getPreferredSiteIdExpression().removeChangeListener(preferredSiteChangedHandler);
    }
  }
}
}
