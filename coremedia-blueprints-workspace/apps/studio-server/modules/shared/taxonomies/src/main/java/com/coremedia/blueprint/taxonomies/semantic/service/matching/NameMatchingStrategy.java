package com.coremedia.blueprint.taxonomies.semantic.service.matching;

import com.coremedia.blueprint.taxonomies.Taxonomy;
import com.coremedia.blueprint.taxonomies.TaxonomyNode;
import com.coremedia.blueprint.taxonomies.TaxonomyUtil;
import com.coremedia.blueprint.taxonomies.semantic.SemanticStrategy;
import com.coremedia.blueprint.taxonomies.semantic.Suggestions;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The semantic service strategy using on of the semantic plugins.
 */
public class NameMatchingStrategy implements SemanticStrategy, InitializingBean {
  private static final Logger LOG = LoggerFactory.getLogger(NameMatchingStrategy.class);

  private ContentRepository contentRepository;
  private String serviceId;

  @Required
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }


  @Override
  public Suggestions suggestions(Taxonomy<?> taxonomy, String capId) {
    HashMap<String, TaxonomyNode> nameMapping = new HashMap<>();
    for (TaxonomyNode node : taxonomy.getAllChildren()) {
      nameMapping.put(node.getName().toLowerCase().trim(), node);    //NOSONAR
    }

    Suggestions items = new Suggestions();
    try {
      //find semantics from service
      Content content = contentRepository.getContent(capId);
      StringBuilder buffer = new StringBuilder();

      //now start to combine field values
      Map<String, CapPropertyDescriptor> descriptorsByName = content.getType().getDescriptorsByName();
      addFieldValue("title", buffer, content, descriptorsByName);
      addFieldValue("detailText", buffer, content, descriptorsByName);
      addFieldValue("teaserTitle", buffer, content, descriptorsByName);
      addFieldValue("teaserText", buffer, content, descriptorsByName);

      String formattedText = formatBuffer(buffer);
      findMatches(nameMapping, content, items, formattedText);

    } catch (Exception e) {
      LOG.error("Resolving suggestions failed.", e);
    }
    return items;
  }

  /**
   * Executes a name matching with all words of the content fields against
   * the taxonomy names.
   *
   * @param nameMapping   The map that contains all keywords mapped to the corresponding node.
   * @param content       The content to evaluate.
   * @param items         The current suggestion list.
   * @param formattedText The formatted content text to evaluate.
   */
  private void findMatches(Map<String, TaxonomyNode> nameMapping, Content content, Suggestions items, String formattedText) {
    for (Map.Entry<String, TaxonomyNode> taxonomy : nameMapping.entrySet()) {
      if (formattedText.contains(taxonomy.getKey())) {
        TaxonomyNode match = nameMapping.get(taxonomy.getKey());
        if (!items.contains(match.getRef()) && !hasTaxonomy(content, match)) {
          Content matchingContent = contentRepository.getContent(TaxonomyUtil.asContentId(match.getRef()));
          items.addSuggestion(matchingContent, -1);
        }
      }
    }
  }

  /**
   * Checks if the given taxonomy content is already assigned to the content object.
   *
   * @param content The content object to check the taxonomy keywords for.
   * @param node    The taxonomy that is used as suggestion when not found in the content.
   * @return True if the taxonomy content is already applied to the content.
   */
  private boolean hasTaxonomy(Content content, TaxonomyNode node) {
    List<Object> children = new ArrayList<>();
    children.addAll(content.getList("subjectTaxonomy"));
    children.addAll(content.getList("locationTaxonomy"));
    children.addAll(content.getList("queryTaxonomy"));

    for (Object child : children) {
      if (((Content) child).getId().equals(TaxonomyUtil.asContentId(node.getRef()))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Formats the buffer to a string, removes all special characters.
   *
   * @param buffer The buffer to format the string from.
   * @return A formatted lowercase string
   */
  private String formatBuffer(StringBuilder buffer) {
    String value = buffer.toString();
    value = value.replaceAll("\\.", " ");
    value = value.replaceAll(",", " ");
    value = value.replaceAll(":", " ");
    value = value.replaceAll(";", " ");
    value = value.replaceAll("-", " ");
    value = value.replaceAll("'", " ");
    value = value.replaceAll("\\[", " ");
    value = value.replaceAll("\\]", " ");
    value = value.replaceAll("/", " ");
    value = value.replaceAll("!", " ");
    value = value.replaceAll("\\?", " ");
    value = value.replaceAll("\"", " ");

    return value.toLowerCase(); //NOSONAR
  }

  /**
   * Adds the value of the field to the string buffer, if field exists.
   *
   * @param field             The field that should be added to the buffer.
   * @param buffer            The buffer the text will be added to.
   * @param content           The current content instance.
   * @param descriptorsByName The descriptors of the content to check the fields from.
   */
  private void addFieldValue(String field, StringBuilder buffer, Content content, Map<String, CapPropertyDescriptor> descriptorsByName) {
    if (descriptorsByName.containsKey(field)) {
      buffer.append(content.getString(field));
      buffer.append(" ");
    }
  }

  @Override
  public String getServiceId() {
    return serviceId;
  }

  @Override
  public void afterPropertiesSet() {
    //nothing
  }
}
