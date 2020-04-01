package com.coremedia.ecommerce.studio.tree {
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ui.data.AbstractTreeRelation;

internal class CategoryTreeRelationImpl extends AbstractTreeRelation {

  override public function getChildrenOf(node:Object):Array {
    var category:Category = node as Category;
    if (!category) {
      return undefined;
    } else {
      return category.getSubCategories();
    }
  }

  override public function getParentUnchecked(node:Object):Object {
    if (node is CatalogObject) {
      //when parent (for category and product variant) ...
      if (node.getParent) {
        //... then take it.
        return node.getParent();
        //when otherwise there is category (for product)...
      } else if (node.getCategory) {
        //... then take the category.
        return node.getCategory();
      }
    } else {
      return undefined;
    }
  }
}
}