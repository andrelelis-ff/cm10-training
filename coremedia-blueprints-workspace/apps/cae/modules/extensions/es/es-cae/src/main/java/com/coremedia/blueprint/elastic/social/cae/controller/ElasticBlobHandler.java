package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.cae.handlers.HandlerBase;
import com.coremedia.blueprint.cae.util.SecureHashCodeGeneratorStrategy;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.elastic.core.api.blobs.Blob;
import com.coremedia.elastic.core.api.blobs.BlobService;
import com.coremedia.objectserver.web.links.Link;
import com.coremedia.transform.BlobTransformer;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_WORD;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ETAG;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_NAME;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_RESOURCE;
import static com.coremedia.objectserver.web.HandlerHelper.createModel;
import static com.coremedia.objectserver.web.HandlerHelper.notFound;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Controller and LinkScheme for
 * {@link Blob blobs}
 */
@Named
@Link
@RequestMapping
public class ElasticBlobHandler extends HandlerBase {

  public static final String SECHASH_SEGMENT = "secHash";
  public static final String WIDTH_SEGMENT = "width";
  public static final String HEIGHT_SEGMENT = "height";
  public static final String TRANSFORM_SEGMENT = "transform";

  private static final String SEGMENT_PROPERTY = "segment";

  // default transformation matching the image dimensions hardcoded in Comment.jsp
  protected static final int DEFAULT_WIDTH = 48;
  protected static final int DEFAULT_HEIGHT = 48;
  protected static final int MAX_WIDTH = 1500;
  protected static final int MAX_HEIGHT = 1000;
  protected static final String URI_PREFIX = "elastic/image";
  private static final String SEGMENT_SITE = "site";
  protected static final String SIMPLE_URI_PATTERN =
          '/' + PREFIX_RESOURCE +
                  "/" + URI_PREFIX +
                  "/{" + SEGMENT_SITE + "}" +
                  "/{" + SEGMENT_ID + ":" + PATTERN_WORD + "}" +
                  "/{" + SEGMENT_ETAG + "}" +
                  "/{" + SECHASH_SEGMENT + "}" +
                  "/{" + SEGMENT_NAME + "}";
  protected static final String URI_PATTERN =
          '/' + PREFIX_RESOURCE +
                  "/" + URI_PREFIX +
                  "/{" + SEGMENT_SITE + "}" +
                  "/{" + SEGMENT_ID + ":" + PATTERN_WORD + "}" +
                  "/{" + SEGMENT_ETAG + "}" +
                  "/{" + WIDTH_SEGMENT + ":" + PATTERN_NUMBER + "}" +
                  "/{" + HEIGHT_SEGMENT + ":" + PATTERN_NUMBER + "}" +
                  "/{" + SECHASH_SEGMENT + "}" +
                  "/{" + SEGMENT_NAME + "}";

  private static final String EMPTY_ETAG = "-";
  @Inject
  private SecureHashCodeGeneratorStrategy secureHashCodeGeneratorStrategy;

  @Inject
  private BlobTransformer blobTransformer;

  @Inject
  private BlobService blobService;

  @Value("${cae.is.preview:false}")
  private boolean isCaePreview;

  protected String getName(Blob o) {
    return o.getFileName();
  }

  /**
   * @return Transformed blob contained in CMMedia object
   */
  protected com.coremedia.cap.common.Blob getTransformedBlob(com.coremedia.cap.common.Blob blob, Integer width, Integer height) {

    // Validate against extension of original blob. This may be different from the actual content type of the
    // transformed blob, but the link generator appends the original extension for performance reasons.
    // if (!BlobHelper.i(extension, blobAdapter, getMimeTypeService())) {
    //  return null;
    //}

    StringBuilder builder = new StringBuilder("scale").append(";");
    builder.append("w").append("=").append(width).append(";");
    builder.append("h").append("=").append(height);

    try {
      return blobTransformer.transformBlob(blob, builder.toString());
    } catch (IOException e) {
      LOG.info("Error transforming blob.", e);
      return null;
    }
  }

  protected boolean eTagMatches(Blob blob, String eTag) {
    String blobETag = blob.getMd5();
    return (blobETag != null ? blobETag.equals(eTag) : EMPTY_ETAG.equals(eTag));
  }

  protected boolean isValid(Map<String, Object> parameters, String secureHashCode) {
    return secureHashCodeGeneratorStrategy.matches(parameters, secureHashCode);
  }

