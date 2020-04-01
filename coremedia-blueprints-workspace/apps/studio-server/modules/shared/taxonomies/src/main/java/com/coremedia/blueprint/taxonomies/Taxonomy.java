package com.coremedia.blueprint.taxonomies;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.List;

/**
 * To support a new taxonomy type, classes must implement this interface
 * and a bean definition has to be added to the web application's spring configuration.
 */
public interface Taxonomy<T> {

  /**
   * Returns the taxonomy id of the strategy, must be unique.
   *
   * @return id
   */
  @NonNull
  String getTaxonomyId();

  /**
   * For each taxonomy request the active siteId parameter is passed so that
   * the taxonomy tree can be build site depending.
   *
   * @return The name ofthe site.
   */
  String getSiteId();

  /**
   * Returns the root node of the taxonomy tree. Usually, this node is not displayed
   * like the folder of documents that contains all keyword items.
   *
   * @return The root node of the taxonomy strategy.
   */
  @NonNull
  TaxonomyNode getRoot();

  /**
   * The reference is the unique id of a node inside the tree. The method
   * looks up the node for the given ref.
   *
   * @param ref The reference of the node to find.
   * @return The node to find or null.
   */
  @Nullable
  TaxonomyNode getNodeByRef(String ref);


  /**
   * Returns the parent of the node with the given id
   *
   * @param ref The id of the node
   * @return The parent of the node.
   */
  @Nullable
  TaxonomyNode getParent(@NonNull String ref);

  /**
   * Returns the children of the given node.
   *
   * @param node   The node to retrieve the children from.
   * @param offset The offset if paging is used.
   * @param count  The page count if paging is used.
   * @return The node list wrapper that contains child nodes.
   */
  @NonNull
  TaxonomyNodeList getChildren(@NonNull TaxonomyNode node, int offset, int count);

  /**
   * The TaxonomyNodeList contains the full path of the node, including itself
   * and the taxonomy root node (origin) that isn't displayed usually.
   *
   * @param node The node to retrieve the path from.
   * @return The node that contains all nodes of the path.
   */
  @NonNull
  TaxonomyNode getPath(@NonNull TaxonomyNode node);

  /**
   * Looks up nodes for the given search text. This method is
   * used for the search text field of link lists and the taxonomy admin console.
   *
   * @param text The search text.
   * @return A list of hits that match the search text.
   */
  @NonNull
  TaxonomyNodeList find(@NonNull String text);

  /**
   * Deletes the given node from the taxonomy tree.
   *
   * @param toDelete The node to be deleted
   * @return The parent of the deleted node or null if the deletion was not possible (e.g. referrers)
   */
  @Nullable
  TaxonomyNode delete(@NonNull TaxonomyNode toDelete);

  /**
   * Creates a new child for the given parent.
   *
   * @param parent The parent to create the child for.
   * @param defaultName The default name to use for the new child.
   * @return The newly created node.
   */
  @NonNull
  TaxonomyNode createChild(@NonNull TaxonomyNode parent, @Nullable String defaultName);

  /**
   * Commits the changes that have been done on the node.
   *
   * @param node The node to store the new values for.
   * @return The updated node instance.
   */
  @NonNull
  TaxonomyNode commit(@NonNull TaxonomyNode node);

  /**
   * Moves the given taxonomy node to the children of the target node.
   *
   * @param node   The node to move.
   * @param target The target node that will be the new parent.
   * @return The move node with updated references.
   */
  @Nullable
  TaxonomyNode moveNode(@NonNull TaxonomyNode node, @NonNull TaxonomyNode target);

  /**
   * Returns a list of all available keywords for this taxonomy.
   *
   * @return list of all available keywords
   */
  @NonNull
  List<TaxonomyNode> getAllChildren();

  /**
   * Returns the document type used for keywords.
   *
   * @return document type
   */
  String getKeywordType();

  /**
   * Returns true if the taxonomy strategy is still valid (and not deleted).
   *
   * @return true if valid
   */
  boolean isValid();

  /**
   * Returns the list of all objects this tag is used in, except taxonomy documents
   * @param node the node to check the links for
   * @param recursive true to check the child nodes of the given node too
   * @return the list of items referencing the given node
   */
  @NonNull
  List<T> getLinks(TaxonomyNode node, boolean recursive);

  /**
   * Returns the list of items that are linking to the given node
   * and would block a deletion of it.
   * @param node the node to check
   * @return the list of items referencing the node
   */
  @NonNull
  List<T> getStrongLinks(@NonNull TaxonomyNode node, boolean recursive);

  /**
   * Returns true if the taxonomy can be modified by the current user.
   */
  default boolean isWriteable() {
    return true;
  }
}
