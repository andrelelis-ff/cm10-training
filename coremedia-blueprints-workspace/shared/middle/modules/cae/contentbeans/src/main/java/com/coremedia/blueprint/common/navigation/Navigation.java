package com.coremedia.blueprint.common.navigation;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTheme;
import com.coremedia.cae.aspect.provider.AspectsProvider;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.user.User;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;

/**
 * Common methods implemented by navigation nodes, like CMNavigation.
 * Navigation objects are Linkable too.
 *
 * @cm.template.api
 */
public interface Navigation extends Linkable {

  /**
   * Returns the children of this navigation object.
   * If no child exist, an empty list will be returned.
   *
   * @return a list of {@link Linkable} objects
   */
  List<? extends Linkable> getChildren();

  /**
   * Returns the parent of this navigation instance.
   */
  Navigation getParentNavigation();

  /**
   * Returns the theme of this navigation instance or its parent.
   *
   * @param developer Use the developer's variant rather than the production theme.
   * @return the theme of this navigation instance or its parent
   */
  CMTheme getTheme(@Nullable User developer);

  /**
   * Returns the root {@link Navigation} object (=Site) for this navigation item.
   * If {@link #isRoot()} is true, getRootNavigation returns this.
   *
   * @return the root {@link Navigation}
   */
  CMNavigation getRootNavigation();

  /**
   * Return the first navigation context within the parent hierarchy which is an instance of CMContext
   */
  CMContext getContext();


  /**
   * <p>
   * Use hidden in order to provide <i>nice</i> URLs for content
   * which should not be reachable by navigation.
   * </p>
   * <p>
   * Semantic: hidden implies hiddenInSitemap.
   * </p>
   *
   * @cm.template.api
   */
  boolean isHidden();

  /**
   * <p>
   * Returns the children which are visible in navigational contexts.
   * </p>
   * <p>
   * I.e. the same list as {@link #getChildren()} except {@link Navigation}
   * documents whose {@link Navigation#isHidden()} flag is true.
   * </p>
   *
   * @return the children which are visible in navigational contexts.
   * @cm.template.api
   */
  List<? extends Linkable> getVisibleChildren();

  /**
   * <p>
   * Returns the value of the document is hidden in sidemap.
   * </p>
   * <p>
   * Do not show this channel in a sitemap. We recommend to use this flag
   * in exceptional cases only, because a sitemap is not very helpful if it
   * differs too much from the actual navigation.
   * </p>
   */
  boolean isHiddenInSitemap();

  /**
   * <p>
   * Returns the children which are visible in sitemaps.
   * </p>
   * <p>
   * Reasonable implementations will delegate to {@link #getVisibleChildren()}
   * and possibly filter the result.  A list which is unrelated to
   * {@link #getVisibleChildren()} would be confusing.
   * </p>
   *
   * @return the children which are visible in sitemaps.
   */
  List<? extends Linkable> getSitemapChildren();

  /**
   * @return true if this navigation item has no parents.
   * @cm.template.api
   */
  boolean isRoot();

  //todo introduced to make Elastic Social Plugin work again - document or remove aspects
  AspectsProvider getAspectsProvider();


  /**
   * Returns the navigation path of this navigation from the {@link #getRootNavigation() root navigation}
   * to this navigation.
   *
   * @return the list of navigations forming the path to this Navigation including this.
   * @cm.template.api
   */
  List<? extends Linkable> getNavigationPathList();

  /**
   * Returns the tree relation for code resources lookup (themes, css, js).
   * <p>
   * In the Blueprint, code resources are declared by channels
   * (CMNavigation properties theme, css, javaScript).  Since code resources
   * may be inherited (for details see CodeResourcesCacheKey), we need a whole
   * tree relation for the lookup, rather than a single CMNavigation content.
   * In a plain Blueprint content world this tree relation will usually
   * match {@link #getParentNavigation()} and {@link #getChildren()}, whose
   * result beans have a 1:1 relation to content objects.
   * <p>
   * Other implementations of Navigation must provide an arbitrary content
   * based tree relation that enables us to lookup code resources to associate
   * with a Navigation instance.
   */
  TreeRelation<Content> getCodeResourcesTreeRelation();
}
