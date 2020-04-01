package com.coremedia.livecontext.ecommerce.ibm.event;

import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.event.InvalidationEvent;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestConnector;
import com.google.common.collect.Iterables;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.lc.test.HoverflyTestHelper.useTapes;
import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_9_0;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ExtendWith(SwitchableHoverflyExtension.class)
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_InvalidationServiceImplIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true
        )
)
@ContextConfiguration(classes = {IbmServiceTestBase.LocalConfig.class, IbmEventConfiguration.class})
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
class InvalidationServiceImplIT extends IbmServiceTestBase {

  private static final long TEST_TIMESTAMP = 1403708220082L;

  @Inject
  private InvalidationServiceImpl invalidationService;

  @Inject
  private WcRestConnector restConnector;

  @Test
  void testPollCacheInvalidations() {
    List<InvalidationEvent> commerceCacheInvalidations = invalidationService.getInvalidations(-1, storeContext);
    assertNotNull(commerceCacheInvalidations);
    assertTrue(!commerceCacheInvalidations.isEmpty());
  }

  @Test
  void testPollCacheInvalidationsWithEntries() {
    //if you have to re-record this test, you should change the timestamp to now-1h or something similar

    List<InvalidationEvent> commerceCacheInvalidations = invalidationService.getInvalidations(TEST_TIMESTAMP, storeContext);

    assertNotNull(commerceCacheInvalidations);
    assertFalse(commerceCacheInvalidations.isEmpty());

    InvalidationEvent commerceCacheInvalidation = commerceCacheInvalidations.get(0);
    assertNotNull(commerceCacheInvalidation.getContentType());
    assertNotNull(commerceCacheInvalidation.getTechId());
    long lastInvalidationTimestamp = Iterables.getLast(commerceCacheInvalidations).getTimestamp();

    assertTrue(lastInvalidationTimestamp > TEST_TIMESTAMP || lastInvalidationTimestamp <= 0);
  }

  @Test
  void testPollCacheInvalidationsError() {
    if (useTapes() || testConfig.getWcsVersion().lessThan(WCS_VERSION_9_0)) {
      String origServiceEndpoint = restConnector.getServiceEndpoint(storeContext);
      try {
        restConnector.setServiceEndpoint("http://does.not.exists/blub");
        Assertions.assertThrows(CommerceException.class, () -> {
          invalidationService.getInvalidations(0, storeContext);
        });
      } finally {
        restConnector.setServiceEndpoint(origServiceEndpoint);
      }
    } else {
      String origServiceEndpoint = restConnector.getServiceSslEndpoint(storeContext);
      try {
        restConnector.setServiceSslEndpoint("https://does.not.exists/blub");
        Assertions.assertThrows(CommerceException.class, () -> {
          invalidationService.getInvalidations(0, storeContext);
        });
      } finally {
        restConnector.setServiceSslEndpoint(origServiceEndpoint);
      }
    }
  }

  @Test
  void testAddCommerceBeanIdValid() {
    Map<String, Object> eventMap = new HashMap<>();
    eventMap.put("contentType", InvalidationServiceImpl.CONTENT_IDENTIFIER_PRODUCT);
    eventMap.put("techId", "4711");
    eventMap.put("name", "Filou");
    InvalidationEvent invalidationEvent = invalidationService.convertEvent(eventMap, 42L);
    assertEquals(42L, invalidationEvent.getTimestamp());

    eventMap = new HashMap<>();
    eventMap.put("contentType", InvalidationServiceImpl.CONTENT_IDENTIFIER_CATEGORY);
    eventMap.put("techId", "4711");
    eventMap.put("name", "Filou");
    invalidationEvent = invalidationService.convertEvent(eventMap, 42L);
    assertEquals(42L, invalidationEvent.getTimestamp());

    eventMap = new HashMap<>();
    eventMap.put("contentType", InvalidationServiceImpl.CONTENT_IDENTIFIER_TOP_CATEGORY);
    eventMap.put("techId", "4711");
    eventMap.put("name", "Filou");
    invalidationEvent = invalidationService.convertEvent(eventMap, 42L);
    assertEquals(42L, invalidationEvent.getTimestamp());

    eventMap = new HashMap<>();
    eventMap.put("contentType", InvalidationServiceImpl.CONTENT_IDENTIFIER_SEGMENT);
    eventMap.put("techId", "4711");
    eventMap.put("name", "Filou");
    invalidationEvent = invalidationService.convertEvent(eventMap, 42L);
    assertEquals(42L, invalidationEvent.getTimestamp());

    eventMap = new HashMap<>();
    eventMap.put("contentType", InvalidationServiceImpl.CONTENT_IDENTIFIER_MARKETING_SPOT);
    eventMap.put("name", "Filou");
    invalidationEvent = invalidationService.convertEvent(eventMap, 42L);
    assertEquals(42L, invalidationEvent.getTimestamp());
  }

  @Test
  void testAddCommerceBeanIdInValid() {
    Map<String, Object> eventMap = new HashMap<>();
    eventMap.put("contentType", "invalidType");
    InvalidationEvent invalidationEvent = invalidationService.convertEvent(eventMap, 42L);
    assertEquals(42L, invalidationEvent.getTimestamp());
  }
}
