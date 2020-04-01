package com.coremedia.blueprint.studio.rest.externalpreview;

import com.coremedia.rest.cap.exception.ParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * The server side of the external preview with a registry of data from Studio instances.
 */
@RestController
@RequestMapping(value = "externalpreview", produces = MediaType.APPLICATION_JSON_VALUE)
public class ExternalPreviewResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalPreviewResource.class);

  private static final String METHOD_UPDATE_PATH = "update";
  private static final String METHOD_CONFIG_PATH = "config";
  private static final String URL_ENCODING = "utf8";
  private static final String REQUEST_METHOD = "GET";

  private static final String PARAM_DATA = "data";
  private static final String PARAM_PREVIEW_URL = "previewUrl";
  private static final String PARAM_TOKEN = "token";
  private static final String PARAM_METHOD = "method";

  private static ExternalPreviewConfigRepresentation configRepresentation = new ExternalPreviewConfigRepresentation();

  ExternalPreviewResource(String restUrl, String previewUrl, String urlPrefix) {
    configRepresentation.setRestUrl(restUrl);
    configRepresentation.setPreviewUrl(previewUrl);
    configRepresentation.setUrlPrefix(urlPrefix);
  }

  /**
   * Forwards the preview data information to the CAE, that that the polling that is executed in it returns
   * the correct content item to preview.
   *
   * @param form The content data that should be shown on the external preview.
   * @return True if the data was forward successfully.
   */
  @PostMapping(METHOD_UPDATE_PATH)
  public boolean updatePreviewData(@RequestBody MultiValueMap<String, String> form) {
    String url = null;
    try {
      String json = form.getFirst(PARAM_DATA);
      String token = form.getFirst(PARAM_TOKEN);
      String method = form.getFirst(PARAM_METHOD);
      if (null != json) {
        json = URLEncoder.encode(json, URL_ENCODING);
      }

      String previewUrl = form.getFirst(PARAM_PREVIEW_URL);
      url = previewUrl + "?token=" + token + "&method=" + method;
      if (json != null && !json.isEmpty()) {
        url += "&data=" + json;
      }
      return sendRequest(url);
    } catch (Exception e) {//NOSONAR
      LOGGER.error("Error applying preview data to " + url + ": " + e.getMessage(), e); //NOSONAR
    }
    return false;
  }

  /**
   * Returns the preview urls and host names for displaying the content to preview.
   *
   * @return configuration
   */
  @GetMapping(METHOD_CONFIG_PATH)
  public ExternalPreviewConfigRepresentation getConfigRepresentation() {
    return configRepresentation;
  }


  /**
   * Executes a GET request that is send to the preview CAE.
   *
   * @param urlString The url string including parameters.
   * @return True if the request was successful.
   * @throws java.io.IOException Thrown if the preview CAE is not available.
   */
  private boolean sendRequest(String urlString) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
    httpCon.setRequestMethod(REQUEST_METHOD);
    httpCon.connect();
    if (httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
      LOGGER.info("Written preview token " + urlString);//NOSONAR
      return true;
    }

    LOGGER.error(httpCon.getResponseCode() + " Error in response: " + httpCon.getResponseMessage());//NOSONAR
    throw new ParameterizedException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to update preview CAE", "CAE Update Error", "Could not update CAE external preview status using URL '" + urlString + "'");
  }
}
