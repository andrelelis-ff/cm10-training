package com.coremedia.blueprint.elastic.social.cae.guid;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.elastic.core.api.settings.Settings;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanIdConverter;
import com.coremedia.objectserver.web.links.Link;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.UUID;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;
import static java.lang.String.format;

/**
 * Handler capable of setting "guid" cookie for elastic social, if ES is activated in settings on the root channel.
 * The links are supposed to be generated and called within a javascript snippet on the home page.
 * An asynchronous javaScirpt call in this snippet passes then the control to the given handler, which sets "guid"
 * session cookie.
 */
@Link
@RequestMapping
public class GuidCookieHandler {

  private static final Logger LOG = LoggerFactory.getLogger(GuidCookieHandler.class);

  /**
   * The name of hte cookie set by this handler. The name is not configurable.
   */
  private String cookieName = "guid";

  /**
   * Public/private key pair used to generate GUID cookie
   */
  private final RSAKeyPair rsaKeyPair;

  private static final ThreadLocal<String> GUID_THREAD_LOCAL = new ThreadLocal<>();

  /**
   * URI segment indicating the links to be processed with the given handler
   */
  static final String GUID_COOKIE_PREFIX = "guid";

  /**
   * URI pattern, for URIs like "/dynamic/service/guid/corporate/6602"
   */
  private static final String URI_PATTERN =
      '/' + PREFIX_DYNAMIC +
      '/' + PREFIX_SERVICE +
      "/"+ GUID_COOKIE_PREFIX +
      "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}";

  private SettingsService settingsService;

  private ContentBeanIdConverter contentBeanIdConverter;

  @Inject
  public GuidCookieHandler(Settings settings) throws NoSuchAlgorithmException {
    this.rsaKeyPair = RSAKeyPair.createFrom(settings);
  }

  /**
   * Resolves links containing "/guid/" segment and sets "guid" cookie for elastic social, if ES is activated in
   * the settings of the root channel.
   *
   * @param channel     root channel
   * @param request     httpServletRequest
   * @param response    httpServletReqponse
   */
  @GetMapping(URI_PATTERN)
  public void handleRequest(@PathVariable(SEGMENT_ID) CMNavigation channel,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {

    Boolean esOn = settingsService.nestedSetting(Arrays.asList("elasticSocial", "enabled"), Boolean.class, channel);

    if (esOn != null && esOn) {
      setCookie(request, response);
    }
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  /**
   * Generates links containing "/guid/" segment which uniquely identify links to be resolved by the given handler,
   * which is capable of processing "guid" cookie.
   *
   * @param channel rootChannel of the site, needed to access site Settings, to check if ES is activated
   * @return UriComponents
   */
  @SuppressWarnings("UnusedDeclaration")
  @Link(type = CMNavigation.class, view = "asGuidCookieLink", uri = URI_PATTERN)
  @Nullable
  public UriComponents buildGUIDLink(@NonNull CMNavigation channel) {

    //we always generate link on the root channel
    channel = channel.isRoot() ? channel : channel.getRootNavigation();

    ImmutableMap<String, String> uriVariables = ImmutableMap.of(
      SEGMENT_ID, getNumericId(channel));

    return UriComponentsBuilder.newInstance().replacePath(URI_PATTERN).buildAndExpand(uriVariables);
  }

  private void setCookie(HttpServletRequest request, HttpServletResponse response){
    String guid = null;
    if (request != null) {
      guid = extractGuid(request);
    }

    if (guid == null || !validateGuid(guid)) {
      guid = createGuid();
      if (response != null) {
        Cookie cookie = new Cookie(cookieName, guid); // NOSONAR rule 'Cookies should be "secure"', but we need it anyway
        cookie.setPath("/");
        response.addCookie(cookie);
      }
    }

    setCurrentGuid(guid);
  }

  private String getNumericId(ContentBean source){
    return contentBeanIdConverter.convert(source);
  }

  boolean validateGuid(String guid) {
    if (Strings.isNullOrEmpty(guid)) {
      return false;
    }
    try {
      String uuid = extractUuidFromGuid(guid);
      String signature = extractSignatureFromGuid(guid);
      Signature sig = Signature.getInstance("SHA1withRSA");
      sig.initVerify(rsaKeyPair.getPublicKey());
      byte[] update = uuid.getBytes();
      sig.update(update, 0, update.length);
      byte[] verify = Hex.decodeHex(signature.toCharArray());
      return sig.verify(verify);
    } catch (DecoderException e) {
      LOG.warn(format("Hex decoder exception while validating signature for %s: %s", guid, e.getMessage()), e);
    } catch (NoSuchAlgorithmException e) {
      LOG.warn(format("No such algorithm while validating signature for %s: %s", guid, e.getMessage()), e);
    } catch (InvalidKeyException e) {
      LOG.warn(format("Invalid key while validating signature for %s: %s", guid, e.getMessage()), e);
    } catch (SignatureException e) {
      LOG.warn(format("Cannot validate signature for %s: %s", guid, e.getMessage()), e);
    } catch (IllegalArgumentException e) {
      LOG.warn(format("Invalid Guid: %s: %s", guid, e.getMessage()), e);
    }
    LOG.warn("Validation of given Guid failed, please check configuration of signCookie private and public key. A new Guid will be created.");
    return false;
  }

  private String extractGuid(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookieName.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  String createGuid() {
    String uuid = UUID.randomUUID().toString();
    try {
      Signature signature = Signature.getInstance("SHA1withRSA");
      signature.initSign(rsaKeyPair.getPrivateKey());
      byte[] bytes = uuid.getBytes();
      signature.update(bytes, 0, bytes.length);
      byte[] signed = signature.sign();
      return uuid + '+' + Hex.encodeHexString(signed);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException("No such algorithm while generating signature for " + uuid + ": " + e.getMessage(), e);
    } catch (InvalidKeyException e) {
      throw new IllegalArgumentException("Invalid key while generating signature for " + uuid + ": " + e.getMessage(), e);
    } catch (SignatureException e) {
      throw new IllegalArgumentException("Cannot generate signature for " + uuid + ": " + e.getMessage(), e);
    }
  }

  @Nullable
  public static String getCurrentGuid() {
    return GUID_THREAD_LOCAL.get();
  }

  public static void setCurrentGuid(String guid) {
    GUID_THREAD_LOCAL.set(guid);
  }

  public static String extractUuidFromGuid(String guid) {
    int index = guid.indexOf('+');
    if (index == -1) {
      throw new IllegalArgumentException("Not a valid guid: " + guid);
    }
    return guid.substring(0, index);
  }

  private static String extractSignatureFromGuid(String guid) {
    int index = guid.indexOf('+');
    if (index == -1) {
      throw new IllegalArgumentException("Not a valid guid: " + guid);
    }
    return guid.substring(index + 1);
  }

  public void setContentBeanIdConverter(ContentBeanIdConverter contentBeanIdConverter) {
    this.contentBeanIdConverter = contentBeanIdConverter;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

}
