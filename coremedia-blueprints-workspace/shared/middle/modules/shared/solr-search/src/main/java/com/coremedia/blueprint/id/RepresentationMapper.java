package com.coremedia.blueprint.id;

import com.coremedia.id.IdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/*
 * Convenience wrapper. Simplification of IdProvider requested in DENALI-1393
 */

public abstract class RepresentationMapper<T> implements Representation<T> {
  private static final Logger LOG = LoggerFactory.getLogger(RepresentationMapper.class);
  private IdProvider idProvider;

  @Override
  public boolean isValid(T bean) {
    // using com.coremedia.id.IdProvider.UnknownId not possible because of runtime error
    return bean != null && !(bean instanceof com.coremedia.id.IdProvider.UnknownId);
  }

  @Override
  public final T fromID(String id) {
    T result = null;
    try {
      result = (T) idProvider.parseId(id);
    } catch (IllegalArgumentException e) {
      // Probably errors occured with old solr documents that are not resolvable anymore with the IdProvider
      // Of course in production there should not be any searchable documents
      // that are not in the CMS anymore but in this case we just forget about them
      // instead of throwing Exceptions that crash the current search request continue.
      LOG.info("Parsing bean with id {} failed: {}", id, e.getMessage());
    }
    return result;
  }

  @Override
  public final String toID(T bean) {
    String id = null;
    try {
      id = idProvider.getId(bean);
    } catch (IllegalArgumentException e) {
      // Probably errors occured with old solr documents that are not resolvable anymore with the IdProvider
      // Of course in production there should not be any searchable documents
      // that are not in the CMS anymore but in this case we just forget about them
      // instead of throwing Exceptions that crash the current search request continue.
      LOG.info("Retrieving id from bean {} failed: {}", bean, e.getMessage());
    }
    return id;
  }

  @Required
  public void setIdProvider(IdProvider idProvider) {
    this.idProvider = idProvider;
  }
}
