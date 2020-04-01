package com.coremedia.blueprint.nuggad.sources;

import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.PropertyProfile;
import com.coremedia.personalization.context.collector.AbstractContextSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link com.coremedia.personalization.context.collector.ContextSource} that reads nugg.ad information from a cookie
 * that have been previously set by JavaScript.
 * If no information is found in request, a redirect will be issued that sends user to a initialization URL.
 * <p/>
 * These string representation of the context is base64-encoded before it's stored in the cookie.
 * <p/>
 * Make sure you set the required properties <code>contextName</code> and <code>cookieName</code> to
 * unique values in order to prevent clashes with other sources or cookies.
 */
@SuppressWarnings({"UnusedDeclaration"})
public final class NuggAdSource extends AbstractContextSource {

  private static final Logger LOG = LoggerFactory.getLogger(NuggAdSource.class);
  private static final String COOKIE_NAME = "nuggAIArray";
  private List parameterNames = new ArrayList();

  private String contextName;

  /**
   * sets the names of the Nugg.Ad parameters in the returned Array from Nugg.Ad.
   *
   * @param parameterNames the ordered list of parameter names (e.g. "gender", "age")
   */
  public void setParameterNames(List parameterNames) {
    this.parameterNames = parameterNames;
  }

  /**
   * Sets the name used to store the profile in the profile collection.
   *
   * @param contextName name of the profile
   */
  @Required
  public void setContextName(final String contextName) {
    this.contextName = contextName;
  }

  /**
   * Decodes a string into a user profile instance. The supplied
   * <code>profileString</code> is the results of a former call
   * to <code>encodeUserProfile</code>.
   *
   * @param profileString the profile string to decode
   * @return the decoded profile or null if the profileString is null
   */
  private PropertyProfile decodeUserProfile(final String profileString) {
    if (profileString != null) {
      PropertyProfile profile = new PropertyProfile();
      String[] values = null;
      try {
        values = URLDecoder.decode(profileString, "UTF-8").split(",");
      } catch (UnsupportedEncodingException e) {
        LOG.warn("Error decoding URL", e);
      }
      for (int i = 0; values != null && i < values.length; i++) {
        profile.setProperty(parameterNames.size() > i ? (String) parameterNames.get(i) : "" + i, Integer.valueOf(values[i]).intValue());
      }
      return profile;
    }
    return null;
  }

  /* -----------------------------------------

    Context management

  ----------------------------------------- */

  private PropertyProfile getProfileFromRequest(final HttpServletRequest request) {
    assert request != null;
    // retrieve the profile collection from our cookie
    final Cookie cookie = getUserProfileCookie(request.getCookies());
    if (cookie != null) {
      return decodeUserProfile(cookie.getValue());
    } else {
      return null;
    }
  }

  // retrieves the cookie containing the user profile
  private Cookie getUserProfileCookie(final Cookie[] cookies) {
    if (cookies != null) {
      for (final Cookie cookie : cookies) {
        if (COOKIE_NAME.equals(cookie.getName())) {
          return cookie;
        }
      }
    }
    return null;
  }

  /* -----------------------------------------

    UserProfileSource interface

  ----------------------------------------- */

  /**
   * Retrieves the profile of the current user from a cookie stores in the request. If there isn't a profile
   * encoded in the request, a new default profile is created and added to the supplied profile collection.
   *
   * @param request           the current request
   * @param response          the current response
   * @param contextCollection the profile collection to which to add new profiles.
   */
  @Override
  public void preHandle(final HttpServletRequest request, final HttpServletResponse response,
                        final ContextCollection contextCollection) {

    PropertyProfile context = getProfileFromRequest(request);
    if (context != null) {
      contextCollection.setContext(contextName, context);
    }
  }


  /* -----------------------------------------

   Object stuff

  ----------------------------------------- */

  /**
   * Returns a human-readable representation of the state of this object. The format may change without notice.
   *
   * @return human-readable representation of this object
   */
  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append('[').append(getClass().getName()).
            append(", contextName=").append(contextName).
            append(", cookieName=").append(COOKIE_NAME).append(']');
    return builder.toString();
  }
}