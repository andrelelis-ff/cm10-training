package com.coremedia.blueprint.personalization.interceptors;

import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.common.logging.PersonalDataLogger;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.PropertyProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

/**
 * Memorizes the IDs of the last visited pages for the current user - up to the value of listSize (through Spring, default: 3)
 */
public class LastVisitedInterceptor extends HandlerInterceptorAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(LastVisitedInterceptor.class);
  private static final PersonalDataLogger PERSONAL_DATA_LOG = new PersonalDataLogger(LOG);

  private static final int DEFAULT_LIST_SIZE = 3;
  public static final String PAGES_VISITED = "pagesVisited";

  private String contextName;
  private ContextCollection contextCollection;
  private int listSize = DEFAULT_LIST_SIZE;

  /**
   * sets the current context name (via spring)
   *
   * @param contextName the name of the context
   */
  @Required
  public void setContextName(String contextName) {
    if(contextName == null) {
      throw new IllegalArgumentException("contextName must not be null");
    }
    this.contextName = contextName;
  }

  /**
   * Getter for the context name
   *
   * @return the context name
   */
  public String getContextName() {
    return contextName;
  }

  /**
   * set's the context collection (via spring)
   *
   * @param contextCollection the contextCollection to use
   */
  @Required
  public void setContextCollection(ContextCollection contextCollection) {
    if(contextCollection == null) {
      throw new IllegalArgumentException("contextCollection must not be null");
    }
    this.contextCollection = contextCollection;
  }

  /**
   * getter for the context collection
   *
   * @return the context collection
   */
  public ContextCollection getContextCollection() {
    return contextCollection;
  }

  @Override
  public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                         final Object handler, final ModelAndView modelAndView) {
    if (modelAndView != null) {
      // get bean from request (through model and view)
      final Object self = HandlerHelper.getRootModel(modelAndView);
      // bean is a page object?
      if (self instanceof Page) {
        Object bean = ((Page) self).getContent();
        // even teasable?
        if (bean instanceof CMTeasable) {
          // get content and content ID
          updateContext(((CMTeasable)bean).getContentId());
        }
      }
    }
  }

  private void updateContext(Integer id) {
    final @PersonalData Object contextObject = contextCollection.getContext(contextName);
    // we check for a PropertyProfile instance here so that we can store a list in it (com.coremedia.personalization.context.BasicPropertyMaintainer supports primitive values only)
    if (contextObject instanceof PropertyProfile) {
      final @PersonalData PropertyProfile context = (PropertyProfile) contextObject;

      // store ids in a list of size #listSize
      final @PersonalData List<Integer> currentValue = new ArrayList<>(listSize);
      currentValue.add(id); //

      final @PersonalData Object contextProperty = context.getProperty(PAGES_VISITED);
      if(contextProperty instanceof List) {
        @SuppressWarnings("unchecked")
        final @PersonalData List<Integer> lastVisited = (List<Integer>) contextProperty;

        @SuppressWarnings("PersonalData") // safe to pass @PersonalData List to #removeAll
        List<Integer> removeAll = currentValue;
        lastVisited.removeAll(removeAll);

        currentValue.addAll(lastVisited.subList(0,min(lastVisited.size(), listSize -1)));
      } else {
        LOG.debug("cannot handle context property of type {}", contextProperty != null ? contextProperty.getClass() : null);
      }

      PERSONAL_DATA_LOG.debug("last visited pages: {}", currentValue);
      context.setProperty(PAGES_VISITED, currentValue);
    } else {
      LOG.debug("cannot handle context of type {}", contextObject != null ? contextObject.getClass() : null);
    }
  }

  public void setListSize(int listSize) {
    this.listSize = listSize;
  }

  public int getListSize() {
    return listSize;
  }
}
