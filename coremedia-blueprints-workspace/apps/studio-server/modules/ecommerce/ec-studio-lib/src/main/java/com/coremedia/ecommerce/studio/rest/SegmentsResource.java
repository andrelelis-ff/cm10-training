package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.ecommerce.studio.rest.model.Segments;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.WORKSPACE_ID_NONE;

/**
 * The resource handles the top level store node "Segments".
 * It is not showed in the catalog tree but used to invalidate the list of available commerce segments
 */
@RestController
@RequestMapping(value = "livecontext/segments/{siteId}/{workspaceId}", produces = MediaType.APPLICATION_JSON_VALUE)
public class SegmentsResource extends AbstractCatalogResource<Segments> {

  @Autowired
  public SegmentsResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @Override
  protected SegmentsRepresentation getRepresentation(@NonNull Map<String, String> params) {
    SegmentsRepresentation representation = new SegmentsRepresentation();
    fillRepresentation(params, representation);
    return representation;
  }

  private void fillRepresentation(@NonNull Map<String, String> params, SegmentsRepresentation representation) {
    Segments segments = getEntity(params);
    StoreContext storeContext = segments.getContext();

    representation.setId(segments.getId());

    // Set segments.
    storeContext.getConnection()
            .getSegmentService()
            .map(segmentService -> segmentService.findAllSegments(storeContext))
            .ifPresent(representation::setSegments);
  }

  @Override
  protected Segments doGetEntity(@NonNull Map<String, String> params) {
    return getStoreContext(params).map(Segments::new).orElse(null);
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Segments segments) {
    Map<String, String> params = new HashMap<>();
    String segmentsId = segments.getId();
    params.put(PATH_ID, segmentsId);

    StoreContext context = segments.getContext();
    params.put(PATH_SITE_ID, context.getSiteId());
    params.put(PATH_WORKSPACE_ID, context.getWorkspaceId().orElse(WORKSPACE_ID_NONE).value());
    return params;
  }
}
