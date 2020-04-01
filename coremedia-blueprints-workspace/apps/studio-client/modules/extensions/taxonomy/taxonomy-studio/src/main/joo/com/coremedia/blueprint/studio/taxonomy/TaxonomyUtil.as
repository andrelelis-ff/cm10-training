package com.coremedia.blueprint.studio.taxonomy {

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentProperties;
import com.coremedia.cap.content.ContentPropertyNames;
import com.coremedia.cms.editor.sdk.desktop.EditorMainView;
import com.coremedia.cms.editor.sdk.desktop.WorkArea;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.cms.editor.sdk.util.StudioConfigurationUtil;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.store.BeanRecord;
import com.coremedia.ui.util.EventUtil;

import ext.Ext;
import ext.StringUtil;

import mx.resources.ResourceManager;

/**
 * Common utility methods for taxonomies.
 */
public class TaxonomyUtil {
  private static const TAXONOMY_SETTINGS:String = "TaxonomySettings";

  private static var latestAdminSelection:TaxonomyNode;


  public static function getLatestSelection():TaxonomyNode {
    return latestAdminSelection;
  }

  public static function setLatestSelection(node:TaxonomyNode):void {
    latestAdminSelection = node;
  }

  public static function decodeHTML(xml:String):String {
    while (xml.indexOf('&nbsp;') !== -1) {
      xml = xml.replace('&nbsp;', ' ');
    }
    return xml;
  }


  public static function escapeHTML(xml:String):String {
    xml = xml.replace(/&/g, "&amp;");

    while (xml.indexOf('>') !== -1) {
      xml = xml.replace('>', '&gt;');
    }
    while (xml.indexOf('<') !== -1) {
      xml = xml.replace('<', '&lt;');
    }
    while (xml.indexOf(' ') !== -1) {
      xml = xml.replace(' ', '&nbsp;');
    }
    return xml;
  }

  public static function getTaxonomyName(taxonomy:Content):String {
    var properties:ContentProperties = taxonomy.getProperties();
    if (properties) {
      var value:String = properties.get('value') as String;
      if (value && value.length > 0) {
        return value;
      }
    }
    return taxonomy.getName();
  }

  /**
   * Invokes the callback function with true or false depending on if the taxonomy is editable or not.
   * @param taxonomyId The taxonomy id to check.
   * @param callback The callback handler.
   * @param content content used for checking if the editor is editable
   */
  public static function isEditable(taxonomyId:String, callback:Function, content:Content = undefined):void {
    if (!content) {
      content = WorkArea.ACTIVE_CONTENT_VALUE_EXPRESSION.getValue();
    }
    if (!content) {
      callback.call(null, true);
    }
    else if (content.isCheckedOutByOther()) {
      callback.call(null, false);
    }
    else if (!content.getState().readable) {
      callback.call(null, false);
    }
    else {
      ValueExpressionFactory.create(ContentPropertyNames.PATH, content).loadValue(function ():void {
        var siteId:String = editorContext.getSitesService().getSiteIdFor(content);
        TaxonomyNodeFactory.loadTaxonomyRoot(siteId, taxonomyId, function (parent:TaxonomyNode):void {
          if (parent) {
            callback.call(null, true);
          }
          else {
            callback.call(null, false);
          }
        });
      });
    }
  }

  /**
   * Loads the settings structs and extracts the list of
   * administration group names.
   * @param callback The callback the group names are passed to.
   */
  public static function loadSettings(callback:Function):void {
    ValueExpressionFactory.createFromFunction(function ():Array {
      return StudioConfigurationUtil.getConfiguration(TAXONOMY_SETTINGS, "administrationGroups", editorContext.getSitesService().getPreferredSite(), true);
    }).loadValue(function (groups:Array):void {
      callback.call(null, groups || []);
    });
  }

  /**
   * Loads the path nodes for the given bean record (content).
   * @param record The record to load the path for.
   * @param content content used to determine the site specific taxonomy for the given path, may be null
   * @param taxonomyId The id of the taxonomy the record is located in.
   * @param callback The callback function the updated record is passed to or null if node does not exist.
   */
  public static function loadTaxonomyPath(record:BeanRecord, content:Content, taxonomyId:String, callback:Function):void {
    var bean:Content = record.getBean() as Content;
    var siteId:String = null;
    if (content && content is Content) {
      siteId = editorContext.getSitesService().getSiteIdFor(content);
    }
    var url:String = 'taxonomies/path?' + Ext.urlEncode({
              taxonomyId: taxonomyId,
              nodeRef: parseRestId(bean),
              site: siteId
            });
    var taxRemoteBean:RemoteBean = beanFactory.getRemoteBean(url);
    taxRemoteBean.load(function ():void {
      EventUtil.invokeLater(function ():void {
        if (taxRemoteBean.get('path')) { //maybe not set if the taxonomy does not exist
          var nodes:Array = taxRemoteBean.get('path')["nodes"];
          var leafNode:TaxonomyNode = new TaxonomyNode(nodes[nodes.length - 1]);
          record.data.leafNode = leafNode;
          record.data.nodes = nodes;
          callback.call(null, record);
        }
        else {
          trace('[INFO]', 'Taxonomy node ' + bean + ' does not exist anymore or is not readable.');
          callback.call(null, record);
        }
      });
    });
  }

  /**
   * Bulk operation to move all the sources nodes to the given target node
   * @param sourceNodes
   * @param targetNode
   * @param callback
   */
  public static function bulkMove(sourceNodes:Array, targetNode:TaxonomyNode, callback:Function):void {
    var url:String = 'taxonomies/bulkmove';
    var bulkOperation:RemoteServiceMethod = new RemoteServiceMethod(url, 'POST');
    bulkOperation.request({
      'taxonomyId': targetNode.getTaxonomyId(),
      'site': targetNode.getSite(),
      'targetNodeRef': targetNode.getRef(),
      'nodeRefs': getNodeRefs(sourceNodes)
    }, function (result:Object):void {
      var resultList:TaxonomyNodeList = new TaxonomyNodeList(result.getResponseJSON().nodes);
      callback.call(null, resultList);
    });
  }

  /**
   * Bulk operation to delete the given nodes
   * @param sourceNodes
   * @param callback
   */
  public static function bulkDelete(sourceNodes:Array, callback:Function):void {
    Ext.getCmp(EditorMainView.ID).getEl().setStyle('cursor', 'wait');
    var url:String = 'taxonomies/bulkdelete';
    var bulkOperation:RemoteServiceMethod = new RemoteServiceMethod(url, 'POST');
    var node:TaxonomyNode = sourceNodes[0];
    bulkOperation.request({
      'taxonomyId': node.getTaxonomyId(),
      'site': node.getSite(),
      'nodeRefs': getNodeRefs(sourceNodes)
    }, function (result:Object):void {
      Ext.getCmp(EditorMainView.ID).getEl().setStyle('cursor', 'default');
      var parentNode:TaxonomyNode = new TaxonomyNode(result.getResponseJSON());
      callback.call(null, parentNode);
    }, function (result:Object):void {
      Ext.getCmp(EditorMainView.ID).getEl().setStyle('cursor', 'default');
      var message:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyEditor_deletion_failed_text');
      message =  StringUtil.format(message, result.getResponseJSON());
      var title:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyEditor_deletion_failed_title');
      MessageBoxUtil.showInfo(title, message);
    });
  }

  /**
   * Bulk operation to check all links to the given nodes
   * @param sourceNodes
   * @param callback called with an array of referred content
   */
  public static function bulkLinks(sourceNodes:Array, callback:Function):void {
    var url:String = 'taxonomies/bulklinks';
    var bulkOperation:RemoteServiceMethod = new RemoteServiceMethod(url, 'POST');
    var node:TaxonomyNode = sourceNodes[0];
    bulkOperation.request({
      'taxonomyId': node.getTaxonomyId(),
      'site': node.getSite(),
      'nodeRefs': getNodeRefs(sourceNodes)
    }, function (result:Object):void {
      callback.call(null, result.getResponseJSON().items as Array);
    });
  }

  /**
   * Bulk operation to check strong links of the given nodes
   * @param sourceNodes
   * @param callback called with an array of referred content
   */
  public static function bulkStrongLinks(sourceNodes:Array, callback:Function):void {
    var url:String = 'taxonomies/bulkstronglinks';
    var bulkOperation:RemoteServiceMethod = new RemoteServiceMethod(url, 'POST');
    var node:TaxonomyNode = sourceNodes[0];
    bulkOperation.request({
      'taxonomyId': node.getTaxonomyId(),
      'site': node.getSite(),
      'nodeRefs': getNodeRefs(sourceNodes)
    }, function (result:Object):void {
      callback.call(null, result.getResponseJSON().items as Array);
    });
  }


  /**
   * Helper method to join the ref values of the given nodes to a string
   * @param sourceNodes the node to join the refs for
   */
  private static function getNodeRefs(sourceNodes:Array):String {
    var result:Array = [];
    for each(var node:TaxonomyNode in sourceNodes) {
      result.push(node.getRef());
    }

    return result.join(",");
  }

  /**
   * Adds the content represented by the given node to the list of the
   * selection expression.
   * @param selectionExpression the current selection
   * @param contentId The id of the node to add to the selection.
   */
  public static function addNodeToSelection(selectionExpression:ValueExpression, contentId:String):void {
    var newSelection:Array = [];

    var child:Content = beanFactory.getRemoteBean(contentId) as Content;
    child.load(function (bean:Content):void {
      newSelection.push(bean);
      var selection:Array = selectionExpression.getValue();
      if (selection) {
        newSelection = selection.concat(newSelection);
      }
      selectionExpression.setValue(newSelection);
    });
  }

  /**
   * Removes the content represented by the given node from the list of the
   * selection expression.
   * @param selectionExpression the current selection
   * @param contentId The node to remove from the selection.
   */
  public static function removeNodeFromSelection(selectionExpression:ValueExpression, contentId:String):void {
    var selection:Array = selectionExpression.getValue();
    var newSelection:Array = [];
    if (selection) {
      for (var i:int = 0; i < selection.length; i++) {
        var selectedContent:Content = selection[i];
        var restId:String = parseRestId(selectedContent);
        if (restId === contentId) {
          continue;
        }
        newSelection.push(selectedContent);
      }
    }
    selectionExpression.setValue(newSelection);
  }

  /**
   * Returns the formatted content REST id, formatted using the CAP id.
   * @param ref
   * @return
   */
  public static function getRestIdFromCapId(ref:String):String {
    if(ref.indexOf('/') !== -1) {
      return 'content/' + ref.substr(ref.lastIndexOf('/') + 1, ref.length);
    }

    return ref.replace('-', '/');
  }

  /**
   * Returns the content id in REST format.
   * @param bean The content to retrieve the REST id from.
   */
  public static function parseRestId(bean:*):String {
    return 'content/' + bean.getNumericId();
  }
}
}
