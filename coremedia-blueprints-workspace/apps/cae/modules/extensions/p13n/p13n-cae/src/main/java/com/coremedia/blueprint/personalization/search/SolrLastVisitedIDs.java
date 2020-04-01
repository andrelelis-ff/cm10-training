package com.coremedia.blueprint.personalization.search;

import com.coremedia.blueprint.personalization.interceptors.LastVisitedInterceptor;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.objectserver.beans.ContentBeanIdScheme;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.PropertyProvider;
import com.coremedia.personalization.search.ArgumentMissingException;
import com.coremedia.personalization.search.SearchFunction;
import com.coremedia.personalization.search.SearchFunctionArguments;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collection;

/**
 * A search function to use the context provided by {@link com.coremedia.blueprint.personalization.interceptors.LastVisitedInterceptor}
 * in a SOLR query.
 */
public class SolrLastVisitedIDs implements SearchFunction {

  private static final Logger LOG = LoggerFactory.getLogger(SolrLastVisitedIDs.class);

  /**
   * The parameter for the used search engine field
   */
  public static final String SEARCH_ENGINE_FIELD_PARAMETER = "field";

  /**
   * The parameter for the used context name
   */
  public static final String CONTEXT_NAME_PARAMETER = "context";

  private String defaultField;
  private String defaultContextName;


  public String getDefaultField() {
    return defaultField;
  }

  public void setDefaultField(String defaultField) {
    this.defaultField = defaultField;
  }

  public String getDefaultContextName() {
    return defaultContextName;
  }

  public void setDefaultContextName(String defaultContextName) {
    this.defaultContextName = defaultContextName;
  }

  @Override
  public String evaluate(ContextCollection contextCollection, SearchFunctionArguments args) {
    if (contextCollection == null) {
      return "";
    }

    final String field = initializeField(args);
    final String contextName = initializeContextName(args);

    // build result string
    final StringBuilder builder = new StringBuilder();
    final @PersonalData Object contextObject = contextCollection.getContext(contextName);
    if (contextObject instanceof PropertyProvider) {
      final @PersonalData PropertyProvider context = (PropertyProvider) contextObject;

      // Suppress warning about assigning @PersonalData to non-annotated variable. We assume that the visited
      // pages as encoded in the result of this SearchFunction do not alone suffice to identify a person anymore.
      @SuppressWarnings("PersonalData")
      final Object contextProperty = context.getProperty(LastVisitedInterceptor.PAGES_VISITED);
      if(contextProperty instanceof Collection) {
        @SuppressWarnings("unchecked")
        final Collection<Object> lastVisited = (Collection<Object>) contextProperty;
        if(!lastVisited.isEmpty()) {
          final Collection<String> contentBeanIds = Collections2.transform(lastVisited, new ContentBeanIdFormattingFunction(lastVisited.size()));
          builder.append(field).append(":(");
          builder.append(StringUtils.join(contentBeanIds, " OR "));
          return builder.append(")").toString();
        } else {
          LOG.debug("ignoring empty list");
        }
      } else {
        LOG.debug("cannot handle context property of type {}", contextProperty != null ? contextProperty.getClass() : null);
      }
    } else {
      LOG.debug("cannot handle context of type {}", contextObject != null ? contextObject.getClass() : null);
    }
    return builder.append("1:2").toString();
  }

  private String initializeContextName(SearchFunctionArguments args) {
    String contextName = args.getString(CONTEXT_NAME_PARAMETER, this.defaultContextName);
    if (contextName == null) {
      throw new ArgumentMissingException(CONTEXT_NAME_PARAMETER, "the name of the context object containing the scores has to be" +
              " supplied via the '" + CONTEXT_NAME_PARAMETER + "' parameter");
    }
    return contextName;
  }

  private String initializeField(SearchFunctionArguments args) {
    String field = args.getString(SEARCH_ENGINE_FIELD_PARAMETER, this.defaultField);
    if (field == null) {
      throw new ArgumentMissingException(SEARCH_ENGINE_FIELD_PARAMETER, "the name of the solr index field that is" +
              " to be searched has to be supplied via the '" + SEARCH_ENGINE_FIELD_PARAMETER + "' parameter");
    }
    return field;
  }

  private static class ContentBeanIdFormattingFunction implements Function<Object, String> {
    private int boostFactor;

    public ContentBeanIdFormattingFunction(int initialBoostFactor) {
      this.boostFactor = initialBoostFactor;
    }

    @Override
    public String apply(@Nullable Object input) {
      final StringBuilder sb = new StringBuilder("\"").append(ContentBeanIdScheme.PREFIX).append(input).append("\"");
      if(boostFactor > 1) {
        sb.append("^").append(boostFactor--);
      }
      return sb.toString();
    }

  }
}
