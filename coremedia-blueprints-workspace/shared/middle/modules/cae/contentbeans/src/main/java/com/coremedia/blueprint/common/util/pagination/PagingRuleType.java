package com.coremedia.blueprint.common.util.pagination;

/**
 * Some factories for paginators.
 */
public enum PagingRuleType {
  DelimitingBlockCountRule(StAXBlockElementPaginator.class, DelimitingPagingPerBlockCountRule.class),
  BlockCountRule(StAXBlockElementPaginator.class, PagingPerBlockCountRule.class),
  CharactersCountAndNextParagraphRule(StAXBlockElementPaginator.class, PagingPerCharactersCountAndNextParagraphRule.class),
  CharactersCountAndNextBlockRule(StAXBlockElementPaginator.class, PagingPerCharactersCountAndNextBlockRule.class),
  CharactersCountAndNextWordRule(StAXPlainTextPaginator.class, PagingPerCharactersCountAndNextWordRule.class);

  private Class paginatorClazz;
  private Class paginationRuleClazz;

  PagingRuleType(Class paginatorClazz, Class paginationRuleClazz) {
    this.paginatorClazz = paginatorClazz;
    this.paginationRuleClazz = paginationRuleClazz;
  }

  /**
   * Create a new Paginator with the given rule.
   */
  public Paginator createPaginator(int pagingUnits) throws IllegalAccessException, InstantiationException {
    Paginator paginator = (Paginator) paginatorClazz.newInstance();
    PagingRule pagingRule = createPagingRule(pagingUnits);
    paginator.setPagingRule(pagingRule);
    return paginator;
  }

  private PagingRule createPagingRule(int pagingUnits) throws InstantiationException, IllegalAccessException {
    PagingRule pagingRule = (PagingRule) paginationRuleClazz.newInstance();
    pagingRule.setPagingUnitsNumber(pagingUnits);
    return pagingRule;
  }
}
