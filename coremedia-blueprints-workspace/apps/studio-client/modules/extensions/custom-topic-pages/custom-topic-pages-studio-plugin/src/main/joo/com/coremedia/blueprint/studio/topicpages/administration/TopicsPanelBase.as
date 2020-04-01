package com.coremedia.blueprint.studio.topicpages.administration {

import com.coremedia.blueprint.studio.TopicsHelper;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentPropertyNames;
import com.coremedia.cap.undoc.content.ContentUtil;
import com.coremedia.cms.editor.sdk.desktop.TabChangePluginBase;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.ContentLocalizationUtil;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.ui.bem.IconWithTextBEMEntities;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.PropertyChangeEvent;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.util.EventUtil;
import com.coremedia.ui.util.QtipUtil;

import ext.EventManager;
import ext.Ext;
import ext.MessageBox;
import ext.StringUtil;
import ext.data.Model;
import ext.event.Event;
import ext.grid.GridPanel;
import ext.panel.Panel;
import ext.selection.RowSelectionModel;
import ext.view.TableView;

/**
 * Base class of the taxonomy administration tab.
 */
[ResourceBundle('com.coremedia.icons.CoreIcons')]
[ResourceBundle('com.coremedia.blueprint.studio.topicpages.TopicPages')]
public class TopicsPanelBase extends Panel {
  /**
   * The value expression that contains the selected topic record.
   */
  [Bindable]
  public var selectionExpression:ValueExpression;

  private static const COMPONENT_ID:String = "topicsPanel";

  private var topicsExpression:ValueExpression;
  private var filterValueExpression:ValueExpression;
  private var taxonomyExpression:ValueExpression;
  private var isFilteredExpression:ValueExpression;

  private var selectionString:String;

  public function TopicsPanelBase(config:TopicsPanelBase = null) {
    config.id = COMPONENT_ID;
    super(config);
  }

  override protected function afterRender():void {
    super.afterRender();
    getSelectionModel().addListener('selectionchange', onSelect);
    getGrid().addListener('afterlayout', addKeyMap);
    editorContext.getSitesService().getPreferredSiteIdExpression().addChangeListener(siteSelectionChanged);
    TabChangePluginBase.getWorkAreaTabChangeExpression().addChangeListener(workAreaTabChanged);
  }

  private function workAreaTabChanged(ve:ValueExpression):void {
    var component:* = ve.getValue();
    if(component as TopicPagesEditor) {
      reload();
    }
  }

  /**
   * Called when the user has changed the site.
   */
  private function siteSelectionChanged():void {
    reload();
  }

  /**
   * Adds the key listener to the grid so that the user can input the topic
   * that should be selected.
   */
  private function addKeyMap():void {
    getGrid().removeListener('afterlayout', addKeyMap);
    EventManager.on(getGrid().getEl(), 'keyup', function (evt:Event, t:*, o:*):void {
      if (!evt.shiftKey && !evt.ctrlKey && !evt.altKey) {
        var code:Number = evt.getCharCode();
        var character:String = String.fromCharCode(code).toLowerCase();
        selectionString += character;
        if (!selectRecordForInput(selectionString)) {
          selectionString = character;
          selectRecordForInput(character);
        }
      }
    });
  }

  private function selectRecordForInput(value:String):Boolean {
    for (var i:int = 0; i < getGrid().getStore().getCount(); i++) {
      var record:Model = getGrid().getStore().getAt(i);
      var name:String = record.data.name;
      if (name.toLowerCase().indexOf(value) === 0) {
        getSelectionModel().select(i, false);
        return true;
      }
    }
    return false;
  }

  private function getSelectionModel():RowSelectionModel {
    return (getGrid().getSelectionModel() as RowSelectionModel);
  }

  /**
   * The selection listener for the grid, will trigger the preview reload for a topic selection.
   */
  private function onSelect():void {
    var record:Model = getSelectionModel().getSelection()[0];
    selectionExpression.setValue(record);
  }

  /**
   * Returns the value expression that contains the list of contents to display as topics.
   * @return
   */
  protected function getTopicsExpression():ValueExpression {
    if (!topicsExpression) {
      topicsExpression = ValueExpressionFactory.create('topics', beanFactory.createLocalBean());
      topicsExpression.setValue([]);
    }
    return topicsExpression;
  }

  /**
   * Returns the value expression that contains the list is filtered cos of length
   * @return
   */
  protected function getIsFilteredExpression():ValueExpression {
    if (!isFilteredExpression) {
      isFilteredExpression = ValueExpressionFactory.create('isFiltered', beanFactory.createLocalBean());
      isFilteredExpression.addChangeListener(function(ve:ValueExpression):void {
        var filtered:Boolean = ve.getValue();
        Ext.getCmp('topicPagesFilteredLabel').setVisible(filtered);
      });
    }
    return isFilteredExpression;
  }


  /**
   * The value expression contains the value of the selected taxonomy.
   * @return
   */
  protected function getTaxonomySelectionExpression():ValueExpression {
    if (!taxonomyExpression) {
      taxonomyExpression = ValueExpressionFactory.create('taxonomy', beanFactory.createLocalBean());
      taxonomyExpression.addChangeListener(reload);
    }
    return taxonomyExpression;
  }

  /**
   * Returns the value expression that contains the active search expression.
   * @return
   */
  protected function getFilterValueExpression():ValueExpression {
    if (!filterValueExpression) {
      filterValueExpression = ValueExpressionFactory.create('topics', beanFactory.createLocalBean());
    }
    return filterValueExpression;
  }

  /**
   * Reloads the list of topics, fired after a search or a taxonomy selection.
   */
  protected function reload():void {
    removePropertyChangeListeners();
    OpenTopicPagesEditorActionBase.isAdministrationEnabled(function(enabled:Boolean):void {
      if(!enabled) {
        return;
      }

      var taxonomyContent:Content = getTaxonomySelectionExpression().getValue();
      if (!taxonomyContent) {
        return;
      }
      var taxonomy:Number = IdHelper.parseContentId(taxonomyContent);
      var term:String = filterValueExpression.getValue() || '';
      var siteId:String = editorContext.getSitesService().getPreferredSiteId();
      TopicsHelper.loadTopics(taxonomy, siteId, term, function (items:Array, filtered:Boolean):void {
        var initCall:Function = function ():void {
          getGrid().getStore().removeListener('load', initCall);
          getSelectionModel().select(0);
          getIsFilteredExpression().setValue(filtered);
        };
        getGrid().getStore().addListener('load', initCall);
        getTopicsExpression().setValue(items);
      });
    });
  }

  /**
   * Returns the instance of the grid panel inside this panel.
   * @return
   */
  private function getGrid():GridPanel {
    return queryById('topicsGrid') as GridPanel;
  }

  /**
   * Displays the name of the topic page.
   */
  protected static function nameRenderer(value:*, metaData:*, record:Model):String {
    return record.data.name;
  }

  /**
   * Displays the page the topic page is linked to.
   */
  protected function pageRenderer(value:*, metaData:*, record:Model):String {
    var id:Number = record.data.topic.getNumericId();
    var pageContent:Content = record.data.page;
    if (pageContent) {
      if (!record.data.rendered) {
        EventUtil.invokeLater(function ():void {//invoke later, otherwise JS error will be thrown that row is undefined.
          pageContent.load(function ():void {
            record.data.rendered = true;
            record.commit(false);
          });
        });
      }
      else {
        pageContent.addPropertyChangeListener(ContentPropertyNames.LIFECYCLE_STATUS, customPageChanged);
        var iconCls:String = ContentLocalizationUtil.getIconStyleClassForContentTypeName(pageContent.getType().getName());
        var tooltipText:String = pageContent.getName();
        var html:String = '<span class="' + IconWithTextBEMEntities.BLOCK + '">'
                + '<span class="' + IconWithTextBEMEntities.ELEMENT_ICON + ' ' + resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_page_icon') + '"></span>'
                + '<a class="' + IconWithTextBEMEntities.ELEMENT_TEXT + '" '+ QtipUtil.formatUnsafeQtip(tooltipText) +' href="#" data-topic-action="open">'
                + resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_name')
                + '</a>'
                + '<span class="' + IconWithTextBEMEntities.BLOCK + '">'
                + '<a id="topicpage-delete-' + id + '" class="' + IconWithTextBEMEntities.ELEMENT_ICON + ' ' + resourceManager.getString('com.coremedia.icons.CoreIcons', 'trash_bin') + '" style="text-decoration:none;" href="#" title="' + resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_deletion_tooltip') + '" data-topic-action="delete"/></span>'
                + '</a>';
        return html;
      }
    }

    if(editorContext.getSitesService().getPreferredSite()) {
      return '<div class="' + IconWithTextBEMEntities.BLOCK +'"><a href="#" id="topicpage-create-' + id + '"  class="' + IconWithTextBEMEntities.ELEMENT_TEXT + '" data-topic-action="create">'
              + resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_create_link') + '</a></div>';
    }
    return resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_no_preferred_site');
  }

  protected function onPageColumnClick(grid:TableView, source:*, rowIndex:Number, someIndex:Number, event:Event):void {
    var data:Object = grid.getStore().getAt(rowIndex).data;
    var id:Number = data.topic.getNumericId();
    var pageContent:Content = data.page;
    var action:String = String(event.getTarget().getAttribute('data-topic-action'));
    if (action === "create") {
      updatePage(id, true);
    } else if (action === "open") {
      openPage(IdHelper.parseContentId(pageContent));
    } else if (action === "delete") {
      deletePage(id, IdHelper.parseContentId(pageContent));
    }
    event.preventDefault();
  }

  private function customPageChanged(e:PropertyChangeEvent):void {
    var status:String = e.newValue;
    if (status === 'deleted') {
      reload();
    }
  }

  /**
   * Called from the page rendered.
   * @param id the content id to open
   */
  public function openPage(id:Number):void {
    var page:Content = ContentUtil.getContent('' + id);
    editorContext.getContentTabManager().openDocument(page);
  }

  /**
   * Called from the page rendered.
   * @param id
   * @param pageId
   */
  public function deletePage(id:Number, pageId:Number):void {
    var page:Content = ContentUtil.getContent('' + pageId);
    MessageBoxUtil.showPrompt(
      resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_deletion_title'),
      StringUtil.format(resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_deletion_text'), page.getName()),
      function (btn:*):void {
        if (btn === 'ok') {
          if (page.isCheckedOutByCurrentSession()) {
            page.checkIn(function ():void {
              editorContext.getContentTabManager().closeDocument(page);
            });
          }
          updatePage(id, false);
        }
      });
  }

  /**
   * Called by the link rendered into the page column.
   * @param id The numeric content id to link/unlink the page for
   * @param create True, if the page should be created. False to delete the linked page.
   */
  public function updatePage(id:Number, create:Boolean):void {
    TopicsHelper.loadSettings(function (settings:Bean):void {
      var topicPageChannel:Content = settings.get('topicPageChannel');
      if(!topicPageChannel) {
        var siteName:String = editorContext.getSitesService().getPreferredSiteName();
        var msg:String = StringUtil.format(resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_no_channel_configured'), siteName);
        MessageBox.alert(resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_no_channel_configured_title'), msg);
        return;
      }

      topicPageChannel.invalidate(function ():void {
        if (topicPageChannel.isCheckedOutByOther()) {
          var msg:String = StringUtil.format(resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_root_channel_checked_out_msg'),
                  topicPageChannel.getName());
          MessageBox.alert(resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_root_channel_checked_out_title'), msg);
          return;
        }
        selectionExpression.setValue(null);
        var selectedRecord:Model = getSelectionModel().getSelection()[0] as Model;
        var siteId:String = editorContext.getSitesService().getPreferredSiteId();
        TopicsHelper.updatePage(id, siteId, create, function (result:*):void {
          ValueExpressionFactory.create(ContentPropertyNames.PATH, result.topicPagesFolder).loadValue(function (path:String):void {
            SESSION.getConnection().getContentRepository().getChild(path, function (child:Content):void {
              if (child) {
                child.invalidate();
              }
              selectedRecord.data.rendered = false;
              selectedRecord.data.page = result.page;
              selectedRecord.commit(false);

              selectionExpression.setValue(selectedRecord);

              var root:Content = result.rootChannel;
              if (!root) {
                var msg:String = StringUtil.format(resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_root_channel_not_found_msg'), editorContext.getSitesService().getPreferredSiteName());
                MessageBox.alert(resourceManager.getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'TopicPages_root_channel_not_found_title'), msg);
              }
              if (result.page) {
                editorContext.getContentTabManager().openDocuments([result.page], true);
              }
            });
          });
        });
      });
    });
  }


  /**
   * Remove registered listeners.
   */
  override protected function onDestroy():void {
    removePropertyChangeListeners();
    super.onDestroy();
    editorContext.getSitesService().getPreferredSiteIdExpression().removeChangeListener(siteSelectionChanged);
    TabChangePluginBase.getWorkAreaTabChangeExpression().removeChangeListener(workAreaTabChanged);
  }

  private function removePropertyChangeListeners():void {
    var topics:Array = getTopicsExpression().getValue();
    topics && topics.forEach(function(data:Object):void {
      var pageContent:Content = data.page;
      if (pageContent) {
        pageContent.removePropertyChangeListener(ContentPropertyNames.LIFECYCLE_STATUS, customPageChanged);
      }
    });
  }
}
}
