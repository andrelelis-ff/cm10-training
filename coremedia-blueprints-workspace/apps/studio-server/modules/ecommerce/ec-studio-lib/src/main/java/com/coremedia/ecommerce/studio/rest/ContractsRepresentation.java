package com.coremedia.ecommerce.studio.rest;

import com.coremedia.livecontext.ecommerce.contract.Contract;

import java.util.Collection;
import java.util.Collections;

/**
 * Contracts representation for JSON.
 */
public class ContractsRepresentation extends AbstractCatalogRepresentation {

  private Collection<Contract> contracts = Collections.emptyList();

  public Collection<Contract> getContracts() {
    return contracts;
  }

  public void setContracts(Collection<Contract> contracts) {
    this.contracts = contracts;
  }
}
