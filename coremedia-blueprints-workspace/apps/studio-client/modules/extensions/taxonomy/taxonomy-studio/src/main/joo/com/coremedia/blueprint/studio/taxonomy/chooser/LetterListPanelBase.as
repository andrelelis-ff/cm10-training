package com.coremedia.blueprint.studio.taxonomy.chooser {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeList;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyUtil;
import com.coremedia.blueprint.studio.taxonomy.rendering.TaxonomyRenderFactory;
import com.coremedia.blueprint.studio.taxonomy.rendering.TaxonomyRenderer;
import com.coremedia.cap.content.Content;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.store.BeanRecord;
import com.coremedia.ui.util.EventUtil;

import ext.container.Container;
import ext.data.Model;
import ext.grid.GridPanel;

/**
 * Displays the active taxonomy node sorted alphabetically.
 */
public class LetterListPanelBase extends Container {
  public const ITEMS_CONTAINER_ITEM_ID:String = "itemsContainer";

  private var selectedValuesExpression:ValueExpression;
  private var listValuesExpression:ValueExpression;
  private var activeLetters:ValueExpression;
  private var selectedLetter:ValueExpression;
  private var selectionExpression:ValueExpression;

  private var selectedNodeId:ValueExpression;
  private var selectedNodeList:ValueExpression;

  private var taxonomyId:String;
  private var activeNodeList:TaxonomyNodeList;

  //used for skipping letter column rendering
  private var letter2NodeMap:Bean;

  private var singleSelection:Boolean;

  public function LetterListPanelBase(config:LetterListPanel = null) {
    super(config);
    singleSelection = config.singleSelection;
    activeLetters = config.activeLetters;
    taxonomyId = config.taxonomyId;
    activeLetters = config.activeLetters;

    selectionExpression = config.selectionExpression;
    selectionExpression.addChangeListener(updateAll);

    selectedNodeId = config.selectedNodeId;
    selectedNodeList = config.selectedNodeList;
    selectedNodeList.addChangeListener(updateUI);

    selectedLetter = config.selectedLetter;
    selectedLetter.addChangeListener(updateSelectedLetter);
  }


  override protected function afterRender():void {
    super.afterRender();
    selectedNodeId.setValue(taxonomyId); //lets start with the root level to show
  }

  protected function getSelectedValuesExpression():ValueExpression {
    if (!selectedValuesExpression) {
      selectedValuesExpression = ValueExpressionFactory.create("values", beanFactory.createLocalBean());
    }
    return selectedValuesExpression;
  }

  protected function getListValuesExpression():ValueExpression {
    if (!listValuesExpression) {
      listValuesExpression = ValueExpressionFactory.create("nodes", beanFactory.createLocalBean());
    }
    return listValuesExpression;
  }

  /**
   * Selects the entry in the list with the active letter
   */
  private function updateSelectedLetter():void {
    var letter:String = selectedLetter.getValue().toLowerCase();
    if (letter) {
      var itemsContainer:Container = queryById(ITEMS_CONTAINER_ITEM_ID) as Container;
      for(var i:int = 0; i<itemsContainer.itemCollection.length; i++) {
        var item:LetterListItemPanel = itemsContainer.itemCollection.getAt(i) as LetterListItemPanel;
        var itemLetter:String = letterRenderer(item.content).toLowerCase();
        if(itemLetter === letter) {
          var height:Number = item.getHeight();
          this.el.dom.scrollTop = height*i;
          break;
        }
      }
    }
  }

  /**
   * Refresh the path and list and button column.
   */
  private function updateUI():void {
    var list:TaxonomyNodeList = selectedNodeList.getValue();
    if (list) {
      activeNodeList = list;
      letter2NodeMap = beanFactory.createLocalBean();
      updateLetterList(list);
      convertNodeListToContentList();
    }
  }


  /**
   * Fills the letter value expression with an array of the active letters.
   * @param list
   */
  private function updateLetterList(list:TaxonomyNodeList):void {
    var letters:Array = [];
    var nodes:Array = list.getNodes();
    for (var i:int = 0; i < nodes.length; i++) {
      var name:String = nodes[i].getName();
      letters.push(name.substr(0, 1).toLowerCase());
    }
    activeLetters.setValue(letters);
  }


  /**
   * Fired when the user double clicks a row.
   * The next taxonomy child level of the selected node is entered then.
   */
  private function updateSelection(contentId:String):void {
    if (contentId) {
      var id:String = TaxonomyUtil.getRestIdFromCapId(contentId);
      if (!activeNodeList.getNode(id).isLeaf()) {
        //fire event for path update
        selectedNodeId.setValue(id);
      }
    }
  }

  private function convertNodeListToContentList():void {
    var contents:Bean = beanFactory.createLocalBean();
    var count:int = activeNodeList.getNodes().length;
    for (var i:int = 0; i < activeNodeList.getNodes().length; i++) {
      var item:TaxonomyNode = activeNodeList.getNodes()[i];
      var child:Content = beanFactory.getRemoteBean(item.getRef()) as Content;
      child.load(function (bean:Content):void {
        var id:String = TaxonomyUtil.parseRestId(bean);
        contents.set(id, bean);
        count--;
        if (count === 0) {
          sortAndApplyContentList(contents);
        }
      });
    }
  }

  /**
   * We do sort by the name of the node, not by the content!!!!!!
   * @param contents
   */
  private function sortAndApplyContentList(contents:Bean):void {
    var sortedContentArray:Array = [];
    var nodes:Array = activeNodeList.getNodes();
    for (var i:int = 0; i < nodes.length; i++) {
      var c:Content = contents.get(nodes[i].getRef());
      sortedContentArray.push(c);
    }
    getListValuesExpression().setValue(sortedContentArray);
  }


  //noinspection JSUnusedLocalSymbols
  /**
   * Displays each name of a taxonomy
   */
  public function taxonomyRenderer(content:Content):String {
    var list:TaxonomyNodeList = selectedNodeList.getValue();
    var node:TaxonomyNode = list.getNode(TaxonomyUtil.getRestIdFromCapId(content.getId()));

    var selected:Boolean = isInSelection(node.getRef());
    var selectionExists:Boolean = selectionExpression.getValue() && (selectionExpression.getValue() as Array).length === 1;

    var renderer:TaxonomyRenderer = null;
    if (singleSelection) {
      renderer = TaxonomyRenderFactory.createSingleSelectionListRenderer(node, getId(), selected, selectionExists);
    }
    else {
      renderer = TaxonomyRenderFactory.createSelectionListRenderer(node, getId(), selected);
    }

    renderer.doRender();
    var html:String = renderer.getHtml();
    return html;
  }

  //noinspection JSUnusedLocalSymbols
  /**
   * Displays each letter of a taxonomy
   */
  public function letterRenderer(content:Content):String {
    var list:TaxonomyNodeList = selectedNodeList.getValue();
    var node:TaxonomyNode = list.getNode(TaxonomyUtil.getRestIdFromCapId(content.getId()));
    var letter:String = node.getName().substr(0, 1).toUpperCase();

    var html:String = '';
    if (!letter2NodeMap.get(letter) || letter2NodeMap.get(letter).getRef() === node.getRef()) {
      html = letter;
      letter2NodeMap.set(letter, node);
    }

    return html;
  }

  /**
   * Handler executed when the node text is clicked on.
   */
  public function nodeClicked(contentId:String):void {
    updateSelection(contentId); //has the same behaviour like when double clicking a row.
  }

  /**
   * Handler executed when the plus button is clicked.
   * Used in TaxonomyRenderer#plusMinusClicked$static
   */
  public function plusMinusClicked(nodeRef:String):void {
    var alreadySelected:Boolean = !isInSelection(nodeRef);
    if (alreadySelected) {
      //add to cache so that after the reload of the current level, the node is marked as not addable.
      TaxonomyUtil.addNodeToSelection(selectionExpression, nodeRef);
    }
    else {
      TaxonomyUtil.removeNodeFromSelection(selectionExpression, nodeRef);
    }
  }

  /**
   * Utility method that checks if the given node is already part of the active selection list.
   * @param contentId
   */
  public function isInSelection(contentId:String):Boolean {
    var selection:Array = selectionExpression.getValue();
    if (selection) {
      for (var i:int = 0; i < selection.length; i++) {
        var selectedContent:Content = selection[i];
        var restId:String = TaxonomyUtil.parseRestId(selectedContent);
        if (restId === contentId) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Executes a commit on all records.
   */
  private function updateAll():void {
    var values:Array = getListValuesExpression().getValue();
    if(values) {
      getListValuesExpression().setValue([]);
      EventUtil.invokeLater(function():void {
        getListValuesExpression().setValue(values);
      });
    }
  }
}
}
