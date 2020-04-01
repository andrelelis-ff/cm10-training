package com.coremedia.blueprint.studio.topicpages.administration {

import com.coremedia.blueprint.base.components.util.UserUtil;
import com.coremedia.blueprint.studio.TopicsHelper;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.desktop.WorkArea;
import com.coremedia.cms.editor.sdk.desktop.WorkAreaTabType;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.StringHelper;
import com.coremedia.ui.data.Bean;

import ext.Action;
import ext.Component;
import ext.Ext;
import ext.button.Button;
import ext.panel.Panel;

import mx.resources.ResourceManager;

/** Opens the Topic page editor **/
[ResourceBundle('com.coremedia.cms.editor.Editor')]
[ResourceBundle('com.coremedia.blueprint.studio.topicpages.TopicPages')]
public class OpenTopicPagesEditorActionBase extends Action {

  internal native function get items():Array;

  public function OpenTopicPagesEditorActionBase(config:OpenTopicPagesEditorAction = null) {
    config.handler = showTopicPagesEditor;
    super(config);
  }

  override public function addComponent(comp:Component):void {
    super.addComponent(comp);
    isAdministrationEnabled(function(enabled:Boolean):void {
      setDisabled(!enabled);
      updateTooltip(enabled);
    });
    editorContext.getSitesService().getPreferredSiteIdExpression().addChangeListener(preferredSiteChangedHandler);
  }

  private function preferredSiteChangedHandler():void {
    isAdministrationEnabled(function(enabled:Boolean):void {
      setDisabled(!enabled);
      updateTooltip(enabled);
      if(!enabled) {
        var topicPagesAdminTab:TopicPagesEditor = Ext.getCmp(TopicPagesEditorBase.TOPIC_PAGES_EDITOR_ID) as TopicPagesEditor;
        if(topicPagesAdminTab) {
          topicPagesAdminTab.destroy();
        }
      }
    });
  }

  private static function updateTooltip(enabled:Boolean):void {
    var msg:String = ResourceManager.getInstance().getString('com.coremedia.cms.editor.Editor', 'Button_disabled_insufficient_privileges');

    var preferredSiteId:String = editorContext.getSitesService().getPreferredSiteId();
    if (!preferredSiteId) {
      msg = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'topic_pages_button_no_preferred_site_tooltip');
    }

    TopicsHelper.loadSettings(function (settingsRemoteBean:Bean):void {
      var topicPageChannel:Content = settingsRemoteBean.get('topicPageChannel');
      if (!topicPageChannel) {
        msg = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'topic_pages_button_no_topic_page_settings_tooltip');
        setButtonTooltip(msg);
      }
    });

    if (enabled) {
      msg = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.topicpages.TopicPages', 'topic_pages_button_tooltip');
    }

    setButtonTooltip(msg);
  }

  private static function setButtonTooltip(msg:String):void {
    var button:Button = (Ext.getCmp('btn-topicpages-editor') as Button);
    if(button) {
      button.setTooltip(msg);
    }
  }

  /**
   * Returns true if the current user can administrate the taxonomies.
   * @return
   */
  public static function isAdministrationEnabled(callback:Function):void {
    TopicsHelper.loadSettings(function (settingsRemoteBean:Bean):void {
      var topicPageChannel:Content = settingsRemoteBean.get('topicPageChannel');
      if(!topicPageChannel) {
        trace("[INFO]", "Topic Pages: could not find root channel for topic pages, please check the TopicPages settings document of the preferred site.");
        callback.call(null, false);
      }
      else {
        var adminGroups:Array = settingsRemoteBean.get('adminGroups');
        if(SESSION.getUser().isAdministrative()) {
          callback.call(null, true);
        } else {
          for(var i:int = 0; i<adminGroups.length; i++) {
            var groupName:String = StringHelper.trim(adminGroups[i],'');
            if(UserUtil.isInGroup(groupName)) {
              callback.call(null, true);
              return;
            }
          }
          callback.call(null, false);
        }
      }
    });
  }


  /**
   * Static call to open the taxonomy admin console.
   */
  private static function showTopicPagesEditor():void {
    var workArea:WorkArea = editorContext.getWorkArea() as WorkArea;
    var topicPagesAdminTab:TopicPagesEditor = Ext.getCmp(TopicPagesEditorBase.TOPIC_PAGES_EDITOR_ID) as TopicPagesEditor;

    if (!topicPagesAdminTab) {
      var workAreaTabType:WorkAreaTabType = workArea.getTabTypeById(TopicPagesEditor.xtype);
      workAreaTabType.createTab(null, function(tab:Panel):void {
        topicPagesAdminTab = tab as TopicPagesEditor;
        workArea.addTab(workAreaTabType, topicPagesAdminTab);
      });
    }
    workArea.setActiveTab(topicPagesAdminTab);
  }

  override public function removeComponent(comp:Component):void {
    super.removeComponent(comp);
    if (items && items.length === 0) {
      editorContext.getSitesService().getPreferredSiteIdExpression().removeChangeListener(preferredSiteChangedHandler);
    }
  }
}
}
