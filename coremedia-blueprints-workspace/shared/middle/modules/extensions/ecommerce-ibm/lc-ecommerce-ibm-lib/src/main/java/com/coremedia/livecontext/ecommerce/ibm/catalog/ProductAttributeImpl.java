package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.livecontext.ecommerce.catalog.ProductAttribute;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

class ProductAttributeImpl implements ProductAttribute {

  private Map<String, Object> delegate;

  ProductAttributeImpl(Map<String, Object> delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getId() {
    return getStringValue(delegate, "identifier");
  }

  @Override
  public String getType() {
    return getStringValue(delegate, "dataType");
  }

  @Override
  public String getUnit() {
    return getStringValue(delegate, "unit");
  }

  @Override
  public String getExternalId() {
    return getStringValue(delegate, "uniqueID");
  }

  @Override
  public String getDisplayName() {
    return getStringValue(delegate, "name");
  }

  @Override
  public String getDescription() {
    return getStringValue(delegate, "description");
  }

  @Nullable
  @Override
  public Object getValue() {
    //noinspection unchecked
    List<Map<String, Object>> valueForKey = DataMapHelper.getList(delegate, "values");

    return valueForKey.stream()
            .findFirst()
            .map(firstValueForKey -> DataMapHelper.getValueForKey(firstValueForKey, "value"))
            .orElse(null);
  }

  @NonNull
  @Override
  public List<Object> getValues() {
    //noinspection unchecked
    List<Map<String, Object>> valueForKey = DataMapHelper.getList(delegate, "values");

    return valueForKey.stream()
            .map(item -> DataMapHelper.getValueForKey(item, "value"))
            .filter(Objects::nonNull)
            .collect(toList());
  }

  @Override
  public boolean isDefining() {
    return "Defining".equals(getStringValue(delegate, "usage"));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ProductAttributeImpl that = (ProductAttributeImpl) o;

    if (delegate != null) {
      return getId().equals(that.getId());
    }

    return that.delegate == null;
  }

  @Override
  public int hashCode() {
    return delegate != null ? getId().hashCode() : 0;
  }

  @Nullable
  private static String getStringValue(@NonNull Map<String, Object> map, @NonNull String key) {
    return DataMapHelper.findString(map, key).orElse(null);
  }
}
