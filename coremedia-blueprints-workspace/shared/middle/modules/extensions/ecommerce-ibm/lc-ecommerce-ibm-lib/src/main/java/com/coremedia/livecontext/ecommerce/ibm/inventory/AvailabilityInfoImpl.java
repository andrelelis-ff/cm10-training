package com.coremedia.livecontext.ecommerce.ibm.inventory;

import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.inventory.AvailabilityInfo;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Map;
import java.util.Objects;

public class AvailabilityInfoImpl implements AvailabilityInfo {

  private final Map<String, Object> delegate;

  public AvailabilityInfoImpl(Map<String, Object> delegate) {
    this.delegate = delegate;
  }

  @Override
  public float getQuantity() {
    return DataMapHelper.findValue(delegate, "availableQuantity", Float.class).orElse(0.0F);
  }

  @Override
  public String getInventoryStatus() {
    return getStringValue(delegate, "inventoryStatus");
  }

  @Override
  public String getUnitOfMeasure() {
    return getStringValue(delegate, "unitOfMeasure");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AvailabilityInfoImpl that = (AvailabilityInfoImpl) o;

    return !(delegate != null
            ? !java.util.Objects.equals(getStringValue(delegate, "productId"), getStringValue(that.delegate, "productId"))
            : that.delegate != null);
  }

  @Override
  public int hashCode() {
    return delegate != null ? Objects.hash(getStringValue(delegate, "productId")) : 0;
  }

  @Nullable
  private static String getStringValue(@NonNull Map<String, Object> map, @NonNull String key) {
    return DataMapHelper.findString(map, key).orElse(null);
  }
}
