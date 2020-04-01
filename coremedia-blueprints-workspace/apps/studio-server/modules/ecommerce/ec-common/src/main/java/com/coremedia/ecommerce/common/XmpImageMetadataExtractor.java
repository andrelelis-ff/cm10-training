package com.coremedia.ecommerce.common;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.options.IteratorOptions;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.coremedia.common.util.Predicate;
import com.coremedia.common.util.Predicates;
import com.drew.metadata.Metadata;
import com.drew.metadata.xmp.XmpDirectory;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterators;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

/**
 * <p>
 * Extracts selected values from image XMP metadata to a property map. The resulting map will contain the property
 * path as key and the property value as map value.
 * </p>
 * <p>
 * The extractor collects all leaf nodes of the XMP with match the specified criteria. By default, without
 * specifying any criteria, all leave nodes will be collected.
 * </p>
 * <dl>
 * <dt><strong>Filter Criteria:</strong></dt>
 * <dd>
 * <dl>
 * <dt><em>Namespace:</em></dt>
 * <dd>The XMP namespace to locate properties in. If not specified all namespaces will be scanned.</dd>
 * <dt><em>Property:</em></dt>
 * <dd>The property inside the namespace to scan. If not specified all properties will be considered.</dd>
 * <dt><em>Predicate:</em></dt>
 * <dd>A predicate to further restrict the selected nodes for example by their path. By default the predicate
 * filters nothing.</dd>
 * </dl>
 * </dd>
 * <dt><strong>Conversion:</strong></dt>
 * <dd>
 * By default property values are converted to their plain String values. If you require a certain format, you
 * might apply a converter function.
 * </dd>
 * <dt><strong>Example:</strong></dt>
 * <dd>
 * <pre>{@code
 * XmpImageMetadataExtractor extractor =
 *     XmpImageMetadataExtractor.builder()
 *                              .atNameSpace("http://iptc.org/std/Iptc4xmpExt/2008-02-29/")
 *                              .atProperty("ArtworkOrObject")
 *                              .filteredBy(new Predicate<XMPPropertyInfo>() {
 *                                  &#64;Override
 *                                  public boolean include(XMPPropertyInfo o) {
 *                                    return o.getPath().endsWith(INVENTORY_INFO);
 *                                  }
 *                              })
 *                              .build();
 * Metadata metadata = ImageMetadataReader.readMetadata(theFile);
 * }</pre>
 * </dd>
 * </dl>
 *
 * @see #builder()
 */
public class XmpImageMetadataExtractor implements Function<Metadata, Map<String, String>> {
  /**
   * Default function to convert property values. Might be replaced upon configuration by a more sophisticated one
   * which for example transforms the values to URIs.
   */
  private static final Function<XMPPropertyInfo, String> DEFAULT_CONVERT_FUNCTION = new Function<XMPPropertyInfo, String>() {
    @Override
    public String apply(XMPPropertyInfo input) {
      return input.getValue();
    }

    @Override
    public String toString() {
      return "<XMPPropertyInfo.getValue()>";
    }
  };
  /**
   * This extractor will just extract leave nodes. It is by intention not configurable (yet). If you want to make
   * it configurable you might want to widen the predicate that it might also prevent nodes from being entered rather
   * than just working on leaves.
   */
  private static final IteratorOptions ITERATOR_OPTIONS = new IteratorOptions().setJustLeafnodes(true);

  /**
   * Namespace to scan. If {@code null} all namespaces will be scanned.
   */
  @Nullable
  private final String schemaNS;
  /**
   * Property to scan. If {@code null} all properties will be taken into account.
   */
  @Nullable
  private final String propertyName;
  /**
   * A predicate to filter leaves by their property info. If no predicate is defined all properties will
   * be taken into account.
   */
  @Nullable
  private final Predicate<XMPPropertyInfo> filterPredicate;
  /**
   * A function to possibly transform property values. By default {@code XMPPropertyInfo.getValue()} is used.
   */
  @Nullable
  private final Function<XMPPropertyInfo, String> convertFunction;

  /**
   * Constructor for extractor. Accessible only via builder pattern.
   *
   * @param schemaNS        namespace to scan
   * @param propertyName    property to scan
   * @param filterPredicate filter for leave nodes
   * @param convertFunction function to convert node values
   */
  private XmpImageMetadataExtractor(@Nullable String schemaNS,
                                    @Nullable String propertyName,
                                    @Nullable Predicate<XMPPropertyInfo> filterPredicate,
                                    @Nullable Function<XMPPropertyInfo, String> convertFunction) {
    this.schemaNS = schemaNS;
    this.propertyName = propertyName;
    this.filterPredicate = filterPredicate;
    this.convertFunction = convertFunction;
  }

  /**
   * Get convert function. If {@link #convertFunction} is unset the default converter is returned.
   *
   * @return converter function
   */
  @NonNull
  private Function<XMPPropertyInfo, String> getConvertFunction() {
    return convertFunction != null ? convertFunction : DEFAULT_CONVERT_FUNCTION;
  }

