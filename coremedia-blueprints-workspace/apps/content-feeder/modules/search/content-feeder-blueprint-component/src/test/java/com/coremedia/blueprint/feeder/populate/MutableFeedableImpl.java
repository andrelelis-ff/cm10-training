package com.coremedia.blueprint.feeder.populate;

import com.coremedia.cap.common.Blob;
import com.coremedia.cap.feeder.FeedableAspect;
import com.coremedia.cap.feeder.FeedableElement;
import com.coremedia.cap.feeder.MutableFeedable;
import com.coremedia.xml.Markup;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

class MutableFeedableImpl implements MutableFeedable {

  @Override
  public void setBlobElement(String s, Blob blob) {}

  @Override
  public void setBlobElement(String s, Blob blob, Map<String, ?> stringMap) {}

  @Override
  public void setDateElement(String s, Calendar calendar) {}

  @Override
  public void setDateElement(String s, Calendar calendar, Map<String, ?> stringMap) {}

  @Override
  public void setDateElement(String s, Date date) {}

  @Override
  public void setDateElement(String s, Date date, Map<String, ?> stringMap) {}

  @Override
  public void setDateElement(String s, long l) {}

  @Override
  public void setDateElement(String s, long l, Map<String, ?> stringMap) {}

  @Override
  public void setMarkupElement(String s, Markup markup) {}

  @Override
  public void setMarkupElement(String s, Markup markup, Map<String, ?> stringMap) {}

  @Override
  public void setNumberElement(String s, Number number) {}

  @Override
  public void setNumberElement(String s, Number number, Map<String, ?> stringMap) {}

  @Override
  public void setStringElement(String s, String s1) {}

  @Override
  public void setStringElement(String s, String s1, Map<String, ?> stringMap) {}

  @Override
  public void setElement(String s, Object o) {}

  @Override
  public void setElement(String s, Object o, Map<String, ?> stringMap) {}

  @Override
  public void setElement(FeedableElement feedableElement) {}

  @Override
  public FeedableElement removeElement(String s) {
    return null;
  }

  @Override
  public void setUpdatedAspects(@NonNull Iterable<? extends FeedableAspect> aspects) {
  }

  @Override
  public void setUpdatedAspect(@Nullable FeedableAspect aspects) {
  }

  @Override
  public void setTriggers(@NonNull Iterable<?> triggers) {
  }

  @Override
  public void setTrigger(@Nullable Object trigger) {
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public boolean isPartialUpdate() {
    return false;
  }

  @NonNull
  @Override
  public Set<FeedableAspect> getUpdatedAspects() {
    return Collections.emptySet();
  }

  @Override
  public boolean hasElement(String s) {
    return false;
  }

  @Override
  public FeedableElement getElement(String s) {
    return null;
  }

  @Override
  public Collection<String> getElementNames() {
    return Collections.emptySet();
  }

  @Override
  public Collection<FeedableElement> getElements() {
    return Collections.emptySet();
  }

  @NonNull
  @Override
  public Collection<Object> getTriggers() {
    return Collections.emptySet();
  }
}
