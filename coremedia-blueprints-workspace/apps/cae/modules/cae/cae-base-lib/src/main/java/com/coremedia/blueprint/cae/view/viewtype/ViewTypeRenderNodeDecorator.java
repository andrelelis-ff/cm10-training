package com.coremedia.blueprint.cae.view.viewtype;

import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.HasViewTypeName;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.objectserver.view.RenderNode;
import com.coremedia.objectserver.view.RenderNodeDecorator;
import org.springframework.util.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A {@link RenderNodeDecorator} implementation that takes optional "view types" into account. Usually the view
 * with which a model should be rendered is determined by rendering logic. This allows for a maximum separation
 * of content and design and enables content re-use. In some cases there is content that should be displayed only
 * in a specific view. Editors can achieve this by linking content to a special CMViewtype content item.
 *
 * This {@link RenderNodeDecorator} combines the requested view name with an optional view type. A view "asTeaser"
 * for a content with view type "login" would become "asTeaser[login]".
 */
public class ViewTypeRenderNodeDecorator implements RenderNodeDecorator {

  private static final String VIEWTYPE_START = "[";
  private static final String VIEWTYPE_END = "]";

  public String decorateViewName(Object self, String viewName) {
    return translateViewName(self, viewName);
  }

  @NonNull
  @Override
  public RenderNode decorateRenderNode(Object self, String viewName) {
    //no bean decoration at the moment
    return new RenderNode(self, translateViewName(self, viewName));
  }

  private static String translateViewName(Object self, String viewName) {
    // create complete view name by appending view types
    Object viewTypeProvider = self instanceof Aspect ? ((Aspect) self).getAggregator() : self;
    if (viewTypeProvider instanceof Page) {
      Page page = (Page) viewTypeProvider;
      viewTypeProvider = page.getContent();
    }
    HasViewTypeName bean = (viewTypeProvider instanceof HasViewTypeName) ? (HasViewTypeName) viewTypeProvider : null;
    return createViewName(viewName, bean);
  }

  private static String createViewName(String viewName, HasViewTypeName self) {
    String result = viewName;
    String viewtype = getViewtypeString(self);
    if (StringUtils.hasLength(result) || StringUtils.hasLength(viewtype)) {
      StringBuilder v = new StringBuilder();
      if (result != null) {
        v.append(result);
      }
      if (viewtype != null && (result == null || !result.endsWith(VIEWTYPE_END))) {
        v.append(viewtype);
      }
      result = v.toString();
    }
    return result;
  }

  private static String getViewtypeString(HasViewTypeName self) {
    if (self == null || self.getViewTypeName() == null) {
      return null;
    }
    return VIEWTYPE_START + self.getViewTypeName() + VIEWTYPE_END;
  }
}
