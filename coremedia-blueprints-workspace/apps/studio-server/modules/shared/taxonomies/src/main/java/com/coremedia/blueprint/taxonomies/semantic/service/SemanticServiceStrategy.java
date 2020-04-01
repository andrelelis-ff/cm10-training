package com.coremedia.blueprint.taxonomies.semantic.service;

import com.coremedia.blueprint.taxonomies.Taxonomy;
import com.coremedia.blueprint.taxonomies.TaxonomyNode;
import com.coremedia.blueprint.taxonomies.TaxonomyUtil;
import com.coremedia.blueprint.taxonomies.semantic.SemanticContext;
import com.coremedia.blueprint.taxonomies.semantic.SemanticEntity;
import com.coremedia.blueprint.taxonomies.semantic.SemanticStrategy;
import com.coremedia.blueprint.taxonomies.semantic.Suggestions;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.rest.cap.content.search.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The semantic service strategy using on of the semantic plugins.
 */
public class SemanticServiceStrategy implements SemanticStrategy {
  private static final Logger LOG = LoggerFactory.getLogger(SemanticServiceStrategy.class);
  private static final int LIMIT = 100;

  private SemanticService semanticService;
  private SearchService searchService;
  private ContentRepository contentRepository;
  private String serviceId;
  private String referencePropertyName;
  private String nameMatchingPropertyName;

  @Required
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  @Required
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  @Required
  public void setSemanticService(SemanticService semanticService) {
    this.semanticService = semanticService;
  }

  @Required
  public void setReferencePropertyName(String property) {
    this.referencePropertyName = property;
  }

  @Required
  public void setNameMatchingPropertyName(String nameMatchingPropertyName) {
    this.nameMatchingPropertyName = nameMatchingPropertyName;
  }

  @Override
  public Suggestions suggestions(Taxonomy<?> taxonomy, String capId) {
    String taxonomyType = taxonomy.getKeywordType();
    List<ContentType> types = new ArrayList<>();
    types.add(contentRepository.getContentType(taxonomyType));

    Suggestions items = new Suggestions();
    try {
      //find semantics from service
      Content content = contentRepository.getContent(capId);
      SemanticContext context = semanticService.analyze(content);

      Map<String, Collection<SemanticEntity>> identifiers = context.getEntities();
      if (identifiers.isEmpty()) {
        LOG.info("No semantic identifiers found for {}", content.getName());
        return new Suggestions();
      }

      //looks up the content repositories taxonomy contents and checks if they match with the suggestion
      TaxonomyNode rootNode = taxonomy.getRoot();
      Content root = contentRepository.getContent(TaxonomyUtil.asContentId(rootNode.getRef()));
      findTaxonomyMatches(content, items, identifiers, root, types);
    } catch (Exception e) {
      LOG.error("Resolving suggestions failed.", e);
    }
    return items;
  }

  @Override
  public String getServiceId() {
    return serviceId;
  }

  /**
   * Looks up matching taxonomy content and put's them in a temporary items store to avoid duplicates.
   *
   * @param content      The content to find the matches for.
   * @param suggestions  The items that have been found.
   * @param identifiers  The semantic entities matching the content.
   * @param rootFolder   The root folder where the possible matches are located.
   * @param contentTypes The content type of the taxonomy that can match.
   */
  private void findTaxonomyMatches(Content content, Suggestions suggestions,
                                   Map<String, Collection<SemanticEntity>> identifiers,
                                   Content rootFolder,
                                   Collection<ContentType> contentTypes) {
    List<Content> allContentChildren = TaxonomyUtil.search(searchService, rootFolder, contentTypes, null, -1);
    for (Map.Entry<String, Collection<SemanticEntity>> stringCollectionEntry : identifiers.entrySet()) {
      Collection<SemanticEntity> entities = stringCollectionEntry.getValue();
      for (SemanticEntity entity : entities) {
        for (Content match : allContentChildren) {
          if (entity.getId().equals(match.getString(referencePropertyName))) {
            addSuggestionNode(suggestions, entity, content, match);
          }
          else if(entity.getName().equals(match.get(nameMatchingPropertyName))) {
            addSuggestionNode(suggestions, entity, content, match);
          }


          if (suggestions.size() >= LIMIT) {
            break;
          }
        }
      }
    }
  }

  /**
   * Checks if the given taxonomy content was already added to the list of suggestions and if the content
   * not already has this taxonomy. If not, the keyword is added to the map store and will be added to a node
   * list representation afterwards.
   *
   * @param suggestions     The store of nodes that contains the node that will be shown as suggestions.
   * @param content         The content that was analyzed for suggestions and checked for existing taxonomies.
   * @param taxonomyContent The taxonomy content/suggestion that should be added to the content if not already applied.
   */
  private void addSuggestionNode(Suggestions suggestions, SemanticEntity entity, Content content, Content taxonomyContent) {
    if (!suggestions.contains(taxonomyContent.getId()) && !hasTaxonomy(content, taxonomyContent)) {
      String relevance = entity.getProperties().get("relevance");
      float weight = -1;
      if (relevance != null) {
        weight = Float.parseFloat(relevance.replace(",", ""));
      }
      suggestions.addSuggestion(taxonomyContent, weight);
    }
  }

  /**
   * Checks if the given taxonomy content is already assigned to the content object.
   *
   * @param content         The content object to check the taxonomy keywords for.
   * @param taxonomyContent The taxonomy content that is used as suggestion when not found in the content.
   * @return True if the taxonomy content is already applied to the content.
   */
  private boolean hasTaxonomy(Content content, Content taxonomyContent) {
    Map<String, Object> properties = content.getProperties();
    List<Object> children = new ArrayList<>();
    if (properties.containsKey("subjectTaxonomy")) {
      children.addAll(content.getList("subjectTaxonomy"));
    }
    if (properties.containsKey("locationTaxonomy")) {
      children.addAll(content.getList("locationTaxonomy"));
    }
    if (properties.containsKey("queryTaxonomy")) {
      children.addAll(content.getList("queryTaxonomy"));
    }

    for (Object child : children) {
      if (((Content) child).getId().equals(taxonomyContent.getId())) {
        return true;
      }
    }
    return false;
  }
}
