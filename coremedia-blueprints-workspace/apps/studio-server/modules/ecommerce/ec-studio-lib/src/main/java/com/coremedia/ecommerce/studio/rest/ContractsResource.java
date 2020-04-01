package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.ecommerce.studio.rest.model.Contracts;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.contract.Contract;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.WORKSPACE_ID_NONE;
import static java.util.stream.Collectors.toList;

/**
 * The resource handles the top level store node "Contracts".
 * It is not showed in the catalog tree but used to invalidate the list of available commerce contracts
 */
@RestController
@RequestMapping(value = "livecontext/contracts/{siteId}/{workspaceId}", produces = MediaType.APPLICATION_JSON_VALUE)
public class ContractsResource extends AbstractCatalogResource<Contracts> {

  @Autowired
  public ContractsResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @Override
  protected ContractsRepresentation getRepresentation(@NonNull Map<String, String> params) {
    ContractsRepresentation representation = new ContractsRepresentation();
    fillRepresentation(params, representation);
    return representation;
  }

  private void fillRepresentation(@NonNull Map<String, String> params, ContractsRepresentation representation) {
    Contracts contracts = getEntity(params);
    if (contracts == null) {
      return;
    }

    StoreContext context = contracts.getContext();

    representation.setId(contracts.getId());

    // Set contracts.
    context.getConnection()
            .getContractService()
            .map(contractService -> contractService.findContractIdsForServiceUser(context))
            .map(ContractsResource::dropDefaultContracts)
            .ifPresent(representation::setContracts);
  }

  @NonNull
  private static Collection<Contract> dropDefaultContracts(Collection<Contract> contracts) {
    return contracts.stream()
            .filter(contract -> !contract.isDefaultContract())
            .collect(toList());
  }

  @Override
  protected Contracts doGetEntity(@NonNull Map<String, String> params) {
    return getStoreContext(params).map(Contracts::new).orElse(null);
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Contracts contracts) {
    Map<String, String> params = new HashMap<>();
    String contractId = contracts.getId();
    params.put(PATH_ID, contractId);

    StoreContext context = contracts.getContext();
    params.put(PATH_SITE_ID, context.getSiteId());
    params.put(PATH_WORKSPACE_ID, context.getWorkspaceId().orElse(WORKSPACE_ID_NONE).value());
    return params;
  }
}
