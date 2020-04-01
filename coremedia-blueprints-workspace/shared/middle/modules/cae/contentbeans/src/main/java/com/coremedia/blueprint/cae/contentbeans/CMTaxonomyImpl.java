package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.cap.content.Content;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Extension class for immutable beans of document type "CMTaxonomy".
 */
public class CMTaxonomyImpl extends CMTaxonomyBase {

  private TreeRelation<Content> treeRelation;

  // --- configuration ----------------------------------------------

  @Required
  public void setTreeRelation(TreeRelation<Content> treeRelation) {
    this.treeRelation = treeRelation;
  }

  public TreeRelation<Content> getTreeRelation() {
    return treeRelation;
  }

  // --- features ---------------------------------------------------

  @Override
  public CMTaxonomy getParent() {
    return createBeanFor(treeRelation.getParentOf(this.getContent()), CMTaxonomy.class);
  }

  @Override
  public List<? extends CMTaxonomy> getTaxonomyPathList() {
    return createBeansFor(treeRelation.pathToRoot(this.getContent()), CMTaxonomy.class);
  }

  /**
   * Returns the taxonomy's value as teaserTitle.
   */
  @Override
  public String getTeaserTitle() {
    String tt = super.getTeaserTitle();
    if (tt == null || tt.trim().length() == 0) {
      tt = getValue();
    }
    return tt;
  }
}
