package com.coremedia.blueprint.cae.handlers;

import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.cae.contentbeans.MergeableResourcesImpl;
import com.coremedia.blueprint.coderesources.CodeResourcesCacheKey;
import com.coremedia.blueprint.coderesources.CodeResourcesModel;
import com.coremedia.blueprint.common.contentbeans.CMAbstractCode;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.MergeableResources;
import com.coremedia.cache.Cache;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.Version;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.user.User;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import com.coremedia.xml.Markup;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_EXTENSION;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_SEGMENTS;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ETAG;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_EXTENSION;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_NAME;
import static com.coremedia.blueprint.coderesources.CodeResourcesModel.TYPE_CSS;
import static com.coremedia.blueprint.coderesources.CodeResourcesModel.TYPE_JS;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_RESOURCE;
import static com.coremedia.objectserver.web.HandlerHelper.createModel;
import static com.coremedia.objectserver.web.HandlerHelper.createModelWithView;
import static com.coremedia.objectserver.web.HandlerHelper.notFound;
import static com.coremedia.objectserver.web.HandlerHelper.redirectTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encodePathSegment;

/**
 * Handler and LinkScheme for all CSS and JavaScript to the requested navigation object
 * supports usage of local resources (from file), minification and merging of resources
 */
@Link
@RequestMapping
public class CodeResourceHandler extends HandlerBase implements ApplicationContextAware, InitializingBean {
  private static final Logger LOG = LoggerFactory.getLogger(CodeResourceHandler.class);

  @VisibleForTesting
  static final String MARKUP_PROGRAMMED_VIEW_NAME = "script";
  private static final String DEFAULT_EXTENSION = "css";

  // --- path segments ---
  private static final String SEGMENT_CHANNEL_WITH_THEME = "channelWithTheme";
  private static final String SEGMENT_CHANNEL_WITH_CODE = "channelWithCode";
  private static final String SEGMENT_PATH = "path";
  private static final String SEGMENT_HASH = "hash";
  private static final String SEGMENT_MODE = "mode";

  private static final String PREFIX_CSS = '/' + PREFIX_RESOURCE + '/' + TYPE_CSS;
  private static final String PREFIX_JS = '/' + PREFIX_RESOURCE + '/' + TYPE_JS;

  /**
   * Link to a merged resource.
   * <p/>
   * e.g. /resource/js/4/6/1035154981/body.js
   * <p>
   * The resources to be merged are taken from the theme of the first channel
   * (4) and the direct code of the second channel (6).  Both channels are
   * optional, which is denoted by 0.
   */
  private static final String URI_SUFFIX_BULK =
          "/{" + SEGMENT_CHANNEL_WITH_THEME + ":" + PATTERN_NUMBER + "}" +
          "/{" + SEGMENT_CHANNEL_WITH_CODE + ":" + PATTERN_NUMBER + "}" +
          "/{" + SEGMENT_HASH + "}" +
          "/{" + SEGMENT_MODE + "}" +
          ".{" + SEGMENT_EXTENSION + ":" + PATTERN_EXTENSION + "}";

  public static final String CSS_PATTERN_BULK = PREFIX_CSS + URI_SUFFIX_BULK;
  public static final String JS_PATTERN_BULK = PREFIX_JS + URI_SUFFIX_BULK;

  private static final Map<String, String> EXTENSION_TO_CODEPROPERTY = ImmutableMap.<String, String>builder()
          .put(TYPE_CSS, CMNavigation.CSS)
          .put(TYPE_JS, CMNavigation.JAVA_SCRIPT)
          .build();
  private static final Map<String, String> EXTENSION_TO_URLPATTERN = ImmutableMap.<String, String>builder()
          .put(TYPE_CSS, CSS_PATTERN_BULK)
          .put(TYPE_JS, JS_PATTERN_BULK)
          .build();

  /**
   * Link to a single resource
   * <p/>
   * e.g. /resource/css/media/reset-123-0.css
   */
  public static final String URI_PATTERN_SINGLE =
          '/' + PREFIX_RESOURCE +
          "/{" + SEGMENT_PATH + ":" + PATTERN_SEGMENTS + "}" +
          "/{" + SEGMENT_NAME + "}" +
          "-{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}" +
          "-{" + SEGMENT_ETAG + ":" + PATTERN_NUMBER + "}" +
          ".{" + SEGMENT_EXTENSION + ":" + PATTERN_EXTENSION + "}";


