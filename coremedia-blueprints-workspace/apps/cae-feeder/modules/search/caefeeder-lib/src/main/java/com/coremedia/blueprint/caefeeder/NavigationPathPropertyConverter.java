package com.coremedia.blueprint.caefeeder;

import com.coremedia.blueprint.base.caefeeder.TreePathKeyFactory;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.feeder.bean.PropertyConverter;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A {@link PropertyConverter} for the property {@link com.coremedia.blueprint.common.contentbeans.CMLinkable#getContexts()}
 * which takes the list of contexts, constructs the navigation path for each and returns them as list of String values.
 * A single navigation path consists of the numeric content ids of the channels separated by slashes.
 * <p>
 * The indexed value can then be used to search for linkables below a given context.
 * <p>
 * For example, a list with values <code>/40/52/202</code> and <code>/40/56/168</code> would be returned for a linkable
 * which is below two channels 202 and 168. Both channels are below the same root channel <code>40</code>.
 */
public class NavigationPathPropertyConverter implements PropertyConverter {

  private TreePathKeyFactory<Content> navigationPathKeyFactory;

  public void setNavigationPathKeyFactory(@NonNull TreePathKeyFactory<Content> navigationPathKeyFactory) {
    this.navigationPathKeyFactory = requireNonNull(navigationPathKeyFactory);
  }

  @Override
  public List<String> convertValue(Object value) {
    if (value == null) {
      return Collections.emptyList();
    }

    List<CMNavigation> navigations = convertToNavigationList(value);

    List<String> result = new ArrayList<>(navigations.size());
    for (CMNavigation navigation : navigations) {
      Content content = navigation.getContent();
      String idPath = idPath(navigationPathKeyFactory.getPath(content));
      if (!idPath.isEmpty()) {
        result.add(idPath);
      }
    }
    return result;
  }

  List<CMNavigation> convertToNavigationList(Object value) {
    if (!(value instanceof Collection)) {
      throw new IllegalArgumentException("Unable to convert " + value);
    }
    Collection c = (Collection)value;
    List<CMNavigation> result = new ArrayList<>(c.size());
    for (Object o : c) {
      if (o instanceof CMNavigation) {
        result.add((CMNavigation)o);
      } else if (o!=null) {
        throw new IllegalArgumentException("Unable to convert collection item: " + o);
      }
    }
    return result;
  }

  @Override
  public Class<List> convertType(Class type) {
    return List.class;
  }

  @NonNull
  private static String idPath(@NonNull List<Content> path) {
    StringBuilder sb = new StringBuilder();
    for (Content content : path) {
      sb.append('/');
      sb.append(IdHelper.parseContentId(content.getId()));
    }
    return sb.toString();
  }

}
