package com.coremedia.blueprint.cae.view;

import com.coremedia.objectserver.view.RenderNode;
import com.coremedia.objectserver.view.RenderNodeDecorator;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;

/**
 * Decorators are called for each RenderNode (=combination of Bean and View) encountered when rendering a response in the CAE.
 */
public class DynamicIncludeRenderNodeDecorator implements RenderNodeDecorator {

  private List<DynamicIncludePredicate> predicates;

  public void setPredicates(List<DynamicIncludePredicate> predicates) {
    this.predicates = predicates;
  }

  @NonNull
  @Override
  public RenderNode decorateRenderNode(Object self, String viewName) {
    RenderNode renderNode = new RenderNode(self, viewName);

    //make Dynamic Include if any predicate matches
    for (DynamicIncludePredicate predicate : predicates) {
      if (predicate.apply(renderNode)) {
        if (predicate instanceof DynamicIncludeProvider) {
          DynamicIncludeProvider dynamicIncludeProvider = (DynamicIncludeProvider) predicate;
          renderNode.setBean(dynamicIncludeProvider.getDynamicInclude(self, viewName));
        } else {
          renderNode.setBean(new DynamicInclude(self, viewName));
        }
        renderNode.setView(null);
        break;
      }
    }

    return renderNode;
  }
}
