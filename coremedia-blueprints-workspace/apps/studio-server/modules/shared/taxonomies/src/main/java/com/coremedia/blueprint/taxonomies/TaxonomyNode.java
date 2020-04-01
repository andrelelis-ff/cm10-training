package com.coremedia.blueprint.taxonomies;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Iterator;

/**
 * A representation object for a taxonomy nodes.
 */
public class TaxonomyNode {

  private TaxonomyNodeList path;
  private String name;
  private String ref;
  private String type;
  private boolean selectable = true;
  private boolean leaf;
  private boolean extendable = true;
  private boolean root = false;
  private String taxonomyId;
  private int level;
  private float weight = -1;
  private String siteId;

  public float getWeight() {
    return weight;
  }

  /**
   * Sets the suggestions weight if node is used in suggestion list.
   *
   * @param weight
   */
  public void setWeight(float weight) {
    this.weight = weight;
  }

  /**
   * Every taxonomy node has a name, which is shown in the taxonomy chooser and editor.
   * The names may be localized by client side resource bundles (for the root nodes).
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * A taxonomy node may reference entities like documents (content id) in the content repository or entries
   * in another database.
   * For the Studio UI this reference is not of interest, but the TaxonomyStrategies rely on it.
   *
   * @return reference
   */
  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }


  /**
   * A taxonomy node might represent an object of a specific 'type' like: 'Country', 'State', 'City', 'Street'. These
   * types might be rendered differently in the frontend.
   *
   * @return type
   */
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  /**
   * A taxonomy node might be selectable (or choosable) in in the taxonomy chooser.
   *
   * @return true if selectable
   */
  public Boolean isSelectable() {
    return selectable;
  }

  public void setSelectable(boolean selectable) {
    this.selectable = selectable;
  }

  /**
   * this flag indicates, that a node has child nodes.
   *
   * @return true if leaf
   */
  public Boolean isLeaf() {
    return leaf;
  }

  public void setLeaf(boolean leaf) {
    this.leaf = leaf;
  }

  /**
   * this flag indicates, that the taxonomy editor may add children to this node. If false this node is leaf-only.
   *
   * @return true if extendable
   */
  public boolean isExtendable() {
    return extendable;
  }

  public void setExtendable(boolean extendable) {
    this.extendable = extendable;
  }

  /**
   * Indicates that this node represents the root of a taxonomy tree, not a taxonomy node in this taxonomy.
   * In most cases - but not necessarily - root nodes do not represent entities and are not selectable.
   *
   * @return true if root
   */
  public boolean isRoot() {
    return root;
  }

  public void setRoot(boolean root) {
    this.root = root;
  }

  /**
   * this property is used to find the TaxonomyStrategy for a given node. *
   */
  public String getTaxonomyId() {
    return taxonomyId;
  }

  public void setTaxonomyId(String taxonomy) {
    this.taxonomyId = taxonomy;
  }

  /**
   * the level (depth) of this node in the taxonomies tree.
   * <p/>
   * the root nodes have level 0, the first level nodes in every taxonomy therefore have level 1.
   *
   * @return depth in treee
   */
  public int getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public String getSiteId() {
    return siteId;
  }

  /**
   * Sets the path as node list for this node.
   *
   * @param path
   */
  public void setPath(TaxonomyNodeList path) {
    this.path = path;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public TaxonomyNodeList getPath() {
    return path;
  }


  /**
   * The plain string path, slash separated.
   *
   * @return path as string
   */
  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public String getPathString() {
    if (path == null) {
      //will be ignored during serialization
      return null;
    }
    StringBuilder builder = new StringBuilder();
    Iterator<TaxonomyNode> it = path.getNodes().iterator();
    int i = 0;
    while (it.hasNext()) {
      TaxonomyNode node = it.next();
      if (i == path.getNodes().size() - 1) {
        builder.append(node.getName());
      } else {
        builder.append(node.getName());
        builder.append("/");
      }
      i++;
    }
    return builder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TaxonomyNode that = (TaxonomyNode) o;

    if (getRef() != null ? !getRef().equals(that.getRef()) : that.getRef() != null) {
      return false;
    }
    if (getTaxonomyId() != null ? !getTaxonomyId().equals(that.getTaxonomyId()) : that.getTaxonomyId() != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = getRef() != null ? getRef().hashCode() : 0;
    result = 31 * result + (getTaxonomyId() != null ? getTaxonomyId().hashCode() : 0);
    return result;
  }
}
