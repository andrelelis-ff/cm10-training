package com.coremedia.livecontext.ecommerce.ibm.search;

import java.util.List;

public class WcSuggestionView {
  private List<WcSuggestion> entry;
  private String identifier;

  public List<WcSuggestion> getEntry() {
    return entry;
  }

  public void setEntry(List<WcSuggestion> entry) {
    this.entry = entry;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }
}
