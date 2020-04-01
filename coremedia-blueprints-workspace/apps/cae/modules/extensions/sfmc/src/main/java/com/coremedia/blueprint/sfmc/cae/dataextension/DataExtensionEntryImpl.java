package com.coremedia.blueprint.sfmc.cae.dataextension;

import com.coremedia.blueprint.base.sfmc.libservices.dataextensions.DataExtensionEntry;
import com.coremedia.blueprint.base.sfmc.libservices.dataextensions.DataExtensionValueType;
import edu.umd.cs.findbugs.annotations.NonNull;

public class DataExtensionEntryImpl implements DataExtensionEntry {
  private String fieldName;
  private DataExtensionValueType type;
  private Object fieldValue;
  private boolean primaryKey;

  public DataExtensionEntryImpl(@NonNull String fieldName,
                                @NonNull DataExtensionValueType type,
                                @NonNull Object fieldValue,
                                boolean primaryKey) {
    this.fieldName = fieldName;
    this.type = type;
    this.fieldValue = fieldValue;
    this.primaryKey = primaryKey;
  }

  @Override
  @NonNull
  public String getFieldName() {
    return fieldName;
  }

  @Override
  @NonNull
  public Object getFieldValue() {
    return fieldValue;
  }

  @Override
  @NonNull
  public DataExtensionValueType getType() {
    return type;
  }

  @Override
  public boolean isPrimaryKey() {
    return primaryKey;
  }
}
