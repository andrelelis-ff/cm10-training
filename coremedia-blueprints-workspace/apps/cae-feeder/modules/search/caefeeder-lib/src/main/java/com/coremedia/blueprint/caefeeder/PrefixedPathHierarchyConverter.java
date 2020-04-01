package com.coremedia.blueprint.caefeeder;

import com.coremedia.blueprint.base.caefeeder.TreePathKeyFactory;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.feeder.bean.PropertyConverter;
import com.coremedia.objectserver.beans.ContentBean;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 *   This converter takes a list of {@link ContentBean ContentBeans} and returns a collection of unique paths that follow
 *   a hierarchically-structured scheme mentioned on the
 *   Solr Wiki (see <a href="https://wiki.apache.org/solr/HierarchicalFaceting">'facet.prefix' based drill down</a>).
 * </p>
 * <p>
 *   All beans in the list will be processed as follows:
 *   <ol>
 *     <li>The {@link TreePathKeyFactory} creates the path <em>'A,B,C'</em> from the bean C in the list.</li>
 *     <li>
 *       The converter creates a depth-prefixed path for each level in the path:
 *       <ul>
 *         <li><em>0/A</em></li>
 *         <li><em>1/A/B</em></li>
 *         <li><em>2/A/B/C</em></li>
 *       </ul>
 *     </li>
 *     <li>These paths will be added to the set of resulting paths.</li>
 *   </ol>
 * </p>
 * <p>
 *   Note:<br>
 *   The converter assumes that the levels of the hierarchy are separated by slashes.
 * </p>
 *
 * @see <a href="https://wiki.apache.org/solr/HierarchicalFaceting">'facet.prefix' based drill down</a>
 */
public class PrefixedPathHierarchyConverter implements PropertyConverter {

  private static final char PATH_SEPARATOR_CHAR = '/';

  private TreePathKeyFactory<NamedTaxonomy> pathKeyFactory;

  /**
   * Sets the path key factory that creates a path of {@link NamedTaxonomy} for a {@link com.coremedia.cap.content.Content}.
   *
   * @param pathKeyFactory the path key factory
   */
  @Required
  public void setPathKeyFactory(TreePathKeyFactory<NamedTaxonomy> pathKeyFactory) {
    this.pathKeyFactory = pathKeyFactory;
  }

  @Override
  public Object convertValue(Object value) {
    if (!(value instanceof Collection)) {
      return Collections.emptyList();
      }

    Collection<?> collection = (Collection<?>) value;

    return collection.stream()
                     .filter(ContentBean.class::isInstance)
                     .map(ContentBean.class::cast)
                     .map(ContentBean::getContent)
                     .map(pathKeyFactory::getPath)
                     .flatMap(PrefixedPathHierarchyConverter::createDepthPrefixedPathSegments)
                     .distinct()
                     .collect(Collectors.toList());
    }

  @NonNull
  private static Stream<String> createDepthPrefixedPathSegments(@NonNull List<NamedTaxonomy> path) {
    DepthPrefixer prefixer = new DepthPrefixer();
    return path.stream()
               .map(NamedTaxonomy::getContent)
               .map(c -> IdHelper.parseContentId(c.getId()))
               .map(prefixer::prefix);
    }

  @Override
  public Class<?> convertType(Class<?> type) {
    return List.class;
      }

  private static class DepthPrefixer {
    private final StringBuilder sb = new StringBuilder();
    private int depth;

  @NonNull
    private String prefix(int id) {
      sb.append(PATH_SEPARATOR_CHAR).append(id);
      String result = String.valueOf(depth) + sb;
      depth++;
      return result;
  }

  }

}
