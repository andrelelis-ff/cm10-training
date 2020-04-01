package com.coremedia.blueprint.assets.cae;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @cm.template.api
 */
public class Notification {

  private NotificationType type;
  private String key;
  private List<?> params;

  public Notification(@NonNull NotificationType type, @NonNull String key, @Nullable List<?> params) {
    this.type = type;
    this.key = key;
    this.params = params == null ? Collections.emptyList() : new ArrayList<>(params);
  }

  /**
   * @cm.template.api
   */
  @NonNull
  public NotificationType getType() {
    return type;
  }

  /**
   * @cm.template.api
   */
  @NonNull
  public String getKey() {
    return key;
  }

  /**
   * @cm.template.api
   */
  public List<?> getParams() {
    return params;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Notification)) {
      return false;
    }

    Notification notification = (Notification) o;

    if (!key.equals(notification.key)) {
      return false;
    }
    // Probably incorrect - comparing Object[] arrays with Arrays.equals
    return Objects.equals(params, notification.params) && type == notification.type;

  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + key.hashCode();
    result = 31 * result + (params != null ? params.hashCode() : 0);
    return result;
  }

  /**
   * @cm.template.api
   */
  public enum NotificationType {
    /**
     * @cm.template.api
     */
    SUCCESS,
    /**
     * @cm.template.api
     */
    INFO,
    /**
     * @cm.template.api
     */
    WARNING,
    /**
     * @cm.template.api
     */
    ERROR
  }
}
