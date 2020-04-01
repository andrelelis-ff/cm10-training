package com.coremedia.blueprint.common.util;

import com.coremedia.cap.common.XmlGrammar;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import com.coremedia.xml.MarkupUtil;
import com.google.common.annotations.VisibleForTesting;

import edu.umd.cs.findbugs.annotations.Nullable;

public class RichtextHelper {
  private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

  @VisibleForTesting
  static final Markup EMPTY_RICHTEXT = MarkupFactory.fromString(XML_HEADER + "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\"/>").withGrammar(XmlGrammar.RICH_TEXT_1_0_NAME);

  // static utility class
  private RichtextHelper() {
  }


  // --- features ---------------------------------------------------

  /**
   * Prepare the given richtext for output in HTML.
   * <p>
   * The result should not be written back into the content repository,
   * otherwise you might lose information or end up in ping pong with other
   * writing clients.
   */
  @Nullable
  public static Markup htmlOptimizedRichtext(@Nullable Markup richtext) {
    if (richtext == null) {
      return null;
    }
    String grammar = richtext.getGrammar();
    if (grammar != null && !XmlGrammar.RICH_TEXT_1_0_NAME.equals(grammar)) {
      throw new IllegalArgumentException("Cannot handle Markup of grammar \"" + grammar + "\"");
    }

    // Check if the richtext is logically empty, and eventually omit any empty
    // elements (e.g. div, span, p) and whitespaces that tend to break the
    // intended HTML layout.
    if (MarkupUtil.isEmptyRichtext(richtext, true)) {
      return EMPTY_RICHTEXT;
    }

    // ... more HTML related richtext tweaking may follow here.

    // Nothing (more) to do.
    return richtext;
  }
}
