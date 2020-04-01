package com.coremedia.livecontext.fragment.links.context;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Helper for accessing a {@link Context livecontext context}.
 */
public final class LiveContextContextHelper {

  private static final String CONTEXT_ATTRIBUTE = "com.coremedia.livecontext.CONTEXT";

  /**
   * Utility class should not have a public default constructor.
   */
  private LiveContextContextHelper() {
  }

  /**
   * Stores a context in the request and makes it available.
   */
  public static void setContext(@NonNull HttpServletRequest request, @NonNull Context context) {
    request.setAttribute(CONTEXT_ATTRIBUTE, context);
  }

  /**
   * Retrieve the context. Will NOT create a context if it does not exist.
   */
  @NonNull
  public static Optional<Context> findContext(@NonNull HttpServletRequest request) {
    Context attribute = (Context) request.getAttribute(CONTEXT_ATTRIBUTE);
    return Optional.ofNullable(attribute);
  }
}
