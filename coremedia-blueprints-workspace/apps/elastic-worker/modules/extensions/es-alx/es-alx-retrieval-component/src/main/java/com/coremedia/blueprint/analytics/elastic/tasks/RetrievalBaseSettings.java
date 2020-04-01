package com.coremedia.blueprint.analytics.elastic.tasks;

/**
 * Interface to be used with {@link com.coremedia.blueprint.base.analytics.elastic.util.SettingsUtil#createProxy(Class, java.util.Map)}
 */
interface RetrievalBaseSettings {

  /**
   * @see com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil#DOCUMENT_PROPERTY_MAX_LENGTH
   */
  int getMaxLength();

  /**
   * @see com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil#KEY_LIMIT
   */
  int getLimit();

  /**
   * @see com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil#KEY_INTERVAL
   */
  int getInterval();

}
