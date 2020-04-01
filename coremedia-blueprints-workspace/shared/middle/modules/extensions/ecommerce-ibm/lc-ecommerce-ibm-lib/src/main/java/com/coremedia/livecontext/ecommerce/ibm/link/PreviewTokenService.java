package com.coremedia.livecontext.ecommerce.ibm.link;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.WORKSPACE_ID_NONE;

/**
 * Provides commerce storefront links
 */
public class PreviewTokenService implements InitializingBean {

  private static final Logger LOG = LoggerFactory.getLogger(PreviewTokenService.class);

  private static final String REQUEST_ATTRIB_PREVIEW_TOKEN = PreviewTokenService.class.getName() + "#previewToken";

  private WcPreviewTokenWrapperService previewTokenWrapperService;

  private CommerceCache commerceCache;

  private long previewTokenLifeTimeInSeconds = TimeUnit.HOURS.toSeconds(3);

  /**
   * Gets a preview token for the current store context.
   * The commerce system can decide if it provides a token or not.
   * The caller has to handle a null result.
   *
   * @param context the current store context
   * @return The preview token requested for the current store context
   */
  @Nullable
  public String getPreviewToken(@NonNull StoreContext context) {

    String result = null;

    HttpServletRequest request = getRequest();
    if (request != null) {
      result = (String) request.getAttribute(REQUEST_ATTRIB_PREVIEW_TOKEN);
    }

    if (result != null) {
      return result;
    }

    try {
      String workspaceIdStr = context.getWorkspaceId()
              .filter(workspaceId -> !workspaceId.equals(WORKSPACE_ID_NONE))
              .map(WorkspaceId::value)
              .orElse(null);
      Optional<ZonedDateTime> cmPreviewDate = context.getPreviewDate();
      String ibmFormattedPreviewDate = cmPreviewDate
              .map(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")::format)
              .orElse(null);
      String timezone = cmPreviewDate
              .map(date -> TimeZone.getTimeZone(date.getZone()).getID())
              .orElse(null);
      boolean isTimeFixed = cmPreviewDate.isPresent();

      WcPreviewTokenParam previewTokenParam = new WcPreviewTokenParam(
              workspaceIdStr,
              ibmFormattedPreviewDate,
              timezone,
              isTimeFixed ? "true" : "false",
              context.getUserSegments().orElse(null),
              String.valueOf(Math.max(1, previewTokenLifeTimeInSeconds / 60))
      );

      WcPreviewToken previewToken = commerceCache.get(
              new PreviewTokenCacheKey(previewTokenParam, context, previewTokenWrapperService, commerceCache));

      result = previewToken != null ? previewToken.getPreviewToken() : null;

      if (request != null) {
        request.setAttribute(REQUEST_ATTRIB_PREVIEW_TOKEN, result);
      }
    } catch (CommerceException e) {
      LOG.warn("Error getting preview token for store context: {}, message: {}", context, e.getMessage());
    }

    return result;
  }

  //--- private ---

  //--- wiring ---

  @Override
  public void afterPropertiesSet() throws Exception {

    long cacheDurationInSeconds = commerceCache.getCacheDurationInSeconds(PreviewTokenCacheKey.CONFIG_KEY_PREVIEW_TOKEN);
    //uapi cache key shall always expire before commerce previewToken expires
    if (previewTokenLifeTimeInSeconds < (cacheDurationInSeconds * 2)) {
      previewTokenLifeTimeInSeconds = cacheDurationInSeconds * 2;
      LOG.info("Increasing previewTokenLifeTimeInSeconds to (cacheDurationInSeconds * 2) = {}",
              previewTokenLifeTimeInSeconds);
    }
  }

  @Nullable
  private static HttpServletRequest getRequest() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes instanceof ServletRequestAttributes) {
      return ((ServletRequestAttributes) requestAttributes).getRequest();
    }
    return null;
  }

  /**
   * sets the lifetime for generated preview tokens in seconds.
   * Value might be overriden by {@link PreviewTokenCacheKey#CONFIG_KEY_PREVIEW_TOKEN} cache duration.
   * {@link #afterPropertiesSet} ensures that {@link #previewTokenLifeTimeInSeconds} is at least twice as long as
   * {@link PreviewTokenCacheKey#CONFIG_KEY_PREVIEW_TOKEN} cache setting in {@link CommerceCache#setCacheTimesInSeconds(Map)}
   * to avoid outdated previewToken in cache.
   *
   * @param previewTokenLifeTimeInSeconds (default is 3 hours, might be increased by higher cache duration times)
   */
  public void setPreviewTokenLifeTimeInSeconds(long previewTokenLifeTimeInSeconds) {
    this.previewTokenLifeTimeInSeconds = previewTokenLifeTimeInSeconds;
  }

  @Required
  public void setCommerceCache(CommerceCache commerceCache) {
    this.commerceCache = commerceCache;
  }

  @Required
  public void setPreviewTokenWrapperService(WcPreviewTokenWrapperService previewTokenWrapperService) {
    this.previewTokenWrapperService = previewTokenWrapperService;
  }
}