  /**
   * If local resources are used, links in CSS files match this pattern.
   */
  public static final String URI_PATTERN_SINGLE_CSS_LINK =
          '/' + PREFIX_RESOURCE +
          "/{" + SEGMENT_PATH + ":" + PATTERN_SEGMENTS + "}" +
          "/{" + SEGMENT_NAME + "}" +
          ".{" + SEGMENT_EXTENSION + ":" + PATTERN_EXTENSION + "}";

  // --- spring configured properties ---
  private CapConnection capConnection;
  private Cache cache;
  private ContentBeanFactory contentBeanFactory;
  private SitesService sitesService;

  // --- settings for minification, merging and local resources ---
  private boolean localResourcesEnabled = false;
  private boolean developerModeEnabled = false;

  // --- spring config -------------------------------------------------------------------------------------------------

  @Required
  public void setCapConnection(CapConnection capConnection) {
    this.capConnection = capConnection;
  }

  /**
   * Links are generated to local resources instead of resources in the content repository if enabled.
   * <p/>
   * Default: false.
   */
  public void setLocalResourcesEnabled(boolean localResourcesEnabled) {
    this.localResourcesEnabled = localResourcesEnabled;
  }

  /**
   * CSS and JavaScript resources are generated as merged and minified if disabled.
   * <br/>
   * Must not be used with local resources.
   * <p/>
   * Default: false.
   */
  public void setDeveloperModeEnabled(boolean developerModeEnabled) {
    this.developerModeEnabled = developerModeEnabled;
  }

  @Required
  public void setCache(Cache cache) {
    this.cache = cache;
  }

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Override
  public void afterPropertiesSet() {
    if(!developerModeEnabled && localResourcesEnabled) {
      throw new IllegalStateException("Illegal setting for resource delivery detected! " +
          "Either turn on CAE developer mode or turn of local resources!");
    }
  }

  // --- Handlers ------------------------------------------------------------------------------------------------------

  /**
   * Handles requests for merged and minified CSS/JS.
   *
   * @param channelWithTheme The {@link com.coremedia.blueprint.common.contentbeans.CMNavigation} that carries the theme
   * @param channelWithCode The {@link com.coremedia.blueprint.common.contentbeans.CMNavigation} that carries code
   * @param extension    The file-extension that was asked for, usually "css" or "js".
   * @param webRequest   The web request
   * @return             The ModelAndView or 404 (not found).
   */
  @GetMapping(value = {JS_PATTERN_BULK,CSS_PATTERN_BULK})
  public ModelAndView handleRequest(@PathVariable(SEGMENT_CHANNEL_WITH_THEME) CMContext channelWithTheme,
                                    @PathVariable(SEGMENT_CHANNEL_WITH_CODE) CMContext channelWithCode,
                                    @PathVariable(SEGMENT_EXTENSION) String extension,
                                    @PathVariable(SEGMENT_HASH) String hash,
                                    @PathVariable(SEGMENT_MODE) String mode,
                                    HttpServletRequest servletRequest,
                                    HttpServletResponse servletResponse,
                                    WebRequest webRequest) {
    Content cwtContent = channelWithTheme==null ? null : channelWithTheme.getContent();
    Content cwcContent = channelWithCode==null ? null : channelWithCode.getContent();
    // Provide the site for absolute link building.  The linked documents
    // (typically fonts and technical images) belong to themes and thus have
    // no inherent site.
    Site site = fetchSite(cwcContent, cwtContent);
    if (site != null) {
      SiteHelper.setSiteToRequest(site, servletRequest);
    }
    User developer = UserVariantHelper.getUser(servletRequest);
    CodeResourcesCacheKey cacheKey = new CodeResourcesCacheKey(cwtContent, cwcContent, codePropertyName(extension), developerModeEnabled, developer);
    CodeResourcesModel codeResourcesModel = cache.get(cacheKey).getModel(mode);
    MergeableResources mergeableResources = new MergeableResourcesImpl(codeResourcesModel, contentBeanFactory, getDataViewFactory());

    String eTag = codeResourcesModel.getETag();
    if (webRequest.checkNotModified(eTag)) {
      // shortcut exit - no further processing necessary
      return null;
    }

    // check scripthash - the client may use an outdated resource link
    boolean isUpToDate = hash.equals(eTag);
    // if eTag does not match URL hash any longer, i.e. code resources have changed
    // else return correct MAV
    return doCreateModelWithView(isUpToDate, mergeableResources, extension, HttpStatus.SEE_OTHER, servletResponse);
  }

