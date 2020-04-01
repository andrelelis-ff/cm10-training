package com.coremedia.blueprint.studio.taxonomy.action {
import com.coremedia.blueprint.studio.TaxonomyStudioPluginBase;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeFactory;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeList;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyUtil;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.ValueExpression;

import ext.Action;
import ext.Component;

import mx.resources.ResourceManager;

/**
 * Opens the taxonomy editor and shows the given taxonomy in the tree.
 */
[ResourceBundle('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin')]
public class EditTaxonomyActionBase extends Action {

  private var taxonomyId:String;
  private var bindTo:ValueExpression;
  internal native function get items():Array;

  public function EditTaxonomyActionBase(config:EditTaxonomyAction = null) {
    config.handler = showTaxonomy;
    config.text = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyLinkList_edit_action_text');
    super(config);
    taxonomyId = config.taxonomyId;
    bindTo = config.bindTo;
    bindTo.addChangeListener(updateDisabled);
  }

  override public function addComponent(comp:Component):void {
    super.addComponent(comp);
    updateDisabled();
  }

  /**
   * Update enabling/disabling selection depending.
   */
  private function updateDisabled():void {
    setDisabled(true);
    TaxonomyStudioPluginBase.isAdministrationEnabled(function(enabled:Boolean) {
      if(enabled && bindTo && bindTo.getValue() && (bindTo.getValue() as Array).length > 0) {
        setDisabled(false);
      }
    });
  }

  /**
   * 1. Open Editor
   * 2. Select node
   */
  public function showTaxonomy():void {
    var activeContent:Content = bindTo.getValue()[0];
    var restId:String = TaxonomyUtil.parseRestId(activeContent);
    var siteId:String = editorContext.getSitesService().getSiteIdFor(activeContent);
    TaxonomyNodeFactory.loadPath(taxonomyId, restId, siteId, function(nodeList:TaxonomyNodeList):void {
      var node:TaxonomyNode = nodeList.getNode(restId);
      TaxonomyUtil.setLatestSelection(node);
      OpenTaxonomyEditorActionBase.showTaxonomyAdministrationWithLatestSelection();
    });
  }

  override public function removeComponent(comp:Component):void {
    super.removeComponent(comp);
    if (items && items.length === 0) {
      bindTo && bindTo.removeChangeListener(updateDisabled);
    }
  }
}
}