package com.coremedia.blueprint.taxonomies.semantic;

import com.coremedia.cap.content.Content;

/**
 * Wrapper model for a content suggestions
 */
public class Suggestion implements Comparable<Suggestion> {
  private Content content;
  private float weight;

  public Suggestion(Content content, float weight) {
    this.content = content;
    this.weight = weight;
  }

  public float getWeight() {
    return weight;
  }

  public Content getContent() {
    return content;
  }

  @Override
  public int compareTo(Suggestion o) {
    if (weight < 0) {
      return getId().compareTo(o.getId());
    }

    if (o.getWeight() > weight) {
      return 1;
    }
    return -1;
  }

  public String getId() {
    return content.getId();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Suggestion that = (Suggestion) o;

    if (Float.compare(that.weight, weight) != 0) {
      return false;
    }

    if (content != null ? !content.equals(that.content) : that.content != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = content != null ? content.hashCode() : 0;
    result = 31 * result + Float.floatToIntBits(weight);
    return result;
  }
}