  /**
   * Handles requests to a single file linked in a CSS file
   *
   * @param baseName        The readable base name of the code resource, excluding the extension.
   * @param extension       The extension of the requested resource.
   * @return                The ModelAndView or 404 (not found).
   */
  @GetMapping(value = URI_PATTERN_SINGLE_CSS_LINK)
  public ModelAndView handleRequest(@PathVariable(SEGMENT_PATH) List<String> path,
                                    @PathVariable(SEGMENT_NAME) String baseName,
                                    @PathVariable(SEGMENT_EXTENSION) String extension,
                                    WebRequest webRequest) throws IOException, MimeTypeParseException {
    if (localResourcesEnabled) {
      return localResource(path, baseName, extension, webRequest);
    } else {
      return notFound();
    }
  }

  @GetMapping(value = URI_PATTERN_SINGLE)
  public ModelAndView handleRequest(@PathVariable(SEGMENT_PATH) List<String> path,
                                    @PathVariable(SEGMENT_ID) CMAbstractCode cmAbstractCode,
                                    @PathVariable(SEGMENT_ETAG) int version,
                                    @PathVariable(SEGMENT_NAME) String baseName,
                                    @PathVariable(SEGMENT_EXTENSION) String extension,
                                    WebRequest webRequest,
                                    HttpServletResponse response) throws IOException, MimeTypeParseException {
    if (localResourcesEnabled) {
      ModelAndView mav = localResource(path, baseName, extension, webRequest);
      // null represents "not modified" and is an appropriate return value
      if (mav==null || !HandlerHelper.isNotFound(mav)) {
        return mav;
      }
      // ... else fallbackthrough to contentResource ...
    }
    return contentResource(extension, cmAbstractCode, baseName, version, response, webRequest);
  }


  // === link schemes ==================================================================================================

