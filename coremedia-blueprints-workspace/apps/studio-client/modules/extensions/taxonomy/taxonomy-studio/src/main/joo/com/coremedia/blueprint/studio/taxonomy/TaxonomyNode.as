package com.coremedia.blueprint.studio.taxonomy {
import com.coremedia.cap.content.Content;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.util.EncodingUtil;

import ext.Ext;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.blueprint.studio.TaxonomyStudioPluginSettings')]
public class TaxonomyNode {
  public static const PROPERTY_PATH:String = "path";
  public static const PROPERTY_HTML:String = "html";//property used to store HTML of search combo
  public static const PROPERTY_NAME:String = "name";
  public static const PROPERTY_REF:String = "ref";
  public static const PROPERTY_TAXONOMY_ID:String = "taxonomyId";

  private var json:Object = {};


  public function TaxonomyNode(object:Object) {
    json = object;
  }

  public function toJson():Object {
    return json;
  }

  public static function forValues(name:String, type:String, ref:String, siteId:String, level:int, root:Boolean, leaf:Boolean, taxonomyId:String, selectable:Boolean, extendable:Boolean):TaxonomyNode {
    var json:* = {
      name:name,
      siteId:siteId,
      type:type,
      ref:ref,
      level:level,
      root:root,
      leaf:leaf,
      taxonomyId:taxonomyId,
      selectable:selectable,
      extendable:extendable
    };
    return new TaxonomyNode(json);
  }

  public function getSite():String {
    return json.siteId;
  }

  /**
   * Every taxonomy node has a name, which is shown in the taxonomy chooser and editor.
   * The names may be localized by client side resource bundles (for the root nodes).
   * @return
   */
  public function getName():String {
    return TaxonomyUtil.escapeHTML(json.name);
  }

  /**
   * Returns the unescaped name.
   * @return
   */
  public function getRawName():String {
    return json.name;
  }

  public function setName(name:String):void {
    json.name = name;
  }

  /**
   * A taxonomy node may reference entities like documents (content id) in the content repository or entries
   * in another database.
   * For the Studio UI this reference is not of interest, but the TaxonomyStrategies rely on it.
   * @return
   */
  public function getRef():String {
    return json.ref;
  }

  public function setRef(ref:String):void {
    json.ref = ref;
  }

  /**
   * A taxonomy node might represent an object of a specific 'type' like: 'Country', 'State', 'City', 'Street'. These
   * types might be rendered differently in the frontend.
   * @return
   */
  public function getType():String {
    return json.type;
  }

  public function setType(type:String):void {
    json.type = type;
  }

  /**
   * A taxonomy node might be selectable (or choosable) in in the taxonomy chooser.
   * @return
   */
  public function isSelectable():Boolean {
    return json.selectable;
  }

  public function setSelectable(selectable:Boolean):void {
    json.selectable = selectable;
  }

  /**
   * this flag indicates, that a node has child nodes.
   * @return
   */
  public function isLeaf():Boolean {
    return json.leaf;
  }

  public function setLeaf(leaf:Boolean):void {
    json.leaf = leaf;
  }

  /**
   * this flag indicates, that the taxonomy editor may add children to this node. If false this node is leaf-only.
   * @return
   */
  public function isExtendable():Boolean {
    return json.extendable;
  }

  public function setExtendable(extendable:Boolean):void {
    json.extendable = extendable;
  }

  /**
   * Indicates that this node represents the root of a taxonomy tree, not a taxonomy node in this taxonomy.
   * In most cases - but not necessarily - root nodes do not represent entities and are not selectable.
   * @return
   */
  public function isRoot():Boolean {
    return json.root;
  }

  public function setRoot(root:Boolean):void {
    json.root = root;
  }

  /** this property is used to find the TaxonomyStrategy for a given node. **/
  public function getTaxonomyId():String {
    return EncodingUtil.decodeFromHTML(json.taxonomyId).split(",")[0].trim();
  }

  public function setTaxonomyId(taxonomy:String):void {
    json.taxonomyId = taxonomy;
  }

  public function getLevel():int {
    return json.level;
  }

  public function setLevel(level:int):void {
    json.level = level;
  }


  /**
   * If the taxonomy node is content, the remote bean of
   * it is invalidated here, e.g. when a node is deleted.
   * @param callback
   */
  public function invalidate(callback:Function):void {
    var thisNode:TaxonomyNode = this;
    var taxContent:Content = beanFactory.getRemoteBean(getRef()) as Content;
    if (taxContent) {
      taxContent.invalidate(function () {
        reloadNode(function (reloaded:TaxonomyNode):void {
          thisNode.json = reloaded.json;
          callback.call(null);
        });
      });
    }
    else {
      callback.call(null);
    }
  }

  public function getPath():TaxonomyNodeList {
    if (json.path) {
      return new TaxonomyNodeList(json.path.nodes);
    }
    return null;
  }

  public function getPathString():String {
    return json.pathString;
  }

  public function getDisplayName():String {
    var name:String = getRawName();
    if(getWeight()) {
      name = name + " (" + getWeight() + ")";
    }
    return TaxonomyUtil.escapeHTML(name);
  }

  /**
   * Triggers a reload of the given node, invokes the callback
   * function with the reloaded node.
   * @param callback
   */
  public function reloadNode(callback:Function):void {
    var url:String = "taxonomies/node?" + toNodeQuery();
    executeNodeOperation(url, callback);
  }

  /**
   * Commits the changes executed on the active node.
   * The commit is triggered once another node is selected.
   * @param callback
   */
  public function commitNode(callback:Function = null):void {
    var url:String = "taxonomies/commit?" + toNodeQuery();
    executeNodeOperation(url, callback);
  }

  /**
   * Loads the parent of this node.
   * @param callback
   */
  public function loadParent(callback:Function):void {
    var url:String = "taxonomies/parent?" + toNodeQuery();
    executeNodeOperation(url, callback);
  }

  /**
   * Creates a new taxonomy node, using the given parent for type and parent.
   * @param callback
   */
  public function createChild(callback:Function):void {
    var url:String = "taxonomies/createChild?" + toNodeQuery() + "&" + Ext.urlEncode({defaultName:ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.TaxonomyStudioPluginSettings', 'taxonomy_default_name')});
    executeNodeOperation(url, callback);
  }

  /**
   * Calls the given REST method which always return a taxonomy node as result.
   * @param url
   * @param callback
   */
  private function executeNodeOperation(url:String, callback:Function):void {
    var remote:RemoteBean = beanFactory.getRemoteBean(url);
    remote.invalidate(function ():void {
      if (remote.getState().readable) {
        var obj:Object = remote.toObject();
        var node:TaxonomyNode = new TaxonomyNode(obj);
        callback(node);
      }
      else {
        callback(null);
      }
    });
  }


  /**
   * Callback returns a node list with all child nodes of this node.
   * @param refresh true to force reload
   * @param callback
   */
  public function loadChildren(refresh:Boolean, callback:Function):void {
    var url:String = "taxonomies/children?" + toNodeQuery();
    TaxonomyNodeFactory.loadRemoteTaxonomyNodeList(url, refresh, callback);
  }

  /**
   * Creates the REST uri for the node actions.
   * @return
   */
  private function toNodeQuery():String {
    var query:String = Ext.urlEncode({taxonomyId:getTaxonomyId(), nodeRef:getRef()});
    if (getSite()) {
      query = Ext.urlEncode({taxonomyId:getTaxonomyId(), nodeRef:getRef(), site:getSite()});
    }
    return query;
  }

  public function getWeight():String {
    if (json.weight !== -1.0) {
      return json.weight;
    }
    return undefined;
  }
}
}
