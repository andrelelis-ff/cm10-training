package com.coremedia.blueprint.personalization.search;

import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.search.ArgumentMissingException;
import com.coremedia.personalization.search.ArgumentSyntaxException;
import com.coremedia.personalization.search.ArgumentUnknownException;
import com.coremedia.personalization.search.ArgumentValueException;
import com.coremedia.personalization.search.SearchFunctionEvaluationException;
import com.coremedia.personalization.search.SearchFunctionPreprocessor;
import com.coremedia.personalization.search.SearchFunctionUnknownException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.HashMap;
import java.util.Map;

/**
 * Encodes exceptions into a dictionary in JSON format
 * that is understood by the <code>SearchQueryPropertyField</code> Studio component defined in
 * the <code>studio/personalization-plugin</code> module.
 */
public final class SolrContextualSearchChecker {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrContextualSearchChecker.class);

  private static final String STATUS_CODE_KEY = "code";
  private static final String STATUS_FNNAME_KEY = "func";
  private static final String STATUS_PARAM_KEY = "param";
  private static final String STATUS_MSG_KEY = "msg";

  private static final String CODE_ARGUMENT_MISSING = "ARGUMENT_MISSING";
  private static final String CODE_ARGUMENT_SYNTAX = "ARGUMENT_SYNTAX";
  private static final String CODE_ARGUMENT_UNKNOWN = "ARGUMENT_UNKNOWN";
  private static final String CODE_ARGUMENT_VALUE = "ARGUMENT_VALUE";
  private static final String CODE_FUNCTION_EVALUATION = "FUNCTION_EVALUATION";
  private static final String CODE_FUNCTION_UNKNOWN = "FUNCTION_UNKNOWN";
  private static final String CODE_GENERAL = "GENERAL";

  private final ObjectMapper mapper = new ObjectMapper();

  private SearchFunctionPreprocessor searchPreprocessor;
  private ContextCollection contextCollection;

  @Required
  public void setSearchPreprocessor(SearchFunctionPreprocessor searchPreprocessor) {
    if(searchPreprocessor == null) {
      throw new IllegalArgumentException("searchPreprocessor must not be null");
    }
    this.searchPreprocessor = searchPreprocessor;
  }

  @Required
  public void setContextCollection(ContextCollection contextCollection) {
    if(contextCollection == null) {
      throw new IllegalArgumentException("contextCollection must not be null");
    }
    this.contextCollection = contextCollection;
  }


  /**
   * Preprocess search functions and return errors as JSON string (if any)
   * @param query to preprocess
   * @return null in case of success, else errors as JSON
   */
  public String checkSearchFunctions(final String query) {
    try {
      final String processedQuery = searchPreprocessor.process(query, contextCollection);
      LOGGER.debug("query pre-processing result of '{}' is '{}'", query, processedQuery);
    } catch (Exception e) {
      return toJSON(e);
    }
    return null;
  }

  /**
   * Creates a JSON representation of the supplied exception.
   *
   * @param ex  the execption to be encoded into a JSON string. Must not be <code>null</code>.
   *
   * @return the encoded exception or <code>null</code> if it couldn't be encoded
   */
  private String toJSON(final Exception ex) {
    // this map will later be encoded into JSON
    final Map<String, String> statusMap = new HashMap<>();

    // fill the statusMap with the appropriate properties, depending on the actual type of the exception
    statusMap.put(STATUS_MSG_KEY, ex.getMessage());
    if (ex instanceof SearchFunctionEvaluationException) {
      final SearchFunctionEvaluationException evalEx = (SearchFunctionEvaluationException)ex;
      statusMap.put(STATUS_FNNAME_KEY, evalEx.getFunctionName());
      final Throwable cause = ex.getCause();
      if (cause instanceof ArgumentMissingException) {
        statusMap.put(STATUS_CODE_KEY, CODE_ARGUMENT_MISSING);
        statusMap.put(STATUS_PARAM_KEY, ((ArgumentMissingException)cause).getArgumentName());
      } else if (cause instanceof ArgumentSyntaxException) {
        statusMap.put(STATUS_CODE_KEY, CODE_ARGUMENT_SYNTAX);
        statusMap.put(STATUS_PARAM_KEY, ((ArgumentSyntaxException)cause).getArgument());
      } else if (cause instanceof ArgumentUnknownException) {
        statusMap.put(STATUS_CODE_KEY, CODE_ARGUMENT_UNKNOWN);
        statusMap.put(STATUS_PARAM_KEY, ((ArgumentUnknownException)cause).getArgumentName());
      } else if (cause instanceof ArgumentValueException) {
        statusMap.put(STATUS_CODE_KEY, CODE_ARGUMENT_VALUE);
        statusMap.put(STATUS_PARAM_KEY, ((ArgumentValueException)cause).getArgumentName());
      } else {
        statusMap.put(STATUS_CODE_KEY, CODE_FUNCTION_EVALUATION);
      }
    } else if (ex instanceof SearchFunctionUnknownException) {
      final SearchFunctionUnknownException evalEx = (SearchFunctionUnknownException)ex;
      statusMap.put(STATUS_CODE_KEY, CODE_FUNCTION_UNKNOWN);
      statusMap.put(STATUS_FNNAME_KEY, evalEx.getFunctionName());
    } else {
      statusMap.put(STATUS_CODE_KEY, CODE_GENERAL);
    }

    try {
      return mapper.writeValueAsString(statusMap);
    } catch (final Exception exx) {
      LOGGER.warn("could not encode search status into JSON", exx);
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
    final StringBuilder builder = new StringBuilder();
    builder.append('[').append(getClass().getName()).append(']');
    return builder.toString();
  }
}
