package com.coremedia.blueprint.studio.taxonomy.rendering {
import com.coremedia.ui.models.bem.BEMBlock;
import com.coremedia.ui.models.bem.BEMElement;
import com.coremedia.ui.models.bem.BEMModifier;

public class TaxonomyBEMEntities {

  public static const NODE_WRAP:BEMBlock = new BEMBlock("cm-taxonomy-node-wrap");

  public static const NODE_BLOCK:BEMBlock = new BEMBlock("cm-taxonomy-node");
  public static const NODE_ELEMENT_BOX:BEMElement = NODE_BLOCK.createElement("box");
  public static const NODE_ELEMENT_NAME:BEMElement = NODE_BLOCK.createElement("name");
  public static const NODE_ELEMENT_CONTROL:BEMElement = NODE_BLOCK.createElement("control");
  public static const NODE_ELEMENT_LINK:BEMElement = NODE_BLOCK.createElement("link");
  public static const NODE_MODIFIER_ARROW:BEMModifier = NODE_BLOCK.createModifier("has-children");
  public static const NODE_MODIFIER_ELLIPSIS:BEMModifier = NODE_BLOCK.createModifier("ellipsis");
  public static const NODE_MODIFIER_LEAF:BEMModifier = NODE_BLOCK.createModifier("leaf");
}
}