  protected Map<String, ?> buildLinkMap(Blob bean, HttpServletRequest request) {

    /**
     * create parameters map. This is more flexible than calling URI_TEMPLATE#expand with the parameters
     * since this way the parameter's sequence is not relevant and the URI_PATTERN can be changed easier
     */
    // Use content type of original blob, not of the transformed blob which may be different.
    // Requesting the transformed blob's content type forces the transformation to be performed, which is too
    // costly for link generation.
    Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
            .put(SEGMENT_ID, bean.getId())
            .put(SEGMENT_ETAG, bean.getMd5())
            .put(SEGMENT_NAME, bean.getFileName())
            .put(SEGMENT_SITE, getSiteId(request))
            .build();

    //generate secure hash from all parameters and add to map
    String secHash = getSecureHashCode(parameters);

    return ImmutableMap.<String, Object>builder()
            .putAll(parameters).put(SECHASH_SEGMENT, secHash)
            .build();
  }

  private static String getSiteId(@NonNull HttpServletRequest request) {
    String absentSiteId = "-";

    Site siteFromRequest = SiteHelper.getSiteFromRequest(request);
    if (siteFromRequest == null) {
      LOG.warn("No site available for request path {}, check your configuration", request.getPathInfo());
      return absentSiteId;
    }

    Content siteRootDocument = siteFromRequest.getSiteRootDocument();
    if (siteRootDocument == null) {
      return absentSiteId;
    }

    return siteRootDocument.getString(SEGMENT_PROPERTY);
  }

  public Map<String, ?> buildLinkMapWithTransformation(Blob bean, Map<String, Object> linkParameters, HttpServletRequest request) {

    Integer height = (Integer) linkParameters.get(HEIGHT_SEGMENT);
    Integer width = (Integer) linkParameters.get(WIDTH_SEGMENT);

    if (height == null) {
      height = DEFAULT_HEIGHT;
    } else if (height > MAX_HEIGHT) {
      height = MAX_HEIGHT;
    }
    if (width == null) {
      width = DEFAULT_WIDTH;
    } else if (width > MAX_WIDTH) {
      width = MAX_WIDTH;
    }

    /**
     * create parameters map. This is more flexible than calling URI_TEMPLATE#expand with the parameters
     * since this way the parameter's sequence is not relevant and the URI_PATTERN can be changed easier
     */
    // Use content type of original blob, not of the transformed blob which may be different.
    // Requesting the transformed blob's content type forces the transformation to be performed, which is too
    // costly for link generation.
    Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
            .put(SEGMENT_ID, bean.getId())
            .put(SEGMENT_ETAG, bean.getMd5())
            .put(WIDTH_SEGMENT, width)
            .put(HEIGHT_SEGMENT, height)
            .put(SEGMENT_NAME, bean.getFileName())
            .put(SEGMENT_SITE, getSiteId(request))
            .build();

    //generate secure hash from all parameters and add to map
    String secHash = getSecureHashCode(parameters);

    return ImmutableMap.<String, Object>builder()
            .putAll(parameters)
            .put(SECHASH_SEGMENT, secHash)
            .build();
  }


  protected String getSecureHashCode(Map<String, Object> parameters) {
    return secureHashCodeGeneratorStrategy.generateSecureHashCode(parameters);
  }

  // --- Handlers ------------------------------------------------------------------------------------------------------

  @GetMapping(value = SIMPLE_URI_PATTERN)
  public ModelAndView handleRequest(@PathVariable(SEGMENT_SITE) String rootSegment,
                                    @PathVariable(SEGMENT_ID) String imageId,
                                    @PathVariable(SEGMENT_ETAG) String eTag,
                                    @PathVariable(SECHASH_SEGMENT) String secHash,
                                    @PathVariable(SEGMENT_NAME) String name,
                                    HttpServletResponse response) {

    ModelAndView result = notFound();

    if (isBlank(imageId)) {
      //no image id given
      return result;
    }

    Blob blob = blobService.get(imageId);

    if (blob == null || (!isCaePreview && blob.getOffline())) {
      return result;
    }

    boolean isUpToDate = eTagMatches(blob, eTag);
    result = doCreateModelWithView(isUpToDate, blob, null, null, response);

    // URL validation: segment must match and hash value must be correct
    String mediaSegment = removeSpecialCharacters(blob.getFileName());
    if (name.equals(mediaSegment)) {
      //name matches, make sure that secHash matches given URL
      Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
              .put(SEGMENT_ID, imageId)
              .put(SEGMENT_ETAG, eTag)
              .put(SEGMENT_NAME, name)
              .put(SEGMENT_SITE, rootSegment)
              .build();

      if (isValid(parameters, secHash)) {
        com.coremedia.cap.common.Blob resultBlob = new BlobAdapter(blob);
        return createModel(resultBlob);
      }
    }

    return result;
  }


