package com.coremedia.blueprint.studio.template {
import com.coremedia.blueprint.studio.template.model.PageTemplate;
import com.coremedia.blueprint.studio.template.model.ProcessingData;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentPropertyNames;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.results.BulkOperationResultItem;
import com.coremedia.cap.content.results.CopyResult;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.ContentCreationUtil;
import com.coremedia.ui.data.FlushResult;
import com.coremedia.ui.data.ValueExpressionFactory;

import mx.resources.ResourceManager;

/**
 * Post processor implementation for CMChannel instances.
 */
[ResourceBundle('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings')]
public class CreateFromTemplateProcessor {

  public static function process(data:ProcessingData, callback:Function):void {
    var template:Array = data.get(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'template_property'));

    convertToPageTemplate(template[0], function(pageTemplate:PageTemplate):void {
      data.set(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'template_property'), pageTemplate);
      if (pageTemplate) {
        copyTemplateFiles(data, function (contents:Array):void {
          initializeNewlyCreatedContents(contents, function ():void {
            renameTemplateChannel(data,function ():void {
              deleteTemplateSymbols(data,function ():void {
                movePageToNavigation(data,function (channel:Content):void {
                  linkToList(data, channel, function ():void {
                    callback.call(null);
                  });
                });
              });
            });
          });
        });
      }
      else {
        var folder:Content = data.getFolder();
        var ct:ContentType = SESSION.getConnection().getContentRepository().getContentType(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'doctype'));
        ContentCreationUtil.createContent(folder, true, false, data.getName(), ct, function(createdContent:Content):void {
          data.setContent(createdContent);
          callback.call(null);
        });
      }
    });
  }

  private static function convertToPageTemplate(templateSymbol:Content, callback:Function):void {
    var folder:Content = templateSymbol.getParent();
    folder.load(function():void{
      var pageTemplate:PageTemplate = new PageTemplate(folder, templateSymbol);
      var templateChannelDocType:ContentType = SESSION.getConnection().getContentRepository().getContentType(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'doctype'));

      var children:Array = folder.getChildDocuments();
      var count:int = children.length;
      for (var i:int = 0; i < children.length; i++) {
        var child:Content = children[i];
        child.load(function (loadedChild:Content):void {
          count--;
          var type:ContentType = loadedChild.getType();
          if (loadedChild.getId() !== pageTemplate.getDescriptor().getId()) {
            if (type.isSubtypeOf(templateChannelDocType)) {
              pageTemplate.setPage(loadedChild);
            }
          }
          if(count === 0) {
            callback.call(null, pageTemplate);
          }
        });
      }
    });
  }

  private static function initializeNewlyCreatedContents(contents:Array, callback:Function):void {
    if (!contents) {
      callback.call(null);
      return;
    }
    contents.forEach(function (content:Content):void {
      var initializer:Function = editorContext.lookupContentInitializer(content.getType());
      if (initializer) {
        initializer(content);
      }
    });

    callback.call(null);
  }

  /**
   * Renames the template channel document after is has been copied to the new
   * editorial folder. The channel is copied afterwards...
   * @param data
   * @param callback
   */
  private static function renameTemplateChannel(data: ProcessingData,
                                                callback:Function):void {
    var initializer:PageTemplate = data.get(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'template_property'));
    var folder:Content = data.get(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'editorial_folder_property'));
    folder.getChild(initializer.getPage().getName(), function (copiedChannel:Content):void {
      copiedChannel.rename(data.getName(), function ():void {
        trace('INFO', 'Renamed template channel "' + initializer.getPage().getName() + '" to "' + data.getName() + '"');
        callback.call(null);
      });
    });
  }

  /**
   * Deletes the symbol document from the copied template folder.
   * @param data
   * @param callback
   */
  private static function deleteTemplateSymbols(data:ProcessingData,
                                                callback:Function):void {
    var targetEditorialFolder:Content = data.get(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'editorial_folder_property'));
    targetEditorialFolder.invalidate(function():void {
      var children:Array = targetEditorialFolder.getChildDocuments();
      var callbackCount:int = children.length;
      var symbols:Array = [];
      for(var i:int = 0; i<children.length; i++) {
        var child:Content = children[i];
        child.load(function(c:Content):void {
          callbackCount--;
          if(c.getName().indexOf(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'template_descriptor_name')) === 0
                  && c.getType().getName() === ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'template_descriptor_type')) {
            symbols.push(c);
          }
          if(callbackCount === 0) {
            deleteDescriptors(symbols, targetEditorialFolder, callback);
          }
        });
      }
      if(children.length === 0) {
        callback.call(null);
      }
    });
  }

  private static function deleteDescriptors(symbols:Array, targetEditorialFolder:Content, callback:Function):void {
    trace('INFO', 'Deleting ' + symbols.length + ' descriptors');
    var callbackCount:int = symbols.length;
    for(var i:int = 0; i<symbols.length; i++) {
      symbols[i].doDelete(function ():void {
        trace('INFO', 'Deleted template descriptor from new editorial folder');
        callbackCount--;
        if(callbackCount === 0) {
          targetEditorialFolder.invalidate(function ():void {
            callback.call(null);
          });
        }
      });
    }
    if(symbols.length === 0) {
      callback.call(null);
    }
  }

  /**
   * Moves the copied channel document from the editorial folder to the navigation/selected folder.
   * @param data
   * @param callback
   */
  private static function movePageToNavigation(data:ProcessingData, callback:Function):void {
    //resolved target name and folder
    var name:String = data.getName();
    var sourceFolder:Content = data.getFolder();
    var targetEditorialFolder:Content = data.get(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'editorial_folder_property'));
    targetEditorialFolder.getChild(name, function (channel:Content):void {
      channel.moveTo(sourceFolder, function (result:FlushResult):void {
        var movedChannel:Content = result.remoteBean as Content;
        movedChannel.invalidate(function():void {
          var channelName:String = movedChannel.getName();
          movedChannel.getProperties().set("title", channelName);
          movedChannel.getProperties().set("segment", channelName);
          movedChannel.flush();

          ValueExpressionFactory.create(ContentPropertyNames.PATH, movedChannel).loadValue(function (path:String):void {
            trace('INFO', 'Moved "' + channel.getPath() + '" to "' + path + '" (' + movedChannel + ')');
            data.setContent(movedChannel);

            //reload folder and invoke callback
            var editorialFolder:Content = data.get(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'editorial_folder_property'));
            //invalidate editorial folder to show that document has been removed and...
            editorialFolder.invalidate(function ():void {
              //...invalidate the target folder to show the moved document.
              sourceFolder.invalidate(function ():void {
                callback.call(null, movedChannel);
              });

            });
          });
        });
      });
    });
  }

  /**
   * Copies the files of the template to the corresponding editorial folder.
   * @param data
   * @param callback
   */
  private static function copyTemplateFiles(data: ProcessingData, callback:Function):void {
    var initializer:PageTemplate = data.get(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'template_property'));
    //resolve the target folder for the template files
    var folder:Content = data.get(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'editorial_folder_property'))[0];
    var folderName:String = data.getExtendedPath(folder);
    //create the target folder...
    trace('INFO', 'Copying template files to new editorial folder "' + folderName + '"');
    SESSION.getConnection().getContentRepository().getChild(folderName, function (folder:Content):void {
      data.set(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'editorial_folder_property'), folder);
      //...and copy the files.
      var toBeCopied:Array = initializer.getFolder().getChildren();
      loadContents(toBeCopied, function ():void {
        toBeCopied = toBeCopied.filter(notCMSymbol);
        SESSION.getConnection().getContentRepository().copyRecursivelyTo(toBeCopied, folder, function (result:CopyResult):void {
          if (result.successful) {
            var contents:Array = result.results.map(toContent);
            callback(contents);
          }
          else {
            trace('[WARN]', 'Template copy failed: ' + result.error.errorName);
            callback.call(null);
          }
        });
      });
    });
  }

  private static function toContent(resultItem:BulkOperationResultItem): Content {
    return resultItem.content;
  }


  /**
   * Ensures that all items of the given content array are loaded.
   * The method is used to skip asynchronous calls afterwards.
   * @param items An array filled with content items.
   * @param callback Called when all items have been loaded.
   */
  private static function loadContents(items:Array, callback:Function):void {
    if(!items || items.length === 0) {
      callback.call(null);
    }
    var index:int = items.length;
    for(var i:int =0; i<items.length; i++) {
      var c:Content = items[i];
      c.load(function():void {
        index--;
        if(index === 0) {
          callback.call(null);
        }
      });
    }
  }

  /**
   * Links the newly created channel to the navigation hierarchy.
   */
  private static function linkToList(data:ProcessingData,
                                     channel:Content,
                                     callback:Function):void {
    var parentContent:Content = data.get(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'parent_property'));
    if (parentContent) {
      parentContent.load(function ():void {
        var children:Array = parentContent.getProperties().get(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'children_property'));
        if (!children) {
          children = [];
        }
        if (children.indexOf(channel) === -1) { //maybe the dialog is linking too.
          children = children.concat(channel);
          parentContent.getProperties().set(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'children_property'), children);
          data.addAdditionalContent(parentContent);
        }
        callback.call(null);
      });
    }
    else {
      callback.call(null);
    }
  }

  /**
   * Filter symbols out of the list of files to be copied.
   * @param loadedContent the folder content
   * @return true if the given content is not a CMSymbol
   */
  private static function notCMSymbol(loadedContent:Content):Boolean {
    return loadedContent.getType() && !loadedContent.getType().isSubtypeOf(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.template.CreateFromTemplateStudioPluginSettings', 'template_descriptor_type'));
  }
}
}
