package com.coremedia.blueprint.cae.navigation;

import com.coremedia.blueprint.base.tree.NavigationLinkListContentTreeRelation;
import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.List;

/**
 * Just a delegation implementation to make the existing NavigationLinkListContentTreeRelation class
 * applicable for Linkable/Navigation instances.
 */
public class CMNavigationLinkListContentTreeRelation implements TreeRelation<Linkable> {

  private NavigationLinkListContentTreeRelation treeRelation;
  private ContentBeanFactory contentBeanFactory;


  // --- construct and configure ------------------------------------

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  @Required
  public void setTreeRelation(NavigationLinkListContentTreeRelation treeRelation) {
    this.treeRelation = treeRelation;
  }

  @Override
  public Collection<Linkable> getChildrenOf(Linkable parent) {
    Content content = ((CMNavigation) parent).getContent();
    Collection<Content> childrenOf = treeRelation.getChildrenOf(content);
    return contentBeanFactory.createBeansFor(childrenOf, Linkable.class);
  }

  @Override
  public Linkable getParentOf(Linkable child) {
    Content parentOf = treeRelation.getParentOf(((CMLinkable) child).getContent());
    return contentBeanFactory.createBeanFor(parentOf, Linkable.class);
  }

  @Override
  public Linkable getParentUnchecked(Linkable child) {
    Content parentUnchecked = treeRelation.getParentUnchecked(((CMLinkable) child).getContent());
    return contentBeanFactory.createBeanFor(parentUnchecked, Linkable.class);
  }

  @Override
  public List<Linkable> pathToRoot(Linkable child) {
    List<Content> contents = treeRelation.pathToRoot(((CMLinkable) child).getContent());
    return contentBeanFactory.createBeansFor(contents, Linkable.class);
  }

  @Override
  public boolean isRoot(Linkable item) {
    return treeRelation.isRoot(((CMLinkable) item).getContent());
  }

  @Override
  public boolean isApplicable(Linkable linkable) {
    return linkable instanceof CMNavigation;
  }
}
