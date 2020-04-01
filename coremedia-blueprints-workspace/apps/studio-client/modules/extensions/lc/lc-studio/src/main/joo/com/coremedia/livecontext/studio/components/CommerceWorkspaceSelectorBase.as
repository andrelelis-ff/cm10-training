package com.coremedia.livecontext.studio.components {
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ecommerce.studio.model.Workspace;
import com.coremedia.ui.components.LocalComboBox;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.StringUtil;
import ext.form.field.IField;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.livecontext.studio.LivecontextStudioPlugin')]
public class CommerceWorkspaceSelectorBase extends LocalComboBox{

  private static const NO_WORKSPACE:String = 'no-workspace';
  private static const NO_WORKSPACE_ENTRY:Object = {id: NO_WORKSPACE,
                                                    name: ResourceManager.getInstance().getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'commerce_workspace_no_workspace')};

  public function CommerceWorkspaceSelectorBase(config:CommerceWorkspaceSelector = null) {
    super(config);
    var activeSiteExpression:ValueExpression = ValueExpressionFactory.createFromFunction(function():Site {
      return editorContext.getSitesService().getPreferredSite();
    });

    //reset the workspace when changing the site
    activeSiteExpression.addChangeListener(function():void {
      setWorkspaceId(NO_WORKSPACE);
      setValue(undefined);
    });
  }
  override public function setValue(value:*):IField {
    //TODO: Workaround for CMS-9822: Somehow the bindListPlugin tries to update the store after the first selection of a workspace
    //which leads to null value
    //the possible cause of the problem is that the list depends on the active store which again depends on the editor preference
    //and we write the workspace id in the same editor preference document.
    if (value === null) {
      return getValue();
    } else {
      return super.setValue(value);
    }
  }

  override public function validate():Boolean {
    validateId();
    return isWorkspaceValid();
  }

  internal function getSelectableWorkspaces():Array {
    var workspaces:Array = getWorkspaces();
    if (!workspaces || workspaces.length === 0) {
      return undefined;
    } else {
      //when there is at least one workspace add a virtual entry 'no workspace'
      var selectableWorkspaces:Array = [NO_WORKSPACE_ENTRY];
      return selectableWorkspaces.concat(workspaces);
    }
  }

  internal function getWorkspaceIdExpression():ValueExpression {
    var expression:ValueExpression = ValueExpressionFactory.createFromFunction(getWorkspaceId);
    expression.setValue = setWorkspaceId;
    return expression;
  }

  private function validateId():void {
    if (isWorkspaceValid()) {
      clearInvalid()
    } else {
      markInvalid(getErrorMessage(CatalogHelper.getInstance().getCommerceWorkspaceExpression().getValue()));
    }
  }

  private function getWorkspaceId():String {
    var workspaceId:String = CatalogHelper.getInstance().getCommerceWorkspaceExpression().getValue();
    if (!isWorkspaceValid()) {
      return getErrorMessage(workspaceId);
    }
    return  workspaceId;
  }

  //noinspection JSMethodCanBeStatic
  private function getErrorMessage(workspaceId:String):String {
    var extractedWorkspaceId:String = workspaceId.substr(workspaceId.lastIndexOf('/') + 1, workspaceId.length);
    var errorMessage:String = StringUtil.format(resourceManager.getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'commerce_workspace_invalid'), extractedWorkspaceId);
    return errorMessage;
  }

  private function isWorkspaceValid():Boolean {
    var workspaceValid:Boolean = true;
    var workspaceId:String = CatalogHelper.getInstance().getCommerceWorkspaceExpression().getValue();
    if (workspaceId) {
      var workspaces:Array = getWorkspaces();
      if (workspaces) {
        workspaceValid = workspaces.some(function (workspace:Workspace):Boolean {
          return workspaceId === workspace.getId();
        });
      }
    }
    return workspaceValid;
  }

  //noinspection JSMethodCanBeStatic
  private function setWorkspaceId(workspaceId:String):void {
    var workspaceExpression:ValueExpression = CatalogHelper.getInstance().getCommerceWorkspaceExpression();
    if (workspaceId === NO_WORKSPACE) {
      // unset the workspace id if 'no-workspace' selected
      // remove the commerce.workspace struct entry
      var commerceStruct:Struct = workspaceExpression.getParent().getValue();
      if (commerceStruct) {
        commerceStruct.getType().removeProperty(CatalogHelper.COMMERCE_STRUCT_WORKSPACE);
      }
    } else {
      workspaceExpression.setValue(workspaceId);
    }
  }

  //noinspection JSMethodCanBeStatic
  private function getWorkspaces():Array {
    var workspaces:Array;
    var activeStore:Store = CatalogHelper.getInstance().getActiveStoreExpression().getValue();
    if (activeStore) {
      if (activeStore.getWorkspaces()) {
        workspaces = activeStore.getWorkspaces().getWorkspaces();
      }
    }
    return workspaces;
  }
}
}