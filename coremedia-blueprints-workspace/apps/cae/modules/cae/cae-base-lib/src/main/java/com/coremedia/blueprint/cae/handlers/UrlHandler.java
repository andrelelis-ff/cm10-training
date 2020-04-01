package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.objectserver.urlservice.UrlServiceRequestParams;
import com.coremedia.objectserver.urlservice.UrlServiceResponse;
import com.coremedia.objectserver.web.links.LinkFormatter;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_INTERNAL;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;

/**
 * A handler used to generate a CAE urls
 */
@RequestMapping
@DefaultAnnotation(NonNull.class)
public class UrlHandler extends AbstractUrlHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UrlHandler.class);

  public static final String OBJECT_NOT_FOUND = "object not found";
  public static final String SITE_NOT_FOUND = "site not found";

  private static final String PREFIX_HANDLER = "url";

  /**
   * Uri pattern for CAE URLs.
   * e.g. /url?id=123
   */
  public static final String URI_PATTERN =
          '/' + PREFIX_INTERNAL +
                  '/' + PREFIX_SERVICE +
                  '/' + PREFIX_HANDLER;

  public UrlHandler(LinkFormatter linkFormatter) {
    super(linkFormatter);
  }

  @PostMapping(value = URI_PATTERN, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<UrlServiceResponse> handleId(@RequestBody List<UrlServiceRequestParams> paramList,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
    LOG.debug("Incoming request. Request parameter list: {}", paramList);
    return paramList.stream().
            map(param -> {
              // cleanup request attributes for each iteration
              request.removeAttribute(SiteHelper.SITE_KEY);
              request.removeAttribute(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION);
              request.removeAttribute(ABSOLUTE_URI_KEY);
              ResponseEntity<String> entity;
              try {
                // note: 'view' param intentionally not used, since there is currently no use case for headless to request a certain view.
                entity = getLink(param.getId(), null, param.getSiteId(), param.getContext(), request, response);
                switch (entity.getStatusCode()) {
                  case BAD_REQUEST:
                    return UrlServiceResponse.createError(SITE_NOT_FOUND);

                  case NOT_FOUND:
                    return UrlServiceResponse.createError(OBJECT_NOT_FOUND);

                  case OK:
                    return UrlServiceResponse.createUrl(entity.getBody());

                  default:
                    return UrlServiceResponse.createError("illegal response state. " + entity.getBody());

                }
              } catch (Exception e) { // NOSONAR
                return UrlServiceResponse.createError(e.getMessage());
              }
            }).collect(Collectors.toList());
  }

}
