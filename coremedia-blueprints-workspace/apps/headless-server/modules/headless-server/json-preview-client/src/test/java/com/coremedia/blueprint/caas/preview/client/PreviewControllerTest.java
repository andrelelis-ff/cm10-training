package com.coremedia.blueprint.caas.preview.client;

import com.coremedia.blueprint.caas.preview.TestConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.TemplateEngine;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.coremedia.blueprint.caas.preview.client.JsonPreviewController.ERROR_MSG_NO_ENTITY;
import static com.coremedia.blueprint.caas.preview.client.JsonPreviewController.REQUEST_NOT_SUCCESSFUL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class PreviewControllerTest {

  private JsonPreviewController previewController;

  private static final String CAAS_SERVER_ENDPOINT = "http://caasServer.com:8080/graphql";

  @Inject
  private CloseableHttpClient httpClient;

  @Inject
  private TemplateEngine htmlTemplateEngine;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CloseableHttpResponse response;

  @Before
  public void init() {
    previewController = new JsonPreviewController(httpClient, htmlTemplateEngine, CAAS_SERVER_ENDPOINT);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void preview_article() throws IOException {
    when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
    when(response.getStatusLine().getStatusCode()).thenReturn(200);
    String jsonData = "{\"data\":{\"content\":{\"article\":{\"name\":\"Test Name\",\"title\":\"Test Title\"}}}}";
    when(response.getEntity().getContent()).thenReturn(IOUtils.toInputStream(jsonData, StandardCharsets.UTF_8));

    ResponseEntity<String> responseEntity = previewController.preview("1234", "article", null);

    assertEquals(200, responseEntity.getStatusCode().value());
    assertNotNull(responseEntity.getBody());
    assertTrue(responseEntity.getBody().contains(StringEscapeUtils.escapeJava(jsonData)));
  }

  @Test
  public void preview_page() throws IOException {
    when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
    when(response.getStatusLine().getStatusCode()).thenReturn(200);
    String jsonData = "{\"data\":{\"content\":{\"page\":{\"name\":\"Test Name\",\"title\":\"Test Title\"}}}}";
    when(response.getEntity().getContent()).thenReturn(IOUtils.toInputStream(jsonData, StandardCharsets.UTF_8));

    ResponseEntity<String> responseEntity = previewController.preview("1234", "page", null);

    assertEquals(200, responseEntity.getStatusCode().value());
    assertNotNull(responseEntity.getBody());
    assertTrue(responseEntity.getBody().contains(StringEscapeUtils.escapeJava(jsonData)));
  }

  @Test
  public void preview_errorNoResponseEntity() throws IOException {
    when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
    when(response.getStatusLine().getStatusCode()).thenReturn(200);
    when(response.getEntity()).thenReturn(null);
    ResponseEntity<String> responseEntity = previewController.preview("1234", "article", null);

    assertEquals(200, responseEntity.getStatusCode().value());
    assertNotNull(responseEntity.getBody());
    assertTrue(responseEntity.getBody().contains(ERROR_MSG_NO_ENTITY));
  }

  @Test
  public void preview_errorNo2xxStatus() throws IOException {
    when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
    when(response.getStatusLine().getStatusCode()).thenReturn(300);
    when(response.getEntity()).thenReturn(null);
    ResponseEntity<String> responseEntity = previewController.preview("1234", "article", null);

    assertEquals(200, responseEntity.getStatusCode().value());
    assertNotNull(responseEntity.getBody());
    assertTrue(responseEntity.getBody().contains(REQUEST_NOT_SUCCESSFUL));
  }

  @Test
  public void preview_errorResponseException() throws IOException {
    when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
    when(response.getStatusLine().getStatusCode()).thenReturn(200);
    String errorMsg = "IO Error";
    when(response.getEntity().getContent()).thenThrow(new IOException(errorMsg));
    ResponseEntity<String> responseEntity = previewController.preview("1234", "article", null);

    assertEquals(200, responseEntity.getStatusCode().value());
    assertNotNull(responseEntity.getBody());
    assertTrue(responseEntity.getBody().contains(errorMsg));
  }

  @Test
  public void preview_errorServerNotAvailable() throws IOException {
    String errorMsg = "Cannot connect to caas server";
    when(httpClient.execute(any(HttpUriRequest.class))).thenThrow(new IOException(errorMsg));

    ResponseEntity<String> responseEntity = previewController.preview("1234", "article", null);

    assertEquals(200, responseEntity.getStatusCode().value());
    assertNotNull(responseEntity.getBody());
    assertTrue(responseEntity.getBody().contains(errorMsg));
  }


  @Test
  public void preview_articlePreviewDate() throws IOException {
    when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
    when(response.getStatusLine().getStatusCode()).thenReturn(200);
    String jsonData = "{\"data\":{\"content\":{\"article\":{\"name\":\"Test Name\",\"title\":\"Test Title\"}}}}";
    when(response.getEntity().getContent()).thenReturn(IOUtils.toInputStream(jsonData, StandardCharsets.UTF_8));

    ResponseEntity<String> responseEntity = previewController.preview("1234", "article", "12-03-2019 00:00 Europe/Berlin");

    assertEquals(200, responseEntity.getStatusCode().value());
    assertNotNull(responseEntity.getBody());
    assertTrue(responseEntity.getBody().contains(StringEscapeUtils.escapeJava(jsonData)));
  }


  @Test
  public void preview_articlePreviewDateParseException() throws IOException {
    when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
    when(response.getStatusLine().getStatusCode()).thenReturn(200);
    String jsonData = "{\"data\":{\"content\":{\"article\":{\"name\":\"Test Name\",\"title\":\"Test Title\"}}}}";
    when(response.getEntity().getContent()).thenReturn(IOUtils.toInputStream(jsonData, StandardCharsets.UTF_8));

    ResponseEntity<String> responseEntity = previewController.preview("1234", "article", "12345678");

    assertEquals(200, responseEntity.getStatusCode().value());
    assertNotNull(responseEntity.getBody());
    assertTrue(responseEntity.getBody().contains(StringEscapeUtils.escapeJava(jsonData)));
  }
}