  @GetMapping(value = URI_PATTERN)
  public ModelAndView handleRequest(@PathVariable(SEGMENT_SITE) String siteId,
                                    @PathVariable(SEGMENT_ID) String imageId,
                                    @PathVariable(SEGMENT_ETAG) String eTag,
                                    @PathVariable(WIDTH_SEGMENT) Integer width,
                                    @PathVariable(HEIGHT_SEGMENT) Integer height,
                                    @PathVariable(SECHASH_SEGMENT) String secHash,
                                    @PathVariable(SEGMENT_NAME) String name,
                                    HttpServletResponse response) {

    ModelAndView result = notFound();

    if (isBlank(imageId)) {
      //no image id given
      return result;
    }

    Blob blob = blobService.get(imageId);

    if (blob == null) {
      return result;
    }

    boolean isUpToDate = eTagMatches(blob, eTag);
    result = doCreateModelWithView(isUpToDate, blob, null, null, response);

    // URL validation: segment must match and hash value must be correct
    String mediaSegment = removeSpecialCharacters(blob.getFileName());
    if (name.equals(mediaSegment)) {
      //name matches, make sure that secHash matches given URL
      Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
              .put(SEGMENT_ID, imageId)
              .put(SEGMENT_ETAG, eTag)
              .put(WIDTH_SEGMENT, width)
              .put(HEIGHT_SEGMENT, height)
              .put(SEGMENT_NAME, name)
              .put(SEGMENT_SITE, siteId)
              .build();

      if (isValid(parameters, secHash)) {

        com.coremedia.cap.common.Blob resultBlob = new BlobAdapter(blob);
        //request is valid, transform blob and return model
        resultBlob = getTransformedBlob(resultBlob, width, height);

        if (resultBlob != null) {
          return createModel(resultBlob);
        }
      }
    }

    return result;
  }

  // --- LinkSchemes ---------------------------------------------------------------------------------------------------

  @Link(type = Blob.class, uri = SIMPLE_URI_PATTERN)
  public Map<String, ?> buildLink(Blob bean, HttpServletRequest request) {
    return buildLinkMap(bean, request);
  }

  @Link(type = Blob.class, parameter = {HEIGHT_SEGMENT, WIDTH_SEGMENT}, uri = URI_PATTERN)
  public Map<String, ?> buildLinkWithWidthAndHeight(Blob bean, Map<String, Object> linkParameters, HttpServletRequest request) {
    return buildLinkMapWithTransformation(bean, linkParameters, request);
  }

  @Link(type = Blob.class, parameter = {TRANSFORM_SEGMENT}, uri = URI_PATTERN)
  public Map<String, ?> buildLinkWithTransformation(Blob bean, Map<String, Object> linkParameters, HttpServletRequest request) {
    Object transform = linkParameters.get(TRANSFORM_SEGMENT);
    if (transform instanceof Boolean && (Boolean) transform) {
      return buildLinkMapWithTransformation(bean, linkParameters, request);
    } else {
      return buildLinkMap(bean, request);
    }
  }

  // the same link format is used for BlobRefs as for Blob
  @Link(type = BlobRef.class, uri = SIMPLE_URI_PATTERN)
  public Map<String, ?> buildLink(BlobRef bean, HttpServletRequest request) {
    Blob blob = blobService.get(bean.getId());
    return buildLinkMap(blob, request);
  }

  @Link(type = BlobRef.class, parameter = {HEIGHT_SEGMENT, WIDTH_SEGMENT}, uri = URI_PATTERN)
  public Map<String, ?> buildLinkWithWidthAndHeight(BlobRef bean, Map<String, Object> linkParameters, HttpServletRequest request) {
    Blob blob = blobService.get(bean.getId());
    return buildLinkMapWithTransformation(blob, linkParameters, request);
  }

  @Link(type = BlobRef.class, parameter = {TRANSFORM_SEGMENT}, uri = URI_PATTERN)
  public Map<String, ?> buildLinkWithTransformation(BlobRef bean, Map<String, Object> linkParameters, HttpServletRequest request) {
    Blob blob = blobService.get(bean.getId());
    Object transform = linkParameters.get(TRANSFORM_SEGMENT);
    if (transform instanceof Boolean && (Boolean) transform) {
      return buildLinkMapWithTransformation(blob, linkParameters, request);
    } else {
      return buildLinkMap(blob, request);
    }
  }
}
