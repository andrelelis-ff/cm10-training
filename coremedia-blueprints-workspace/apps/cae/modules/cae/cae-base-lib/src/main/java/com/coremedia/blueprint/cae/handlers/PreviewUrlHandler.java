package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.objectserver.web.links.LinkFormatter;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_INTERNAL;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * A handler used for previewurl purposes: Takes a "id" request parameter and returns the url to the resource that is denoted
 * by this id.
 */
@CrossOrigin
@RequestMapping
@DefaultAnnotation(NonNull.class)
public class PreviewUrlHandler extends AbstractUrlHandler {

  private static final String PREFIX_PREVIEW = "preview";
  private static final String PREFIX_HANDLER = "previewurl";

  /**
   * Uri pattern for preview URLs.
   * e.g. /previewurl?id=123&view=fragmentPreview
   */
  public static final String URI_PATTERN =
          '/' + PREFIX_INTERNAL +
          '/' + PREFIX_PREVIEW +
          '/' + PREFIX_HANDLER;

  public static final String URI_PATTERN_LEGACY =
          '/' + PREFIX_HANDLER;

  public PreviewUrlHandler(LinkFormatter linkFormatter) {
    super(linkFormatter);
  }

  @GetMapping(value = URI_PATTERN, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<String> handle(@RequestParam("id") String id,
                                       @RequestParam(value = "view", required = false) String view,
                                       @RequestParam(value = "site", required = false) String siteId,
                                       @RequestParam(value = "context", required = false) Object context,
                                       @NonNull HttpServletRequest request,
                                       @NonNull HttpServletResponse response) {
    return super.getLink(id, view, siteId, context, request, response);
  }

  /**
   * @deprecated since 1910: use @{PreviewUrlHandler#handle(String, String, String, String, HttpServletRequest, HttpServletResponse)} instead
   */
  @GetMapping(value = URI_PATTERN_LEGACY, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  @Deprecated
  public ResponseEntity<String> handleId(@RequestParam("id") String id,
                                         @RequestParam(value = "view", required = false) String view,
                                         @RequestParam(value = "site", required = false) String siteId,
                                         @RequestParam(value = "context", required = false) Object context,
                                         @RequestParam(value = "taxonomyId", required = false) String taxonomyId,
                                         @NonNull HttpServletRequest request,
                                         @NonNull HttpServletResponse response) {
    storeTaxonomy(request, taxonomyId);
    return super.getLink(id, view, siteId, context, request, response);
  }

  /**
   * Stores the taxonomy content into the request.
   * The taxonomy parameter is used for the custom topic pages.
   *
   * @param taxonomyId The numeric content id of the taxonomy node.
   */
  private static void storeTaxonomy(@NonNull HttpServletRequest request, @Nullable String taxonomyId) {
    if (!isNullOrEmpty(taxonomyId)) {
      String id = IdHelper.formatContentId(taxonomyId);
      HttpSession session = request.getSession(true);
      session.setAttribute(RequestAttributeConstants.ATTR_NAME_PAGE_MODEL, id);
    }
  }
}
