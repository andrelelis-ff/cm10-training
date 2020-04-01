package com.coremedia.livecontext.fragment.links.context.accessors;

import com.coremedia.livecontext.fragment.links.context.Context;
import com.coremedia.livecontext.fragment.links.context.LiveContextContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Provide access to LiveContext
 * Call openAccessToContext to make context available via @link{LiveContextContextHelper}
 * <p>
 * Extracted to make available to Servlet Filters (required for Elastic Social)
 */
public class LiveContextContextAccessor {

  private static final Logger LOG = LoggerFactory.getLogger(LiveContextContextAccessor.class);

  private StandardContextResolver contextResolver = new StandardContextResolver();

  /**
   * Extract the context and make it available via LC1ContextHelper
   * Operation is idemPotent, i.e. can be called several times
   *
   * @return the context
   */
  @Nullable
  public Context openAccessToContext(@NonNull HttpServletRequest request) {
    try {
      Optional<Context> contextFromAttribute = LiveContextContextHelper.findContext(request);
      if (contextFromAttribute.isPresent()) {
        return contextFromAttribute.get();
      }

      Context contextFromHeaders = contextResolver.resolveContext(request);

      // Store the context and make it available to LiveContextContextHelper.
      LiveContextContextHelper.setContext(request, contextFromHeaders);
      return contextFromHeaders;
    } catch (Exception e) {
      LOG.error("Error retrieving LiveContext context", e);
      return null;
    }
  }
}
