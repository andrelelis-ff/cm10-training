package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.ecommerce.studio.rest.model.Workspaces;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * The resource handles the top level store node "Segments".
 * It is not showed in the catalog tree but used to invalidate the list of available commerce segments
 */
@RestController
@RequestMapping(value = "livecontext/workspaces/{" + AbstractCatalogResource.PATH_SITE_ID + "}", produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkspacesResource extends AbstractCatalogResource<Workspaces> {

  @Autowired
  public WorkspacesResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @Override
  protected WorkspacesRepresentation getRepresentation(@NonNull Map<String, String> params) {
    WorkspacesRepresentation representation = new WorkspacesRepresentation();
    fillRepresentation(params, representation);
    return representation;
  }

  private void fillRepresentation(@NonNull Map<String, String> params, WorkspacesRepresentation representation) {
    Workspaces workspaces = getEntity(params);

    if (workspaces == null) {
      throw new CatalogBeanNotFoundRestException("Could not load workspaces bean.");
    }

    StoreContext storeContext = workspaces.getContext();

    representation.setId(workspaces.getId());
    storeContext.getConnection().getWorkspaceService()
            .map(workspaceService -> workspaceService.findAllWorkspaces(storeContext))
            .ifPresent(representation::setWorkspaces);
  }

  @Override
  protected Workspaces doGetEntity(@NonNull Map<String, String> params) {
    return getStoreContext(params).map(Workspaces::new).orElse(null);
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Workspaces entity) {
    return Collections.singletonMap(PATH_SITE_ID, entity.getContext().getSiteId());
  }
}
