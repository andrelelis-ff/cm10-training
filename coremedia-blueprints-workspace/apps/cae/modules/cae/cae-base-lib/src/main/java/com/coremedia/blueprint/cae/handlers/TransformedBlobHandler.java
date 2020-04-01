package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.util.SecureHashCodeGeneratorStrategy;
import com.coremedia.blueprint.common.contentbeans.CMMedia;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapBlobRef;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentObject;
import com.coremedia.cap.transform.TransformImageService;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.links.Link;
import com.coremedia.transform.TransformedBeanBlob;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.activation.MimeType;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_EXTENSION;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_EXTENSION;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_NAME;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_RESOURCE;

/**
 * Controller and LinkScheme for transformed blobs
 *
 * @see com.coremedia.transform.TransformedBeanBlob
 */
@Link
@RequestMapping
public class TransformedBlobHandler extends HandlerBase {

  private static final Logger LOG = LoggerFactory.getLogger(TransformedBlobHandler.class);

  private static final String URI_PREFIX = "image";
  private static final String TRANSFORMATION_SEGMENT = "transformationName";
  private static final String DIGEST_SEGMENT = "digest";
  private static final String SECHASH_SEGMENT = "secHash";
  public static final String WIDTH_SEGMENT = "width";
  public static final String HEIGHT_SEGMENT = "height";

  private ValidationService<ContentBean> validationService = null;

  private SecureHashCodeGeneratorStrategy secureHashCodeGeneratorStrategy;
  private TransformImageService transformImageService;
  /**
   * URI Pattern for transformed blobs.
   * e.g. /image/4302/landscape_ratio4x3/590/442/969e0a0b2eb79df86df7ffecd1375115/eg/london.jpg
   */
  public static final String URI_PATTERN =
          '/' + PREFIX_RESOURCE +
          "/" + URI_PREFIX +
                  "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}" +
                  "/{" + TRANSFORMATION_SEGMENT + "}" +
                  "/{" + WIDTH_SEGMENT + ":" + PATTERN_NUMBER + "}" +
                  "/{" + HEIGHT_SEGMENT + ":" + PATTERN_NUMBER + "}" +
                  "/{" + DIGEST_SEGMENT + "}" +
                  "/{" + SECHASH_SEGMENT + "}" +
                  "/{" + SEGMENT_NAME + "}" +
                  ".{" + SEGMENT_EXTENSION + ":" + PATTERN_EXTENSION + "}";

  // --- spring config -------------------------------------------------------------------------------------------------

  public void setValidationService(ValidationService<ContentBean> validationService) {
    this.validationService = validationService;
  }

  public void setSecureHashCodeGeneratorStrategy(SecureHashCodeGeneratorStrategy secureHashCodeGeneratorStrategy) {
    this.secureHashCodeGeneratorStrategy = secureHashCodeGeneratorStrategy;
  }

  @Required
  public void setTransformImageService(TransformImageService transformImageService) {
    this.transformImageService = transformImageService;
  }

  // --- Handlers ------------------------------------------------------------------------------------------------------

  @GetMapping(value = URI_PATTERN)
  public ModelAndView handleRequest(@PathVariable(SEGMENT_ID) ContentBean contentBean,
                                    @PathVariable(TRANSFORMATION_SEGMENT) String transformationName,
                                    @PathVariable(WIDTH_SEGMENT) Integer width,
                                    @PathVariable(HEIGHT_SEGMENT) Integer height,
                                    @PathVariable(DIGEST_SEGMENT) String digest,
                                    @PathVariable(SECHASH_SEGMENT) String secHash,
                                    @PathVariable(SEGMENT_NAME) String name,
                                    @PathVariable(SEGMENT_EXTENSION) String extension,
                                    WebRequest webRequest) {
    if (!(contentBean instanceof CMMedia)) {
      return HandlerHelper.notFound();
    }
    if (validationService != null && !validationService.validate(contentBean)) {
      return HandlerHelper.notFound("The content item you are trying to preview is invalid and cannot be viewed.");
    }

    CMMedia media = getDataViewFactory().loadCached((CMMedia) contentBean, null);
    // URL validation: segment must match and hash value must be correct
    String segment = removeSpecialCharacters(media.getContent().getName());
    if (name.equals(segment)) {
      //name matches, make sure that secHash matches given URL
      Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
              .put(SEGMENT_ID, ((CMMedia) contentBean).getContentId())
              .put(TRANSFORMATION_SEGMENT, transformationName)
              .put(WIDTH_SEGMENT, width)
              .put(HEIGHT_SEGMENT, height)
              .put(DIGEST_SEGMENT, digest)
              .put(SEGMENT_NAME, name)
              .put(SEGMENT_EXTENSION, extension)
              .build();
      if (secureHashCodeGeneratorStrategy.matches(parameters, secHash)) {
        //request is valid, resolve blob and return model
        Blob transformedBlob = getTransformedBlob(media, transformationName, extension, width, height);
        if (transformedBlob != null) {
          if (webRequest.checkNotModified(transformedBlob.getETag())) {
            // shortcut exit - no further processing necessary
            return null;
          }
          return HandlerHelper.createModel(transformedBlob);
        }
      }
    }

    return HandlerHelper.notFound();
  }

