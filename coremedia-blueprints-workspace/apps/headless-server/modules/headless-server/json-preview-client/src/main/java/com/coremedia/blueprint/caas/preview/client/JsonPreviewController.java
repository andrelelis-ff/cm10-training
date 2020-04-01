package com.coremedia.blueprint.caas.preview.client;

import com.google.common.base.Charsets;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.NoSuchElementException;


@RestController
@Api(value = "/previewurl", tags = "Json Preview Client")
@DefaultAnnotation(NonNull.class)
public class JsonPreviewController {
  static final String ERROR_MSG_NO_QUERY_DEFINITION = "No json preview query definition available for selected document type.";
  static final String ERROR_MSG_NO_ENTITY = "No response entity available.";

  static final String REQUEST_NOT_SUCCESSFUL = "Request not successful";

  private static final Logger LOG = LoggerFactory.getLogger(JsonPreviewController.class);
  private static final DateTimeFormatter STUDIO_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm VV");

  private static final String PREVIEW_PATH = "/preview";
  private static final String PARAM_NUMERIC_ID = "numericId";
  private static final String PARAM_TYPE = "type";

  private static final String TEMPLATE_PATH = "html/previewJson.html";
  private static final String TEMPLATE_VAR_PREVIEW_JSON = "previewJson";
  private static final String TEMPLATE_VAR_ERROR = "error";

  private HttpClient httpClient;
  private ITemplateEngine templateEngine;
  private String caasServerEndpoint;

  public JsonPreviewController(HttpClient httpClient, @Qualifier("htmlTemplateEngine") TemplateEngine templateEngine, @Value("${caasserver.endpoint}") String caasServerEndpoint) {
    this.httpClient = httpClient;
    this.templateEngine = templateEngine;
    this.caasServerEndpoint = caasServerEndpoint;
  }

  @GetMapping(PREVIEW_PATH + "/{" + PARAM_NUMERIC_ID + "}/{" + PARAM_TYPE + "}")
  @Timed
  public ResponseEntity<String> preview(
          @ApiParam(value = "The id of the item", required = true) @PathVariable String numericId,
          @ApiParam(value = "The type of the item", required = true) @PathVariable String type,
          @ApiParam(value = "The preview date", required = false) @RequestParam(required = false, name = "previewDate") String previewDate) {

    Context ctx = new Context();

    String query;
    try {
      query = IOUtils.resourceToString("/previewclient/graphql/content.graphql", Charsets.UTF_8);
    } catch (IOException e) {
      return getResponseEntity(ctx, TEMPLATE_VAR_ERROR, ERROR_MSG_NO_QUERY_DEFINITION);
    }

    HttpResponse response;
    try {
      response = executePostRequest(caasServerEndpoint, numericId, type, query, previewDate, httpClient);
    } catch (IOException e) {
      return getResponseEntity(ctx, TEMPLATE_VAR_ERROR, e.getMessage());
    }

    if (response.getEntity() == null) {
      return getResponseEntity(ctx, TEMPLATE_VAR_ERROR, ERROR_MSG_NO_ENTITY);
    }

    try (
        InputStream is = response.getEntity().getContent();
        JsonParser parser = Json.createParser(is)) {
      parser.next();
      JsonObject jsonObject = parser.getObject();
      return getResponseEntity(ctx, TEMPLATE_VAR_PREVIEW_JSON, jsonObject.toString());
    } catch (IOException|IllegalStateException| JsonException|NoSuchElementException e) {
      return getResponseEntity(ctx, TEMPLATE_VAR_ERROR, e.getMessage());
    }
  }

  private ResponseEntity<String> getResponseEntity(Context ctx, String templateVar, Object content) {
    ctx.setVariable(templateVar, content);
    String htmlContent = processTemplate(ctx);
    return ResponseEntity.ok().body(htmlContent);
  }

  private String processTemplate(Context ctx) {
    return templateEngine.process(TEMPLATE_PATH, ctx);
  }



  private static HttpResponse executePostRequest(String caasServerEndpoint, String id, String type, String query, String previewDate, HttpClient httpClient) throws IOException {
    URI uri = getCaasServerUri(caasServerEndpoint);

    JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
    objectBuilder = objectBuilder.add("variables", Json.createObjectBuilder().add("id", id).add("type", type).build());
    objectBuilder = objectBuilder.add("query", query);

    StringEntity entity = new StringEntity(objectBuilder.build().toString());

    HttpPost httpPost = new HttpPost(uri);
    httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
    httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    if (StringUtils.isNotBlank(previewDate)) {
      try {
        ZonedDateTime parsedDate = ZonedDateTime.parse(previewDate, STUDIO_DATE_FORMATTER);
        String formattedDate = DateUtils.formatDate(Date.from(parsedDate.toInstant()), DateUtils.PATTERN_RFC1123);
        httpPost.addHeader("X-Preview-Date", formattedDate);
      } catch (DateTimeParseException e) {
        LOG.warn("Invalid previewDate: {}", e.getMessage());
      }
    }
    httpPost.setEntity(entity);

    HttpResponse response = httpClient.execute(httpPost);
    if (!HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
      throw new IOException(REQUEST_NOT_SUCCESSFUL + ": " + uri + " " + response.getStatusLine().getReasonPhrase());
    }
    return response;
  }

  private static URI getCaasServerUri(String caasServerEndpoint) {
    return UriComponentsBuilder.fromHttpUrl(caasServerEndpoint).build().toUri();
  }
}
