package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.coderesources.ThemeService;
import com.coremedia.blueprint.common.contentbeans.CMDownload;
import com.coremedia.blueprint.common.contentbeans.CMImage;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapBlobRef;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentObject;
import com.coremedia.cap.user.User;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_EXTENSION;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_WORD;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ETAG;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_EXTENSION;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_NAME;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_PROPERTY;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_RESOURCE;
import static com.coremedia.objectserver.web.HandlerHelper.createModel;
import static com.coremedia.objectserver.web.HandlerHelper.notFound;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.decode;
import static org.springframework.web.util.UriUtils.encode;

/**
 * Handler and LinkScheme for {@link com.coremedia.cap.common.CapBlobRef blobs}.
 * <p>
 *   Per default generated links end with the pattern 'contentName-property.extension';
 *   the extension is then guessed by the {@link com.coremedia.mimetype.MimeTypeService}.
 * </p>
 * <p>
 *   For CMDownload content it is also possible to use the stored filename instead. This feature can be enabled
 *   by setting the boolean Setting {@code useCMDownloadFilename} to {@code true} in the respective CMDownload document
 *   or in its parent channel hierarchy. If no filename is set the default pattern is used as fallback.
 * </p>
 */
@Link
@RequestMapping
public class CapBlobHandler extends HandlerBase {
  static final String BLOB_DEFAULT_EXTENSION = "raw";

  private static final String CLASSIFIER_BLOB = "blob";
  private static final String CLASSIFIER_CODERESOURCEBLOB = "crblob";
  private static final String EMPTY_ETAG = "-";

  private static final String SEGMENT_FILENAME = "filename";

  //e.g. /resource/blob/126/4fb7741a1080d02953ac7d79c76c955c/media-favicon.ico
  public static final String URI_PATTERN = "/" + PREFIX_RESOURCE + "/" + CLASSIFIER_BLOB +
          "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}" +
          "/{" + SEGMENT_ETAG + "}" +
          "/{" + SEGMENT_FILENAME + "}";

  public static final String CODERESOURCEBLOB_URI_PATTERN = "/" + PREFIX_RESOURCE + "/" + CLASSIFIER_CODERESOURCEBLOB +
          "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}" +
          "/{" + SEGMENT_ETAG + "}" +
          "/{" + SEGMENT_NAME + "}" +
          "-{" + SEGMENT_PROPERTY + ":" + PATTERN_WORD + "}" +
          ".{" + SEGMENT_EXTENSION + ":" + PATTERN_EXTENSION + "}";

  private static final String NAME_PROP_EXT_REGEX = "(?<" + SEGMENT_NAME + ">.*)" +
          "-(?<" + SEGMENT_PROPERTY + ">" + PATTERN_WORD + ")" +
          ".(?<" + SEGMENT_EXTENSION + ">" + PATTERN_EXTENSION + ")";

  private ValidationService<ContentBean> validationService;
  private ThemeService themeService;
  private ContentBeanFactory contentBeanFactory;


  // --- configure --------------------------------------------------

  @Required
  public void setValidationService(ValidationService<ContentBean> validationService) {
    this.validationService = validationService;
  }