  /**
   * Get predicate to filter leave nodes. If {@link #filterPredicate} is unset the <em>Always True</em> predicate is
   * returned.
   *
   * @return filter predicate
   */
  @NonNull
  private Predicate<XMPPropertyInfo> getFilterPredicate() {
    return ofNullable(filterPredicate).orElse(Predicates.alwaysTrue());
  }

  /**
   * Extracts metadata property values from the given metadata. Especially if the metadata are null or no
   * XMP metadata are available, an empty map is returned.
   *
   * @param metadata metadata to extract propeties from
   * @return map from property path to property value of extracted metadata
   */
  @Override
  @NonNull
  public Map<String, String> apply(@Nullable Metadata metadata) {
    if (metadata == null) {
      return Collections.emptyMap();
    }
    Collection<XmpDirectory> directories = metadata.getDirectoriesOfType(XmpDirectory.class);
    if (directories == null) {
      return Collections.emptyMap();
    }
    Predicate<XMPPropertyInfo> predicate = getFilterPredicate();
    Function<XMPPropertyInfo, String> convert = getConvertFunction();
    Map<String, String> result = new HashMap<>();
    for (XmpDirectory directory : directories) {
      Iterator<XMPPropertyInfo> iterator = xmpPropertyIterator(directory);
      while (iterator.hasNext()) {
        XMPPropertyInfo next = iterator.next();
        if (predicate.include(next)) {
          result.put(next.getPath(), convert.apply(next));
        }
      }
    }
    return result;
  }

  private Iterator<XMPPropertyInfo> xmpPropertyIterator(XmpDirectory directory) {
    XMPMeta meta = directory.getXMPMeta();
    if (meta == null) {
      return Collections.emptyIterator();
    }
    Iterator<?> iterator;
    try {
      iterator = meta.iterator(schemaNS, propertyName, ITERATOR_OPTIONS);
    } catch (XMPException e) {
      throw new XmpException(e);
    }
    return Iterators.filter(iterator, XMPPropertyInfo.class);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("schemaNS", schemaNS)
                      .add("propertyName", propertyName)
                      .add("filterPredicate", filterPredicate)
                      .add("convertFunction", convertFunction)
                      .add("hash", Integer.toHexString(System.identityHashCode(this)))
                      .toString();
  }

  /**
   * Builder for extractor.
   *
   * @return builder
   */
  @NonNull
  public static Builder builder() {
    return new BuilderImpl();
  }

  /**
   * Builder for extractor.
   */
  public interface Builder {
    /**
     * Namespace to scan. If {@code null} all namespaces will be scanned.
     *
     * @return self-reference
     */
    @NonNull
    Builder atNameSpace(@Nullable String schemaNS);

    /**
     * Property to scan. If {@code null} all properties will be taken into account.
     *
     * @return self-reference
     */
    @NonNull
    Builder atProperty(@Nullable String propertyName);

    /**
     * A predicate to filter leaves by their property info. If no predicate is defined all properties will
     * be taken into account.
     *
     * @return self-reference
     */
    @NonNull
    Builder filteredBy(@Nullable Predicate<XMPPropertyInfo> filterPredicate);

    /**
     * A function to possibly transform property values. By default {@code XMPPropertyInfo.getValue()} is used.
     *
     * @return self-reference
     */
    @NonNull
    Builder convertValueBy(@Nullable Function<XMPPropertyInfo, String> convertFunction);

    /**
     * Create the configured metadata extractor.
     *
     * @return metadata extractor
     */
    @NonNull
    XmpImageMetadataExtractor build();
  }

  private static final class BuilderImpl implements Builder {
    @Nullable
    private String schemaNS;
    @Nullable
    private String propertyName;
    @Nullable
    private Predicate<XMPPropertyInfo> filterPredicate;
    @Nullable
    private Function<XMPPropertyInfo, String> convertFunction;

    @Override
    @NonNull
    public Builder atNameSpace(@Nullable String schemaNS) {
      this.schemaNS = schemaNS;
      return this;
    }

    @Override
    @NonNull
    public Builder atProperty(@Nullable String propertyName) {
      this.propertyName = propertyName;
      return this;
    }

    @Override
    @NonNull
    public Builder filteredBy(@Nullable Predicate<XMPPropertyInfo> filterPredicate) {
      this.filterPredicate = filterPredicate;
      return this;
    }

    @Override
    @NonNull
    public Builder convertValueBy(@Nullable Function<XMPPropertyInfo, String> convertFunction) {
      this.convertFunction = convertFunction;
      return this;
    }

    @Override
    @NonNull
    public XmpImageMetadataExtractor build() {
      return new XmpImageMetadataExtractor(schemaNS, propertyName, filterPredicate, convertFunction);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
                        .add("schemaNS", schemaNS)
                        .add("propertyName", propertyName)
                        .add("filterPredicate", filterPredicate)
                        .add("convertFunction", convertFunction)
                        .add("hash", Integer.toHexString(System.identityHashCode(this)))
                        .toString();
    }
  }

}
