package com.coremedia.ecommerce.studio.rest;

import com.coremedia.livecontext.ecommerce.p13n.Segment;

import java.util.Collections;
import java.util.List;

/**
 * Segments representation for JSON.
 */
public class SegmentsRepresentation extends AbstractCatalogRepresentation {

  private List<Segment> segments = Collections.emptyList();

  public List<Segment> getSegments() {
    return segments;
  }

  public void setSegments(List<Segment> segments) {
    this.segments = segments;
  }
}
