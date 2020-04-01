package com.coremedia.catalog.studio.library {
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentCreateResult;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.publication.PublicationService;
import com.coremedia.cms.editor.sdk.ContentTreeRelation;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtilInternal;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ui.logging.Logger;

import ext.StringUtil;
import ext.window.MessageBoxWindow;

import mx.resources.ResourceManager;

/**
 * Intercepts the new content creation and updates the calculateDisabled/hidden state for catalog document types.
 */
[ResourceBundle('com.coremedia.cms.editor.EditorErrors')]
public class CatalogTreeRelation implements ContentTreeRelation {

  public static const PROPERTY_CONTEXTS:String = "contexts";
  public static const PROPERTY_CHILDREN:String = "children";

  public static const CONTENT_TYPE_CATEGORY:String = CatalogHelper.CONTENT_TYPE_CM_CATEGORY;
  public static const CONTENT_TYPE_PRODUCT:String = CatalogHelper.CONTENT_TYPE_CM_PRODUCT;

  private var linkListContainers:Object;
  private var linkListParents:Object;

  public function CatalogTreeRelation() {
    linkListContainers = {
      "CMCategory": PROPERTY_CHILDREN
    };
    linkListParents = {
      "CMHasContexts": PROPERTY_CONTEXTS
    };
  }

  public function folderNodeType():String {
    return CONTENT_TYPE_CATEGORY;
  }

  public function leafNodeType():String {
    return CONTENT_TYPE_PRODUCT;
  }

  /**
   * @return true if the given content is of the given type, undefined if something is not loaded yet, false otherwise.
   */
  private static function typeIs(content:Content, typeName:String):Boolean {
    var contentType:ContentType = content.getType();
    if (contentType === undefined) {
      return undefined;
    }
    return contentType.getName() && contentType.getName() === typeName;
  }

  public function isLeafNode(content:Content):Boolean {
    return typeIs(content, CONTENT_TYPE_PRODUCT);
  }

  public function isFolderNode(content:Content):Boolean {
    return typeIs(content, CONTENT_TYPE_CATEGORY);
  }


  public function getParent(content:Content):Content {
    var parents:Array = getParents(content);
    if (parents === undefined) {
      return undefined;
    }
    if (parents && parents.length > 0) {
      return parents[0];
    }
    return null;
  }

  public function getParents(content:Content):Array {
    var linkListContainerType:String = getLinkListContainerType(content);
    if (linkListContainerType === undefined) {
      return undefined;
    }
    if (linkListContainerType) {
      var parentCategories:Array = content.getReferrersWithNamedDescriptor(linkListContainerType,
              linkListContainers[linkListContainerType]);
      if (parentCategories === undefined) {
        return undefined;
      }
      return parentCategories || null;
    }
    var linkListParentType:String = getLinkListParentType(content);
    if (linkListParentType === undefined) {
      return undefined;
    }
    if (linkListParentType) {
      var contexts:Array = content.getProperties().get(linkListParents[linkListParentType]);
      if (contexts === undefined) {
        return undefined;
      }
      return contexts;
    }
    return null;
  }

  private static function lookupType(typeMapping:Object, content:Content):String {
    var contentType:ContentType = content.getType();
    if (contentType === undefined) {
      return undefined;
    }
    for (var typeName:String in typeMapping) {
      if (contentType.isSubtypeOf(typeName)) {
        return typeName;
      }
    }
    return null;
  }

  private function getLinkListContainerType(content:Content):String {
    return lookupType(linkListContainers, content);
  }

  private function getLinkListParentType(content:Content):String {
    return lookupType(linkListParents, content);
  }

  public function getSubFolders(content:Content):Array {
    var linkListContainerType:String = getLinkListContainerType(content);
    switch (linkListContainerType) {
      case undefined:
        return undefined;
      case null:
        return [];
    }
    return content.getProperties().get(linkListContainers[linkListContainerType]);
  }

  public function getLeafContent(content:Content):Array {
    //get the children of the selected category
    var filtered:Array = [];
    var linkingChildren:Array = content.getReferrersWithNamedDescriptor(CONTENT_TYPE_PRODUCT, PROPERTY_CONTEXTS);
    if (linkingChildren === undefined) {
      return undefined;
    }
    for each(var child:Content in linkingChildren) {
      if (child.isInProduction()) {
        filtered.push(child);
      }
    }
    return filtered;
  }

  public function mayMove(contents:Array, newParent:Content):Boolean {
    if (contents.some(function (content:Content):Boolean {
              return getParent(content) === null;
            })) {
      return false;    // must not move root
    }
    if (!newParent) {
      return true;
    }
    if (newParent.isFolder() || newParent.getType().getName() !== CONTENT_TYPE_CATEGORY) {
      return false;
    }
    for each(var source:Content in contents) {
      var typeName:String = source.getType().getName();
      if (typeName !== CONTENT_TYPE_PRODUCT && typeName !== CONTENT_TYPE_CATEGORY) {
        return false;
      }

      //check drop on itself
      if (IdHelper.parseContentId(source) === IdHelper.parseContentId(newParent)) {
        return false;
      }

      // check for root node and paste into same parent
      var parent:Content = getParent(source);
      if (!parent || IdHelper.parseContentId(parent) === IdHelper.parseContentId(newParent)) {
        return false;
      }

      //check paste into children
      var targetParent:Content = getParent(newParent);
      while (targetParent) {
        if (IdHelper.parseContentId(targetParent) === IdHelper.parseContentId(source)) {
          return false;
        }
        targetParent = getParent(targetParent);
      }
    }
    return true;
  }

  public function move(sources:Array, newParent:Content, callback:Function = undefined):void {
    if (mayMove(sources, newParent)) {
      var command:CatalogMove = new CatalogMove(this, sources, newParent, callback);
      command.execute();
    }
  }

  public function mayCreate(folder:Content, contentType:ContentType):Boolean {
    if(contentType.getName() === undefined) {
      return undefined;
    }
    if(folder.getType().getName() === undefined) {
      return undefined;
    }

    var isCatalogType:Boolean = contentType.getName() === CONTENT_TYPE_CATEGORY || contentType.getName() === CONTENT_TYPE_PRODUCT;
    return folder.getType().getName() === CONTENT_TYPE_CATEGORY && isCatalogType;
  }

  public function mayCopy(sources:Array, newParent:Content):Boolean {
    //TODO category copy currently not allowed!
    for each(var source:Content in sources) {
      if (source.getType().isSubtypeOf(CONTENT_TYPE_CATEGORY)) {
        return false;
      }
    }

    return mayMove(sources, newParent);
  }

  public function copy(sources:Array, newParent:Content, callback:Function = undefined):void {
    var command:CatalogCopy = new CatalogCopy(this, sources, newParent, callback);
    command.execute();
  }

  /**
   * @param contents the contents to delete, products or categories.
   * @param callback optional callback
   */
  public function deleteContents(contents:Array, callback:Function = undefined):void {
    var command:CatalogDelete = new CatalogDelete(this, contents, callback);
    command.execute();
  }


  public function undeleteContents(contents:Array, callback:Function = undefined):void {
    var command:CatalogUndelete = new CatalogUndelete(this, contents, callback);
    command.execute();
  }

  public function mayDelete(contents:Array):Boolean {
    for each(var source:Content in contents) {
      if (!source.isInProduction()) {
        return false;
      }

      var typeName:String = source.getType().getName();
      //products can always be deleted or at least unlinked if there is more than one parent
      if (typeName !== CONTENT_TYPE_PRODUCT && typeName !== CONTENT_TYPE_CATEGORY) {
        return false;
      }

      if (this.getParents(source) === undefined) {
        return false;
      }

      //check if the content must be unlinked first
      if (getParents(source).length > 1) {
        return false;
      }

      //well, for categories we have to check if there are remaining products in it
      if (typeName === CONTENT_TYPE_CATEGORY) {
        var leafContent:Array = getLeafContent(source);
        var children:Array = getSubFolders(source);
        if (leafContent) {
          children = children.concat(leafContent);
        }
        for each(var child:Content in children) {
          if (child.isInProduction()) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public function addChildNeedsFolderCheckout(folder:Content, childType:String):Boolean {
    return childType === CONTENT_TYPE_CATEGORY;
  }


  public function provideRepositoryFolderFor(contentType:ContentType, folderNode:Content, childNodeName:String, callback:Function):void {
    var repository:ContentRepository = SESSION.getConnection().getContentRepository();
    if (contentType.isSubtypeOf(CONTENT_TYPE_CATEGORY)) {
      repository.getFolderContentType().create(folderNode.getParent(), childNodeName, function (result:ContentCreateResult):void {
        if (result.error) {
          Logger.error('Error creating folder "' + childNodeName + '"in ' + folderNode.getParent().getPath() + ": "
          + result.error.errorCode + "/" + result.error.errorName);
        }
        if (result.createdContent) {
          callback(result.createdContent);
        }
      });
    }
    else if (contentType.isSubtypeOf(CONTENT_TYPE_PRODUCT)) {
      callback(folderNode.getParent());
    }
  }

  public function rename(content:Content, newName:String, callback:Function = null):void {
    content.getParent().getParent().getChild(newName, function (duplicateChild:Content, absolutePath:String = null):void {
      if (duplicateChild === null) {
        if (isFolderNode(content)) {
          content.getParent().rename(newName);
        }
        content.rename(newName, callback);
      }
      else {
        MessageBoxUtilInternal.show(ResourceManager.getInstance().getString('com.coremedia.cms.editor.EditorErrors', 'duplicateName_title'),
                StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.cms.editor.EditorErrors', 'duplicateName_message'), content.getName()),
                MessageBoxWindow.ERROR, MessageBoxWindow.OK, callback, true, true);
      }
    });
  }

  /**
   * Shows an error dialog about checked out content. Since we are working on content documents, not folders
   * we have to take extra care about the lifecycle state when documents are copied or moved.
   * @param target the target is that already checked out by another user.
   */
  public function showCheckoutError(target:Content):void {
    CatalogTreeRelationHelper.showCheckoutError(target);
  }

  public function addChildNodes(treeParent:Content, sources:Array, callback:Function):void {
    if (CatalogTreeRelationHelper.validateCheckoutState(sources.concat(treeParent))) {
      var modifications:Array = [];
      //update linking
      for each (var source:Content in sources) {
        modifications.push(CatalogTreeRelationHelper.addCategoryChild(treeParent, source));
      }

      //update checkin state
      for each(var modified:Content in modifications) {
        if (modified) {
          modified.invalidate(function (refreshContent:Content):void {
            if (refreshContent.isCheckedOut()) {
              refreshContent.checkIn();
            }
          });
        }
      }

      callback();
    }
  }

  public function withdraw(contents:Array, publicationService:PublicationService, callback:Function):void {
    var repository:ContentRepository = SESSION.getConnection().getContentRepository();
    repository.getPublicationService().withdrawAllFromTree(contents, CONTENT_TYPE_CATEGORY, PROPERTY_CHILDREN, callback);
  }

  public function showInTree(contents:Array, view:String = null, treeModelId:String = null):void {
    new ShowInCatalogTreeHelper(contents).showItems(treeModelId);
  }

}
}
