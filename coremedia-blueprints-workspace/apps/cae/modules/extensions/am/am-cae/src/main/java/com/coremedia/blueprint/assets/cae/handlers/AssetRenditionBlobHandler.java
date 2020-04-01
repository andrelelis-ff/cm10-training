package com.coremedia.blueprint.assets.cae.handlers;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.blueprint.cae.handlers.CapBlobHandler;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapBlobRef;
import com.coremedia.objectserver.web.links.Link;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_EXTENSION;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_WORD;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ETAG;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_EXTENSION;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_NAME;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_PROPERTY;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_RESOURCE;
import static com.coremedia.objectserver.web.HandlerHelper.notFound;

/**
 * <p>
 *   Specialized blob handler for blobs from assets.
 * </p>
 * <p>
 *   The request handler also validates if the blob is still published.
 * </p>
 * <p>
 *   The default handling and link creation is delegated to the {@link CapBlobHandler}.
 * </p>
 */
@Link
@RequestMapping
public class AssetRenditionBlobHandler {

  private static final char ASSET_NAME_SEPARATOR = '-';

  //e.g. /resource/asset/126/4fb7741a1080d02953ac7d79c76c955c/xyz-print.pdf
  private static final String ASSET_URI_PATTERN = '/' + PREFIX_RESOURCE +
          "/asset" +
          "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}" +
          "/{" + SEGMENT_ETAG + "}" +
          "/{" + SEGMENT_NAME  + ":" + PATTERN_WORD + "}" +
          ASSET_NAME_SEPARATOR + "{" + SEGMENT_PROPERTY + "}" +
          ".{" + SEGMENT_EXTENSION + ":" + PATTERN_EXTENSION + "}";

  private CapBlobHandler capBlobHandler;
  private boolean isPreview;

  @Required
  public void setCapBlobHandler(CapBlobHandler capBlobHandler) {
    this.capBlobHandler = capBlobHandler;
  }

  @Value("${cae.is.preview:false}")
  public void setPreview(boolean preview) {
    this.isPreview = preview;
  }

  @GetMapping(value = ASSET_URI_PATTERN)
  public ModelAndView handleAssetRenditionRequest(@PathVariable(SEGMENT_ID) AMAsset asset,
                                                  @PathVariable(SEGMENT_ETAG) String eTag,
                                                  @PathVariable(SEGMENT_PROPERTY) String propertyName,
                                                  @PathVariable(SEGMENT_EXTENSION) String extension,
                                                  WebRequest webRequest,
                                                  HttpServletResponse response) {
    //maybe the asset name contains separator symbols, so we are only interested in the last segment
    String[] propertySegments = propertyName.split(String.valueOf(ASSET_NAME_SEPARATOR));
    String cleanPropertyName = propertySegments[propertySegments.length-1];

    // for live CAE, check if requested blob is a published rendition
    // before delegating the handling to the default cap blob handler
    List<AMAssetRendition> assetRenditions = isPreview
            ? asset.getRenditions()
            : asset.getPublishedRenditions();
    for (AMAssetRendition assetRendition : assetRenditions) {
      if (assetRendition.getName().equals(cleanPropertyName)) {
        // Set content disposition header to prevent opening of new tab for download
        response.addHeader("Content-Disposition", "attachment");
        return capBlobHandler.handleRequest(asset, eTag, cleanPropertyName, extension, webRequest, response);
      }
    }
    return notFound();
  }

  @Link(type = AMAssetRendition.class, uri = ASSET_URI_PATTERN)
  public Map<String, String> buildRenditionLink(AMAssetRendition assetRendition) {
    Blob blob = assetRendition.getBlob();
    if (blob == null) {
      throw new IllegalArgumentException("Cannot render link for asset rendition without blob");
    }
    Map<String, String> uriComponentParameters = new HashMap<>();
    uriComponentParameters.putAll(capBlobHandler.linkParameters((CapBlobRef) blob));

    return Collections.unmodifiableMap(uriComponentParameters);
  }
}
