package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.p13n.Segment;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.WORKSPACE_ID_NONE;

/**
 * A catalog {@link com.coremedia.livecontext.ecommerce.p13n.Segment} object as a RESTful resource.
 */
@RestController
@RequestMapping(value = "livecontext/segment/{" + AbstractCatalogResource.PATH_SITE_ID + "}/{" + AbstractCatalogResource.PATH_WORKSPACE_ID + "}/{" + AbstractCatalogResource.PATH_ID + "}", produces = MediaType.APPLICATION_JSON_VALUE)
public class SegmentResource extends AbstractCatalogResource<Segment> {

  @Autowired
  public SegmentResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @Override
  protected SegmentRepresentation getRepresentation(@NonNull Map<String, String> params) {
    SegmentRepresentation representation = new SegmentRepresentation();
    fillRepresentation(params, representation);
    return representation;
  }

  private void fillRepresentation(@NonNull Map<String, String> params, SegmentRepresentation representation) {
    Segment entity = getEntity(params);

    if (entity == null) {
      throw new CatalogBeanNotFoundRestException("Could not load segment bean.");
    }

    representation.setId(CommerceIdFormatterHelper.format(entity.getId()));
    representation.setName(entity.getName());
    representation.setExternalId(entity.getExternalId());
    representation.setExternalTechId(entity.getExternalTechId());
  }

  @Override
  protected Segment doGetEntity(@NonNull Map<String, String> params) {
    StoreContext storeContext = getStoreContext(params).orElse(null);
    if (storeContext == null) {
      return null;
    }

    CommerceConnection commerceConnection = storeContext.getConnection();
    CommerceId commerceId = commerceConnection.getIdProvider().formatSegmentId(params.get(PATH_ID));
    return commerceConnection.getSegmentService()
            .map(segmentService -> segmentService.findSegmentById(commerceId, storeContext))
            .orElse(null);
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Segment segment) {
    CommerceId segmentId = segment.getId();
    String externalId = segmentId.getExternalId().orElseGet(segment::getExternalId);
    StoreContext context = segment.getContext();
    Map<String, String> params = new HashMap<>();
    params.put(PATH_ID, externalId);
    params.put(PATH_SITE_ID, context.getSiteId());
    params.put(PATH_WORKSPACE_ID, context.getWorkspaceId().orElse(WORKSPACE_ID_NONE).value());
    return params;
  }
}
