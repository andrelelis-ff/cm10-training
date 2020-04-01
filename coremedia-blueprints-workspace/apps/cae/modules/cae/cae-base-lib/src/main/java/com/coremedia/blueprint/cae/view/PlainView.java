package com.coremedia.blueprint.cae.view;

import com.coremedia.xml.MarkupUtil;
import com.coremedia.objectserver.view.TextView;
import com.coremedia.xml.Markup;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;

/**
 * Programmed view that renders a given {@link Markup} as plain text
 */
public class PlainView implements TextView {

  @Override
  public void render(Object bean, String view, Writer writer, @NonNull HttpServletRequest request,
                     @NonNull HttpServletResponse response) {
    if (!(bean instanceof Markup)) {
      throw new IllegalArgumentException(bean + " is no " + Markup.class);
    }

    Markup markup = (Markup) bean;
    MarkupUtil.asPlain(markup, writer);
  }
}
