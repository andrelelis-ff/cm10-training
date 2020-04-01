package com.coremedia.blueprint.common.util;

import com.coremedia.cap.content.Content;
import com.google.common.base.Predicate;

public class IsInProductionPredicate implements Predicate<Object> {
  @Override
  public boolean apply(Object input) {
    if (input instanceof Content) {
      return ((Content)input).isInProduction();
    }

    return true;
  }
}
