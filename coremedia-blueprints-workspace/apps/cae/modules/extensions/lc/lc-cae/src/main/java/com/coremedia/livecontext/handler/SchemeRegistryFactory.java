package com.coremedia.livecontext.handler;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.springframework.beans.factory.FactoryBean;

import java.util.Collections;
import java.util.List;

/**
 * Spring factory for a {@link org.apache.http.conn.scheme.SchemeRegistry}.
 */
public class SchemeRegistryFactory implements FactoryBean<SchemeRegistry> {

  private List<Scheme> schemes = Collections.emptyList();

  public void setSchemes(List<Scheme> schemes) {
    this.schemes = schemes != null ? schemes : Collections.<Scheme>emptyList();
  }

  @Override
  public SchemeRegistry getObject() throws Exception {
    SchemeRegistry registry = new SchemeRegistry();

    for (Scheme scheme : schemes) {
      registry.register(scheme);
    }

    return registry;
  }

  @Override
  public Class<SchemeRegistry> getObjectType() {
    return SchemeRegistry.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}