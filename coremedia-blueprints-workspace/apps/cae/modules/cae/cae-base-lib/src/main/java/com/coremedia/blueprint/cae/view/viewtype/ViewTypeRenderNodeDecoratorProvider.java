package com.coremedia.blueprint.cae.view.viewtype;

import com.coremedia.objectserver.view.RenderNodeDecorator;
import com.coremedia.objectserver.view.RenderNodeDecoratorProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;

/**
 * {@link RenderNodeDecoratorProvider} implementation that provides a {@link ViewTypeRenderNodeDecorator}
 */
public class ViewTypeRenderNodeDecoratorProvider implements RenderNodeDecoratorProvider {

  private RenderNodeDecorator decorator;

  public void setDecorator(RenderNodeDecorator decorator) {
    this.decorator = decorator;
  }

  @Override
  public RenderNodeDecorator getDecorator(String viewName, Map model, Locale locale, HttpServletRequest request) {
    //viewName, locale and request not needed here
    return decorator;
  }
}
