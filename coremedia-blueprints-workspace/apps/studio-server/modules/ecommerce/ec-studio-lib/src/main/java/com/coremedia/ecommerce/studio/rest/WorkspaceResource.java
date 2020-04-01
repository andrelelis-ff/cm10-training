package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.workspace.Workspace;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A catalog {@link com.coremedia.livecontext.ecommerce.p13n.Segment} object as a RESTful resource.
 */
@RestController
@RequestMapping(value = "livecontext/workspace/{" + AbstractCatalogResource.PATH_SITE_ID + "}/{" + AbstractCatalogResource.PATH_ID + "}", produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkspaceResource extends AbstractCatalogResource<Workspace> {

  @Autowired
  public WorkspaceResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @Override
  protected WorkspaceRepresentation getRepresentation(@NonNull Map<String, String> params) {
    WorkspaceRepresentation representation = new WorkspaceRepresentation();
    fillRepresentation(params, representation);
    return representation;
  }

  private void fillRepresentation(@NonNull Map<String, String> params, WorkspaceRepresentation representation) {
    Workspace entity = getEntity(params);

    if (entity == null) {
      throw new CatalogBeanNotFoundRestException("Could not load workspace bean.");
    }

    representation.setId(CommerceIdFormatterHelper.format(entity.getId()));
    representation.setName(entity.getName());
    representation.setExternalId(entity.getExternalId());
    representation.setExternalTechId(entity.getExternalTechId());
  }

  @Override
  protected Workspace doGetEntity(@NonNull Map<String, String> params) {
    String id = params.get(PATH_ID);

    return getStoreContext(params)
            .flatMap(WorkspaceResource::findAllWorkspaces)
            .flatMap(workspaces -> findWorkspaceById(workspaces, id))
            .orElse(null);
  }

  @NonNull
  private static Optional<List<Workspace>> findAllWorkspaces(@NonNull StoreContext storeContext) {
    return storeContext.getConnection()
            .getWorkspaceService()
            .map(workspaceService -> workspaceService.findAllWorkspaces(storeContext));
  }

  @NonNull
  private static Optional<Workspace> findWorkspaceById(@NonNull List<Workspace> workspaces, @NonNull String id) {
    return workspaces.stream()
            .filter(workspace -> id.equals(workspace.getExternalTechId()))
            .findFirst();
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Workspace workspace) {
    CommerceId workspaceId = workspace.getId();
    String externalId = workspaceId.getExternalId().orElseGet(workspace::getExternalId);
    Map<String, String> params = new HashMap<>();
    params.put(PATH_ID, externalId);
    params.put(PATH_SITE_ID, workspace.getContext().getSiteId());
    return params;
  }
}
