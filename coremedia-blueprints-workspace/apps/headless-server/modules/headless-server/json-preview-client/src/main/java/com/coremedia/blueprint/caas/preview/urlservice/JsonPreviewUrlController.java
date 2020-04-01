package com.coremedia.blueprint.caas.preview.urlservice;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@Api(value = "/previewurl", tags = "Preview Url")
@DefaultAnnotation(NonNull.class)
public class JsonPreviewUrlController {

  private static final String PREVIEW_URL_PATH = "/previewurl";

  private static final String X_CSRF_TOKEN = "X-CSRF-Token";

  private String previewClientUrl;

  public JsonPreviewUrlController(@Value("${previewclient.url}") String previewClientUrl) {
    this.previewClientUrl = previewClientUrl;
  }

  @GetMapping(PREVIEW_URL_PATH)
  @Timed()
  public ResponseEntity<String> previewUrl(@ApiParam(value = "Content id", required = true) @RequestParam(name = "id") String id,
                                           @ApiParam(value = "Type of the content object", required = true) @RequestParam(name = "contentType") String contentType,
                                           @ApiIgnore HttpServletRequest request) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setAccessControlAllowHeaders(Collections.singletonList(X_CSRF_TOKEN));

    String numericId = getNumericId(id);
    if (StringUtils.isBlank(numericId)) {
      return ResponseEntity.badRequest().headers(httpHeaders).body("Invalid id: " + id);
    }

    if (StringUtils.isBlank(contentType)) {
      return ResponseEntity.badRequest().headers(httpHeaders).body("Invalid content type: empty");
    }

    String uri = getClientPreviewUrl(numericId, contentType, previewClientUrl, request.getParameterMap());
    return ResponseEntity.ok().headers(httpHeaders).body(uri);
  }

  @Nullable
  private String getNumericId(String id) {
    String[] parts = StringUtils.split(id, "/");
    if (parts.length > 0) {
      return parts[parts.length - 1];
    }
    return null;
  }

  private static String getClientPreviewUrl(String id, String pathSegment, String previewClientUrl, Map<String, String[]> parameterMap) {
    MultiValueMap<String, String> params = getParamsAsMultiValueMap(new HashMap<>(parameterMap));
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(previewClientUrl)
        .replacePath("preview")
        .pathSegment(id, pathSegment)
        .queryParams(params);

    return builder.build().toUriString();
  }

  private static MultiValueMap<String, String> getParamsAsMultiValueMap(Map<String, String[]> parameterMap) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    parameterMap.remove("id");
    parameterMap.forEach((k, v) -> params.put(k, Arrays.asList(v)));
    return params;
  }
}
