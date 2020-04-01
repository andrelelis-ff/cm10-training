package com.coremedia.livecontext.handler;

import com.coremedia.objectserver.web.links.Link;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;

/**
 * Redirects to the given URL in the path.
 * <p>
 * The URL must be encoded once except the / character, it must be encoded twice.
 * Otherwise Spring wouldn't find the handler.
 * <p>
 * The link is rendered containing a replaceable part in the servlet path which can be replaced with the url where the
 * handler must redirect to.
 */
@RequestMapping
@Link
public class NextURLRedirectHandler {

  /**
   * The placeholder to replace with the url to redirect to.
   */
  public static final String LINK_PLACEHOLDER = "NEXT_URL_PLACEHOLDER";

  private static final String NEXT_URL = "nexturl";

  private static final String LINK = '/' + PREFIX_SERVICE + "/nexturl/" + LINK_PLACEHOLDER;

  private static final String URI_PATTERN = '/' + PREFIX_SERVICE + "/nexturl/" + "{" + NEXT_URL + ":.*}";

  @GetMapping({URI_PATTERN})
  public String handleRequest(@PathVariable(NEXT_URL) String nexturl) throws UnsupportedEncodingException {
    String decode = URLDecoder.decode(nexturl, "UTF-8");
    return "redirect:" + decode;
  }

  @Link(type = LinkTypeNextUrl.class)
  public String buildLink(HttpServletRequest request) {
    return LINK;
  }

  public enum LinkTypeNextUrl {
    NEXTURL
  }
}
