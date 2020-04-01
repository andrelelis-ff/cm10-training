package com.coremedia.blueprint.cae.handlers;

import com.coremedia.objectserver.web.links.Link;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_SEGMENTS;

@Link
public class StaticUrlHandler extends HandlerBase {

  private static final String URI_PREFIX = "";
  private static final String STATIC_URL = "staticUrl";

  private static final String URI_PATTERN =
          "/" + URI_PREFIX +
                  "/{" + STATIC_URL + ":" + PATTERN_SEGMENTS + "}";

  @Link(type = String.class, uri = URI_PATTERN)
  public UriComponents buildLink(Object staticUrl, Map<String, Object> linkParameters) {
    List<String> paths = splitPathInfo(String.valueOf(staticUrl));

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
            .pathSegment(URI_PREFIX)
            .pathSegment(paths.toArray(new String[paths.size()]));
    addLinkParametersAsQueryParameters(uriBuilder, linkParameters);
    return uriBuilder.build();
  }
}
