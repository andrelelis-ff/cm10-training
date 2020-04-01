package com.coremedia.catalog.studio.library {
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.results.BulkOperationResult;
import com.coremedia.cap.content.results.CopyResult;
import com.coremedia.cms.editor.sdk.util.ContentLocalizationUtil;
import com.coremedia.ui.logging.Logger;

import ext.MessageBox;
import ext.StringUtil;

import mx.resources.ResourceManager;

/**
 * Contains some catalog content actions and helper methods.
 */
[ResourceBundle('com.coremedia.catalog.studio.CatalogStudioPlugin')]
public class CatalogTreeRelationHelper {
  public static const PROPERTY_CONTEXTS:String = CatalogTreeRelation.PROPERTY_CONTEXTS;
  public static const PROPERTY_CHILDREN:String = CatalogTreeRelation.PROPERTY_CHILDREN;

  public static const CONTENT_TYPE_CATEGORY:String = CatalogTreeRelation.CONTENT_TYPE_CATEGORY;
  public static const CONTENT_TYPE_PRODUCT:String = CatalogTreeRelation.CONTENT_TYPE_PRODUCT;


  public static function showCheckoutError(target:Content):void {
    var docType:String = ContentLocalizationUtil.localizeDocumentTypeName(target.getType().getName());
    var msg:String = StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.catalog.studio.CatalogStudioPlugin', 'catalog_checkout_error_message'), docType, target.getName());
    MessageBox.alert(ResourceManager.getInstance().getString('com.coremedia.catalog.studio.CatalogStudioPlugin', 'catalog_checkout_error_title'), msg);
  }

  /**
   * Copies the products into the given category, updates the linking afterwards.
   * @param sources the products to copy
   * @param newParent the parent category
   * @param callback the callback called once the process is finished.
   */
  public static function copyAndLinkProducts(sources:Array, newParent:Content, callback:Function):void {
    var contentRepository:ContentRepository = SESSION.getConnection().getContentRepository();
    contentRepository.copyRecursivelyTo(sources, newParent.getParent(), function(result:CopyResult):void {
      if(result.successful) {
        for each(var item in result.results) {
          if(item.content is Content) {
            var newProduct:Content = item.content;
            newProduct.getProperties().set(PROPERTY_CONTEXTS, [newParent]);
            newProduct.checkIn();
          }
        }
        if(callback) {
          callback();
        }
      }
      else {
        Logger.error("Failed to copy source into parent folder '" + newParent.getName() + "': " + result.error.errorName);
      }
    });
  }

  /**
   * Validates if the list of given content can be modified.
   */
  public static function validateCheckoutState(contents:Array):Boolean {
    for each(var source:Content in contents) {
      if (source && source.isCheckedOutByOther()) {
        showCheckoutError(source);
        return false;
      }
    }
    return true;
  }

  /**
   * Filters the given contents for the given type
   * @param contents the contents to filter
   * @param type the type to filter for.
   */
  public static function filterForType(contents:Array, type:String):Array {
    var filtered:Array = [];
    for each(var content:Content in contents) {
      var typeName:String = content.getType().getName();
      if (typeName === type) {
        filtered.push(content);
      }
    }
    return filtered;
  }

  /**
   * Restores the original check-in/out state
   */
  public static function restoreCheckInOutState(checkedInContents:Array):void {
    for each(var c:Content in checkedInContents) {
      c.checkIn();
    }
  }

  /**
   * Stores all contents that are currently checked out
   */
  public static function storeCheckInOutState(contents:Array):Array {
    var checkedInContents:Array = [];
    for each(var parent:Content in contents) {
      if (!parent.isCheckedOut()) {
        checkedInContents.push(parent);
      }
    }
    return checkedInContents;
  }

  /**
   * Adds a category or product to the given category.
   * @param parentCategory the category to add the child to
   * @param child the child to add, may be a product or a category.
   * @return the content that has been modified to update the linking AND was not checked out before
   */
  public static function addCategoryChild(parentCategory:Content, child:Content):Content {
    var returnValue:Content = null;
    if (child.getType().isSubtypeOf(CONTENT_TYPE_CATEGORY)) {
      if(!parentCategory.isCheckedOut()) {
        returnValue = parentCategory;
      }
      var parentChildren:Array = (parentCategory.getProperties().get(PROPERTY_CHILDREN) as Array).slice();
      parentChildren.push(child);
      parentCategory.getProperties().set(PROPERTY_CHILDREN, parentChildren);
    }
    else if (child.getType().isSubtypeOf(CONTENT_TYPE_PRODUCT)) {
      var contexts:Array = (child.getProperties().get(PROPERTY_CONTEXTS) as Array).slice();
      if (contexts.indexOf(parentCategory) === -1) {
        if(!child.isCheckedOut()) {
          returnValue = child;
        }
        contexts.push(parentCategory);
      }
      child.getProperties().set(PROPERTY_CONTEXTS, contexts);
    }
    return returnValue;
  }

  /**
   * Removes a category or product to the given category.
   * @param parentCategory the category to remove the child from
   * @param child the child to remove, may be a product or a category.
   * @return the content that has been modified to update the linking AND was not checked out before
   */
  public static function removeCategoryChild(parentCategory:Content, child:Content):Content {
    var returnValue:Content = null;
    if (child.getType().isSubtypeOf(CONTENT_TYPE_CATEGORY)) {
      if(!parentCategory.isCheckedOut()) {
        returnValue = parentCategory;
      }
      var parentChildren:Array = (parentCategory.getProperties().get(PROPERTY_CHILDREN) as Array).slice();
      while(parentChildren.indexOf(child) !== -1) {
        parentChildren.splice(parentChildren.indexOf(child), 1);
      }
      parentCategory.getProperties().set(PROPERTY_CHILDREN, parentChildren);
    }
    else if (child.getType().isSubtypeOf(CONTENT_TYPE_PRODUCT)) {
      var contexts:Array = child.getProperties().get(PROPERTY_CONTEXTS).slice();
      if (contexts.indexOf(parentCategory) !== -1) {
        if(!child.isCheckedOut()) {
          returnValue = child;
        }
        contexts.splice(contexts.indexOf(parentCategory), 1);
      }
      child.getProperties().set(PROPERTY_CONTEXTS, contexts);
    }
    return returnValue;
  }

  /**
   * Updates the location of the dropped content. This ensures that products or categories that are moved
   * to another category are also moved to the corresponding folder.
   * This is mandatory to resolve the unique name problem.
   * @param sources the sources that have been dropped to a category
   * @param target the target categories the sources have been dropped to
   */
  public static function updateLocation(sources:Array, target:Content, callback:Function = undefined):void {
    var repository:ContentRepository = SESSION.getConnection().getContentRepository();
    repository.moveTo(sources, target.getParent(), function (result:BulkOperationResult):void {
      if (result.error) {
        Logger.error('Error copying products or categories to folder ' + target.getParent().getPath() + ": "
        + result.error.errorCode + "/" + result.error.errorName);
      }
      if (callback) {
        callback(result);
      }
    });
  }
}
}