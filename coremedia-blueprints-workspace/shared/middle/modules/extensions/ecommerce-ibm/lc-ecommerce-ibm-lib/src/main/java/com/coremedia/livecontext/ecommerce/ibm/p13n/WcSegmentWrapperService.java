package com.coremedia.livecontext.ecommerce.ibm.p13n;

import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdUtils;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractWcWrapperService;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestServiceMethod;
import com.coremedia.livecontext.ecommerce.ibm.user.UserContextHelper;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper.getStoreId;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_7;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

/**
 * A service that uses the rest connector to get segment information from IBM WebSphere Commerce.
 */
public class WcSegmentWrapperService extends AbstractWcWrapperService {

  private static final Logger LOG = LoggerFactory.getLogger(WcSegmentWrapperService.class);

  private static final WcRestServiceMethod<Map, Void> FIND_ALL_SEGMENTS = WcRestServiceMethod
          .builder(HttpMethod.GET, "store/{storeId}/segment", Void.class, Map.class)
          .secure(true)
          .requiresAuthentication(true)
          .previewSupport(true)
          .build();

  private static final WcRestServiceMethod<Map, Void> FIND_SEGMENT_BY_ID = WcRestServiceMethod
          .builder(HttpMethod.GET, "store/{storeId}/segment/{id}", Void.class, Map.class)
          .secure(true)
          .requiresAuthentication(true)
          .previewSupport(true)
          .build();

  private static final WcRestServiceMethod<Map, Void> FIND_SEGMENTS_BY_USER = WcRestServiceMethod
          .builder(HttpMethod.GET, "store/{storeId}/segment?q=byUserId&qUserId={id}", Void.class, Map.class)
          .secure(true)
          .requiresAuthentication(true)
          .previewSupport(true)
          .build();

  /**
   * Gets a list of all customer segments.
   *
   * @param storeContext the current store context
   * @param userContext  the current user context
   * @return A map which contains the JSON response data retrieved from the commerce server
   */
  @Nullable
  @SuppressWarnings("unchecked")
  public Map<String, Object> findAllSegments(@NonNull StoreContext storeContext, UserContext userContext) {
    try {
      if (StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_7_7)) {
        return emptyMap();
      }

      List<String> variableValues = singletonList(getStoreId(storeContext));
      Map<String, String[]> parameters = getOptionalParameters(storeContext);

      return getRestConnector().callService(FIND_ALL_SEGMENTS, variableValues, parameters, null, storeContext,
              userContext)
              .orElse(null);
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }

  /**
   * Gets a segment map by a given external id.
   *
   * @param externalId   the external id
   * @param storeContext the store context
   * @param userContext  the current user context
   * @return the segment map which contains the JSON response data retrieved from the commerce server or null if no spot was found
   * @throws com.coremedia.livecontext.ecommerce.common.CommerceException if something is wrong with the catalog connection
   */
  @Nullable
  @SuppressWarnings("unchecked")
  public Map<String, Object> findSegmentByTechId(String externalId, @NonNull StoreContext storeContext,
                                                 UserContext userContext) {
    try {
      if (StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_7_7)) {
        return null;
      }

      List<String> variableValues = asList(getStoreId(storeContext), externalId);
      Map<String, String[]> parameters = getOptionalParameters(storeContext);

      Map<String, Object> data = getRestConnector().callService(FIND_SEGMENT_BY_ID, variableValues, parameters, null,
              storeContext, userContext)
              .orElse(null);

      if (data != null) {
        List<Map<String, Object>> memberGroups = DataMapHelper.getList(data, "MemberGroup");
        if (!memberGroups.isEmpty()) {
          Map<String, Object> firstSegment = memberGroups.get(0);
          String segmentId = DataMapHelper.findString(firstSegment, "id").orElse("");
          if (!segmentId.isEmpty()) {
            return firstSegment;
          }
        }
      }

      return null;
    } catch (CommerceException e) {
      //ibm returns 403 or 500 instead of 404 for a unknown segment id
      LOG.warn("CommerceException", e);
      return null;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }

  /**
   * Gets a segment map by a given id (in a coremedia internal format like "ibm:///catalog/segment/081523924379243").
   *
   * @param id           the coremedia internal id
   * @param storeContext the store context
   * @param userContext  the current user context
   * @return the segment map which contains the JSON response data retrieved from the commerce server or null if no segment was found
   * @throws com.coremedia.livecontext.ecommerce.common.CommerceException  if something is wrong with the catalog connection
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidIdException if the id is in a wrong format
   */
  @Nullable
  @SuppressWarnings("unused")
  public Map<String, Object> findSegmentById(@NonNull CommerceId id, @NonNull StoreContext storeContext,
                                             UserContext userContext) {
    try {
      String externalId = CommerceIdUtils.getExternalIdOrThrow(id);
      return findSegmentByTechId(externalId, storeContext, userContext);
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }

  /**
   * Gets a map of customer segments that is associated to the current context user.
   *
   * @param storeContext the current store context
   * @param userContext  the current user context
   * @return A map which contains the JSON response data retrieved from the commerce server.
   */
  @Nullable
  @SuppressWarnings("unchecked")
  public Map<String, Object> findSegmentsByUser(@NonNull StoreContext storeContext, UserContext userContext) {
    try {
      if (StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_7_7)) {
        return emptyMap();
      }

      Integer forUserId = UserContextHelper.getForUserId(userContext);
      if (forUserId == null) {
        return emptyMap();
      }

      List<String> variableValues = asList(getStoreId(storeContext), forUserId + "");
      Map<String, String[]> parameters = getOptionalParameters(storeContext);

      return getRestConnector()
              .callService(FIND_SEGMENTS_BY_USER, variableValues, parameters, null, storeContext, userContext)
              .orElseGet(Collections::emptyMap);
    } catch (CommerceException e) {
      // Commerce returns 403 when the user being queried does not belong to any member groups
      if (e.getResultCode() == 403) {
        return emptyMap();
      }
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }

  @NonNull
  private Map<String, String[]> getOptionalParameters(@NonNull StoreContext storeContext) {
    return buildParameterMap()
            .withCurrency(storeContext)
            .withLanguageId(storeContext)
            .build();
  }
}
