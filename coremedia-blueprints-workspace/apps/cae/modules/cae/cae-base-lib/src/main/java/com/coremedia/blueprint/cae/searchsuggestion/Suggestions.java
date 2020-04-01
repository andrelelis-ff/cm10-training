package com.coremedia.blueprint.cae.searchsuggestion;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;

import java.util.List;

public class Suggestions extends ForwardingList<Suggestion> {

  private List<Suggestion> delegate = Lists.newArrayList();

  @Override
  public List<Suggestion> delegate() {
    return delegate;
  }


}
