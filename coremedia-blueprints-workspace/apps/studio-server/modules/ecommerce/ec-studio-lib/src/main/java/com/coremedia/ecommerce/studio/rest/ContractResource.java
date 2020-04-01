package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdUtils;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.contract.Contract;
import com.coremedia.livecontext.ecommerce.contract.ContractService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.WORKSPACE_ID_NONE;

/**
 * A catalog {@link Contract} object as a RESTful resource.
 */
@RestController
@RequestMapping(value = "livecontext/contract/{siteId}/{workspaceId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
public class ContractResource extends AbstractCatalogResource<Contract> {

  @Autowired
  public ContractResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @Override
  protected ContractRepresentation getRepresentation(@NonNull Map<String, String> params) {
    ContractRepresentation representation = new ContractRepresentation();
    fillRepresentation(params, representation);
    return representation;
  }

  private void fillRepresentation(@NonNull Map<String, String> params, ContractRepresentation representation) {
    Contract contract = getEntity(params);
    representation.setId(CommerceIdFormatterHelper.format(contract.getId()));
    representation.setName(contract.getName());
    representation.setExternalId(contract.getExternalId());
    representation.setExternalTechId(contract.getExternalTechId());
  }

  @Override
  protected Contract doGetEntity(@NonNull Map<String, String> params) {
    String id = params.get(PATH_ID);

    return getStoreContext(params)
            .flatMap(storeContext -> storeContext
                    .getConnection()
                    .getContractService()
                    .flatMap(contractService -> findContract(contractService, storeContext, id)))
            .orElse(null);
  }

  @NonNull
  private static Optional<Contract> findContract(@NonNull ContractService contractService,
                                                 @NonNull StoreContext storeContext, String id) {
    // Iterating all eligible contracts is a workaround since the
    // `findContractById` call does not consider the store ID.
    // Therefor we make use of the eligible call since it does consider
    // the store ID.
    return contractService.findContractIdsForServiceUser(storeContext).stream()
            .filter(contract -> externalIdMatches(contract, id))
            .findFirst()
            .map(contract -> contractService.findContractById(contract.getId(), storeContext));
  }

  private static boolean externalIdMatches(@NonNull Contract contract, String id) {
    String externalId = contract.getExternalId();
    return externalId != null && externalId.equals(id);
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Contract contract) {
    Map<String, String> params = new HashMap<>();
    CommerceId contractId = contract.getId();
    String externalId = CommerceIdUtils.getExternalIdOrThrow(contractId);
    params.put(PATH_ID, externalId);

    StoreContext context = contract.getContext();
    params.put(PATH_SITE_ID, context.getSiteId());
    params.put(PATH_WORKSPACE_ID, context.getWorkspaceId().orElse(WORKSPACE_ID_NONE).value());
    return params;
  }
}
