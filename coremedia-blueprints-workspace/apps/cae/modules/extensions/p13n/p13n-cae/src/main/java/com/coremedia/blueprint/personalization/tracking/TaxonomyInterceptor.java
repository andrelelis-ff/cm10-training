package com.coremedia.blueprint.personalization.tracking;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.scoring.ScoringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An  interceptor that can be placed into the list of interceptors of a Spring MVC chain to update the current
 * user's context based on the <code>self</code> bean stored in the {@link org.springframework.web.servlet.ModelAndView}.
 * <br/></br>
 * The interceptor assumes that the <code>self</code> bean provides access to a property containing a list of taxonomies.
 * <br/><br/>
 * Each taxonomy is supplied as an event to a {@link com.coremedia.personalization.scoring.ScoringContext} retrieved
 * from the installed {@link com.coremedia.personalization.context.ContextCollection}.
 */
public class TaxonomyInterceptor extends HandlerInterceptorAdapter {
  private static final Logger LOG = LoggerFactory.getLogger(TaxonomyInterceptor.class);

  private ContextCollection contextCollection;

  private final Map<String,String> propertyToContextMap = new HashMap<>();

  /**
   * The mapping configured here maps a property path relative to the current page's content bean
   * to a personalization context. The property path must resolve to a link list property of content
   * type CMTaxonomy.
   *
   * @param map the mapping to set
   */
  @Required
  public void setPropertyToContextMap(Map<String, String> map) {
    if(map == null || map.isEmpty()) {
      throw new IllegalArgumentException("propertyToContextMap mapping must neither be null nor empty");
    }
    this.propertyToContextMap.putAll(map);
  }

  /**
   * Sets the context collection used to retrieve the scoring context to be used by this interceptor.
   *
   * @param contextCollection the context collection to be used
   */
  @Required
  public void setContextCollection(final ContextCollection contextCollection) {
    this.contextCollection = contextCollection;
  }

  @Override
  public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                         final Object handler, final ModelAndView modelAndView) {
    try {
      if (modelAndView != null) {
        final Object self = HandlerHelper.getRootModel(modelAndView);
        if (self != null) {
          for(Map.Entry<String,String> entry : propertyToContextMap.entrySet()) {
            final String taxonomyPropertyName = entry.getKey();
            final List<String> taxonomies = extractTaxonomies(self, taxonomyPropertyName);
            if (!taxonomies.isEmpty()) {
              updateContext(taxonomies, entry.getValue());
            }
          }
        } else {
          LOG.debug("ModelAndView does not contain a bean called 'self'. A bean of this name is required for the " +
                  "interceptor to do its work.");
        }
      } else {
        LOG.debug("no modelAndView supplied - ignoring request");
      }
    } catch (final Exception ex) {
      LOG.warn("exception while updating keyword weights", ex);
    }
  }

  private List<String> extractTaxonomies(final Object self, String taxonomyPropertyName) {
    // extracts the list of taxonomies
    try {
      if (self instanceof Page && ((Page)self).getContent() instanceof CMLinkable) {
        final Object taxonomyProp = PropertyAccessorFactory.forBeanPropertyAccess(self).getPropertyValue("content." + taxonomyPropertyName);
        if (taxonomyProp instanceof List) {
          @SuppressWarnings("unchecked") List<CMTaxonomy> taxonomies = (List<CMTaxonomy>) taxonomyProp;
          final List<String> result = new ArrayList<>(taxonomies.size());
          for (CMTaxonomy taxonomy : taxonomies) {
            result.add(taxonomy.getContent().getId());
          }
          return result;
        } else {
          LOG.debug("Taxonomy property '{}' is empty or of invalid type (not a Linklist)", taxonomyPropertyName);
        }
      }
      return Collections.emptyList();
    } catch (InvalidPropertyException | PropertyAccessException ex) {
      LOG.debug("could not access the bean property containing the taxonomies to be processed");
      return Collections.emptyList();
    }
  }

  private void updateContext(final List<String> taxonomies, String contextName) {
    final @PersonalData ScoringContext context = contextCollection.getContext(contextName, ScoringContext.class);
    if (context != null) {
      LOG.debug("registering scores for context {}: {}", contextName, taxonomies);
      context.processEvents(taxonomies);
    } else {
      LOG.debug("no scoring context of name '{}' available in context collection; skipping keyword-weight update", contextName);
    }
  }
}