  /**
   * Generated a link to a single resource file. Depending on resource settings,
   * the link will either point to a local file or a file inside the repository.
   *
   * @param cmAbstractCode  The contentBean, should be of type {@link com.coremedia.blueprint.common.contentbeans.CMAbstractCode}
   * @return                UriComponents of the generated link.
   */
  @Link(type = CMAbstractCode.class, uri = URI_PATTERN_SINGLE)
  public UriComponents buildLink(CMAbstractCode cmAbstractCode, UriTemplate uriTemplate) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(uriTemplate.toString());
    final String dataUrl = cmAbstractCode.getDataUrl();
    if (dataUrl != null && !dataUrl.isEmpty()) {
      return UriComponentsBuilder.fromUriString(dataUrl).build();
    }
    String extension = getExtension(cmAbstractCode.getContentType(), DEFAULT_EXTENSION);
    String resourceName = formatResourceName(cmAbstractCode);
    String path = formatContentPath(cmAbstractCode);
    if (path == null) {
      path = extension;
    }
    String id = getId(cmAbstractCode);
    String latestVersion = String.valueOf(getLatestVersion(cmAbstractCode.getContent()));
    return uriBuilder.buildAndExpand(path, resourceName, id, latestVersion, extension);
  }

  /**
   * Generated a link to a merged version of all resources of a page.
   * Use {@link com.coremedia.blueprint.base.links.UriConstants.Segments#SEGMENT_EXTENSION} via cm:param in cm:link to specify
   * the resources to use: "css" or "js" are available
   * To support {@link HandlerHelper#redirectTo(Object, String)} the extension may also passed as view parameter.
   *
   * @param mergeableResources The MergeableResources used to build the link.
   * @return A UriComponents containing the parts of the generated link.
   */
  @Link(type = MergeableResources.class)
  public UriComponents buildLink(MergeableResources mergeableResources) {
    CodeResourcesModel model = mergeableResources.getCodeResourceModel();
    String extension = model.getCodeType();
    Map<String,Object> parameters = ImmutableMap.<String, Object>builder()
            .put(SEGMENT_CHANNEL_WITH_THEME, segmentForCodeResourcesLink(model.getChannelWithTheme()))
            .put(SEGMENT_CHANNEL_WITH_CODE, segmentForCodeResourcesLink(model.getChannelWithCode()))
            .put(SEGMENT_HASH, model.getETag())
            .put(SEGMENT_MODE, model.getHtmlMode())
            .put(SEGMENT_EXTENSION, extension)
            .build();
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
    uriBuilder.path(urlPattern(extension));
    return uriBuilder.buildAndExpand(parameters);
  }


  // --- internal ---------------------------------------------------

  private Site fetchSite(Content... contents) {
    return Stream.of(contents).filter(Objects::nonNull)
            .map(c -> sitesService.getContentSiteAspect(c).getSite())
            .filter(Objects::nonNull).findFirst().orElse(null);
  }

  // creates a Markup/script model
  private ModelAndView contentResource(String extension, CMAbstractCode cmAbstractCode, String name,
                                       int version, HttpServletResponse response, WebRequest webRequest) {

    if (webRequest.checkNotModified(cmAbstractCode.getContent().getModificationDate().getTimeInMillis())) {
      // shortcut exit - no further processing necessary
      return null;
    }

    // URL validation: check that extension is OK and name matches expectation
    if (!isExtensionValid(extension, cmAbstractCode) || !isNameSegmentValid(name, cmAbstractCode)) {
      return notFound();
    }

    Markup markup = cmAbstractCode.getCode();
    if (markup == null) {
      return notFound();
    }

    int latestVersion = getLatestVersion(cmAbstractCode.getContent());
    boolean isUpToDate = version == latestVersion;
    if (isUpToDate  || !isSingleNode()) {
      response.setContentType(cmAbstractCode.getContentType());
      return createModelWithView(markup, MARKUP_PROGRAMMED_VIEW_NAME);
    } else if (version > 0 && version < latestVersion) {
      // let's redirect in single node mode
      return redirectTo(cmAbstractCode);
    }

    // the requested resource is known not to exist in single node mode
    return notFound();
  }

  // creates a Blob/DEFAULT model
  private ModelAndView localResource(List<String> path, String baseName, String extension, WebRequest webRequest) throws IOException, MimeTypeParseException {
    String name = baseName + '.' + extension;
    String resourcePath = '/' + joinPath(path) + '/' + name;
    Resource resource = obtainApplicationContext().getResource(resourcePath);
    if (resource.isReadable()) {
      if (webRequest.checkNotModified(resource.lastModified())) {
        // shortcut exit - no further processing necessary
        return null;
      }
      // buffer resource stream to make it markable
      @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
      InputStream bufferedInputStream = new BufferedInputStream(resource.getInputStream());
      String mimeType = getMimeTypeService().detectMimeType(bufferedInputStream, name, null);
      Blob blob = capConnection.getBlobService().fromInputStream(bufferedInputStream, mimeType);
      return createModel(blob);
    } else {
      LOG.warn("File {} not found in local resources, but was linked in the content!", resourcePath);
      return notFound();
    }
  }

  private String codePropertyName(String extension) {
    String result = EXTENSION_TO_CODEPROPERTY.get(extension);
    if (result==null) {
      throw new IllegalArgumentException("There is no CMNavigation code property for " + extension);
    }
    return result;
  }

  private String urlPattern(String extension) {
    String result = EXTENSION_TO_URLPATTERN.get(extension);
    if (result==null) {
      throw new IllegalArgumentException("There is no URL pattern for " + extension);
    }
    return result;
  }

  /**
   * Helper Method to retrieve the latest checked in version of a resource content object.
   * @param content the content object
   * @return the current version.
   */
  private int getLatestVersion(Content content) {
    Version v = content.isCheckedIn() ? content.getCheckedInVersion() : content.getWorkingVersion();
    return IdHelper.parseVersionId(v.getId());
  }

  private static String formatContentPath(CMAbstractCode cmAbstractCode) {
    // path will contain file name as last element
    String contentPath = cmAbstractCode.getContent().getPath();
    if (contentPath==null) {
      // content is deleted, get last path.
      contentPath = cmAbstractCode.getContent().getLastPath();
    }
    if (contentPath!=null) {
      // remove root slash and filename, ignore case
      contentPath = contentPath.substring(1, contentPath.lastIndexOf('/')).toLowerCase();
    }
    return contentPath;
  }

  private String formatResourceName(CMAbstractCode code) {
    String name = code.getContent().getName();
    String extension = getExtension(code.getContentType(), DEFAULT_EXTENSION);
    if (name.endsWith('.'+extension)) {
      name = name.substring(0, name.length() - 1 - extension.length());
    }
    return uriEncode(name);
  }

  private String uriEncode(String name) {
    return encodePathSegment(name, UTF_8);
  }

  /**
   * Helper Method to check the validity of the resource name.
   * @param name The reference name to check against.
   * @param code The code object.
   * @return Result of the validity check.
   */
  private boolean isNameSegmentValid(String name, CMAbstractCode code) {
    return name != null && name.equals(formatResourceName(code));
  }

  /**
   * Helper Method to check the validity of the resource name.
   * @param extension The reference extension name to check against.
   * @param code The code object.
   * @return Result of the validity check.
   */
  private boolean isExtensionValid(String extension, CMAbstractCode code) {
    return extension != null && extension.equals(getExtension(code.getContentType(), DEFAULT_EXTENSION));
  }

  private static int segmentForCodeResourcesLink(Content content) {
    return content==null ? 0 : IdHelper.parseContentId(content.getId());
  }
}
