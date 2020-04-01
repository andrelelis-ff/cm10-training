package com.coremedia.blueprint.studio.taxonomy.administration {

import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeFactory;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeList;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyUtil;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;

import ext.panel.Panel;

/**
 * Base class of the taxonomy administration tab.
 */
public class TaxonomyEditorBase extends Panel {

  private var siteSelectionExpression:ValueExpression;
  private var searchResultExpression:ValueExpression;

  public function TaxonomyEditorBase(config:TaxonomyEditor = null) {
    super(config);
  }

  /**
   * The value expression contains the active selected site.
   * @return
   */
  protected function getSiteSelectionExpression():ValueExpression {
    if (!siteSelectionExpression) {
      siteSelectionExpression = ValueExpressionFactory.create('site', beanFactory.createLocalBean());
    }
    return siteSelectionExpression;
  }

  /**
   * The value expression contains the active search result.
   * @return
   */
  protected function getSearchResultExpression():ValueExpression {
    if (!searchResultExpression) {
      searchResultExpression = ValueExpressionFactory.create('search', beanFactory.createLocalBean());
    }
    return searchResultExpression;
  }

  /**
   * Displays the path of the given node.
   * @param node
   */
  public function showNodeSelectedNode():void {
    var node:TaxonomyNode = TaxonomyUtil.getLatestSelection();
    //show last selected node.
    if (node && !node.isRoot()) { //do not select root node, path retrieving will fail.
      TaxonomyNodeFactory.loadPath(node.getTaxonomyId(), node.getRef(), node.getSite(),
              function (nodeList:TaxonomyNodeList):void {
                selectNode(nodeList);
              });
    }
  }

  /**
   * Selects the given node in the tree.
   * @param nodeList
   */
  public function selectNode(nodeList:TaxonomyNodeList):void {
    searchResultExpression.setValue(nodeList);
  }
}
}
