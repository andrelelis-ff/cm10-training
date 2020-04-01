package com.coremedia.blueprint.cae.view;

import com.coremedia.objectserver.view.TextView;
import com.coremedia.objectserver.view.ViewUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

/**
 * This abstract servlet view transforms objects (see concrete implementations for details which ones are possible)
 * into valid JSON using {@link com.google.gson.Gson} to create {@link com.google.gson.JsonElement},
 * a base class for {@link com.google.gson.JsonObject} and {@link com.google.gson.JsonArray}.
 * <p/>
 * The result will be written to the response and the content type is set to "application/json".
 *
 * @deprecated return Objects from handlers that Jackson's ObjectMapper can understand.
 */
@Deprecated
public abstract class AbstractJsonView implements TextView {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractJsonView.class);

  // charset=UTF-8 isn't really needed, but something else is adding a default charset if it's missing
  public static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

  private static final Gson GSON = new Gson();

  @Override
  public void render(Object bean, String view, Writer out, @NonNull HttpServletRequest request,
                     @NonNull HttpServletResponse response) {
    String result = new JsonObject().toString();
    try {

      JsonElement jsonObject = getJSON(bean);
      if (jsonObject != null) {
        result = GSON.toJson(jsonObject);
      }
    } catch (Exception exception) {
      handleException(response, bean, exception);
    }

    try {
      response.setContentType(getContentType());
      out.write(result);
    } catch (IOException e) {
      ViewUtils.rethrow(e, bean, view, this);
    }
  }

  public String getContentType() {
    return JSON_CONTENT_TYPE;
  }

  private void handleException(@NonNull HttpServletResponse response, Object bean, Exception exception) {
    LOG.error("Error while serializing bean {}", bean, exception);
    try {
      response.sendError(SC_INTERNAL_SERVER_ERROR);
    } catch (IOException e) {
      LOG.error("Error while sending error response for bean {}", bean, e);
    }
  }

  public abstract <T> JsonElement getJSON(T bean);
}

