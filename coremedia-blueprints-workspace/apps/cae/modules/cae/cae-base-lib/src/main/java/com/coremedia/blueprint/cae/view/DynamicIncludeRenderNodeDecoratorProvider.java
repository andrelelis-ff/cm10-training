package com.coremedia.blueprint.cae.view;

import com.coremedia.objectserver.view.RenderNode;
import com.coremedia.objectserver.view.RenderNodeDecorator;
import com.coremedia.objectserver.view.RenderNodeDecoratorProvider;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The DecoratorProvider is called for the root bean / the servlet view of a request.
 */
public class DynamicIncludeRenderNodeDecoratorProvider implements RenderNodeDecoratorProvider {

  private RenderNodeDecorator decorator;
  private List<DynamicIncludePredicate> predicates;

  @Required
  public void setPredicates(List<DynamicIncludePredicate> predicates) {
    this.predicates = predicates;
  }

  @Required
  public void setDecorator(RenderNodeDecorator decorator) {
    this.decorator = decorator;
  }

  @Override
  public RenderNodeDecorator getDecorator(String viewName, Map model, Locale locale, HttpServletRequest request) {

    Object self = model.get("self");
    RenderNode renderNode = new RenderNode(self,viewName);

    for (DynamicIncludePredicate predicate : predicates) {
      // Do not make dynamic includes for root beans. This needs to be done to avoid recursions.
      // Otherwise the same fragment could be loaded via ajax over and over again.
      // This check ensures that the upcoming dynamic render node is at least one level below the current render node
      // in the render node hierarchy.
      if (predicate.apply(renderNode)) {
        return null;
      }
    }

    return decorator;
  }
}
