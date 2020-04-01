package com.coremedia.livecontext.ecommerce.ibm.cae.storefront;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static com.coremedia.livecontext.ecommerce.ibm.cae.storefront.IbmStoreFrontService.IBM_WCP_USERACTIVITY_COOKIE_NAME;
import static com.coremedia.livecontext.ecommerce.ibm.cae.storefront.IbmStoreFrontService.IBM_WC_USERACTIVITY_COOKIE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class UserSessionServiceImplUserIdTest {

  @InjectMocks
  private UserSessionServiceImpl testling;

  private String validUserId = "1002";
  private String anonymousUserId = "-1002";
  private String validStoreId = "10202";
  private String restCookieString = "abc,asdfa";
  private String restCookieStringWithDel = "asdfa,DEL";

  @Test
  public void resolveUserIdFromCookieDataIsValidNameForLive() {
    String validValue = validUserId + "," + validStoreId + "," + restCookieString;
    boolean ignoreAnonymous = false;

    String resolvedId = testling.resolveUserIdFromCookieData(IBM_WC_USERACTIVITY_COOKIE_NAME, validValue, validStoreId, ignoreAnonymous);

    assertEquals(validUserId, resolvedId);
  }

  @Test
  public void resolveUserIdFromCookieDataIsValidNameForLiveButDeleted() {
    String deletedValue = validUserId + "," + validStoreId + "," + restCookieStringWithDel;
    boolean ignoreAnonymous = false;

    String resolvedId = testling.resolveUserIdFromCookieData(IBM_WC_USERACTIVITY_COOKIE_NAME, deletedValue, validStoreId, ignoreAnonymous);

    assertNull(resolvedId);
  }

  @Test
  public void resolveUserIdFromCookieDataIsValidNameForPreview() {
    String validValue = validUserId + "," + validStoreId + "," + restCookieString;
    boolean ignoreAnonymous = false;

    String resolvedId = testling.resolveUserIdFromCookieData(IBM_WCP_USERACTIVITY_COOKIE_NAME, validValue, validStoreId, ignoreAnonymous);

    assertEquals(validUserId, resolvedId);
  }

  @Test
  public void resolveUserIdFromCookieDataIsValidNameForPreviewButDeleted() {
    String deletedValue = validUserId + "," + validStoreId + "," + restCookieStringWithDel;
    boolean ignoreAnonymous = false;

    String resolvedId = testling.resolveUserIdFromCookieData(IBM_WCP_USERACTIVITY_COOKIE_NAME, deletedValue, validStoreId, ignoreAnonymous);

    assertNull(resolvedId);
  }

  @Test
  public void resolveUserIdFromCookieDataUnknownName() {
    String validValue = validUserId + "," + validStoreId + "," + restCookieString;
    boolean ignoreAnonymous = false;

    String resolvedId = testling.resolveUserIdFromCookieData("WC_PERSISTENT_COOKIE", validValue, validStoreId, ignoreAnonymous);

    assertNull(resolvedId);
  }

  @Test
  public void resolveUserIdFromCookieDataDifferentStoreId() {
    String validValue = validUserId + "," + validStoreId + "," + restCookieString;
    String currentStoreId = "1234567";
    boolean ignoreAnonymous = false;

    String resolvedId = testling.resolveUserIdFromCookieData(IBM_WC_USERACTIVITY_COOKIE_NAME, validValue, currentStoreId, ignoreAnonymous);

    assertNull(resolvedId);
  }

  @Test
  public void resolveUserIdFromCookieDataValueToShortJustOne() {
    String toShortValue = validUserId;
    boolean ignoreAnonymous = false;

    String resolvedId = testling.resolveUserIdFromCookieData(IBM_WC_USERACTIVITY_COOKIE_NAME, toShortValue, validStoreId, ignoreAnonymous);

    assertNull(resolvedId);
  }

  @Test
  public void resolveUserIdFromCookieDataAnonynmousDontIgnore() {
    String validAnonymousValue = anonymousUserId + "," + validStoreId + "," + restCookieString;
    boolean ignoreAnonymous = false;

    String resolvedId = testling.resolveUserIdFromCookieData(IBM_WC_USERACTIVITY_COOKIE_NAME, validAnonymousValue, validStoreId, ignoreAnonymous);

    assertEquals(anonymousUserId, resolvedId);
  }

  @Test
  public void resolveUserIdFromCookieDataAnonynmousDoIgnore() {
    String validAnonymousValue = anonymousUserId + "," + validStoreId + "," + restCookieString;
    boolean ignoreAnonymous = true;

    String resolvedId = testling.resolveUserIdFromCookieData(IBM_WC_USERACTIVITY_COOKIE_NAME, validAnonymousValue, validStoreId, ignoreAnonymous);

    assertNull(resolvedId);
  }

  @Test
  public void resolveUserIdFromCookieDataFirstSegmentIsNotANumber() {
    String invalidValue = "anyString" + "," + validStoreId + "," + restCookieString;
    boolean ignoreAnonymous = false;

    String resolvedId = testling.resolveUserIdFromCookieData(IBM_WC_USERACTIVITY_COOKIE_NAME, invalidValue, validStoreId, ignoreAnonymous);

    assertNull(resolvedId);
  }
}
