package com.coremedia.blueprint.caefeeder;

import com.coremedia.blueprint.base.caefeeder.TreePathKeyFactory;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.cap.feeder.bean.PropertyConverter;
import com.coremedia.objectserver.beans.ContentBean;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract class to convert a collection of {@link CMTaxonomy} beans to a comma-separated string of values for all
 * taxonomies including ancestors - unless {@link #setIgnoreParents(boolean)} was set to true.
 *
 * <p>The actual comma-separated values are created in subclasses.
 */
public abstract class TaxonomyPropertyConverter implements PropertyConverter, InitializingBean {
  private static final Logger LOGGER = LoggerFactory.getLogger(TaxonomyPropertyConverter.class);

  private boolean ignoreParents;
  private TreePathKeyFactory<NamedTaxonomy> taxonomyPathKeyFactory;

  @Override
  public void afterPropertiesSet() throws Exception {
    Preconditions.checkState(ignoreParents || taxonomyPathKeyFactory != null,
                             "taxonomyPathKeyFactory must be set to a non-null value or ignoreParents must be true");
  }

  @Override
  public Object convertValue(Object object) {
    if (!(object instanceof Collection)) {
      return null;
    }

    Collection<?> collection = (Collection<?>) object;
    Stream<CMTaxonomy> taxonomies = typed(collection.stream(), CMTaxonomy.class);
    Stream<String> converted;
    if (ignoreParents) {
      converted = taxonomies.map(this::convertTaxonomy);
    } else {
      converted = taxonomies.map(ContentBean::getContent)
                            .map(taxonomyPathKeyFactory::getPath) // recursive fragment cache key lookup
                            .flatMap(Collection::stream)
                            .map(this::convertNamedTaxonomy);
    }

    // skip empty values and duplicates and combine into comma-separated result string
    String result = converted.filter(s -> s != null && !s.trim().isEmpty())
                             .distinct()
                             .collect(Collectors.joining(","));
    if (result.isEmpty()) {
      result = null;
    }
    LOGGER.debug("Converted {} to {}", collection, result);
    return result;
  }

  private static <T> Stream<T> typed(Stream<?> input, Class<T> type) {
    return input.filter(type::isInstance).map(type::cast);
  }

  @Nullable
  protected abstract String convertNamedTaxonomy(@NonNull NamedTaxonomy namedTaxonomy);

  @Nullable
  protected abstract String convertTaxonomy(@NonNull CMTaxonomy taxonomy);

  @Override
  public Class<?> convertType(Class<?> type) {
    return String.class;
  }

  /**
   * In some use cases the parents of a taxonomy are not required. This can be achieved by setting the parameter to true
   * @param ignoreParents whether parents can be ignored, default is false
   */
  public void setIgnoreParents(boolean ignoreParents) {
    this.ignoreParents = ignoreParents;
  }

  public void setTaxonomyPathKeyFactory(TreePathKeyFactory<NamedTaxonomy> taxonomyPathKeyFactory) {
    this.taxonomyPathKeyFactory = taxonomyPathKeyFactory;
  }
}
