package com.coremedia.blueprint.studio.template {
import com.coremedia.blueprint.studio.template.model.ProcessingData;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.user.User;
import com.coremedia.cms.editor.sdk.components.StudioDialog;
import com.coremedia.cms.editor.sdk.components.folderprompt.FolderCreationResult;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.folderchooser.FolderChooserListView;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.util.ContentCreationUtil;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.cms.editor.sdk.util.StudioConfigurationUtil;
import com.coremedia.ui.components.StatefulTextField;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.mixins.IValidationStateMixin;
import com.coremedia.ui.mixins.ValidationState;

import ext.Ext;

import ext.StringUtil;

import mx.resources.ResourceManager;

/**
 * The base class of the create from template dialog creates.
 */
[ResourceBundle('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPlugin')]
[ResourceBundle('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings')]
[ResourceBundle('com.coremedia.blueprint.base.components.navigationlink.NavigationLinkField')]
public class CreateFromTemplateDialogBase extends StudioDialog {
  private var _disabledValueExpression:ValueExpression;
  private var _model:ProcessingData;
  private var _errorMessages:Object = {};
  private var _baseFolderEditorialVE:ValueExpression;
  private var _baseFolderNavigationVE:ValueExpression;
  private var _folderValueExpression:ValueExpression;
  private var _editorialFolderValueExpression:ValueExpression;
  private var _nameField:StatefulTextField;

  public static const NAME_FIELD_ID:String = 'nameField';
  public static const TEMPLATE_CHOOSER_FIELD_ID:String = "templateChooserField";
  public static const PARENT_PAGE_FIELD_ID:String = 'parentPageFieldId';
  public static const BASE_FOLDER_CHOOSER_ID:String = 'baseFolderChooser';
  public static const CONTENT_BASE_FOLDER_CHOOSER_ID:String = 'contentBaseFolderChooser';
  public static const EDITOR_CONTAINER_ITEM_ID:String = "editorContainer";

  public function CreateFromTemplateDialogBase(config:CreateFromTemplateDialog = null) {
    super(config);
    _errorMessages[NAME_FIELD_ID] = resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPlugin', 'name_not_valid_value');
    _errorMessages[TEMPLATE_CHOOSER_FIELD_ID] = resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPlugin', 'template_chooser_empty_text');
    _errorMessages[BASE_FOLDER_CHOOSER_ID] = resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPlugin', 'page_folder_combo_validation_message');
  }

  // The height is set when a studio user resize the window manually. Since the displayed tab can change while the window is hidden,
  // we need to reset the height when it is shown again. Otherwise the auto-resizing functionality will not work anymore.
  // The maxHeight has to be set explicit to ensure correct window scrolling for many list items.
  override protected function onShow(animateTarget:* = undefined, callback:Function = null, scope:Object = null):void {
    this.height = null;
    this.maxHeight = Ext.getBody().getHeight() - this.getHeader().getHeight();
    super.onShow(animateTarget,callback,scope);
  }

  override protected function afterRender():void {
    super.afterRender();

    _nameField = queryById(NAME_FIELD_ID) as StatefulTextField;
    _nameField.on('blur', validateForm);

    getModel().set(ProcessingData.FOLDER_PROPERTY, []);
    getModel().set(resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'editorial_folder_property'), []);

    getBaseFolderVE().loadValue(function (path:String):void {
      setBaseFolderInModel(path);
      getBaseFolderVE().addChangeListener(loadAndSetBaseFolder);
    });

    getContentBaseFolderVE().loadValue(function (path:String):void {
      setContentBaseFolderInModel(path);
      getContentBaseFolderVE().addChangeListener(loadAndSetContentBaseFolder);
    });

    validateForm();
  }

  private function loadAndSetBaseFolder():void {
    getBaseFolderVE().loadValue(function (path:String):void {
      setBaseFolderInModel(path);
    });
  }

  private function loadAndSetContentBaseFolder():void {
    getContentBaseFolderVE().loadValue(function (path:String):void {
      setContentBaseFolderInModel(path);
    });
  }

  private function setBaseFolderInModel(path:String):void {
    if (path) {
      var baseFolder:Content = editorContext.getSession().getConnection().getContentRepository().getChild(path);
      if (baseFolder) {
        getModel().set(ProcessingData.FOLDER_PROPERTY, [baseFolder]);
      }
    }
  }

  private function setContentBaseFolderInModel(path:String):void {
    if (path) {
      var contentBaseFolder:Content = editorContext.getSession().getConnection().getContentRepository().getChild(path);
      if (contentBaseFolder) {
        getModel().set(resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'editorial_folder_property'), [contentBaseFolder]);
      }
    }
  }

  protected function getContentType(contentypeName:String):ContentType {
    return editorContext.getSession().getConnection().getContentRepository().getContentType(contentypeName);
  }

  override protected function onDestroy():void {
    _model.removeValueChangeListener(validateForm);
    getBaseFolderVE().removeChangeListener(loadAndSetBaseFolder);
    getContentBaseFolderVE().removeChangeListener(loadAndSetContentBaseFolder);
    super.onDestroy();
  }

  /**
   * Creates the model that is used for this dialog.
   * @return
   */
  protected function getModel():ProcessingData {
    if (!_model) {
      _model = new ProcessingData();

      //pre-fill default values
      var site:Site = editorContext.getSitesService().getPreferredSite();
      if(site) {
        var root:Content = site.getSiteRootDocument();
        var property:String = resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'parent_property');
        _model.set(property, root);
      }

      _model.addValueChangeListener(validateForm);
    }
    return _model;
  }

  private function validateForm():void {
    getDisabledValueExpression().setValue(false);

    //we can use the field validator since after the field becomes invalid, no event is fired to correct the value of the bound value expression
    validateAsync(_nameField, nameValidator);
    validate(queryById(CONTENT_BASE_FOLDER_CHOOSER_ID), contentBaseFolderValidator);
    validate(queryById(BASE_FOLDER_CHOOSER_ID), baseFolderValidator);
    validate(queryById(TEMPLATE_CHOOSER_FIELD_ID));
  }

  private function validate(editor:*, validatorFunction:Function = null):void {
    if (editor) {
      validatorFunction = validatorFunction || editor.initialConfig['validate'];
      if (validatorFunction) {
        var result:Boolean = validatorFunction(editor);
        applyValidationResult(editor, result);
      }
    }
  }

  private function validateAsync(editor:*, validator:Function):void {
    if (editor) {
      if (validator) {
        validator.call(null, function (errorMessage:String):void {
          applyValidationResult(editor, !(errorMessage && errorMessage.length > 0), errorMessage);
        });
      }
    }
  }

  private function applyValidationResult(editor:*, result:Boolean, errorMessage:String = null):void {
    var statefulEditor:IValidationStateMixin = editor as IValidationStateMixin;
    var errorMsg:String = errorMessage ? errorMessage : _errorMessages[editor.itemId];
    if (!errorMsg) {
      errorMsg = resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPlugin', 'template_create_missing_value');
    }
    if (!result) {
      if (statefulEditor) {
        statefulEditor.validationState = ValidationState.ERROR;
        statefulEditor.validationMessage = errorMsg;
      }
      getDisabledValueExpression().setValue(true);
    } else {
      if (statefulEditor) {
        statefulEditor.validationState = null;
        statefulEditor.validationMessage = null;
      }
    }
  }

  /**
   * Invokes the post processing and closes the dialog
   */
  protected function handleSubmit():void {
    var data:ProcessingData = getModel();
    var folder:Content = data.get(ProcessingData.FOLDER_PROPERTY)[0];
    var path:String = data.getExtendedPath(folder);
    var parent:Content = data.get(resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'parent_property'));

    if (!parent) {
      MessageBoxUtil.showConfirmation(resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPlugin', 'text'),
              resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPlugin', 'no_parent_page_selected_warning'),
              resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPlugin', 'no_parent_page_selected_warning_buttonText'),
              function (buttonId:String):void {
                if (buttonId === "ok") {
                  doCreation(path);
                }
              });
    } else {
      parent.invalidate(function ():void {
        if (parent.isCheckedOutByOther()) {
          parent.getEditor().load(function (user:User):void {
            var msg:String = StringUtil.format(resourceManager.getString('com.coremedia.blueprint.base.components.navigationlink.NavigationLinkField', 'layout_error_msg'), user.getName());
            MessageBoxUtil.showError(resourceManager.getString('com.coremedia.blueprint.base.components.navigationlink.NavigationLinkField', 'layout_error'), msg);
          })
        } else {
          doCreation(path);
        }
      });
    }
  }


  /**
   * Performs the creation of the content
   * @param path the navigation path
   */
  private function doCreation(path:String):void {
    //first ensure that all folders exist
    var data:ProcessingData = getModel();

    var editorialFolder:Content = data.get(resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'editorial_folder_property'))[0];
    var editorialFolderName:String = data.getExtendedPath(editorialFolder);
    ContentCreationUtil.createRequiredSubfolders(path, function (result:FolderCreationResult):void {
      if (result.success) {
        var navigationFolder:Content = result.baseFolder;
        ContentCreationUtil.createRequiredSubfolders(editorialFolderName, function (editorialResult:FolderCreationResult):void {
          if (editorialResult.success) {
            destroy();

            //apply the folder instance to the processing data
            data.set(ProcessingData.FOLDER_PROPERTY, navigationFolder);
            CreateFromTemplateProcessor.process(data, function ():void {
              trace('INFO', 'Finished create from template');
              var content:Content = data.getContent();
              var initializer:Function = editorContext.lookupContentInitializer(content.getType());
              if (initializer) {
                initializer(content);
              }

              var parent:Content = data.get(resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'parent_property'));
              if (parent) {
                parent.invalidate(function ():void {
                  editorContext.getContentTabManager().openDocument(parent);
                  openNewPageInTab(data);
                });
              } else {
                openNewPageInTab(data);
              }
            });
          } else {
            MessageBoxUtil.showError(resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPlugin', 'text'),
                    resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPlugin', 'editor_folder_could_not_create_message'));
            editorialResult.remoteError.setHandled(true);
          }
        });
      } else {
        MessageBoxUtil.showError(resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPlugin', 'text'),
                resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPlugin', 'page_folder_could_not_create_message'));
        result.remoteError.setHandled(true);
      }
    });
  }

  private function openNewPageInTab(data:ProcessingData):void {
    var newPage:Content = data.getContent();
    newPage.invalidate(function ():void {
      editorContext.getContentTabManager().openDocument(newPage);
    });
  }

  protected function getDisabledValueExpression():ValueExpression {
    if (!_disabledValueExpression) {
      _disabledValueExpression = ValueExpressionFactory.create('disabled', beanFactory.createLocalBean());
      _disabledValueExpression.setValue(true);
    }
    return _disabledValueExpression;
  }

  protected function nameValidator(callback:Function):void {
    var repository:ContentRepository = SESSION.getConnection().getContentRepository();
    var name:String = getModel().get(ProcessingData.NAME_PROPERTY);
    if (!(name && repository.isValidName(getModel().get(ProcessingData.NAME_PROPERTY)))) {
      callback(_errorMessages[NAME_FIELD_ID]);
      return;
    }

    var folder:Content = getModel().get(ProcessingData.FOLDER_PROPERTY);
    if (folder is Array) {
      folder = folder[0];
    }
    if (folder && folder.getPath().length > 0) {
      var createFolderPath:String = folder.getPath() + "/" + name;
      SESSION.getConnection().getContentRepository().getChild(createFolderPath, function (c:Content):void {
        if (c) {
          callback(_errorMessages[BASE_FOLDER_CHOOSER_ID]);
        }
        else {
          callback("");
        }
      });
    } else {
      callback("");
    }
  }

  protected function templateChooserNonEmptyValidator():Boolean {
    var ve:ValueExpression = ValueExpressionFactory.create(
            resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'template_property'), getModel());
    return ve && ve.getValue() && (ve.getValue() as Array).length > 0;
  }

  protected function contentBaseFolderValidator(folderChooserListView:FolderChooserListView):Boolean {
    var folder:Content = getModel().get(resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'editorial_folder_property'));
    if (folder is Array) {
      folder = folder[0];
    }
    return folder && folder.getPath() && folder.getPath().length > 0;
  }

  protected function baseFolderValidator():Boolean {
    var folder:Content = getModel().get(ProcessingData.FOLDER_PROPERTY);
    if (folder is Array) {
      folder = folder[0];
    }
    return folder && folder.getPath() && folder.getPath().length > 0;
  }

  protected function getContentBaseFolderVE():ValueExpression {
    if (!_baseFolderEditorialVE) {
      _baseFolderEditorialVE = ValueExpressionFactory.createFromFunction(baseFolderEditorialCalculation);
    }
    return _baseFolderEditorialVE;
  }

  protected function getBaseFolderVE():ValueExpression {
    if (!_baseFolderNavigationVE) {
      _baseFolderNavigationVE = ValueExpressionFactory.createFromFunction(baseFolderNavigationCalculation);
    }
    return _baseFolderNavigationVE;
  }

  protected function getNavigationFolders():Array {
    var baseFolder:String = baseFolderNavigationCalculation();
    if (baseFolder) {
      return [baseFolder];
    }
    return [];
  }

  protected function getEditorialFolders():Array {
    var baseFolder:String = baseFolderEditorialCalculation();
    if (baseFolder) {
      return [baseFolder];
    }
    return [];
  }

  private function baseFolderNavigationCalculation():String {
    return baseFolderCalculation("paths.navigation", getNavigationFolderFallback);
  }

  private function baseFolderEditorialCalculation():String {
    return baseFolderCalculation("paths.editorial", getEditorialFolderFallback);
  }

  private function baseFolderCalculation(configuration:String, fallback:Function):String {
    var retPath:String = baseFolderCalculationRaw(configuration, fallback);
    if (retPath === undefined) {
      return undefined;
    }

    var diffSelectedParentPageAndNavigationPath:String = getDiffNavigationFolderParentFolder();
    if (diffSelectedParentPageAndNavigationPath === undefined) {
      return undefined;
    }

    if (diffSelectedParentPageAndNavigationPath) {
      retPath += "/" + diffSelectedParentPageAndNavigationPath;
    }

    return retPath;
  }

  private static function baseFolderCalculationRaw(configuration:String, fallback:Function):String {
    var folder:Content = StudioConfigurationUtil.getConfiguration("Content Creation", configuration);

    if (folder === undefined) {
      return undefined;
    }

    if (folder === null) {
      return fallback();
    } else {
      return folder.getPath();
    }
  }

  private function getDiffNavigationFolderParentFolder():String {
    var folderNavigation:String = baseFolderCalculationRaw("paths.navigation", getNavigationFolderFallback);
    if (folderNavigation === undefined) {
      return undefined;
    }
    var parent:Content = getModel().get(resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'parent_property'));
    if (!parent) {
      return null;
    }

    var parentFolder:Content = parent.getParent();

    if (parentFolder === undefined) {
      return undefined;
    }

    var parentFolderPath:String = parentFolder.getPath();
    if (parentFolderPath === undefined) {
      return undefined;
    }

    if (parentFolderPath.substr(0, folderNavigation.length) === folderNavigation) {
      return parentFolderPath.substr(folderNavigation.length + 1);
    } else {
      return null;
    }
  }

  protected static function getNavigationFolderFallback():String {
    return getFolderFallback(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'doctype'));
  }

  protected static function getEditorialFolderFallback():String {
    return getFolderFallback("CMArticle");
  }

  protected static function getFolderFallback(docType:String):String {
    var folder:Content = StudioConfigurationUtil.getConfiguration("Content Creation", "paths." + docType);
    if (folder) {
      return folder.getPath();
    }
    // fallback to preferred site's root folder if nothing is set in content creation
    var site:Site = editorContext.getSitesService().getPreferredSite();
    if (site && site.getSiteRootFolder()) {
      return site.getSiteRootFolder().getPath();
    }

    return folder === null ? null : undefined; // null or undefined
  }

  internal function getFolderValueExpression():ValueExpression {
    if (!_folderValueExpression) {
      _folderValueExpression = ValueExpressionFactory.create(ProcessingData.FOLDER_PROPERTY, getModel());
    }
    return _folderValueExpression;
  }

  internal function getEditorialFolderValueExpression():ValueExpression {
    if (!_editorialFolderValueExpression) {
      _editorialFolderValueExpression = ValueExpressionFactory.create(resourceManager.getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'editorial_folder_property'), getModel());
    }
    return _editorialFolderValueExpression;
  }

}
}
