package com.coremedia.blueprint.studio.taxonomy.administration {

import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeFactory;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeList;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyUtil;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.dependencies.DependencyTracker;
import com.coremedia.ui.models.bem.BEMBlock;
import com.coremedia.ui.models.bem.BEMModifier;

import ext.Component;
import ext.Ext;
import ext.data.Model;
import ext.data.Store;
import ext.grid.GridPanel;
import ext.grid.plugin.GridViewDragDropPlugin;
import ext.selection.RowSelectionModel;

import js.KeyEvent;

[ResourceBundle('com.coremedia.icons.CoreIcons')]
[ResourceBundle('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin')]
public class TaxonomyExplorerColumnBase extends GridPanel {

  public static const DRAG_DROP_PLUGIN_ID:String = "dragdrop";

  private static const COLUMN_ENTRY_BLOCK:BEMBlock = new BEMBlock("cm-column-entry");
  private static const COLUMN_ENTRY_MODIFIER_EMPTY:BEMModifier = COLUMN_ENTRY_BLOCK.createModifier("empty");
  private static const COLUMN_ENTRY_MODIFIER_NOT_EXTENDABLE:BEMModifier = COLUMN_ENTRY_BLOCK.createModifier("no-extendable");
  private static const COLUMN_ENTRY_MODIFIER_LEAF:BEMModifier = COLUMN_ENTRY_BLOCK.createModifier("leaf");
  private static const COLUMN_ENTRY_MODIFIER_MARKED_FOR_COPY:BEMModifier = COLUMN_ENTRY_BLOCK.createModifier("marked-for-copy");

  private var globalSelectedNodeExpression:ValueExpression;
  private var entriesValueExpression:ValueExpression;
  private var siteSelectionExpression:ValueExpression;

  private var parentNode:TaxonomyNode;
  private var activeNode:TaxonomyNode;

  private var taxonomyExplorerColumnDropTarget:TaxonomyExplorerColumnDropTarget;

  public function TaxonomyExplorerColumnBase(config:TaxonomyExplorerColumn = null) {
    super(config);
    siteSelectionExpression = config.siteSelectionExpression;
    parentNode = config.parentNode;
    globalSelectedNodeExpression = config.selectedNodeExpression;
    initColumn();

    getSelectionModel().addListener('selectionchange', selectionChanged);
    addListener('rowclick', onPanelClick);
    addListener("beforedestroy", onBeforeDestroy, this, {single: true});
  }


  //Dnd is causing focus issues/console errors: since we don't need the focus on the column, we skip the parent call
  override public function focus(selectText:* = undefined, delay:* = undefined, callback:Function = null, scope:Function = null):Component {
    return this;
  }

  override protected function afterRender():void {
    super.afterRender();
    addKeyNavigation();


    taxonomyExplorerColumnDropTarget = new TaxonomyExplorerColumnDropTarget(this);

    var ddPlugin:GridViewDragDropPlugin = getView().getPlugin(DRAG_DROP_PLUGIN_ID) as GridViewDragDropPlugin;
    if (ddPlugin && ddPlugin.dragZone) {
      ddPlugin.dragZone['getDragText'] = getDragText;
      //DnD is causing focus issues, so we skip the focus handling here and simply call the parent
      ddPlugin.dragZone['onValidDrop'] = function (target:*, e:*, id:*):void {
        this.callParent([target, e, id]);
      };
      ddPlugin.dropZone.addToGroup('taxonomies');
      ddPlugin.dropZone.onNodeOver = taxonomyExplorerColumnDropTarget.notifyOnNodeOver;
      ddPlugin.dropZone.onNodeDrop = taxonomyExplorerColumnDropTarget.notifyOnNodeDrop;
      ddPlugin.dropZone.onContainerOver = taxonomyExplorerColumnDropTarget.notifyOnContainerOver;
    }
  }

  /**
   * Registers the additional key handlers for left/right navigation.
   */
  private function addKeyNavigation():void {
    addListener("cellkeydown", onKeyInput);
  }

  private function onKeyInput(grid:*, td:*, cellIndex:*, record:*, tr:*, rowIndex:*, e:*, eOpts:*):Boolean {
    var key:Number = e.getKey();
    if (key === KeyEvent.DOM_VK_LEFT) {
      if (globalSelectedNodeExpression && parentNode) {
        activeNode = parentNode;
        globalSelectedNodeExpression.setValue([parentNode]);
        getExplorerPanel().getColumnContainer(parentNode).selectNode(parentNode);
        return false;
      }
    }
    else if (key === KeyEvent.DOM_VK_RIGHT && !activeNode.isLeaf()) {
      activeNode.loadChildren(true, function (list:TaxonomyNodeList):void {
        if (list.size() > 0) {
          var selectNode:TaxonomyNode = list.getNodes()[0];
          globalSelectedNodeExpression.setValue([selectNode]);
          getExplorerPanel().getColumnContainer(selectNode).selectNode(selectNode);
        }
      });
      return false;
    }
    else if(key === KeyEvent.DOM_VK_RIGHT) {
      return false;
    }

    return true;
  }

  /**
   * Triggered when the reload button is pressed.
   */
  public function reload():void {
    getEntriesValueExpression().setValue(undefined);
    initColumn(true);
  }

  /**
   * Returns the parent node this column has been created for.
   * @return
   */
  public function getParentNode():TaxonomyNode {
    return parentNode;
  }

  public function getEntriesValueExpression():ValueExpression {
    if (!entriesValueExpression) {
      var emptyBean:Bean = beanFactory.createLocalBean();
      entriesValueExpression = ValueExpressionFactory.create("nodes", emptyBean);
    }
    return entriesValueExpression;
  }

  protected function selectionChanged():void {
    var selections:Array = (getSelectionModel() as RowSelectionModel).getSelection();
    var selectionResult:Array = [];
    for each(var selection:Model in selections) {
      var selectedNode:TaxonomyNode = TaxonomyNode.forValues(selection.data.name,
              selection.data.type,
              selection.data.ref,
              selection.data.siteId,
              selection.data.level,
              selection.data.root,
              selection.data.leaf,
              selection.data.taxonomyId,
              selection.data.selectable,
              selection.data.extendable);
      selectionResult.push(selectedNode);
    }

    if (globalSelectedNodeExpression) {
      activeNode = null;
      if (selectionResult.length > 0) {
        activeNode = selectionResult[0];
      }
      globalSelectedNodeExpression.setValue(selectionResult);
    }
  }

  /**
   * Selects the given node in the list
   * record entry.
   * @param node
   * @param callback optional callback method
   */
  public function selectNode(node:TaxonomyNode, callback:Function = undefined):void {
    activeNode = node;
    doSelect(callback);
  }

  /**
   * Selects the active node or clears the selection
   * if the active node is not set.
   * @param callback optional callback method
   */
  private function doSelect(callback:Function = undefined):void {
    ValueExpressionFactory.createFromFunction(function ():Boolean {
      if (!getStore().isLoaded()) {
        DependencyTracker.dependOnObservable(getStore(), "load");
        return undefined;
      }
      return true;
    }).loadValue(function (loaded:Boolean):void {
      if (activeNode) {
        for (var i:int = 0; i < getStore().getCount(); i++) {
          var nodeRecord:Model = getStore().getAt(i);
          if (nodeRecord.data.ref === activeNode.getRef()) {
            (getSelectionModel() as RowSelectionModel).select([nodeRecord], false, true);
            getView().focusRow(i);
          }
        }
      } else {
        (getSelectionModel() as RowSelectionModel).deselectRange(0, getStore().getCount() - 1);
      }

      if (callback) {
        callback.call(null);
      }
    });
  }

  /**
   * Searches the list for the given node and updates the
   * record entry.
   * @param node
   */
  public function updateNode(node:TaxonomyNode):Boolean {
    var nodeStore:Store = getStore();
    for (var i:int = 0; i < nodeStore.getCount(); i++) {
      var nodeRecord:Model = nodeStore.getAt(i);
      if (nodeRecord.data.ref === node.getRef()) {
        nodeRecord.data.name = node.getName();
        nodeRecord.data.type = node.getType();
        nodeRecord.data.root = node.isRoot();
        nodeRecord.data.extendable = node.isExtendable();
        nodeRecord.data.selectable = node.isSelectable();
        nodeRecord.data.leaf = node.isLeaf();
        nodeRecord.commit(false);
        return true;
      }
    }
    return false;
  }

  /**
   * Loads the values into the list.
   */
  public function initColumn(reload:Boolean = true):void {
    updateLoadStatus(resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyExplorerColumn_emptyText_loading'));
    var callback:Function = function (list:TaxonomyNodeList):void {
      getEntriesValueExpression().setValue(list.toJson());
      if (list.toJson().length === 0) {
        updateLoadStatus(resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyExplorerColumn_emptyText_no_keywords'));
      }
    };
    if (parentNode) {
      parentNode.loadChildren(true, callback);
    } else {
      var site:String = siteSelectionExpression.getValue();
      TaxonomyNodeFactory.loadTaxonomies(site, callback, reload);
    }
  }

  private function updateLoadStatus(text:String):void {
    getView()['emptyText'] = text;
    if (isVisible(true)) {
      getView().refresh();
    }
  }

  private function getDragText():String {
    var selection:Array = globalSelectedNodeExpression.getValue();
    var names:Array = [];
    for each(var node:TaxonomyNode in selection) {
      names.push(node.getName());
    }
    var text:String = names.join(", ");
    if (text.length > 100) {
      text = text.substr(0, 100) + "...";
    }

    return text;
  }

  //noinspection JSUnusedLocalSymbols
  /**
   * Displays the image for each link list item.
   * @param value
   * @param metaData
   * @param record
   * @return
   */
  protected function nameColRenderer(value:*, metaData:*, record:Model):String {
    var name:String = record.data.name;
    var modifiers:Array = [];

    if (record.data.root) {
      if (record.data.siteId) {
        var i18nName:String = resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', name);
        if (i18nName) {
          name = i18nName;
        }

        var siteName:String = editorContext.getSitesService().getSite(record.data.siteId).getName();
        name += ' (' + siteName + ')';
      }
    } else {
      if (name.length === 0) {
        name = resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyExplorerColumn_undefined');
        modifiers.push(COLUMN_ENTRY_MODIFIER_EMPTY);
      }
      if (!record.data.extendable || !record.data.selectable) {
        modifiers.push(COLUMN_ENTRY_MODIFIER_NOT_EXTENDABLE);
      } else if (record.data.leaf) {
        modifiers.push(COLUMN_ENTRY_MODIFIER_LEAF);
      }
    }
    if (getExplorerPanel().isMarkedForCopying(record.data.ref)) {
      modifiers.push(COLUMN_ENTRY_MODIFIER_MARKED_FOR_COPY);
    }

    // determine css classes
    var cls:String = COLUMN_ENTRY_BLOCK.getCSSClass();
    modifiers.forEach(function (modifier:BEMModifier):void {
      cls += " " + modifier.getCSSClass();
    });

    // convert to html
    return '<div class="' + cls + '">' + name + '</div>';
  }

  /**
   * Executed for a regular click on the panel, updates
   * backward selections that are on the same selection path.
   */
  private function onPanelClick():void {
    selectionChanged();
  }

  /**
   * Returns the parent taxonomy explorer panel.
   * @return
   */
  private static function getExplorerPanel():TaxonomyExplorerPanel {
    return Ext.getCmp('taxonomyExplorerPanel') as TaxonomyExplorerPanel;
  }

  //noinspection JSMethodCanBeStatic,JSUnusedLocalSymbols
  /**
   * The pointer column renderer shows a '>' symbol if the node is not a leaf.
   * @param value
   * @param metaData
   * @param record
   * @return
   */
  protected function pointerColRenderer(value:*, metaData:*, record:Model):String {
    var leaf:Boolean = record.data.leaf;
    if (!leaf) {
      return '<span class="' + resourceManager.getString('com.coremedia.icons.CoreIcons', 'arrow_right') + '"></span>';
    }
    return '';
  }

  private function onBeforeDestroy():void {
    taxonomyExplorerColumnDropTarget && taxonomyExplorerColumnDropTarget.unreg();
    if (getEl()) {
      removeListener("cellkeydown", onKeyInput);
    }
  }
}
}
