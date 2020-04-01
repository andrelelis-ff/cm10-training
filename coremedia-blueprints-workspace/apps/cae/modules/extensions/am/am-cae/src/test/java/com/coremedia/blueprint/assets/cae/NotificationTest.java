package com.coremedia.blueprint.assets.cae;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class NotificationTest {


  @Test
  public void test() {
    Notification notification1 = new Notification(Notification.NotificationType.SUCCESS, "key1", null);
    Notification notification2 = new Notification(Notification.NotificationType.INFO, "key2", null);
    Notification notificationSameAs2 = new Notification(Notification.NotificationType.INFO, "key2", null);
    List<?> differentParams = ImmutableList.of("something");
    Notification notificationWithDifferentParams = new Notification(Notification.NotificationType.INFO, "key2", differentParams);

    assertFalse(notification1.equals(notification2));
    //noinspection EqualsWithItself
    assertTrue(notification1.equals(notification1));
    assertTrue(notification2.equals(notificationSameAs2));
    assertFalse(notification2.equals(notificationWithDifferentParams));
    //noinspection ObjectEqualsNull
    assertFalse(notification1.equals(null));

    assertEquals(Notification.NotificationType.SUCCESS, notification1.getType());
    assertEquals("key1", notification1.getKey());
    assertEquals(differentParams, notificationWithDifferentParams.getParams());

    assertNotEquals(notification1.hashCode(), notification2.hashCode());
  }
}
