package com.coremedia.blueprint.personalization.search;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.PropertyProvider;
import com.coremedia.personalization.context.util.IdHelper;
import com.coremedia.personalization.rulelang.XMLCoDec;
import com.coremedia.personalization.search.ArgumentMissingException;
import com.coremedia.personalization.search.SearchFunction;
import com.coremedia.personalization.search.SearchFunctionArguments;
import com.coremedia.xml.Markup;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Compute the taxonomies referenced by the segments active for the current request for use within a SOLR
 * query.
 */
public class SolrSegmentTaxonomies implements SearchFunction {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrSegmentTaxonomies.class);

  private static final String WHITE_SPACE_REGEXP = "[\\s]+";

  /**
   * The parameter for the used search engine field
   */
  private static final String SEARCH_ENGINE_FIELD_PARAMETER = "field";

  /**
   * The parameter for the used context name
   */
  private static final String CONTEXT_NAME_PARAMETER = "context";

  private static final String SUBJECT_TAXONOMY_KEY = "subjectTaxonomies";

  public static final String SEGMENT_CONTENT_TYPE = "CMSegment";

  private static final String CMSEGMENT_CONDITIONS = "conditions";
  private static final String DEFAULT_SEARCH_TERM = "1:2";

  private ContentRepository contentRepository;
  private String defaultField;
  private String defaultContextName;
  private String field;

  public void setDefaultField(String defaultField) {
    this.defaultField = defaultField;
  }

  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  public void setDefaultContextName(String defaultContextName) {
    this.defaultContextName = defaultContextName;
  }

  @Override
  public String evaluate(ContextCollection contextCollection, SearchFunctionArguments args) {

    field = args.getString(SEARCH_ENGINE_FIELD_PARAMETER, this.defaultField);
    final String contextName = args.getString(CONTEXT_NAME_PARAMETER, this.defaultContextName);


    if (field == null) {
      throw new ArgumentMissingException(SEARCH_ENGINE_FIELD_PARAMETER, "the name of the solr index field that is" +
              " to be searched has to be supplied via the '" + SEARCH_ENGINE_FIELD_PARAMETER + "' parameter");
    }
    if (contextName == null) {
      throw new ArgumentMissingException(CONTEXT_NAME_PARAMETER, "the name of the context object containing the scores has to be" +
              " supplied via the '" + CONTEXT_NAME_PARAMETER + "' parameter");
    }

    // Suppress warning about passing @PersonalData PropertyProvider to #getTaxonomySearchString. We assume that this
    // function is only used in a way that the result string does not suffice to identify a person anymore.
    @SuppressWarnings("PersonalData")
    final String searchTerm = getTaxonomySearchString(contextCollection.getContext(contextName, PropertyProvider.class));

    return StringUtils.isBlank(searchTerm) ? DEFAULT_SEARCH_TERM : searchTerm;
  }

  private String getTaxonomySearchString(PropertyProvider segmentContext) {
    final StringBuilder builder = new StringBuilder();

    if (segmentContext != null) {
      final List<String> segmentIDs = getSegmentIds(segmentContext);

      final List<Integer> taxonomyIDs = getTaxonomyIDs(segmentIDs);
      if (!taxonomyIDs.isEmpty()) {

        builder.append(field).append(":(");

        builder.append(StringUtils.join(taxonomyIDs," OR "));

        builder.append(")");
      }
    }

    return builder.toString();

  }

  /**
   * Return a list of the content ids representing the current segments.
   */
  private static List<String> getSegmentIds(PropertyProvider segmentContext) {
    final List<String> segmentIDs = new ArrayList<>();
    for (final String segmentName : segmentContext.getPropertyNames()) {
      if (segmentContext.getProperty(segmentName, false)) {
        segmentIDs.add(segmentName);
      }
    }
    return segmentIDs;
  }

  private List<Integer> getTaxonomyIDs(List<String> segmentIDs) {
    final List<Integer> taxonomyIDs = new ArrayList<>();

    for (String segmentId : segmentIDs) {
      final Content segment = contentRepository.getContent(segmentId);
      if(segment != null) {
        final ContentType contentType = segment.getType();
        if (contentType.isSubtypeOf(SEGMENT_CONTENT_TYPE)) {

          final List<Integer> segmentTaxonomyIDs = getTaxonomyIDsForSegment(segment);
          taxonomyIDs.addAll(segmentTaxonomyIDs);
        } else {
          LOGGER.info("ignoring non segment content type {}", contentType.getName());
        }
      } else {
        LOGGER.info("no content found for id {}", segmentId);
      }
    }

    return taxonomyIDs;
  }

  private List<Integer> getTaxonomyIDsForSegment(Content segment) {
    final List<Integer> taxonomyIDs = new ArrayList<>();

    final Markup conditionsMarkup = segment.getMarkup(CMSEGMENT_CONDITIONS);
    if (conditionsMarkup != null) {
      final String conditions = XMLCoDec.getXMLRulesAsString(conditionsMarkup.asXml());
      final String conditionItems[] = conditions.split(WHITE_SPACE_REGEXP);
      for (String conditionItem : conditionItems) {

        // here we include only the taxonomies that are marked as 'greater' (or equal) because these are the important ones
        if(conditionItem.startsWith(SUBJECT_TAXONOMY_KEY) && conditionItem.contains(">")){
          final int numericId = IdHelper.parseInternalContentIdFromString(conditionItem);
          if(numericId > 0) {
            taxonomyIDs.add(numericId);
          }
        }
      }
    }
    return taxonomyIDs;
  }

}
