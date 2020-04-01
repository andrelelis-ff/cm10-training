package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.common.contentbeans.CMTheme;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.links.Link;
import com.google.common.collect.ImmutableMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.VIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_RESOURCE;

/**
 * Controller and LinkScheme for
 * {@link CMTheme themes}
 */
@Link
@RequestMapping
public class ThemeHandler extends HandlerBase {

  private static final String URI_PREFIX = "theme";

  public static final String URI_PATTERN =
          '/' + PREFIX_RESOURCE +
                  "/" + URI_PREFIX +
                  "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}";

  // --- Handlers ------------------------------------------------------------------------------------------------------

  @GetMapping(value = URI_PATTERN)
  public ModelAndView handleRequest(@PathVariable(SEGMENT_ID) CMTheme theme,
                                    @RequestParam(value = VIEW_PARAMETER, required = false) String view) {

    return HandlerHelper.createModelWithView(theme, view);
  }


  // --- LinkSchemes ---------------------------------------------------------------------------------------------------

  @Link(type = CMTheme.class, uri = URI_PATTERN)
  public UriComponents buildLink(CMTheme bean, @Nullable String viewName) {
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance().path(URI_PATTERN);

    if (viewName != null) {
      uriComponentsBuilder.queryParam(VIEW_PARAMETER, viewName);
    }

    Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
            .put(SEGMENT_ID, bean.getContentId())
            .build();

    return uriComponentsBuilder.buildAndExpand(parameters);
  }
}
