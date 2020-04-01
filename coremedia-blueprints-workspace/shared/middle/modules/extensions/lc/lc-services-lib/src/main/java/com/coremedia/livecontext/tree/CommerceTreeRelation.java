package com.coremedia.livecontext.tree;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.livecontext.ecommerce.catalog.Category;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tree Relation based on an external category hierarchy.
 */
public class CommerceTreeRelation implements TreeRelation<Category> {

  @Override
  public Collection<Category> getChildrenOf(Category parent) {
    return parent.getChildren();
  }

  @Override
  public Category getParentOf(Category child) {
    return child.getParent();
  }

  @Override
  public Category getParentUnchecked(Category child) {
    return getParentOf(child);
  }

  @Override
  public List<Category> pathToRoot(Category child) {
    List<Category> result = new ArrayList<>();
    List<Category> breadcrumb = child.getBreadcrumb();
    if (!breadcrumb.isEmpty()) {
      Category rootCategory = breadcrumb.get(0).getParent();
      result.add(rootCategory);
      result.addAll(breadcrumb);
    }
    return result;
  }

  @Override
  public boolean isRoot(Category item) {
    return getParentOf(item) == null;
  }

  @Override
  public boolean isApplicable(Category item) {
    return true;
  }
}
