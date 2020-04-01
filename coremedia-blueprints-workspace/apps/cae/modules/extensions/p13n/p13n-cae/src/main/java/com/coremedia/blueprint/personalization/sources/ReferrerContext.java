package com.coremedia.blueprint.personalization.sources;

import com.coremedia.common.logging.PersonalDataExceptions;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.personalization.context.CoDecException;
import com.coremedia.personalization.context.ContextCoDec;
import com.coremedia.personalization.context.DirtyFlagMaintainer;
import com.coremedia.personalization.context.PropertyProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReferrerContext implements PropertyProvider, DirtyFlagMaintainer {

  private static final Logger LOG = LoggerFactory.getLogger(ReferrerContext.class);

  private static final String URL_PROP = "url";
  private static final String SEARCHENGINE_PROP = "searchengine";
  private static final String QUERY_PROP = "query";

  private static final Pattern GOOGLE_REGEXP = Pattern.compile("^http://(\\w+\\.)+google.*(\\?|&)q=([^&]+).*");
  private static final Pattern BING_REGEXP = Pattern.compile("^http://(\\w+\\.)+bing.*(\\?|&)q=([^&]+).*");
  private static final Pattern YAHOO_REGEXP = Pattern.compile("^http://(\\w+\\.)+yahoo.*(\\?|&)p=([^&]+).*");
  private static final int REGEXP_URL_POSITION = 3;

  private boolean isDirty = true;
  private final Map<String, String> referrers = new HashMap<>();

  /**
   * Encodes/decodes ScoringContexts from and to Strings.
   */
  public static final class CoDec implements ContextCoDec {

    private final ObjectMapper objectMapper;

    /**
     * Initialize new Objectmapper on instantiation
     */
    public CoDec() {
      this.objectMapper = new ObjectMapper();
    }

    @Override
    public @PersonalData Object contextFromString(@PersonalData String str) {
      if (str == null) {
        throw new IllegalArgumentException("supplied str must not be null");
      }

      try {
        // Suppress warning about assigning @PolyPersonalData result from #readValue to non-annotated local variable
        // Okay, because the properties are added to the returned context, and the return type is @PolyPersonalData
        @SuppressWarnings("PersonalData")
        Map props = objectMapper.readValue(str, Map.class);

        ReferrerContext context = (ReferrerContext) createNewContext();
        context.referrers.putAll(props);
        context.isDirty = false;
        return context;
      } catch (IOException ex) {
        throw PersonalDataExceptions
                .logCauseAndCreateException(getClass(), ex, CoDecException::new, "unable to decode context");
      }
    }

    @Override
    public @PersonalData String stringFromContext(@PersonalData Object context) {
      if (context == null) {
        throw new IllegalArgumentException("Supplied context is null");
      }
      if (!(context instanceof ReferrerContext)) {
        throw new IllegalArgumentException("supplied context is not of required type ReferrerContext: " + context.getClass());
      }

      try {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
          throw new IllegalStateException("Servlet request attributes not available.");
        }

        HttpServletRequest request = attributes.getRequest();

        @PersonalData ReferrerContext ctx = (ReferrerContext) context;
        String referer = request.getHeader("referer");
        if (referer != null) {
          ctx.referrers.put(URL_PROP, referer);
          if (!addGoogleProperties(ctx.referrers, referer) && !addBingProperties(ctx.referrers, referer)) {
            addYahooProperties(ctx.referrers, referer);
          }
        }
        return objectMapper.writeValueAsString(((ReferrerContext) context).referrers);
      } catch (Exception ex) { // NOSONAR
        throw PersonalDataExceptions
                .logCauseAndCreateException(getClass(), ex, CoDecException::new, "unable to encode context");
      }
    }

    private boolean addGoogleProperties(Map<String, String> context, String referer) {
      Matcher match = GOOGLE_REGEXP.matcher(referer);
      if (match.matches()) {
        context.put(SEARCHENGINE_PROP, "google");
        context.put(QUERY_PROP, urlDecode(match.group(REGEXP_URL_POSITION)));
        return true;
      } else {
        return false;
      }
    }

    private boolean addBingProperties(Map<String, String> context, String referer) {
      Matcher match = BING_REGEXP.matcher(referer);
      if (match.matches()) {
        context.put(SEARCHENGINE_PROP, "bing");
        context.put(QUERY_PROP, urlDecode(match.group(REGEXP_URL_POSITION)));
        return true;
      } else {
        return false;
      }
    }

    private boolean addYahooProperties(Map<String, String> context, String referer) {
      Matcher match = YAHOO_REGEXP.matcher(referer);
      if (match.matches()) {
        context.put(SEARCHENGINE_PROP, "yahoo");
        context.put(QUERY_PROP, urlDecode(match.group(REGEXP_URL_POSITION)));
        return true;
      } else {
        return false;
      }
    }

    private String urlDecode(String str) {
      assert str != null;
      try {
        return URLDecoder.decode(str, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        LOG.error("UTF-8 encoding not supported! Are you kidding me?!?", ex);
        return null;
      }
    }

    /**
     * Returns a human-readable representation of the state of this object. The format may change without notice.
     *
     * @return human-readable representation of this object
     */
    @Override
    public String toString() {
      return "[" + getClass().getName() + ']';
    }

    @Override
    public Object createNewContext() {
      return new ReferrerContext();
    }
  }

  public boolean isEmpty() {
    return referrers.isEmpty();
  }

  @Override
  public boolean isDirty() {
    return isDirty;
  }

  @Override
  public void setDirty(boolean value) {
    this.isDirty = value;
  }

  @Override
  public @PersonalData Object getProperty(String key) {
    return referrers.get(key);
  }

  @Override
  public <T> @PersonalData T getProperty(String key, T defaultValue) {
    T score = (T) referrers.get(key);
    return score != null ? score : defaultValue;
  }

  @Override
  public @PersonalData Collection<String> getPropertyNames() {
    return referrers.keySet();
  }

  /**
   * Returns a human-readable representation of the state of this object. The format may change without notice.
   *
   * @return human-readable representation of this object
   */
  @Override
  public String toString() {
    return "[" + getClass().getName() + ']';
  }
}
