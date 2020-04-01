package com.coremedia.blueprint.studio.taxonomy.selection {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeList;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyUtil;
import com.coremedia.blueprint.studio.taxonomy.rendering.TaxonomyRenderFactory;
import com.coremedia.blueprint.studio.taxonomy.rendering.TaxonomyRenderer;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.premular.DocumentForm;
import com.coremedia.cms.editor.sdk.premular.DocumentTabPanel;
import com.coremedia.cms.editor.sdk.util.ContentLocalizationUtil;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.store.BeanRecord;

import ext.EventManager;
import ext.LoadMask;
import ext.container.Container;
import ext.grid.GridPanel;

import js.KeyEvent;

/**
 *
 */
[ResourceBundle('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin')]
public class TaxonomySuggestionsLinkListPanelBase extends GridPanel {

  private var suggestionsExpression:ValueExpression;
  private var bindTo:ValueExpression;

  private var propertyValueExpression:ValueExpression;
  private var taxonomyId:String;

  private var loadMask:LoadMask;
  private var cache:TaxonomyCache;

  private var selectedPositionsExpression:ValueExpression;
  private var selectedItemsExpression:ValueExpression;

  private var documentFormParent:DocumentForm;
  private var needsUpdateAfterContentChange:Boolean = false;


  /**
   * @param config The configuration options. See the config class for details.
   *
   * @see com.coremedia.cms.editor.sdk.config.linkListPropertyFieldGridPanelBase
   */
  public function TaxonomySuggestionsLinkListPanelBase(config:TaxonomySuggestionsLinkListPanel = null) {
    super(config);

    documentFormParent = findParentBy(function (cont:Container):Boolean {
      return cont is DocumentForm && cont.up() && cont.up() is DocumentTabPanel;
    }) as DocumentForm;

    if (documentFormParent) {
      mon(documentFormParent, "activate", documentFormActivated);
    }

    if (!config.disableSuggestions) {
      bindTo = config.bindTo;

      propertyValueExpression = bindTo.extendBy('properties.' + config.propertyName);
      propertyValueExpression.addChangeListener(propertyChanged);
      bindTo.addChangeListener(contentChanged);
      taxonomyId = config.taxonomyId;

      cache = new TaxonomyCache(bindTo, propertyValueExpression, taxonomyId);
    }
  }

  override protected function afterRender():void {
    super.afterRender();

    var loadMaskCfg:LoadMask = LoadMask({
      target: this
    });
    loadMaskCfg.msg = resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomySuggestions_loading');
    loadMask = new LoadMask(loadMaskCfg);
    loadMask.disable();

    updateSuggestions(true);

    EventManager.on(getEl(), 'keyup', function (evt:KeyEvent, t:*, o:*):void {
      if(evt.keyCode === KeyEvent.DOM_VK_ENTER || evt.keyCode === KeyEvent.DOM_VK_RETURN) {
        var values:Array = getSelectedValuesExpression().getValue();
        if(values.length > 0) {
          var selection:Content = values[0];
          var ref:String = TaxonomyUtil.parseRestId(selection);
          plusMinusClicked(ref);
        }
      }
    });
  }

  /**
   * Compute the disabled state of the add-all button.
   *
   * @param forceReadOnlyValueExpression the expression
   * @return a VE which disables the add all button
   */
  protected function getAddAllDisabledVE(forceReadOnlyValueExpression:ValueExpression):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function():Boolean {
      var readOnly:Boolean = forceReadOnlyValueExpression && forceReadOnlyValueExpression.getValue();
      var suggestions:Array = (getSuggestionsExpression() && getSuggestionsExpression().getValue()) || [];
      return readOnly || suggestions.length === 0;
    });
  }

  /**
   * Fired when the taxonomy property of the content has been changed.
   * We use this event to refresh (not reload) the taxonomy list.
   */
  private function propertyChanged():void {
    updateSuggestions(false);
  }

  /**
   * Fired when the underlying content changes
   * We use this event to refresh and reload the taxonomy list iff
   * the surrounding document form is visible. Otherwise, wait
   * for activation.
   */
  private function contentChanged():void {
    if (documentFormParent && documentFormParent.isVisible()) {
      updateSuggestions(true);
      needsUpdateAfterContentChange = false;
    } else {
      needsUpdateAfterContentChange = true;
    }
  }

  /**
   * After activation of the surrounding document form
   */
  private function documentFormActivated():void {
    if (needsUpdateAfterContentChange) {
      updateSuggestions(true);
      needsUpdateAfterContentChange = false;
    }
  }

  protected function getSuggestionsExpression():ValueExpression {
    if (!suggestionsExpression) {
      suggestionsExpression = ValueExpressionFactory.create('hits', beanFactory.createLocalBean());
    }
    return suggestionsExpression;
  }

  //noinspection JSMethodCanBeStatic
  protected function formatUnreadableName(record:BeanRecord):String {
    var content:Content = record.getBean() as Content;
    return ContentLocalizationUtil.formatNotReadableName(content);
  }

  /**
   * Loads the values into the list.
   */
  private function updateSuggestions(reload:Boolean = false):void {
    if (!initialConfig.disableSuggestions) {
      setBusy(true);

      var callback:Function = function (list:TaxonomyNodeList):void {
        if (list) {
          convertResultToContentList(list);
        }
      };

      if (reload) {
        cache.invalidate(callback);
      }
      else {
        cache.loadSuggestions(callback);
      }
    }
  }

  /**
   * Updates the empty list label so that loading is indicated.
   * @param busy
   */
  public function setBusy(busy:Boolean = false):void {
    if(!loadMask) {
      return;
    }

    if (busy) {
      loadMask.show();
    } else {
      loadMask.hide();
    }
  }

  /**
   * Return a value expression evaluating to an array of selected positions (indexes) in the link list.
   * @return a value expression evaluating to an array of selected position
   */
  public function getSelectedPositionsExpression():ValueExpression {
    return selectedPositionsExpression || (selectedPositionsExpression = ValueExpressionFactory.create('positions', beanFactory.createLocalBean()));
  }

  /**
   * Return a value expression evaluating to an array of selected values (elements) in the link list.
   * @return a value expression evaluating to an array of selected values
   */
  public function getSelectedValuesExpression():ValueExpression {
    return selectedItemsExpression || (selectedItemsExpression = ValueExpressionFactory.create('items', beanFactory.createLocalBean()));
  }

  private function convertResultToContentList(list:TaxonomyNodeList):void {
    var items:Array = list.getNodes();
    var contents:Array = [];
    var callbackCount:int = items.length;
    for (var i:int = 0; i < items.length; i++) {
      var item:TaxonomyNode = items[i];
      var child:Content = beanFactory.getRemoteBean(item.getRef()) as Content;
      child.load(function (bean:Content):void {
        contents.push(bean);
        callbackCount--;
        if (callbackCount === 0) {
          getSuggestionsExpression().setValue(contents);

          //force re-rendering of records (e.g. if suggestion evaluation type has been changed)
          for(var i:int = 0; i<getStore().getCount(); i++) {
            var record:BeanRecord = getStore().getAt(i) as BeanRecord;
            record.data.html = null;
            record.commit();
          }
          setBusy(false);
        }
      });
    }
    if (items.length === 0) {
      getSuggestionsExpression().setValue([]);
      setBusy(false);
    }
  }

  /**
   * Adds all items of the list to the keyword list.
   */
  public function addAllKeywordsHandler():void {
    var suggestions:Array = getSuggestionsExpression().getValue();
    var existingEntries:Array = propertyValueExpression.getValue();
    var newEntries:Array = [];
    for (var i:int = 0; i < existingEntries.length; i++) {
      newEntries.push(existingEntries[i]);
    }
    for (var j:int = 0; j < suggestions.length; j++) {
      newEntries.push(suggestions[j]);
    }
    propertyValueExpression.setValue(newEntries);
    updateSuggestions(false);
  }

  /**
   * Trigger a new evaluation of the content for suggestions.
   */
  protected function reloadKeywordsHandler():void {
    updateSuggestions(true);
  }

  //noinspection JSUnusedLocalSymbols
  /**
   * Displays each path item of a taxonomy
   * @param value
   * @param metaData
   * @param record
   * @return
   */
  protected function taxonomyRenderer(value:*, metaData:*, record:BeanRecord):String {
    TaxonomyUtil.loadTaxonomyPath(record, bindTo.getValue(), taxonomyId, function (updatedRecord:BeanRecord):void {
      var content:Content = record.getBean() as Content;
      var renderer:TaxonomyRenderer = TaxonomyRenderFactory.createSuggestionsRenderer(record.data.nodes, getId(), cache.getWeight(content.getId()));
      renderer.doRender(function (html:String):void {
        if (record.data.html !== html) {
          record.data.html = html;
          record.commit(false);
        }
      });
    });
    if (!record.data.html) {
      return "<div class='loading'>" + resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyLinkList_status_loading_text') + "</div>";
    }
    return record.data.html;
  }

  //noinspection JSUnusedGlobalSymbols
  /**
   * Removes the given taxonomy
   */
  public function plusMinusClicked(nodeRef:String):void {
    TaxonomyUtil.removeNodeFromSelection(propertyValueExpression, nodeRef);
    TaxonomyUtil.addNodeToSelection(propertyValueExpression, nodeRef);
  }

  override protected function onDestroy():void {
    bindTo.removeChangeListener(contentChanged);
    documentFormParent && mun(documentFormParent, "activate", documentFormActivated);
    propertyValueExpression && propertyValueExpression.removeChangeListener(propertyChanged);

    loadMask && loadMask.destroy();

    super.onDestroy();
  }
}
}
