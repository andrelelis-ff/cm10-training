package com.coremedia.blueprint.personalization.include;

import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

@DefaultAnnotation(NonNull.class)
class P13NDynamicContainerCacheKey extends CacheKey<Boolean> {
  private final List items;

  P13NDynamicContainerCacheKey(List items) {
    this.items = items;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    P13NDynamicContainerCacheKey that = (P13NDynamicContainerCacheKey) obj;
    return this.items.equals(that.items);
  }

  @Override
  public int hashCode() {
    return items.hashCode();
  }

  @Override
  public Boolean evaluate(Cache cache) throws Exception {
    return P13NDynamicContainerStrategy.containsP13NItemRecursively(items);
  }
}