  @Required
  public void setThemeService(ThemeService themeService) {
    this.themeService = themeService;
  }

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }


  // --- Handlers ------------------------------------------------------------------------------------------------------

  @GetMapping(value = CODERESOURCEBLOB_URI_PATTERN)
  public ModelAndView handleCodeResourceBlobRequest(@PathVariable(SEGMENT_ID) ContentBean contentBean,
                                                    @PathVariable(SEGMENT_ETAG) String eTag,
                                                    @PathVariable(SEGMENT_PROPERTY) String propertyName,
                                                    @PathVariable(SEGMENT_EXTENSION) String extension,
                                                    WebRequest webRequest,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) {
    // Check for a developer variant first.
    // This can succeed on content management servers only.
    // Live servers do not support developer variants.
    User developer = UserVariantHelper.getUser(request);
    if (developer!=null) {
      CapBlobRef developerVariant = lookupDeveloperVariant(contentBean, propertyName, extension, developer);
      if (developerVariant != null) {
        // The eTag is meaningless in this case, since it refers to the
        // original blob.  Simply create a model.
        return createModel(developerVariant);
      }
    }

    // No developer variant found, return the standard result.
    // This is the standard case, esp. in production environments.
    return handleRequest(contentBean, eTag, propertyName, extension, webRequest, response);
  }

  @GetMapping(value = URI_PATTERN)
  public ModelAndView handleRequest(@PathVariable(SEGMENT_ID) ContentBean contentBean,
                                    @PathVariable(SEGMENT_ETAG) String eTag,
                                    WebRequest webRequest,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
    // we have to extract the filename ourselves because the injected path variable has problems with some special characters (e.g. ';')
    StringBuffer requestURL = request.getRequestURL();
    int lastSlashIndex = requestURL.lastIndexOf("/");
    String encodedFilename = requestURL.substring(lastSlashIndex + 1);
    String filename = decode(encodedFilename, UTF_8);

    if (contentBean instanceof CMDownload) {
      String storedFilename = ((CMDownload) contentBean).getFilename();
      if (!StringUtils.isEmpty(storedFilename) && storedFilename.equals(filename)) {
        return handleRequest(contentBean, eTag, CMDownload.DATA, null, webRequest, response);
      }
    }

    Matcher matcher = Pattern.compile(NAME_PROP_EXT_REGEX).matcher(filename);
    if (matcher.matches()) {
      String propertyName = matcher.group(SEGMENT_PROPERTY);
      String extension = matcher.group(SEGMENT_EXTENSION);

      return handleRequest(contentBean, eTag, propertyName, extension, webRequest, response);
    }

    return notFound();
  }

  public ModelAndView handleRequest(ContentBean contentBean,
                                    String eTag,
                                    String propertyName,
                                    String extension,
                                    WebRequest webRequest,
                                    HttpServletResponse response) {
    if (contentBean == null || !validationService.validate(contentBean)) {
      return notFound();
    }

    CapBlobRef blob = resolveBlob(contentBean, propertyName);

    if (blob == null) {
      return notFound();
    }

    if (webRequest.checkNotModified(blob.getETag())) {
      // shortcut exit - no further processing necessary
      return null;
    }

    if (extension != null && !validateBlobExtension(extension, blob)) {
      return notFound();
    }

    // URL validation: redirect to "correct" blob URL, if etag does not match.
    // The client may just have an old version of the URL.
    return doCreateModelWithView(eTagMatches(blob, eTag), blob, null, null, response);
  }

  // --- LinkSchemes ---------------------------------------------------------------------------------------------------

  @Link(type = CapBlobRef.class)
  public UriComponents buildLink(CapBlobRef bean) {
    return buildLink(bean, null);
  }

  @Link(type = CMDownload.class)
  @SuppressWarnings("unused")
  public String buildLinkForDownload(@NonNull CMDownload download, @Nullable String viewName) {
    if (FRAGMENT_PREVIEW.equals(viewName)) {
      // Do not build the download link for the fragment preview. Let other handlers build the link instead.
      return null;
    }
    CapBlobRef blob = (CapBlobRef) download.getData();
    return blob != null ? buildLink(blob, download.getFilename()).toUriString() : "#";
  }

  private UriComponents buildLink(CapBlobRef bean, String filename) {
    String classifier = mayHaveDeveloperVariants(bean) ? CLASSIFIER_CODERESOURCEBLOB : CLASSIFIER_BLOB;
    String id = String.valueOf(IdHelper.parseContentId(bean.getCapObject().getId()));
    String etag = bean.getETag();
    if (etag == null) {
      etag = EMPTY_ETAG;
    }

    String effectiveFilename;
    if (StringUtils.isEmpty(filename)) {
      String name = getName(bean);
      String propertyName = bean.getPropertyName();
      String extension = getExtension(bean.getContentType(), BLOB_DEFAULT_EXTENSION);
      effectiveFilename = name + "-" + propertyName + "." + extension;
    } else {
      effectiveFilename = encode(filename, UTF_8.name());
    }

    return UriComponentsBuilder.newInstance()
            .pathSegment(PREFIX_RESOURCE, classifier, id, etag, effectiveFilename)
            .build();
  }

  /**
   * Convenience link handler that directly creates links to a CMImage's data property.
   * @param image the CMImage
   * @return the link to the CMImage's data property
   */
  @Link(type = CMImage.class)
  public UriComponents buildLink(CMImage image) {
    return buildLink((CapBlobRef) image.getData());
  }

  /**
   * Useful for custom link building.
   */
  public Map<String, String> linkParameters(CapBlobRef bean) {
    String etag = bean.getETag();
    return ImmutableMap.<String, String>builder()
            .put(SEGMENT_ID, String.valueOf(IdHelper.parseContentId(bean.getCapObject().getId())))
            .put(SEGMENT_ETAG, etag != null ? etag : EMPTY_ETAG)
            .put(SEGMENT_NAME, getName(bean))
            .put(SEGMENT_PROPERTY, bean.getPropertyName())
            .put(SEGMENT_EXTENSION, getExtension(bean.getContentType(), BLOB_DEFAULT_EXTENSION))
            .build();
  }


  // === internal ======================================================================================================

  /**
   * Look for a developer variant of the blob.
   */
  private CapBlobRef lookupDeveloperVariant(ContentBean contentBean, String propertyName, String extension, User developer) {
    Content original = contentBean.getContent();
    Content developerVariant = themeService.developerVariant(original, developer);
    if (!original.equals(developerVariant)) {
      ContentBean developerVariantBean = contentBeanFactory.createBeanFor(developerVariant, ContentBean.class);
      if (developerVariantBean != null && validationService.validate(developerVariantBean)) {
        CapBlobRef blobRef = resolveBlob(developerVariantBean, propertyName);
        if (validateBlobExtension(extension, blobRef)) {
          return blobRef;
        }
      }
    }

    return null;
  }

  private CapBlobRef resolveBlob(ContentBean contentBean, String propertyName) {
    if (contentBean == null) {
      return null;
    }

    Content content = contentBean.getContent();
    CapPropertyDescriptor propertyDescriptor = content.getType().getDescriptor(propertyName);
    if (propertyDescriptor != null && Objects.equals(propertyDescriptor.getType(), CapPropertyDescriptorType.BLOB)) {
      return contentBean.getContent().getBlobRef(propertyName);
    }

    return null;
  }

  private boolean validateBlobExtension(String extension, CapBlobRef blobRef) {
    return blobRef != null && isValidExtension(extension, blobRef);
  }

  private boolean eTagMatches(CapBlobRef blob, String eTag) {
    String blobETag = blob.getETag();
    return blobETag != null ? blobETag.equals(eTag) : EMPTY_ETAG.equals(eTag);
  }

  private String getName(CapBlobRef o) {
    if (o.getCapObject().isContentObject() && ((ContentObject) o.getCapObject()).isContent()) {
      String contentName = ((Content) o.getCapObject()).getName();
      return removeSpecialCharacters(contentName);
    }
    throw new IllegalArgumentException("Not a Content Blob: " + o);
  }

  /**
   * Checks whether the blob possibly has developer variants.
   * <p>
   * Currently only CMImage documents are used in themes.
   */
  private boolean mayHaveDeveloperVariants(CapBlobRef blobRef) {
    return blobRef.getCapObject().getType().isSubtypeOf(CMImage.NAME);
  }

  private boolean isValidExtension(String extension, Blob blobRef) {
    if (extension == null) {
      return true;
    }
    String validExtension = getMimeTypeService().getExtensionForMimeType(blobRef.getContentType().getBaseType());
    if(extension.equalsIgnoreCase(validExtension) ) {
      return true;
    }
    if (LOG.isInfoEnabled()) {
      LOG.info("Requested blob property " + blobRef + " with illegal extension. Valid extensions are " + validExtension + " and no extension");
    }
    return BLOB_DEFAULT_EXTENSION.equals(extension);
  }
}
