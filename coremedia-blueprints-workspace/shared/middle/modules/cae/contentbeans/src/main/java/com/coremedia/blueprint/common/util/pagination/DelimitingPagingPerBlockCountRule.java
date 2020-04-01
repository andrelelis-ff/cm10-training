package com.coremedia.blueprint.common.util.pagination;

import org.springframework.util.Assert;

import java.util.Stack;
import java.util.StringTokenizer;


class DelimitingPagingPerBlockCountRule implements PagingRule, DelimitingPagingRule {
  private static final int DEFAULT_MAX_BLOCKS_PER_PAGE = 5;
  private static final String PAGE_SEPERATOR_TAGS = "p table pre blockquote ul ol";
  private static final String DELIMITER_TAGS = "p";
  private static final String DELIMITER_CLASSES = "p--heading-4 p--heading-3 p--heading-2 p--heading-1";

  private Paginator paginator;
  private int maxBlocksPerPage = DEFAULT_MAX_BLOCKS_PER_PAGE;

  private String delimiterTags = DELIMITER_TAGS;
  private String delimiterClasses = DELIMITER_CLASSES;

  private String[] classTokens;
  private boolean ignoreDelimiterClasses = false;

  @Override
  public void setPaginator(Paginator pagingFilter) {
    this.paginator = pagingFilter;
  }

  @Override
  public void setPagingUnitsNumber(int pagingUnitsNumber) {
    maxBlocksPerPage = pagingUnitsNumber;
  }


  @Override
  public int getPagingUnitsNumber() {
    return maxBlocksPerPage;
  }

  @Override
  public boolean match(String localName) {
    if (paginator==null) {
      throw new IllegalStateException("Must set a paginator before using match");
    }
    Assert.notNull(localName);
    return (PAGE_SEPERATOR_TAGS.contains(localName)) && (paginator.getBlockCounter() >= maxBlocksPerPage);
  }

  @Override
  public boolean matchesDelimiter(String localName, String classAttr) {
    return this.matchesDelimiterTags(localName) && (this.isIgnoreDelimiterClasses() || this.hasIntersectingClasses(classAttr));
  }

  @Override
  public boolean matchesDelimiterTags(String localName) {
    return (null != this.delimiterTags) && this.delimiterTags.contains(localName);
  }

  public boolean hasIntersectingClasses(String classes) {

    String[] delims = this.getClassTokens();
    if (delims == null || delims.length == 0 || classes == null) {
      return false;
    }

    StringTokenizer tknzr = new StringTokenizer(classes, " ", false);

    while (tknzr.hasMoreTokens()) {
      String token = tknzr.nextToken();
      for (String delim1 : delims) {
        if (delim1.contentEquals(token)) {
          return true;
        }
      }
    }

    return false;
  }

  public String getDelimiterTags() {
    return delimiterTags;
  }

  public void setDelimiterTags(String tags) {
    this.delimiterTags = tags;
  }

  public String getDelimiterClasses() {
    return delimiterClasses;
  }

  public void setDelimiterClasses(String delimiterClasses) {
    this.delimiterClasses = delimiterClasses;
    this.setClassTokens(new String[]{});
  }

  public boolean isIgnoreDelimiterClasses() {
    return ignoreDelimiterClasses;
  }

  public void setIgnoreDelimiterClasses(boolean ignoreDelimiterClasses) {
    this.ignoreDelimiterClasses = ignoreDelimiterClasses;
  }

  public String[] getClassTokens() {

    if (classTokens == null) {
      String delims = getDelimiterClasses();

      if (delims == null) {
        return new String[0];
      }

      StringTokenizer tknzr = new StringTokenizer(delims, " ", false);
      Stack<String> stack = new Stack<>();

      while (tknzr.hasMoreTokens()) {
        String tkn = tknzr.nextToken();
        stack.add(tkn);
      }

      classTokens = new String[stack.size()];
      classTokens = stack.toArray(classTokens);
    }

    return classTokens.clone();
  }

  protected void setClassTokens(String[] classTokens) {
    this.classTokens = classTokens.clone();
  }
}
