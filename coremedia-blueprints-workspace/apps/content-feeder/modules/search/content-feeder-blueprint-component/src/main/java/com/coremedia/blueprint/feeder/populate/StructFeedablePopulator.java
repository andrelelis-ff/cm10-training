package com.coremedia.blueprint.feeder.populate;

import com.coremedia.cap.common.InvalidPropertyValueException;
import com.coremedia.cap.common.NoSuchPropertyDescriptorException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.feeder.MutableFeedable;
import com.coremedia.cap.feeder.populate.FeedablePopulator;
import com.coremedia.cap.struct.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Transforms Struct-Properties to String representation for the solr index.
 * Keys and String-Values will be indexed.
 */
public class StructFeedablePopulator implements FeedablePopulator<Object> {
  private static final Logger LOG = LoggerFactory.getLogger(StructFeedablePopulator.class);
  private String solrFieldName = "textbody";
  private List<String> propertyNames;

  @Override
  public void populate(MutableFeedable feedable, Object source) {
    if (source == null || feedable == null) {
      throw new IllegalArgumentException("source and feedable must not be null");
    } else if (!(source instanceof Content)) {
      throw new IllegalArgumentException("This only works for Content, not for " + source.getClass().getName());
    }

    Content content = (Content) source;
    if (content.isFolder()) {
      return;
    }

    StringBuilder indexContent = new StringBuilder();
    for (String propertyName : propertyNames) {
      Struct struct;
      try {
        struct = content.getStruct(propertyName);
      } catch (NoSuchPropertyDescriptorException ex) {
        //do nothing
        continue;
      } catch (InvalidPropertyValueException ex) {
        LOG.error("StructFeedablePopulator: Invalid configuration for property " + propertyName);
        continue;
      }
      if (struct != null) {
        Map<String, Object> structMap = struct.toNestedMaps();
        String curContent = getStringFromStructMap(structMap);
        indexContent.append(curContent);
      }
    }
    feedable.setStringElement(solrFieldName, indexContent.toString());
  }

  /**
   * Resolves the properties map to a String representation used in the solr index.
   *
   * @param propertiesMap input map
   * @return String to be indexed in solr index
   */
  private String getStringFromStructMap(Map<String, Object> propertiesMap) {
    StringBuilder sb = new StringBuilder();

    for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();

      //all keys will be added to the index
      sb.append(key);
      sb.append(" ");

      //only String Properties will be added to the index
      if (value instanceof String) {
        sb.append(value);
        sb.append(" ");
      }
      // resolve sub-structs
      else if (value instanceof Map && !((Map) value).isEmpty()) {
        sb.append(getStringFromStructMap((Map<String, Object>) value));
      }
    }
    return sb.toString();
  }

  public void setSolrFieldName(String solrFieldName) {
    this.solrFieldName = solrFieldName;
  }

  public void setPropertyNames(List<String> propertyNames) {
    this.propertyNames = propertyNames;
  }
}
