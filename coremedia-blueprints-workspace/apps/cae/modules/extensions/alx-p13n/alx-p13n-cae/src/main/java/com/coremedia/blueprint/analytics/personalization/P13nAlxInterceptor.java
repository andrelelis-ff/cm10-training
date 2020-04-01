package com.coremedia.blueprint.analytics.personalization;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.PropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <p>An interceptor that makes CoreMedia Personalization properties visible to a view.
 * It stores a map named 'p13nAlxProperties' in
 * the supplied model that contains the following properties:</p>
 * <ul>
 * <li><b>segmentIds:</b> String array of numeric ids of the personalization segments the current user
 * is a member of.</li>
 * <li><b>segmentNames:</b> String array of the names of the personalization segments the current user
 * is a member of.</li>
 * </ul>
 * <p>This interceptor assumes that segments are stored in a context of type {@link PropertyProvider}, and that
 * a segment is represented by a CAP content id (representing the segment).</p>
 */
public final class P13nAlxInterceptor extends HandlerInterceptorAdapter {
  private static final Logger LOG = LoggerFactory.getLogger(P13nAlxInterceptor.class);

  private static final String DEFAULT_SEGMENT_CONTEXT_NAME = "segment";
  private static final String ALX_PROPERTIES_KEY = "p13nAlxProperties";

  private ContextCollection contextCollection;
  private String segmentContextName = DEFAULT_SEGMENT_CONTEXT_NAME;

  private ContentRepository contentRepository;

  /**
   * Returns the context collection from which the segment context is retrieved.
   *
   * @return the context collection
   */
  public ContextCollection getContextCollection() {
    return contextCollection;
  }

  /**
   * The personalization context-collection from which the segment context is to be retrieved.
   *
   * @param contextCollection the personalization context-collection
   */
  @Required
  public void setContextCollection(final ContextCollection contextCollection) {
    if (contextCollection == null) {
      throw new IllegalArgumentException("supplied contextCollection must not be null");
    }
    this.contextCollection = contextCollection;
  }

  /**
   * Returns the name of the segment contexts.
   *
   * @return name of the segment context
   */
  public String getSegmentContextName() {
    return segmentContextName;
  }

  /**
   * Sets the content repository to retrieve segment content objects.
   *
   * @param contentRepository the content repository to be used
   */
  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    if (contentRepository == null) {
      throw new IllegalArgumentException("property contentRepository must not be null");
    }
    this.contentRepository = contentRepository;
  }

  /**
   * The name of the personalization context containing segment information.
   *
   * @param segmentContextName name of the segment context
   */
  public void setSegmentContextName(final String segmentContextName) {
    if (segmentContextName == null) {
      throw new IllegalArgumentException("supplied segmentContextName must not be null");
    }
    this.segmentContextName = segmentContextName;
  }

  @Override
  public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                         final Object handler, final ModelAndView modelAndView) {
    try {
      if (modelAndView != null && !modelAndView.wasCleared()) {
        final SegmentIdAndNameBuilder segmentIdAndNameBuilder = new SegmentIdAndNameBuilder();
        segmentIdAndNameBuilder.build();
        final Map<String,Object> properties = new HashMap<>(2);
        properties.put("segmentIds", segmentIdAndNameBuilder.segmentIds);
        properties.put("segmentNames", segmentIdAndNameBuilder.segmentNames);
        modelAndView.addObject(ALX_PROPERTIES_KEY, Collections.unmodifiableMap(properties));
      } else {
        LOG.debug("no modelAndView supplied - ignoring request");
      }
    } catch (final Exception ex) {
      LOG.warn("exception while extracting analytics properties", ex);
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

  /**
   * Helper class to build arrays of segment ids and names. Retrieves the
   * {@link com.coremedia.cap.content.Content} instances corresponding to the supplied
   * segment names. Any invalid content ids or names are skipped.
   */
  private final class SegmentIdAndNameBuilder {

    private String[] segmentNames;
    private String[] segmentIds;

    void build() {
      assert contextCollection != null;

      final @PersonalData PropertyProvider segmentContext = contextCollection.getContext(segmentContextName, PropertyProvider.class);

      final List<String> segmentIdList = new ArrayList<>();
      final List<String> segmentNameList = new ArrayList<>();

      if (segmentContext != null) {

        // extract the segment IDs and names
        for (final String segmentName : segmentContext.getPropertyNames()) {
          if (segmentContext.getProperty(segmentName, false)) {
            if (IdHelper.isContentId(segmentName)) {
              segmentIdList.add(Integer.toString(IdHelper.parseContentId(segmentName)));
              final Content content = contentRepository.getContent(segmentName);
              if (content != null) {
                segmentNameList.add(content.getName());
              } else {
                LOG.debug("'{}' does not exist, skipping segment", segmentName);
              }
            } else {
              LOG.info("cannot handle {} as segment, only CAP content ids are supported", segmentName);
            }
          }
        }

      } else {
        LOG.debug("no segment context of name '{}' available", segmentContextName);
      }

      this.segmentNames = segmentNameList.toArray(new String[segmentNameList.size()]);
      this.segmentIds = segmentIdList.toArray(new String[segmentIdList.size()]);
    }

  }
}