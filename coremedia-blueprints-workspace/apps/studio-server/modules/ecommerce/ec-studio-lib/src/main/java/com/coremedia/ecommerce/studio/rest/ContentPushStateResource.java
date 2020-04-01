package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import com.coremedia.livecontext.ecommerce.push.PushService;
import com.coremedia.livecontext.ecommerce.push.PushState;
import com.coremedia.rest.linking.EntityResource;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.PathParam;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.ecommerce.studio.rest.ContentPushStateResource.URI_PATH;

@RestController
@RequestMapping(value = URI_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ContentPushStateResource implements EntityResource<PushState> {

  private static final String PATH_PARAM_CAP_ID = "id";
  static final String URI_PATH = "livecontext/pushState/content/{" + PATH_PARAM_CAP_ID + ":[0-9]+}";

  private final ContentRepository contentRepository;
  private final CommerceConnectionSupplier commerceConnectionSupplier;

  private String id;

  public ContentPushStateResource(@NonNull ContentRepository contentRepository, @NonNull CommerceConnectionSupplier commerceConnectionSupplier) {
    this.contentRepository = contentRepository;
    this.commerceConnectionSupplier = commerceConnectionSupplier;
  }

  public String getId() {
    return id;
  }

  @PathParam(PATH_PARAM_CAP_ID)
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public PushState getEntity(@NonNull Map<String, String> pathVariables) {
    Content content = contentRepository.getContent(pathVariables.get(PATH_PARAM_CAP_ID));

    Optional<CommerceConnection> connection = commerceConnectionSupplier.findConnection(content);
    if (!connection.isPresent()){
      return null;
    }

    Optional<PushService> pushServiceOptional = connection.flatMap(CommerceConnection::getPushService);
    if (!pushServiceOptional.isPresent()){
      return null;
    }

    return pushServiceOptional
            .map(pushService -> pushService.getPushState(content.getId(), connection.get().getStoreContext()))
            .orElse(null);
  }

  @GetMapping
  public PushStateRepresentation get(@PathVariable Map<String, String> params) {
    PushState pushState = getEntity(params);
    if (pushState == null){
      throw new NotFoundException("Push State bean not found");
    }
    return getRepresentation(pushState);
  }


  @NonNull
  static PushStateRepresentation getRepresentation(@NonNull PushState pushState){
    Date date = pushState.getModificationDate()
            .map(modificationDate -> Date.from(modificationDate.toInstant())).orElse(null);

    return new PushStateRepresentation(pushState.getState().name(), date);
  }
}
