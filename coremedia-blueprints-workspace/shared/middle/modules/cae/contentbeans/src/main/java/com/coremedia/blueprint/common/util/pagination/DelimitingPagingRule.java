package com.coremedia.blueprint.common.util.pagination;

interface DelimitingPagingRule {

  boolean matchesDelimiter(String localName, String classes);

  boolean matchesDelimiterTags(String localName);
}
