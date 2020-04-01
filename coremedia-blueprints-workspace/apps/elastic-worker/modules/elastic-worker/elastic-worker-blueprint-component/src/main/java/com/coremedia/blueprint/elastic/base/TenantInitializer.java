package com.coremedia.blueprint.elastic.base;

import com.coremedia.elastic.core.api.tenant.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.util.Collection;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;

/**
 * This bean registers the tenants configured in content repository (via root channel settings).
 */
@SuppressWarnings("UnusedDeclaration") // used by Spring - it's a managed bean
public class TenantInitializer {

  private static final Logger LOG = LoggerFactory.getLogger(TenantInitializer.class);
  private static final String DELAY_PATTERN = "${tenant.recomputation.interval:60000}";
  private static final String INTERNAL_TENANT = "internal";

  @Inject
  private TenantService tenantService;

  @Inject
  private TenantHelper tenantHelper;

  private void registerTenantsFromContent() {
    final Collection<String> configuredTenants = tenantHelper.readTenantsFromContent();
    final Collection<String> registeredTenants = tenantService.getRegistered();
    deregisterTenants(configuredTenants, registeredTenants);
    registerTenants(configuredTenants, registeredTenants);
  }

  private void registerTenants(Collection<String> configuredTenants, Collection<String> registeredTenants) {
    final Collection<String> tenantsToAdd = filter(configuredTenants, not(in(registeredTenants)));
    if(!tenantsToAdd.isEmpty()) {
      LOG.debug("registering tenants {}", tenantsToAdd);
      tenantService.registerAll(tenantsToAdd);
    }
  }

  private void deregisterTenants(Collection<String> configuredTenants, Collection<String> registeredTenants) {
    final Collection<String> tenantsToRemove = filter(registeredTenants, not(in(configuredTenants)));
    // The internal tenant must not be deregistered
    tenantsToRemove.remove(INTERNAL_TENANT);
    if(!tenantsToRemove.isEmpty()) {
      LOG.debug("deregistering tenants {}", tenantsToRemove);
      tenantService.deregisterAll(tenantsToRemove);
    }
  }

  @Scheduled(
          fixedDelayString = DELAY_PATTERN,
          initialDelayString = "0")
  void initializeTenants() {
    try {
      registerTenantsFromContent();
    } catch (Exception e) {
      LOG.info("caught unexpected exception while computing tenants", e);
    }
  }
}