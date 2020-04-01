package com.coremedia.blueprint.studio {
import com.coremedia.blueprint.base.components.navigationlink.NavigationLinkFieldWrapper;
import com.coremedia.cms.editor.sdk.quickcreate.QuickCreate;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentPropertyNames;
import com.coremedia.cms.editor.sdk.quickcreate.processing.ProcessingData;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.Component;

import mx.resources.ResourceManager;

/**
 * Extension, the enhances the QuickCreateDialog for CMChannel with the parent navigation editor
 */
[ResourceBundle('com.coremedia.cms.editor.sdk.quickcreate.QuickCreate')]
public class CMChannelExtension {

  //content and custom properties for quick create dialog
  public static const PARENT_PROPERTY:String = "parentChannel";
  public static const CHILDREN_PROPERTY:String = "children";

  public static const CONTENT_TYPE_PAGE:String = "CMChannel";

  public static function register(contentType:String):void {
    /**
     * Apply custom properties for CMChannel
     */
    QuickCreate.addQuickCreateDialogProperty(contentType, PARENT_PROPERTY,
            function (data:ProcessingData, properties:Object):Component {
              return createComponent(data, properties, contentType)
            });

    QuickCreate.addSuccessHandler(contentType, process);
  }

  /**
   * Creates the UI Component for the Quick Creation Dialog
   * @param data the Data Process Object
   * @param properties The properties of the bound object
   * @param contentType The contentType of the document to be created
   * @return the UI Component
   */
  private static function createComponent(data:ProcessingData, properties:Object, contentType:String):Component {
    var c:Content = null;
    if (properties.bindTo) {
      c = properties.bindTo.getValue();
    }
    if (c && c.getType().isSubtypeOf(contentType)) {
      data.set(PARENT_PROPERTY, c);
      ValueExpressionFactory.create(ContentPropertyNames.PATH, c.getParent()).loadValue(function (path:String):void {
        data.set(ProcessingData.FOLDER_PROPERTY, path);
      });
    }
    properties.label = ResourceManager.getInstance().getString('com.coremedia.cms.editor.sdk.quickcreate.QuickCreate', 'parent_label');
    properties.doctype = contentType;
    return new NavigationLinkFieldWrapper(NavigationLinkFieldWrapper(properties));
  }

  /**
   * Adds a hook for processing the creation of CMChannel
   * @param content the content to created
   * @param data the processing data with varios informations
   * @param callback the function to call after processing
   */
  private static function process(content:Content, data:ProcessingData, callback:Function):void {

    //parent property is read from a link list, so resolve value from array
    var parentContent:Content = data.get(PARENT_PROPERTY);
    content.getProperties().set('title',content.getName());

    if(parentContent) {
      linkToList(parentContent, content, CHILDREN_PROPERTY, data, function():void {
        callback.call(null);
      });
    }
    else {
      callback.call(null);
    }
  }

  private static function linkToList(parentContent:Content, content:Content, property:String, data:ProcessingData, callback:Function):void {
    if(parentContent) {
      parentContent.load(function():void {
        var children:Array = parentContent.getProperties().get(property);
        if(!children) {
          children = [];
        }
        if(children.indexOf(content) === -1) { //maybe the dialog is linking too.
          children = children.concat(content);
          parentContent.getProperties().set(property, children);
          data.addAdditionalContent(parentContent);
        }
        callback.call(null);
      });
    }
    else {
      callback.call(null);
    }
  }

}
}
