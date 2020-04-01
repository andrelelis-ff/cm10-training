package com.coremedia.blueprint.common.layout;

import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.util.ContainerFlattener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Container class that can be used to render the container dynamically, e.g., as dynamic include.
 */
public class DynamizableCMTeasableContainer implements DynamizableContainer {

  private static final Logger LOG = LoggerFactory.getLogger(DynamizableCMTeasableContainer.class);

  private static final String PROPERTY_PATH_DEFAULT = "items";

  protected CMTeasable teasable;
  private String propertyPath;

  public DynamizableCMTeasableContainer(@NonNull CMTeasable teasable, @Nullable String propertyPath) {
    this.teasable = teasable;
    this.propertyPath = propertyPath != null ? propertyPath : PROPERTY_PATH_DEFAULT;
  }

  public CMTeasable getTeasable() {
    return teasable;
  }

  public String getPropertyPath() {
    return propertyPath;
  }

  @Override
  public List<? extends CMTeasable> getItems() {
    try {
      return resolveItems();
    } catch (BeansException e) {
      LOG.warn("Cannot call getter '{}' on bean '{}' ({})", propertyPath, teasable.getContent().getPath(), e.getMessage());
    }
    return Collections.emptyList();
  }

  @Override
  public List<?> getFlattenedItems() {
    return ContainerFlattener.flatten(this, Object.class);
  }

  @SuppressWarnings("unchecked")
  public List<? extends CMTeasable> resolveItems() {

    List items = Collections.emptyList();

    if (StringUtils.isNotBlank(propertyPath) && !PROPERTY_PATH_DEFAULT.equals(propertyPath)) {
      Object value = PropertyAccessorFactory.forBeanPropertyAccess(teasable).getPropertyValue(propertyPath);
      if (value instanceof List) {
        items = (List) value;
      }

    } else if (teasable instanceof Container) {
      items = ((Container)teasable).getItems();
    }

    return items;
  }

}
