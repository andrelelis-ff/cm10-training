package com.coremedia.blueprint.taxonomies;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A representation object for a list of taxonomy nodes.
 */
public class TaxonomyNodeList {

  private List<TaxonomyNode> nodes;

  public TaxonomyNodeList() {
    nodes = new ArrayList<>();
  }

  public TaxonomyNodeList(List<TaxonomyNode> nodes) {
    super();
    setNodes(nodes);
  }

  public void sortByName() {
    Collections.sort(nodes, new TaxonomyNodeComparator());
  }

  public List<TaxonomyNode> getNodes() {
    return nodes;
  }

  public final void setNodes(List<TaxonomyNode> nodes) {
    this.nodes = nodes;
  }


  public int getSize() {
    return this.nodes.size();
  }

  public boolean contains(TaxonomyNode node) {
    return nodes.contains(node);
  }

  private static class TaxonomyNodeComparator implements Comparator<TaxonomyNode>, Serializable {

    private static final long serialVersionUID = 5667734265087336585L;

    @Override
    public int compare(TaxonomyNode o1, TaxonomyNode o2) {
      Collator collator = Collator.getInstance();
      return collator.compare(o1.getName(), o2.getName());
    }
  }
}
