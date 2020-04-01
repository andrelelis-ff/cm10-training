package com.coremedia.blueprint.studio.taxonomy.selection {

import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeList;
import com.coremedia.blueprint.studio.taxonomy.rendering.TaxonomyRenderFactory;
import com.coremedia.blueprint.studio.taxonomy.rendering.TaxonomyRenderer;
import com.coremedia.blueprint.studio.taxonomy.rendering.TaxonomySearchComboRenderer;
import com.coremedia.cap.content.Content;
import com.coremedia.ui.components.StatefulComboBox;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.impl.RemoteService;
import com.coremedia.ui.logging.Logger;
import com.coremedia.ui.util.AbortingAjaxProxy;

import ext.Ext;
import ext.XTemplate;
import ext.data.JsonStore;
import ext.data.Model;
import ext.data.proxy.AjaxProxy;
import ext.data.reader.JsonReader;
import ext.tip.QuickTipManager;

import js.XMLHttpRequest;

[ResourceBundle('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin')]
public class TaxonomySearchFieldBase extends StatefulComboBox {

  /**
   * Name of the property of search suggestions result containing the search hits.
   * @eventType hits
   */
  private static const NODES:String = "nodes";

  /**
   * Name of the property of search suggestions result item containing the number of appearances of the suggested value.
   */
  protected static const SUGGESTION_COUNT:String = "size";

  internal static var autoSuggestResultTpl:XTemplate = new XTemplate(
          '<tpl for="."><div style="width:' + TaxonomySearchComboRenderer.LIST_WIDTH + 'px;padding: 2px 0px; ">{' + 'html' + '}</div></tpl>'
  );

  private var searchResultExpression:ValueExpression;

  private var showSelectionPath:Boolean;
  private var taxonomyId:String;

  // It is assumed that cachedValue always corresponds to a valid tag.
  // Consequently its always originates from onNodeSelection().
  private var cachedValue:*;

  private var siteSelectionExpression:ValueExpression;
  private var resetOnBlur:Boolean;

  private var valueManuallyChanged:Boolean = false;

  private var httpProxy:AjaxProxy;

  public function TaxonomySearchFieldBase(config:TaxonomySearchField = null) {
    taxonomyId = config.taxonomyId;
    siteSelectionExpression = config.siteSelectionExpression;
    if (siteSelectionExpression) {
      siteSelectionExpression.addChangeListener(siteSelectionChanged);
    }
    searchResultExpression = config.searchResultExpression;
    showSelectionPath = config.showSelectionPath;

    if (showSelectionPath === undefined) {
      showSelectionPath = true;
    }

    this.resetOnBlur = config.resetOnBlur;

    if (taxonomyId === undefined) {
      taxonomyId = "";
    }

    var superConfig:TaxonomySearchField = TaxonomySearchField({});
    super(TaxonomySearchField(Ext.apply(superConfig, config)));


    getStore().addListener('datachanged', validate);
    addListener("afterrender", validate);
    addListener('focus', doFocus);
    addListener('select', onNodeSelection);
    addListener('keydown', function ():void {
      valueManuallyChanged = true;
      QuickTipManager.getQuickTip().hide();
    });
  }

  internal function getSearchSuggestionsDataProxy(taxId:String, siteSelectionExpression:ValueExpression):AjaxProxy {
    if(!httpProxy) {
      this.siteSelectionExpression = siteSelectionExpression;
      var reader:JsonReader = JsonReader({});
      reader.rootProperty = NODES;

      //noinspection JSUnusedGlobalSymbols
      httpProxy = Ext.create(AbortingAjaxProxy, {
        failure: function (response:XMLHttpRequest):void {
          Logger.info('Taxonomy search request failed:' + response.responseText);
        },
        method: "GET",
        url: RemoteService.calculateRequestURI('taxonomies/find?' + getTaxonomyIdParam(taxId) + getSiteParam()),
        reader: reader
      });
    }

    return httpProxy;
  }

  private function siteSelectionChanged():void {
    reset();
    ((getStore() as JsonStore).proxy as AjaxProxy).setUrl(RemoteService.calculateRequestURI('taxonomies/find?' + getTaxonomyIdParam(taxonomyId) + getSiteParam()));
  }

  //noinspection JSUnusedLocalSymbols
  /**
   * Creates the HTML that is displayed for the search hits.
   */
  protected static function renderHTML(component:*, record:Model):String {
    if(record.data.path) {
      var nodes:Array = record.data.path.nodes;
      var renderer:TaxonomyRenderer = TaxonomyRenderFactory.createSearchComboRenderer(nodes, record.data.ref);
      renderer.doRender();
      var html:String = renderer.getHtml();
      return html;
    }
    return null;
  }


  private function isValidTagPrefix():Boolean {
    return (!getValue() || getStore().getCount() > 0);
  }

  protected function tagPrefixValidValidator():* {
    if (!valueManuallyChanged
            || getValue() === cachedValue
            || (minChars && getValue() && (getValue() as String).length < minChars)
            || isValidTagPrefix()) {
      return true;
    } else {
      return resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomySearch_no_hit');
    }
  }

  /**
   * The on focus event handler for the textfield/combo, resets the status of the field.
   */
  public function doFocus():void {
    this.lastQuery = undefined;
    if(!resetOnBlur) {
      (getStore() as JsonStore).load({});
      if (getValue()) {
        cachedValue = getValue();
      }
    } else {
      setValue("");
    }
  }

  /**
   * Appends the taxonomy id param to the search query if set.
   * @return
   */
  private static function getTaxonomyIdParam(taxId:String):String {
    if (taxId) {
      return 'taxonomyId=' + taxId;
    }
    return '';
  }

  /**
   * Returns the site param if there is a site selected.
   * @return
   */
  private function getSiteParam():String {
    if (siteSelectionExpression && siteSelectionExpression.getValue()) {
      return '&site=' + siteSelectionExpression.getValue();
    }
    return '';
  }

  /**
   * Sets the selected path as string of resets the textfield after selection.
   * @param selection
   */
  private function setSelectionString(selection:*):void {
    if (showSelectionPath) {
      setValue(selection);
    }
    else {
      setValue("");
    }
  }

  //not static
  //noinspection JSMethodCanBeStatic
  public function getEmptyTextText(ve:ValueExpression):String {
    if (ve) {
      return resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomySearch_empty_linklist_text');
    }
    return resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomySearch_empty_search_text');
  }

  //noinspection JSUnusedLocalSymbols
  /**
   * Handler function for node selection.
   */
  private function onNodeSelection(combo:TaxonomySearchField, record:Model, index:Number):void {
    // Harden for possibly malformed 'select' events.
    if (!record || !record.data || !record.data[TaxonomyNode.PROPERTY_REF]) {
      setValue("");
      return;
    }
    var content:Content = beanFactory.getRemoteBean(record.data[TaxonomyNode.PROPERTY_REF]) as Content;
    content.load(function (c:Content):void {
      setSelectionString(record.data.name);
      cachedValue = record.data.name;
      var path:TaxonomyNodeList = new TaxonomyNodeList(record.data.path.nodes);
      searchResultExpression.setValue(path);
      setValue("");
    });
  }
}
}
