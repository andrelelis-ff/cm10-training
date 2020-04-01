package com.coremedia.blueprint.common.util;

import com.coremedia.blueprint.common.util.pagination.PagingRuleType;
import com.coremedia.xml.Markup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public final class ParagraphHelper {
  private static final Logger LOG = LoggerFactory.getLogger(ParagraphHelper.class);

  public static final int DEFAULT_PARAGRAPH_PAGING_UNITS = 1;

  private ParagraphHelper() {
  }

  public static List<Markup> createParagraphs(Markup xml) {
    return ParagraphHelper.createParagraphs(xml, DEFAULT_PARAGRAPH_PAGING_UNITS);
  }

  public static List<Markup> createParagraphs(Markup xml, int pagingUnits) {
    return createParagraphs(xml, pagingUnits, PagingRuleType.DelimitingBlockCountRule);
  }

  public static List<Markup> createParagraphs(Markup xml, int pagingUnits, String pagingRuleTypeName) {
    if (pagingRuleTypeName == null || pagingRuleTypeName.equalsIgnoreCase("")) {
      return createParagraphs(xml, pagingUnits);
    }
    return createParagraphs(xml, pagingUnits, PagingRuleType.valueOf(pagingRuleTypeName));
  }

  public static List<Markup> createParagraphs(Markup xml, int pagingUnits, PagingRuleType pagingRuleType) {
    try {
      return pagingRuleType.createPaginator(pagingUnits).split(xml);
    } catch (Exception e) {
      LOG.error("Cannot create paragraphs", e);
      return Collections.emptyList();
    }
  }
}
