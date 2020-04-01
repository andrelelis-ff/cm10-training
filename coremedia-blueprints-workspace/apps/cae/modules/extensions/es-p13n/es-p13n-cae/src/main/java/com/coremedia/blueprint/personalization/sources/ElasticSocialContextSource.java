package com.coremedia.blueprint.personalization.sources;

import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.elastic.core.api.models.Model;
import com.coremedia.elastic.core.api.models.ModelService;
import com.coremedia.elastic.core.api.users.User;
import com.coremedia.personalization.context.ContextCoDec;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.DirtyFlagMaintainer;
import com.coremedia.personalization.context.collector.AbstractContextSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.coremedia.common.logging.BaseMarker.PERSONAL_DATA;

/**
 * A {@link com.coremedia.personalization.context.collector.ContextSource} that stores and retrieves
 * contexts to and from CoreMedia's "Elastic Social".<br />
 */
public final class ElasticSocialContextSource extends AbstractContextSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSocialContextSource.class);
  private static final String P13N_CONTEXT_COLLECTION = "p13n_contexts";
  private static final String CONTEXT_PROPERTY = "encodedContext";

  // name of the context we're responsible for
  private String contextName;

  // codec used to transform the context from and to a String
  private ContextCoDec codec;

  // generic service to access data from Elastic Social
  private ModelService modelService;


  /* -----------------------------------------------

    Getters & Setters

   ----------------------------------------------- */

  /**
   * Sets the name of the context that is managed by this source. This is a required
   * property.
   *
   * @param contextName name of the context that is managed by this source. Must
   *                    not be <code>null</code>.
   */
  @Required
  public void setContextName(final String contextName) {
    if (contextName == null) {
      throw new IllegalArgumentException("supplied contextName must not be null");
    }
    this.contextName = contextName;
  }

  /**
   * Sets the codec used to serialize and deserialize context instances. This
   * is a required property.
   *
   * @param codec the codec to be used. Must not be <code>null</code>.
   */
  @Required
  public void setContextCoDec(final ContextCoDec codec) {
    if (codec == null) {
      throw new IllegalArgumentException("supplied codec must not be null");
    }
    this.codec = codec;
  }

  /**
   * Sets the {@link ModelService} to persist the context data with.
   *
   * @param modelService the modelService to be used. Must not be <code>null</code>.
   */
  @Required
  public void setModelService(final ModelService modelService) {
    if (modelService == null) {
      throw new IllegalArgumentException("supplied modelService must not be null");
    }
    this.modelService = modelService;
  }

/* -----------------------------------------------

   Elastic Social Helper methods

 ----------------------------------------------- */

  // persist Context in Elastic Social
  private void storeContext(User user, @PersonalData Object context) {
    Model codecModel = modelService.get(getContextModelKey(user), P13N_CONTEXT_COLLECTION);
    if (codecModel == null) {
      codecModel = modelService.create(getContextModelKey(user), P13N_CONTEXT_COLLECTION);
    }
    codecModel.setProperty(CONTEXT_PROPERTY, codec.stringFromContext(context));
    codecModel.save();
  }


  /* -----------------------------------------------

    Actual implementation of ContextSource interface

  ----------------------------------------------- */

  @Override
  public void preHandle(HttpServletRequest request, HttpServletResponse response, ContextCollection contextCollection) {
    assert contextCollection != null;
    assert contextName != null;
    assert codec != null;

    final User user = UserHelper.getLoggedInUser();
    if (user != null) {
      // retrieve the serialized context from Elastic Social
      final Model codecModel = modelService.get(getContextModelKey(user), P13N_CONTEXT_COLLECTION);
      final @PersonalData String serializedContext = (codecModel != null)
              ? codecModel.getProperty(CONTEXT_PROPERTY, String.class)
              : null;

      // deserialize the context
      if (serializedContext != null) {
        final @PersonalData Object context = codec.contextFromString(serializedContext);
        contextCollection.setContext(contextName, context);
      } else {
        LOGGER.debug(PERSONAL_DATA, "no existing context of name '{}' found for Elastic Social user '{}'; creating new context.", contextName, user.toIdString());
        contextCollection.setContext(contextName, codec.createNewContext());
      }
    } else {
      LOGGER.debug("no active Elastic Social user found; skipping profile retrieval");
    }
  }

  private String getContextModelKey(User user) {
    return user.getId() + "_" + contextName;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, ContextCollection contextCollection) {
    assert contextCollection != null;
    assert contextName != null;
    assert codec != null;

    final User user = UserHelper.getLoggedInUser();
    if (user != null) {
      final @PersonalData Object context = contextCollection.getContext(contextName);
      if (context != null) {
        if (context instanceof DirtyFlagMaintainer) {
          if (((DirtyFlagMaintainer) context).isDirty()) {
            storeContext(user, context);
            ((DirtyFlagMaintainer) context).setDirty(false);
          }
        } else {
          storeContext(user, context);
        }
      }
    } else {
      LOGGER.debug("cannot write context '{}' into Elastic Social repository because there isn't any active user", contextName);
    }
  }

  /* -----------------------------------------------

    Object's Overrides

   ----------------------------------------------- */

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
            append(", codec=").append(codec).append(']');
    return builder.toString();
  }
}
