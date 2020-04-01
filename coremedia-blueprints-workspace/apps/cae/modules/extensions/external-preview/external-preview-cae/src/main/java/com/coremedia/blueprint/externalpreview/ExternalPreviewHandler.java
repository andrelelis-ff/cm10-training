package com.coremedia.blueprint.externalpreview;

import com.coremedia.blueprint.links.BlueprintUriConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Accepts Studio requests to update the data to preview for a specific user, identified by a token.
 * Invalidates outdated preview data automatically.
 */
@RequestMapping
public class ExternalPreviewHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalPreviewHandler.class);

  private static final String URI_PATTERN = "/" + BlueprintUriConstants.Prefixes.PREFIX_SERVICE + "/externalpreview";

  //the max length for data
  private static final int MAX_DATA_LENGTH = 4048;
  private static final int MAX_URL_LENGTH = 2000;


  private static final String PARAMETER_METHOD = "method";
  private static final String PARAMETER_DATA = "data";
  private static final String PARAMETER_TOKEN = "token";

  private static final String METHOD_UPDATE = "update";
  private static final String METHOD_LIST = "list";
  private static final String METHOD_LOGIN = "login";
  private static final String METHOD_INVALIDATE = "invalidate";

  private static final String STATUS_ERROR = "{\"status\":\"no matching token found for polling\"}";
  private static final String STATUS_ERROR_METHOD = "{\"status\":\"no matching method found\"}";
  private static final String STATUS_ERROR_LOGIN = "{\"status\":\"no matching token found after login\"}";
  private static final String STATUS_OK = "{\"status\":\"ok\"}";


  @GetMapping(value = URI_PATTERN)
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String token = request.getParameter(PARAMETER_TOKEN);
    String method = request.getParameter(PARAMETER_METHOD);

    if (method == null) {
      writeResponse(response, STATUS_ERROR_METHOD);
    } else if (method.equalsIgnoreCase(METHOD_UPDATE)) { //data put by the Studio Server extension
      String data = request.getParameter(PARAMETER_DATA);
      ResponseEntity responseEntity = updatePreviewData(token, data);
      if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
        LOGGER.error("Error in preview controller: " + responseEntity.getBody());//NOSONAR
        response.setStatus(responseEntity.getStatusCode().value());
        writeResponse(response, String.valueOf(responseEntity.getBody()));
      }
    } else if (method.equalsIgnoreCase(METHOD_INVALIDATE)) { //invalidate token
      PreviewInfoService.getInstance().removePreview(token);
    } else if (method.equalsIgnoreCase(METHOD_LOGIN)) { //token login
      loginUser(response, token);
    } else if (method.equalsIgnoreCase(METHOD_LIST)) { //data requested by the external preview HTML
      readPreviewData(response, token);
    } else {
      writeResponse(response, STATUS_ERROR_METHOD);
    }

    return null;
  }

  /**
   * Validates the login of the user by the given token.
   *
   * @param response The response to write the validation result into.
   * @param token    The token that identifies the user.
   * @throws IOException
   */
  private void loginUser(HttpServletResponse response, String token) throws IOException {
    String result = STATUS_OK;
    PreviewInfoItem item = PreviewInfoService.getInstance().getPreviewInfo(token);
    if (item == null) {
      result = STATUS_ERROR;
    }
    writeResponse(response, result);
  }

  /**
   * Writes the preview json data to the response if data is available for the given token.
   *
   * @param response The response to write the data into.
   * @param token    The token that identifies the user data.
   */
  private void readPreviewData(HttpServletResponse response, String token) throws IOException {
    PreviewInfoItem item = PreviewInfoService.getInstance().getPreviewInfo(token);
    String result;
    if (item != null) {
      result = item.asJSON();
    } else {
      result = STATUS_ERROR_LOGIN;
    }
    writeResponse(response, result);
  }

  /**
   * Updates the user preview data for the given token, if data is valid.
   *
   * @param token The token that identifies the preview data.
   * @param data  The json data that contains the preview information.
   */
  private ResponseEntity updatePreviewData(String token, String data) {
    if (isValidData(data)) {
      PreviewInfoService.getInstance().applyPreview(token, data);
      return ResponseEntity.status(HttpStatus.OK).build();
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("External preview CAE controller rejected to store preview data: '" + data + "'");
    }
  }

  /**
   * Checks if the data is valid JSON data and does
   * not exceed the maximum size.
   *
   * @param json The json data passed by the Studio
   */
  private boolean isValidData(String json) {
    JsonNode dataNode = readJsonNode(json);
    if (dataNode == null) {
      return false;
    }

    if (dataNode.get("previewUrl") == null) {
      return false;
    }
    String previewUrl = dataNode.get("previewUrl").textValue();
    if (previewUrl == null || !previewUrl.startsWith("http") || previewUrl.length() > MAX_URL_LENGTH) {
      return false;
    }
    if (dataNode.get("name") == null || dataNode.get("name").textValue() == null) {
      return false;
    }
    return dataNode.get("id") != null && dataNode.get("id").intValue() >= 0;
  }

  /**
   * Creates a json node instance from the json string value.
   *
   * @param json The json data in string format.
   * @return The jackson JsonNode representation
   */
  private JsonNode readJsonNode(String json) {
    if (json == null || json.length() > MAX_DATA_LENGTH) {
      return null;
    }
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode df = mapper.readValue(json, JsonNode.class);
      return df.iterator().next();
    } catch (IOException e) {
      LOGGER.warn("Invalid json data passed to the external preview CAE controller: " + json, e);
    }
    return null;
  }

  /**
   * Writes the json response
   *
   * @param response The response object to write.
   * @param result   The JSON data that contains the request result.
   * @throws IOException
   */
  private void writeResponse(HttpServletResponse response, String result) throws IOException {
    response.setContentType("application/json");
    PrintWriter writer = response.getWriter();
    writer.write(result);
  }
}
