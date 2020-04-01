package com.coremedia.blueprint.cae.searchsuggestion;

/**
 * A model of a search term suggestion containing the suggested term and the number of results for this term.
 */
public class Suggestion implements Comparable<Suggestion> {

  private String suggestTerm = "";
  private String originalTerm = "";
  private Long count = (long) 0;

  public Suggestion(String suggestTerm, String originalTerm, Long count) {
    this.suggestTerm = suggestTerm;
    this.count = count;
    this.originalTerm = originalTerm;
  }

  // create field label like "Label (Hits)"
  public String getLabel() {
    return suggestTerm + " (" + count + ")";
  }

  public String getValue() {
    return suggestTerm;
  }

  @Override
  public int compareTo(Suggestion o) {
    // descending order
    return o.count.compareTo(count);
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

    if (!count.equals(that.count)) {
      return false;
    }
    if (!suggestTerm.equals(that.suggestTerm)) {
      return false;
    }
    if (!originalTerm.equals(that.originalTerm)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = suggestTerm.hashCode();
    result = 31 * result + originalTerm.hashCode();
    result = 31 * result + count.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return suggestTerm;
  }
}
