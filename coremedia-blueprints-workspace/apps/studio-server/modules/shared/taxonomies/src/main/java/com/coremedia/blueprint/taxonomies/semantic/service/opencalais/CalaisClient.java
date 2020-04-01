package com.coremedia.blueprint.taxonomies.semantic.service.opencalais;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Client for OpenCalais. This class is responsible for requests to the OpenCalais API endpoint.
 * It contains a Http client and a Http connection manager to manage network requests.
 */
public class CalaisClient {

  private static final Logger LOG = LoggerFactory.getLogger(CalaisClient.class);

  private final ObjectMapper mapper = new ObjectMapper();

  private String uniqueAccessKey;
  private String apiUrl;

  private static PoolingClientConnectionManager conMgr = new PoolingClientConnectionManager();

  /*
   * A connection manager.
   */
  static {
    // Increase max total connection to 20
    conMgr.setMaxTotal(20);
    // Increase default max connection per route to 20
    conMgr.setDefaultMaxPerRoute(20);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        super.run();
        if (conMgr != null) {
          try {
            conMgr.shutdown();
          } catch (Exception e) {
            // silently
          }
        }
      }
    });
  }

  /*
   * Client to perform network requests.
   */
  private HttpClient client = new DefaultHttpClient(conMgr);

  /**
   * Entry method to perform OpenCalais request and retrieve analyzed data.

   + The result data will be preprocessed, especially the entity objects will be collected from the response in order to
   * become first class objects. These objects are used for matching with internal CoreMedia taxonomies.
   * The original response will be preserved in the result map as well.
   * This map contains the following entries:
   * <ul>
   *   <li>
   *     info: info string from the response
   *     meta: meta information
   *     entities: list of entity objects collected from the response
   *     payload: the original JSON response
   *   </li>
   * </ul>
   *
   * @param text The text to analyze.
   * @return The preprocessed response.
   */
  public Map<String, Object> analyze(String text) {
    HttpPost method = createPostMethod();
    method.setEntity(new StringEntity(text, "UTF-8"));
    return doRequest(method);
  }

  private Map<String, Object> doRequest(HttpPost method) {
    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Sending POST request to {}", method.getURI().toString());
      }
      HttpResponse response = client.execute(method);
      int returnCode = response.getStatusLine().getStatusCode();
      String payload = IOUtils.toString(response.getEntity().getContent());

      if (LOG.isDebugEnabled()) {
        LOG.debug("Return code is {}", returnCode);
        LOG.debug("Retrieved data: \n{}", payload);
      }

      if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
        LOG.error("The Post method is not implemented by this URI {}.", apiUrl);
      } else if (returnCode == HttpStatus.SC_OK) {
        try {
          //noinspection unchecked
          Map<String, Object> map = mapper.readValue(payload, Map.class);
          return processResponse(map, payload);
        } catch (JsonParseException e) {
          throw new IOException("Error parsing OpenCalais response. Unexpected response: " + payload);
        }
      } else {
        LOG.error("Post request to {} failed. Return code is {}.", method.getURI().toString(), returnCode);
        LOG.error("Payload is \n {}", payload);
      }
    } catch (Exception e) {
      LOG.error("Error during request to OpenCalais.", e);
    } finally {
      method.releaseConnection();
    }
    return null;
  }


  private HttpPost createPostMethod() {
    HttpPost method = new HttpPost(apiUrl);
    // Set mandatory parameters
    method.addHeader("X-AG-Access-Token", uniqueAccessKey);
    // Set input content type
    method.addHeader("Content-Type", "text/plain");
    // Set response/output format
    method.addHeader("outputformat", "application/json");
    return method;
  }

  private Map<String, Object> processResponse(Map<String, Object> map,
                                              final String payload) {
    //noinspection unchecked
    Map<String, Object> doc = (Map<String, Object>) map.remove("doc");
    final Map<String, Object> info = extractObject(doc, "info");
    final Map<String, Object> meta = extractObject(doc, "meta");
    List<Map<String, Object>> entities = getEntities(map);
    Map<String, Object> resultMap = new HashMap<String, Object>();
    resultMap.put("info", info);
    resultMap.put("meta", meta);
    resultMap.put("entities", entities);
    resultMap.put("payload", payload);
    return resultMap;
  }

  private List<Map<String, Object>> getEntities(Map<String, Object> root) {
    List<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();
    for (Map.Entry<String, Object> me : root.entrySet()) {
      //noinspection unchecked
      Map<String, Object> map = (Map<String, Object>) me.getValue();
      map.put("_uri", me.getKey());
      String group = (String) map.get("_typeGroup");
      if ("entities".equals(group)) {
        entities.add(map);
      }
    }
    return entities;
  }

  private static Map<String, Object> extractObject(Map<String, Object> map,
                                                   String key) {
    //noinspection unchecked
    return (Map<String, Object>) map.remove(key);
  }

  @SuppressWarnings("unused")
  public String getUniqueAccessKey() {
    return uniqueAccessKey;
  }

  public void setUniqueAccessKey(String uniqueAccessKey) {
    this.uniqueAccessKey = uniqueAccessKey;
  }

  @SuppressWarnings("unused")
  public String getApiUrl() {
    return apiUrl;
  }

  public void setApiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
  }

}
