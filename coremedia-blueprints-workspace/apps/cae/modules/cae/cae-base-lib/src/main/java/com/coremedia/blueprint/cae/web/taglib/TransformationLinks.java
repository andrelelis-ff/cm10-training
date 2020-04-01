package com.coremedia.blueprint.cae.web.taglib;

import java.util.Map;

/**
 * Combines the information about the transformation with the given "name" in a simple bean that can be easily
 * serialized into a JSON object. This resulting JSON object is processed by the JavaScript that handles the
 * responsive image handling (see "jquery.coremedia.responsiveimages.js").
 * Consequently every change to this data structure is likely to require changes to the JavaScript.
 */
public final class TransformationLinks {
  private final String name;
  private final Integer ratioWidth;
  private final Integer ratioHeight;
  private final Map<Integer, String> linksForWidth;

  public TransformationLinks(String name, Integer ratioWidth, Integer ratioHeight, Map<Integer, String> linksForWidth) {
    this.name = name;
    this.ratioWidth = ratioWidth;
    this.ratioHeight = ratioHeight;
    this.linksForWidth = linksForWidth;
  }

  public String getName() {
    return name;
  }

  public Integer getRatioWidth() {
    return ratioWidth;
  }

  public Integer getRatioHeight() {
    return ratioHeight;
  }

  public Map<Integer, String> getLinksForWidth() {
    return linksForWidth;
  }

}
