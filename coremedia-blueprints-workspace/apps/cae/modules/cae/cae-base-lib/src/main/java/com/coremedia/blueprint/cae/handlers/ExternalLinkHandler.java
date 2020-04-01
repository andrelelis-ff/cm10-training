package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.common.contentbeans.CMExternalLink;
import com.coremedia.objectserver.web.links.Link;
import com.coremedia.objectserver.web.links.LiteralLink;
import org.apache.commons.lang3.StringUtils;

/**
 * Linkscheme for {@link com.coremedia.blueprint.common.contentbeans.CMExternalLink}
 */
@Link
public class ExternalLinkHandler extends HandlerBase {

  @Link(type = CMExternalLink.class)
  public LiteralLink buildLinkForExternalLink(CMExternalLink externalLink, String view) {
    if (FRAGMENT_PREVIEW.equals(view)) {
      // Do not build the external link for the fragment preview. Let other handlers build the link instead.
      return null;
    }
    String url = externalLink.getUrl();
    if (StringUtils.isBlank(url)) {
      return null;
    } else {
      return new LiteralLink(url);
    }
  }
}
