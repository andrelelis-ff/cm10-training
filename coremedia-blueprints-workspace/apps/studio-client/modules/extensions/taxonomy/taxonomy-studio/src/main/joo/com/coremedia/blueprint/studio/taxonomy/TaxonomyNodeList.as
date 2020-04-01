package com.coremedia.blueprint.studio.taxonomy {
/**
 * JSON representation fora list of nodes.
 * The list can be a search result or a list of nodes that represent the path of a node.
 */
public class TaxonomyNodeList {

  private var nodes:Array;
  private var json:Array;

  public function TaxonomyNodeList(object:Array) {
    nodes = [];
    for (var i:int = 0; i < object.length; i++) {
      var node:TaxonomyNode = new TaxonomyNode(object[i]);
      nodes.push(node);
    }
    json = object;
  }

  public function getPath():String {
    return json['path'];
  }

  public function toJson():Array {
    return json;
  }

  public function size():int {
    return nodes.length;
  }

  public function setNodes(nodesArray:Array):void {
    nodes = nodesArray;
  }

  public function getNode(ref:String):TaxonomyNode {
    for (var i:int = 0; i < nodes.length; i++) {
      if (nodes[i].getRef() === ref) {
        return nodes[i];
      }
    }
    return null;
  }

  public function getNodeForDisplayName(name:String):TaxonomyNode {
    for (var i:int = 0; i < nodes.length; i++) {
      var hit:TaxonomyNode = nodes[i];
      if (hit.getDisplayName() === TaxonomyUtil.escapeHTML(name)) {
        return hit;
      }
    }
    return null;
  }

  public function getNodes():Array {
    return nodes;
  }

  public function getLeafRef():String {
    return nodes[nodes.length - 1].getRef();
  }

  public function getLeafParentRef():String {
    return nodes[nodes.length - 2].getRef();
  }
}
}