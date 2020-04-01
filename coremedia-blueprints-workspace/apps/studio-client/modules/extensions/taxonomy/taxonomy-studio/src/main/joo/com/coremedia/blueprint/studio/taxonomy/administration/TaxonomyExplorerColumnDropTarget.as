package com.coremedia.blueprint.studio.taxonomy.administration {

import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;

import ext.Ext;
import ext.data.Model;
import ext.dd.DragDropManager;
import ext.dd.DragSource;
import ext.dd.DropTarget;
import ext.event.Event;
import ext.selection.RowSelectionModel;

public class TaxonomyExplorerColumnDropTarget extends DropTarget {
  private var column:TaxonomyExplorerColumnBase;
  private var expander:ColumnExpander;

  public function TaxonomyExplorerColumnDropTarget(component:TaxonomyExplorerColumnBase) {
    super(component.getEl(), DropTarget({
      ddGroup:"taxonomies"
    }));
    this.column = component;
  }

  public function notifyOnNodeOver(nodeData:Object, source:DragSource, e:Event, data:Object):String {
    return notifyOver(source, e, data);
  }

  public function notifyOnNodeDrop(nodeData:Object, source:DragSource, e:Event, data:Object):Boolean {
    return notifyDrop(source, e, data);
  }

  public function notifyOnContainerOver(source:DragSource, e:Event, data:Object):String {
    return notifyOver(source, e, data)
  }

  override public function notifyEnter(source:DragSource, e:Event, data:Object):String {
    return notifyOver(source, e, data);
  }

  override public function notifyOver(source:DragSource, e:Event, data:Object):String {
    var sourceNodes:Array = getSourceNodes(data);
    var targetNode:TaxonomyNode = isWriteable(data, e);
    if(!targetNode) {
      if(expander) {
        expander.cancel();
        expander = null;
      }
      return dropNotAllowed;
    }

    //display the new columns if a node (not leaf) is hovered long enough
    var sourceNode:TaxonomyNode = sourceNodes[0];
    if(targetNode.getLevel() >= sourceNode.getLevel() && !isAlreadyExpanded(targetNode)) { //=>we can not destroy our drag source, so we can only expand child columns
      expand(targetNode);
    }
    else if(expander) {
      expander.cancel();
    }

    return dropAllowed;
  }

  /**
   * Checks if the current column is already expanded, which means
   * it is already resolved as parent.
   * @param targetNode
   * @return
   */
  private function isAlreadyExpanded(targetNode:TaxonomyNode):Boolean {
    return targetNode.getRef() == column.getParentNode().getRef();
  }

  /**
   * Expand the node if is not a
   * @param activeTargetNode
   */
  private function expand(targetNode:TaxonomyNode):void {
    if(!expander) {
      expander = new ColumnExpander(targetNode);
      expander.expand();
      return;
    }

    //check existing expandler if the hovered not is already expanding.
    if(!expander.expands(targetNode)) {
      expander.cancel();
      expander = new ColumnExpander(targetNode);
      expander.expand();
    }
  }

  override public function notifyOut(source:DragSource, e:Event, data:Object):void {
    //nothing
  }

  override public function notifyDrop(source:DragSource, e:Event, data:Object):Boolean {
    var sourceNodes:Array = getSourceNodes(data);
    var targetNode:TaxonomyNode = isWriteable(data, e);
    if(!targetNode) {
      return false;
    }

    var taxonomyExplorer:TaxonomyExplorerPanel = Ext.getCmp('taxonomyExplorerPanel') as TaxonomyExplorerPanel;
    taxonomyExplorer.moveNodes(sourceNodes, targetNode);
    return true;
  }

  /**
   *
   * @param data
   * @return
   */
  private static function getSourceNodes(data:Object):Array {
    var result:Array = [];
    var records:Array = data.records;
    for each(var record:Object in records) {
      var json:* = record.data;
      var sourceNode:TaxonomyNode = new TaxonomyNode(json);
      result.push(sourceNode);
    }
    return result;
  }

  /**
   * Checks if a drop can be performed.
   * @param data
   * @param e
   * @return
   */
  private function isWriteable(data:Object, e:Event):TaxonomyNode {
    var sourceNodes:Array = getSourceNodes(data);

    //check if the mouse if over a region with records
    var target:* = e.getTarget();
    var rowIndex:Number = column.getView().indexOf(target);
    if(!column.getStore().getAt(rowIndex)) { //we drop an a column, not on a specific node
      //no drop on the root column, only on root nodes!
      if(column.getItemId() === 'taxonomyRootsColumn') {
        return null;
      }

      if(!data.view.grid) {
        return null;
      }

      //check if the dragged node is hovering over a column that is a child of it
      //this could be enabled but an additional check is missing then: if the new parent is the dragged node itself!
      if(parseInt(column.getItemId().split("-")[1]) > parseInt(data.view.grid.getItemId().split("-")[1])) {
        return null;
      }

      if(column.getItemId() !== data.view.grid.getItemId()) {
        var parentNode:TaxonomyNode = column.getParentNode();
        return parentNode;
      }
      return null;
    }
    var targetJson:* = column.getStore().getAt(rowIndex).data;
    var targetNode:TaxonomyNode = new TaxonomyNode(targetJson);


    for each(var s1:TaxonomyNode in sourceNodes) {
      //check if the mouse is still inside the dragged record
      if (s1.getRef() === targetNode.getRef()) {
        return null;
      }

      //check if we are still inside the same taxonomy tree
      if (s1.getTaxonomyId() !== targetNode.getTaxonomyId()) {
        return null;
      }

      //check if the dragged node is a parent of the entered node
      if(targetNode.getLevel() > s1.getLevel()) {
        return null;
      }
    }


    //check if the mouse is on the immediate parent, so dropping makes no sense (and also leads to errors)
    //We using the fact here that the parent must be the selected node of the corresponding column since
    //we can not determine the parent synchronously.
    var taxonomyExplorer:TaxonomyExplorerPanel = Ext.getCmp('taxonomyExplorerPanel') as TaxonomyExplorerPanel;
    var targetColumn:TaxonomyExplorerColumn = taxonomyExplorer.getColumnContainer(targetNode);
    var selected:Model = (targetColumn.getSelectionModel() as RowSelectionModel).getSelection()[0];

    for each(var s4:TaxonomyNode in sourceNodes) {
      if(targetNode.getLevel() === s4.getLevel()-1 && selected.data.ref === targetNode.getRef()) { //direct parent and selected check
        return null;
      }
    }
    DragDropManager.refreshCache({taxonomies:true}); //new drop zones are not registered during a drag!!!!!
    return targetNode;
  }
}
}

