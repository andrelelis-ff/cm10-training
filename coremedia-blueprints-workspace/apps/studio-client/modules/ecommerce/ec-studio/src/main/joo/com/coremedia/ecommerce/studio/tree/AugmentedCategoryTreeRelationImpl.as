package com.coremedia.ecommerce.studio.tree {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ecommerce.studio.augmentation.augmentationService;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Product;
import com.coremedia.ecommerce.studio.model.ProductVariant;
import com.coremedia.ui.data.AbstractTreeRelation;

internal class AugmentedCategoryTreeRelationImpl extends AbstractTreeRelation {

  override public function getChildrenOf(node:Object):Array {
    //TODO: currently this method is not used by page grid which is interested only in the parent relation.
    return null;
  }

  override public function getParentUnchecked(node:Object):Object {
    //first check if the node is a catalog object
    if (node is CatalogObject) {
      return getParentUncheckedForCatalogObject(node as CatalogObject);
    }

    //now check if the node is a content
    if (node is Content) {
      return getParentUncheckedForContent(node as Content);
    }
  }

  private function getParentUncheckedForContent(content:Content) {
    //we need to check if the content is a site root document...
    var siteId:String = editorContext.getSitesService().getSiteIdFor(content);
    if (siteId === undefined) {
      return undefined;
    } else if (siteId === null) {
      return null;
    }

    var siteRootDocument:Content = editorContext.getSitesService().getSiteRootDocument(siteId);
    //...if so the parent is null which means the parent hierarchy ends here.
    if (content === siteRootDocument) {
      return null;
    }

    var catalogObject:CatalogObject = augmentationService.getCatalogObject(content);

    if (!catalogObject) {
      return catalogObject;
    }

    var product:Product = catalogObject as Product;
    if (product) {
      var productCategory:Category = product.getCategory();
      if (!productCategory) {
        return productCategory;
      }
      return getParentUnchecked(productCategory);
    }

    var category:Category = Category(categoryTreeRelation.getParentUnchecked(catalogObject));
    if (category === null) {
      //this is a root category.
      //we define the parent of the root category document as the site root document.
      //TODO: this is not clean as site root document is strictly not augmenting a category.
      return siteRootDocument;
    } else if (category) {
      var augmentedCategory:Content = augmentationService.getContent(category);
      if (augmentedCategory === undefined) {
        return undefined;
      }
      if (augmentedCategory === null) {
        return getParentUnchecked(category);
      }
      if (augmentedCategory) {
        return augmentedCategory;
      }
    }
  }

  private function getParentUncheckedForCatalogObject(catalogObject:CatalogObject) {
    var augmentingContent:Content = augmentationService.getContent(catalogObject);
    //if the catalog object is augmented...
    if (augmentingContent) {
      //then it's it.
      return augmentingContent;
    } else {
      //otherwise continue with the parent
      return getParentUnchecked(categoryTreeRelation.getParentUnchecked(catalogObject));
    }
  }
}
}