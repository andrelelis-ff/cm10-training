package com.coremedia.catalog.studio.actions {
import com.coremedia.cap.content.Content;
import com.coremedia.catalog.studio.library.CatalogTreeRelation;
import com.coremedia.catalog.studio.library.CatalogTreeRelationHelper;
import com.coremedia.cms.editor.sdk.ContentTreeRelation;
import com.coremedia.cms.editor.sdk.ContentTreeRelationProvider;
import com.coremedia.cms.editor.sdk.actions.ActionConfigUtil;
import com.coremedia.cms.editor.sdk.actions.ContentAction;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.ui.data.ValueExpression;

import ext.StringUtil;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.catalog.studio.actions.CatalogActions')]
public class UnlinkActionBase extends ContentAction {
  private var folderValueExpression:ValueExpression;

  public function UnlinkActionBase(config:UnlinkAction = null) {
    super(UnlinkAction(ActionConfigUtil.extendConfig(config, 'unlink', {handler: startUnlink})));
    this.folderValueExpression = config.folderValueExpression;
  }

  protected override function isDisabledFor(contents:Array):Boolean {
    for each(var content:Content in contents) {
      var type:String = content.getType() && content.getType().getName();
      if (type !== CatalogTreeRelation.CONTENT_TYPE_CATEGORY && type !== CatalogTreeRelation.CONTENT_TYPE_PRODUCT) {
        return true;
      }

      var treeRelation:ContentTreeRelation = ContentTreeRelationProvider.getContentTreeRelation();
      if(treeRelation === undefined) {
        return undefined;
      }
      if(treeRelation === null) {
        return true;
      }

      //we must use a CatalogTreeRelation here, otherwise disable the action
      var catalogTreeRelation:CatalogTreeRelation = treeRelation as CatalogTreeRelation;
      if(catalogTreeRelation == null) {
        return true;
      }

      var parents:Array = catalogTreeRelation.getParents(content);
      if (!parents || parents.length <= 1) {
        return true;
      }
    }
    return false;
  }


  protected override function isHiddenFor(contents:Array):Boolean {
    // only the delete button should be shown otherwise
    return isDisabledFor(contents);
  }

  private function startUnlink():void {
    var contents:Array = getContents();
    if (!contents || !contents.length) {
      return;
    }

    var category:Content = folderValueExpression.getValue();
    var title:String = ResourceManager.getInstance().getString('com.coremedia.catalog.studio.actions.CatalogActions', 'Action_unlink_title');
    var message:String = StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.catalog.studio.actions.CatalogActions', 'Action_unlink_message'), category.getName());
    MessageBoxUtil.showConfirmation(title, message, ResourceManager.getInstance().getString('com.coremedia.catalog.studio.actions.CatalogActions', 'Action_unlink_text'),
            function (btn:*):void {
              if (btn === 'ok') {
                doUnlink(category, contents);
              }
            });
  }

  /**
   * Removes the selection from the linked category.
   * The parent category is determined using the current tree selection.
   * @param contents the contents to unlink
   */
  private function doUnlink(category:Content, contents:Array):void {
    //validate category checkout
    if (!CatalogTreeRelationHelper.validateCheckoutState([category])) {
      return;
    }

    //validate product checkout
    var products:Array = CatalogTreeRelationHelper.filterForType(contents, CatalogTreeRelationHelper.CONTENT_TYPE_PRODUCT);
    if (!CatalogTreeRelationHelper.validateCheckoutState(products)) {
      return;
    }

    var checkedInContents:Array = CatalogTreeRelationHelper.storeCheckInOutState(contents.concat([category]));

    for each(var content:Content in contents) {
      CatalogTreeRelationHelper.removeCategoryChild(category, content);
    }

    CatalogTreeRelationHelper.restoreCheckInOutState(checkedInContents);
  }
}
}
