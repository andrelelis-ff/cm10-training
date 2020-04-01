package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.ecommerce.studio.rest.model.ChildRepresentation;
import com.coremedia.livecontext.ecommerce.p13n.MarketingSpot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Marketing representation for JSON.
 */
public class MarketingRepresentation extends AbstractCatalogRepresentation {

  private List<MarketingSpot> marketingSpots = Collections.emptyList();

  public List<MarketingSpot> getMarketingSpots() {
    return marketingSpots;
  }

  public void setMarketingSpots(List<MarketingSpot> marketingSpots) {
    this.marketingSpots = RepresentationHelper.sort(marketingSpots);
  }

  public Map<String, ChildRepresentation> getChildrenByName() {
    Map<String, ChildRepresentation> result = new LinkedHashMap<>();
    for (MarketingSpot child : marketingSpots) {
      ChildRepresentation childRepresentation = new ChildRepresentation();
      childRepresentation.setChild(child);
      childRepresentation.setDisplayName(child.getExternalId());
      result.put(CommerceIdFormatterHelper.format(child.getId()), childRepresentation);
    }
    return RepresentationHelper.sortChildren(result);
  }

}
