package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.action.webflow.WebflowActionState;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.objectserver.beans.ContentBean;
import com.google.common.collect.ImmutableMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_NAVIGATION;
import static org.springframework.util.CollectionUtils.isEmpty;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ACTION;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ROOT;

@SuppressWarnings("LocalCanBeFinal")
public class DefaultPageActionHandler extends WebflowHandlerBase {

  @Nullable
  protected UriComponents buildLinkInternal(
          @NonNull CMAction action,
          @NonNull UriTemplate uriPattern,
          @NonNull Map<String, Object> linkParameters) {

    Objects.requireNonNull(action, "No action provided for building a link.");

    String actionName = getVanityName(action);
    Navigation context = getNavigation(action);
    UriComponentsBuilder result = UriComponentsBuilder.fromPath(uriPattern.toString());
    result = addLinkParametersAsQueryParameters(result, linkParameters);
    List<String> pathSegments = getPathSegments(context);
    if (isEmpty(pathSegments)) {
      throw new IllegalStateException("Could not calculate the path segments for " + context);
    }

    return result.buildAndExpand(ImmutableMap.of(
            SEGMENT_ID, getId(action),
            SEGMENTS_NAVIGATION, joinPath(getPathSegments(context)),
            SEGMENT_ACTION, actionName
    ));
  }

  @Override
  protected WebflowActionState getWebflowActionState(CMAction action, ModelAndView webFlowOutcome, String flowId, String flowViewId) {
    return new WebflowActionState(action, webFlowOutcome.getModelMap(), flowId, flowViewId);
  }

}
