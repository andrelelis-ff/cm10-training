package com.coremedia.blueprint.studio.taxonomy.filter {

import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeList;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyUtil;
import com.coremedia.blueprint.studio.taxonomy.rendering.TaxonomyRenderFactory;
import com.coremedia.blueprint.studio.taxonomy.rendering.TaxonomyRenderer;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.undoc.content.ContentUtil;
import com.coremedia.cms.editor.sdk.collectionview.search.FilterPanel;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.store.BeanRecord;
import com.coremedia.ui.util.createComponentSelector;

import ext.EventManager;

import ext.StringUtil;
import ext.grid.GridPanel;

import js.KeyEvent;

/**
 * The non-UI part of a filter for the collection view that allows to select
 * the taxonomies of documents to be included in the search result.
 */
[ResourceBundle('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin')]
public class TaxonomyFilterPanelBase extends FilterPanel {

  public static const TAXONOMY_NODE_GRID_ITEM_ID:String = "taxonomyFilterSelection";

  /**
   *  The filter ID for this filter. It is used as identifier in saved searches.
   */
  public var filterId:String;

  /**
   * The filter property storing the keyword.
   */
  public static const TAXONOMIES_PROPERTY:String = "taxonomies";

  /**
   *  The taxonomy id to use for the selection.
   */
  [Bindable]
  public var taxonomyId:String;

  /**
   * The name of the SOLR field to apply the search for.
   */
  [Bindable]
  public var propertyName:String;

  /**
   * Contains the active selection.
   */
  private var selectionExpression:ValueExpression;

  /**
   * Contains the search result.
   */
  private var searchResultExpression:ValueExpression;

  /**
   * Create a new filter panel.
   *
   * @param config the configuration
   */
  public function TaxonomyFilterPanelBase(config:TaxonomyFilterPanelBase = null) {
    super(config);

    // Update the UI once and after state changes.
    getStateBean().addValueChangeListener(stateBeanChanged);
    stateBeanChanged();
  }

  override protected function afterRender():void {
    super.afterRender();
    addListener('afterlayout', addKeyListener);
  }

  private function addKeyListener():void {
    removeListener('afterlayout', addKeyListener);
    var grid:GridPanel = query(createComponentSelector().itemId(TAXONOMY_NODE_GRID_ITEM_ID).build())[0] as GridPanel;
    EventManager.on(grid.getEl(), 'keyup', function (evt:KeyEvent, t:*, o:*):void {
      if (evt.keyCode === KeyEvent.DOM_VK_DELETE ||
              evt.keyCode === KeyEvent.DOM_VK_ENTER ||
              evt.keyCode === KeyEvent.DOM_VK_RETURN ||
              evt.keyCode === KeyEvent.DOM_VK_SPACE) {
        var values:Array = getSelectionExpression().getValue();
        for each(var selection:Content in values) {
          var ref:String = TaxonomyUtil.parseRestId(selection);
          plusMinusClicked(ref);
        }
      }
    });
  }

  /**
   * The model has changed. Update the UI.
   */
  private function stateBeanChanged():void {
    var stateBean:Bean = getStateBean();
    var selection:Array = stateBean.get(TAXONOMIES_PROPERTY) || [];

    var currentTaxonomies:Array = [];
    for (var i:uint = 0; i < selection.length; i++) {
      var content:Content = selection[i];
        currentTaxonomies.push(content);
      }
    getSelectionExpression().setValue(currentTaxonomies);
  }

  /**
   * Called when the user has made a selection.
   */
  private function selectionChanged():void {
    var selection:Array = getSelectionExpression().getValue();
    getStateBean().set(TAXONOMIES_PROPERTY, selection);
  }

  /**
   * Returns the value expression that contains the active selection.
   * @return
   */
  protected function getSelectionExpression():ValueExpression {
    if(!selectionExpression) {
      selectionExpression = ValueExpressionFactory.create('selection', beanFactory.createLocalBean());
      selectionExpression.addChangeListener(selectionChanged);
    }
    return selectionExpression;
  }

  /**
   * Returns the value expression that contains the current search result.
   * @return
   */
  protected function getSearchResultExpression():ValueExpression {
    if(!searchResultExpression) {
      searchResultExpression = ValueExpressionFactory.create('search', beanFactory.createLocalBean());
      searchResultExpression.addChangeListener(function():void {
        var selection:TaxonomyNodeList = searchResultExpression.getValue() as TaxonomyNodeList;
        if (selection) {
          var leafRef:String = selection.getLeafRef();
          var keyword:Content = ContentUtil.getContent(leafRef);
          keyword.load(function ():void {
            var values:Array = selectionExpression.getValue();
            if(values.indexOf(keyword) === -1) {
              values = values.concat(keyword);
            }
            selectionExpression.setValue(values);
          });
        }
      });
    }
    return searchResultExpression;
  }

  /**
   * Removes the given taxonomy. Invoked from the rendered selection.
   */
  public function plusMinusClicked(nodeRef:String):void {
    TaxonomyUtil.removeNodeFromSelection(selectionExpression, nodeRef);
  }

  /**
   * Displays each path item of a taxonomy
   * @param value
   * @param metaData
   * @param record
   * @return
   */
  protected function taxonomyRenderer(value:*, metaData:*, record:BeanRecord):String {
    TaxonomyUtil.loadTaxonomyPath(record, null, taxonomyId, function (updatedRecord:BeanRecord):void {
      var renderer:TaxonomyRenderer = TaxonomyRenderFactory.createSelectedListWithoutPathRenderer(record.data.nodes, getId(), false);
      renderer.doRender(function (html:String):void {
        if (record.data.html !== html) {
          record.data.html = html;
          record.commit(false);
        }
      })
    });
    if (!record.data.html) {
      return "<div class='loading'>" + resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyLinkList_status_loading_text') + "</div>";
    }
    return record.data.html;
  }

  /**
   * @inheritDoc
   */
  override public function buildQuery():String {
    var stateBean:Bean = getStateBean();
    var keywords:Array = stateBean.get(TAXONOMIES_PROPERTY) || [];
    if (keywords.length === 0) {
      // The entire filter can be omitted.
      return null;
    } else {
      var queryTerms:Array = [];
      for (var i:uint = 0; i < keywords.length; i++) {
        var keyword:Content = keywords[i];
        var param:int = IdHelper.parseContentId(keyword);
        queryTerms.push(StringUtil.format(propertyName.toLowerCase()+ ":{0}", param));
      }
      return queryTerms.join(" OR ");
    }
  }

  /**
   * @inheritDoc
   */
  override public function getDefaultState():Object {
    var state:Object = {};
    state[TAXONOMIES_PROPERTY] = [];
    return state;
  }

  /**
   * @inheritDoc
   */
  override public function getFilterId():String {
    if(!filterId) {
      throw new Error("filterId has not been set for taxonomy filter");
    }
    return filterId;
  }
}
}