  // --- LinkSchemes ---------------------------------------------------------------------------------------------------

  @Link(type = TransformedBeanBlob.class, parameter = {HEIGHT_SEGMENT, WIDTH_SEGMENT}, uri = URI_PATTERN)
  public Map<String, ?> buildLink(TransformedBeanBlob bean, Map<String, String> linkParameters) {
    if (!(bean.getBean() instanceof CMMedia)) {
      return null;
    }

    CapBlobRef original = (CapBlobRef) bean.getOriginal();
    int contentId = IdHelper.parseContentId(original.getCapObject().getId());

    int height = Integer.valueOf(linkParameters.get(HEIGHT_SEGMENT));
    int width = Integer.valueOf(linkParameters.get(WIDTH_SEGMENT));

    /*
     * create parameters map. This is more flexible than calling URI_TEMPLATE#expand with the parameters
     * since this way the parameter's sequence is not relevant and the URI_PATTERN can be changed easier
     */
    // Use content type of original blob, not of the transformed blob which may be different.
    // Requesting the transformed blob's content type forces the transformation to be performed, which is too
    // costly for link generation.
    MimeType contentType = original.getContentType();
    Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
            .put(SEGMENT_ID, contentId)
            .put(TRANSFORMATION_SEGMENT, bean.getTransformName())
            .put(WIDTH_SEGMENT, width)
            .put(HEIGHT_SEGMENT, height)
            .put(DIGEST_SEGMENT, bean.getETag())
            .put(SEGMENT_NAME, getName(original))
            .put(SEGMENT_EXTENSION, getExtension(contentType, CapBlobHandler.BLOB_DEFAULT_EXTENSION))
            .build();

    //generate secure hash from all parameters and add to map
    String secHash = secureHashCodeGeneratorStrategy.generateSecureHashCode(parameters);

    return ImmutableMap.<String, Object>builder()
            .putAll(parameters)
            .put(SECHASH_SEGMENT, secHash)
            .build();
  }

  /**
   * Return the transformed blob contained in the given CMMedia object, including all additional delivery transformations.
   * The returned blob may then be used to render information of the transformed blob in addition to the link, such as
   * the download size.
   *
   * <p>Note that accessing any of the methods {@link com.coremedia.cap.common.Blob#getContentType()}
   * or {@link com.coremedia.cap.common.Blob#getSize()} will trigger the transformation of the blob. While the
   * transformation result is cached, this is generally much more costly than merely generating a link to the
   * transformed blob, esp. if transformed blobs are cached by a CDN.
   */
  public Blob getTransformedBlob(CMMedia media, String transformName, String extension, Integer width, Integer height) {
    Optional<Blob> data = transformImageService.transformWithDimensions(media.getContent(), CMMedia.DATA, transformName, width, height);
    return data.orElse(null);
  }

  // === internal ======================================================================================================

  private String getName(CapBlobRef o) {
    if (o.getCapObject().isContentObject() && ((ContentObject) o.getCapObject()).isContent()) {
      String contentName = ((Content) o.getCapObject()).getName();
      return removeSpecialCharacters(contentName);
    }
    throw new IllegalArgumentException("Not a Content Blob: " + o);
  }
}
