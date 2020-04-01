package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.common.contentbeans.CMCollection;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.layout.Pagination;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.VIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ROOT;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;

@Link
@RequestMapping
public class PaginationHandler extends PageHandlerBase {
  private static final Logger LOG = LoggerFactory.getLogger(PaginationHandler.class);

  private static final String PARAMETER_PAGENUM = "pageNum";
  private static final String ACTION_NAME = "pagination";

  private static final String URI_PATTERN =
          '/' + PREFIX_SERVICE +
                  "/"+ACTION_NAME +
                  "/{" + SEGMENT_ROOT + "}" +
                  "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}";


  // --- Handler ----------------------------------------------------

  @GetMapping(value = URI_PATTERN)
  public <T> ModelAndView handlePaginationRequest(
          @PathVariable(SEGMENT_ID) CMCollection<T> container,
          @RequestParam(value = VIEW_PARAMETER, required = false) String view,
          @RequestParam(value = PARAMETER_PAGENUM, required = false, defaultValue = "0") int pagenum,
          HttpServletRequest request) {
    if (!container.isPaginated()) {
      LOG.debug("{} does not support pagination, cannot create pagination ModelAndView.", container);
      return HandlerHelper.notFound();
    }

    // Some templates rely on the page in the request
    CMNavigation navigation = getContextHelper().contextFor(container);
    Page page = asPage(navigation, container, UserVariantHelper.getUser(request));
    request.setAttribute(ContextHelper.ATTR_NAME_PAGE, page);

    // Create the actual model
    Pagination self = container.asPagination(pagenum);
    return HandlerHelper.createModelWithView(self, view);
  }


  // --- Link Scheme ------------------------------------------------

  @Link(type=Pagination.class, parameter=PARAMETER_PAGENUM, uri=URI_PATTERN)
  public UriComponents buildLinkForPagination(Pagination pagination,
                                              UriTemplate uriTemplate,
                                              Map<String, Object> linkParameters) {
    UriComponentsBuilder uri = UriComponentsBuilder.fromPath(uriTemplate.toString());
    uri = addLinkParametersAsQueryParameters(uri, linkParameters);
    CMNavigation navigation = getContextHelper().contextFor(pagination.linkable());
    return uri.buildAndExpand(ImmutableMap.of(
            SEGMENT_ID, getId(pagination.linkable()),
            SEGMENT_ROOT, navigation.getRootNavigation().getSegment()));
  }
}
