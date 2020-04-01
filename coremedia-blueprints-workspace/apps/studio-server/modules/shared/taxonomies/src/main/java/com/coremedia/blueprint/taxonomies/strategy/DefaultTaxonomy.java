package com.coremedia.blueprint.taxonomies.strategy;

import com.coremedia.blueprint.taxonomies.TaxonomyNode;
import com.coremedia.blueprint.taxonomies.TaxonomyNodeList;
import com.coremedia.blueprint.taxonomies.TaxonomyUtil;
import com.coremedia.blueprint.taxonomies.cycleprevention.TaxonomyCycleValidator;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
import com.coremedia.cap.common.descriptors.LinkPropertyDescriptor;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.Version;
import com.coremedia.cap.content.authorization.AccessControl;
import com.coremedia.cap.content.publication.PublicationService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.rest.cap.content.search.SearchService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * A strategy which represents the folder/document structure of the content repository
 * as a taxonomy...
 * <p/>
 * this class is maybe not very useful but it demonstrates how to implement taxonomy strategies.
 */
public class DefaultTaxonomy extends TaxonomyBase { // NOSONAR  cyclomatic complexity

  private static final String ROOT_TYPE = "root";

  private static final Logger LOG = LoggerFactory.getLogger(DefaultTaxonomy.class);

  private static final int LIMIT = 10;
  private static final String VALUE = "value";
  private static final String CHILDREN = "children";

  private static final String NEW_KEYWORD = "new keyword";
  private static final String ROOT_SETTINGS_DOCUMENT = "_root";
  private static final String TYPE_SETTINGS = "CMSettings";
  private static final String SETTINGS_STRUCT = "settings";
  private static final String ROOTS_LIST = "roots";

  private ContentRepository contentRepository;
  private ContentType taxonomyContentType = null;
  private TaxonomyCycleValidator taxonomyCycleValidator;
  private TaxonomyNode root;
  private Content rootFolder;

  private SearchService searchService;

  public DefaultTaxonomy(Content rootFolder, String siteId, ContentType type, ContentRepository contentRepository,
                         SearchService searchService, TaxonomyCycleValidator taxonomyCycleValidator) {
    super(rootFolder.getName(), siteId);

    this.rootFolder = rootFolder;
    this.contentRepository = contentRepository;
    this.searchService = searchService;
    this.taxonomyContentType = type;
    this.taxonomyCycleValidator = taxonomyCycleValidator;

    // Constructor Calls Overridable Method
    root = createEmptyNode();
    root.setName(getTaxonomyId());
    root.setSiteId(siteId);
    root.setSelectable(false);
    root.setRoot(true);
    root.setRef(TaxonomyUtil.getRestIdFromCapId(rootFolder.getId()));
    root.setType(ROOT_TYPE);
    root.setLevel(0);
  }

  @Override
  public boolean isWriteable() {
    Content content = asContent(getRoot());
    AccessControl accessControl = contentRepository.getAccessControl();
    return accessControl.mayCreate(content, taxonomyContentType);
  }

  @Override
  public boolean isValid() {
    return rootFolder.isReadable() && rootFolder.isInProduction() && !rootFolder.getChildDocuments().isEmpty();
  }

  @Override
  @Nullable
  public TaxonomyNode getNodeByRef(@NonNull String ref) {
    if (root.getRef().equals(ref)) {
      return root;
    }

    Content content = getContent(ref);
    if (content == null) {
      return null;
    }
    return asNode(content);
  }

  @Override
  @NonNull
  public TaxonomyNode getRoot() {
    return root;
  }

  @Override
  @Nullable
  public TaxonomyNode getParent(@NonNull String ref) {
    Content nodeContent = getContent(ref);
    if (nodeContent == null) {
      return null;
    }

    Content parent = getParent(nodeContent);
    if (parent == null) {
      return null;
    }
    return asNode(parent);
  }

  @Override
  @NonNull
  public TaxonomyNodeList getChildren(@NonNull TaxonomyNode node, int offset, int count) {
    if (node.isRoot()) {
      return getTopLevel();
    }

    Content content = asContent(node);
    return asNodeList(getValidChildren(content), offset, count, false);
  }

  @Override
  @NonNull
  public TaxonomyNode getPath(@NonNull TaxonomyNode node) {
    Content content = asContent(node);
    List<Content> path = new ArrayList<>();
    buildPathRecursively(content, path);

    TaxonomyNodeList list = asNodeList(path, -1, -1, true);
    node.setPath(list);
    return node;
  }

  @Override
  @NonNull
  public List<Content> getLinks(@NonNull TaxonomyNode node, boolean recursive) {
    List<Content> result = new ArrayList<>();
    Content content = asContent(node);

    //search recursively
    List<Content> allChildren = collectAllTaxonomiesBelow(content, recursive);

    for (Content child : allChildren) {
      for (Content ref : child.getReferrers()) {
        if (!result.contains(ref)
                && ref.isInProduction()
                && ref.getName().equals(ROOT_SETTINGS_DOCUMENT)
                && !ref.getType().isSubtypeOf(taxonomyContentType)
                && !ref.getType().isSubtypeOf("EditorPreferences")) {
          result.add(ref);
        }
      }
    }

    return result;
  }


  @Override
  @NonNull
  public List<Content> getStrongLinks(@NonNull TaxonomyNode node, boolean recursive) {
    Content content = asContent(node);
    List<Content> result = new ArrayList<>();

    List<Content> allChildren = collectAllTaxonomiesBelow(content, recursive);

    for (Content child : allChildren) {
      for (Content ref : child.getReferrers()) {
        if (ref.getName().equals(ROOT_SETTINGS_DOCUMENT)) {
          continue;
        }
        if (!result.contains(ref) && ref.isInProduction() && !ref.getType().isSubtypeOf(taxonomyContentType)) {
          if (!isWeakLinked(child, ref)) {
            result.add(ref);
          }
        }
      }
    }

    return result;
  }

  @Override
  @NonNull
  public TaxonomyNodeList find(@NonNull String text) {
    TaxonomyNodeList list = new TaxonomyNodeList();
    List<TaxonomyNode> hits = new ArrayList<>();
    if (StringUtils.isBlank(text)) {
      return list;
    }

    String query = TaxonomyUtil.formatQuery(text);//NOSONAR
    List<Content> matches = TaxonomyUtil.search(searchService, rootFolder, taxonomyContentType, query, LIMIT);
    for (Content match : matches) {
      if (match.isDeleted()) {
        continue;
      }
      if (taxonomyCycleValidator.isCyclic(match, taxonomyContentType)) {
        continue;
      }

      if (StringUtils.containsIgnoreCase(match.getName(), text) ||
              StringUtils.containsIgnoreCase(match.getString(VALUE), text)) {
        TaxonomyNode hit = asNode(match);
        hit.setPath(getPath(hit).getPath());
        hits.add(hit);
      }
    }
    list.setNodes(hits);
    return list;
  }

  @Override
  @Nullable
  public TaxonomyNode moveNode(@NonNull TaxonomyNode node, @NonNull TaxonomyNode target) {  // NOSONAR  cyclomatic complexity
    //retrieve the contents we need for this operation
    Content nodeContent = asContent(node);
    Content parent = getParent(nodeContent);
    Content targetContent = asContent(target);

    if (parent != null && targetContent.getId().equals(parent.getId())) {
      LOG.warn("Can not move '" + node.getName() + "' to '" + targetContent.getName() + "', it's already there.");
      return asNode(nodeContent);
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Moving '" + node.getName() + "' to '" + targetContent.getName() + "'");
    }

    //checkout the content objects first.
    if (!nodeContent.isCheckedOut()) {
      nodeContent.checkOut();
    }
    if (!targetContent.isFolder() && !targetContent.isCheckedOut()) {
      targetContent.checkOut();
    }
    if (parent != null && !parent.isCheckedOut()) {
      parent.checkOut();
    }

    //remove child relation in the parent
    if (parent != null) {
      List<Content> children = new ArrayList<>(getValidChildren(parent));
      children.remove(nodeContent);
      parent.set(CHILDREN, children);
    }

    //now move the content to the new target by appending as child (we do not set the parent relation anymore!)
    if (targetContent.isDocument()) { // NOSONAR //target can be the root folder too!!!
      List<Content> targetChildren = new ArrayList<>(getValidChildren(targetContent));
      targetChildren.add(nodeContent);
      targetContent.set(CHILDREN, targetChildren);
    }
    else {
      List<Content> rootNodesFromSettings = getRootNodesFromSettings();
      if (rootNodesFromSettings != null) {
        rootNodesFromSettings.add(nodeContent);
        updateRootNodeSettings(rootNodesFromSettings);
      }
    }


    //finally update the lifecycle status
    if (nodeContent.isCheckedOut()) {
      nodeContent.checkIn();
    }
    approveAndPublish(nodeContent);
    if (parent != null) {
      if (parent.isCheckedOut()) {
        parent.checkIn();
      }
      approveAndPublish(parent);
    }

    if (!targetContent.isFolder() && targetContent.isCheckedOut()) {
      targetContent.checkIn();
    }
    approveAndPublish(targetContent);

    //return updated ref
    return getNodeByRef(node.getRef());
  }

  @Override
  @Nullable
  public TaxonomyNode delete(@NonNull TaxonomyNode toDelete) {
    Content deleteMe = asContent(toDelete);
    Content parent = getParent(deleteMe);

    LOG.info("Deleting taxonomy node {}", toDelete);
    if (deleteMe.isCheckedOut()) {
      deleteMe.checkIn();
    }

    //collect all sub nodes we have to delete
    List<Content> allChildren = collectAllTaxonomiesBelow(deleteMe, true);
    deleteChildren(allChildren);

    unlinkFromParent(deleteMe, parent);

    return (parent == null) ? root : asNode(parent);
  }

  @NonNull
  private List<Content> collectAllTaxonomiesBelow(@NonNull Content node, boolean recursively) {
    if (!recursively) {
      return ImmutableList.of(node);
    }

    List<Content> taxonomies = new ArrayList<>();
    collectChildren(node, taxonomies);
    return taxonomies;
  }

  /**
   * Collects recursively all nodes of the given node
   */
  private void collectChildren(Content node, List<Content> allChildren) {
    allChildren.add(node);
    List<Content> children = getValidChildren(node);
    allChildren.addAll(children);
    for (Content child : children) {
      collectChildren(child, allChildren);
    }
  }

  /**
   * Deletes the given taxonomy content respecting the current lifecycle.
   *
   * @param children the taxonomy contents to delete
   */
  private void deleteChildren(List<Content> children) {
    PublicationService publicationService = contentRepository.getPublicationService();

    for (Content child : children) {
      if (publicationService.isPublished(child)) {
        publicationService.toBeWithdrawn(child);
        publicationService.approvePlace(child);
        publicationService.publish(child);
      }
      child.delete();
    }
  }

  /**
   * Removes the given node from the parent
   *
   * @param child  the child to delete
   * @param parent the parent to remove the child from
   */
  private void unlinkFromParent(Content child, Content parent) {
    if (parent != null) {
      if (!parent.isCheckedOut()) {
        parent.checkOut();
      }
      List<Content> children = new ArrayList<>(getValidChildren(parent));
      children.remove(child);
      parent.set(CHILDREN, children);
      parent.checkIn();
      if (contentRepository.getPublicationService().isPublished(parent)) {
        approveAndPublish(parent);
      }
    }
    else {
      List<Content> rootNodesFromSettings = getRootNodesFromSettings();
      if (rootNodesFromSettings != null) {
        rootNodesFromSettings.remove(child);
        updateRootNodeSettings(rootNodesFromSettings);
      }
    }
  }

  @Override
  @NonNull
  public TaxonomyNode createChild(@NonNull final TaxonomyNode parentNode,
                                  @Nullable final String defaultName) {
    Content parent = (parentNode.isRoot()) ? null : asContent(parentNode);
    ContentType type = parent == null ? taxonomyContentType : parent.getType();
    Content folder = rootFolder;

    //check if the corresponding parent folder is used
    if (parent != null && parent.isDocument()) {
      folder = parent.getParent();
    }

    String defaultNameNonNull = Strings.isNullOrEmpty(defaultName) ? NEW_KEYWORD : defaultName;
    Content content = type.createByTemplate(folder, NEW_KEYWORD, "{3} ({1})", Collections.EMPTY_MAP);
    content.set(VALUE, defaultNameNonNull);
    content.checkIn();

    updateContentName(content, defaultNameNonNull);

    if (parent != null) {
      if (!parent.isCheckedOut()) {
        parent.checkOut();
      }
      List<Content> children = new ArrayList<>(getValidChildren(parent));
      children.add(content);
      parent.set(CHILDREN, children);
      parent.checkIn();
    }
    else {
      List<Content> rootNodesFromSettings = getRootNodesFromSettings();
      if (rootNodesFromSettings != null) {
        rootNodesFromSettings.add(content);
        updateRootNodeSettings(rootNodesFromSettings);
      }
    }

    return asNode(content);
  }

  @Override
  @NonNull
  public TaxonomyNode commit(@NonNull TaxonomyNode node) {
    Content content = asContent(node);
    Content parent = getParent(content);
    try {
      if (!content.isDeleted()) {
        //test if renaming is required
        if (!node.getName().equals(content.getName())) {
          String newNodeName = getTaxonomyDocumentName(content);
          if (!node.getName().equals(newNodeName)) {
            LOG.info("Ignoring taxonomy renaming of {} to {}", content.getName(), newNodeName);
            return node;
          }

          //check out document and...
          if (!content.isCheckedOut()) {
            content.checkOut();
          }

          //...check if we have checked out it with our session
          if (content.isCheckedOutByCurrentSession()) {
            // rename content
            String name = content.getString(VALUE);
            if (!StringUtils.isEmpty(name)) {
              content.rename(StringUtils.trim(newNodeName));
            }
          }
        }

        publish(content);

        // publish parent if necessary...(publishing of parent must be done before publishing the child node, otherwise "An internal link of this document could not be published.")
        if (parent != null && parent.getCheckedInVersion() != null && !contentRepository.getPublicationService().isPublished(parent.getCheckedInVersion())) {
          LOG.info("Publishing parent {} of {}", parent, content);
          if (parent.isCheckedOut()) {
            parent.checkIn();
          }
          approveAndPublish(parent);
        }

        return asNode(content);
      }
    } catch (Exception e) { //NOSONAR
      LOG.error("Error committing " + node + ": " + e.getMessage(), e);
    }
    return asNode(content);
  }

  /**
   * Used for the name matching strategy to resolve all matching taxonomies for text
   * by simple name matching.
   *
   * @return all children
   */
  @Override
  @NonNull
  public List<TaxonomyNode> getAllChildren() {
    List<TaxonomyNode> allChildren = new ArrayList<>();
    List<Content> matches = new ArrayList<>();
    findAll(rootFolder, matches);
    for (Content child : matches) {
      if (TaxonomyUtil.isTaxonomy(child, taxonomyContentType)) {
        allChildren.add(asNode(child));
      }
    }
    return allChildren;
  }


  @Override
  public String getKeywordType() {
    return taxonomyContentType.getName();
  }

  // === HELPER ========================================================================================================

  @Nullable
  private List<Content> getRootNodesFromSettings() {
    Content rootSettings = rootFolder.getChild(ROOT_SETTINGS_DOCUMENT);
    if (rootSettings != null && rootSettings.getType().getName().equals(TYPE_SETTINGS)) {
      Struct settings = rootSettings.getStruct(SETTINGS_STRUCT);
      return new ArrayList<>(settings.getLinks(ROOTS_LIST));
    }

    return null;
  }

  private void updateRootNodeSettings(@NonNull List<Content> topNodes) {
    Content rootSettings = rootFolder.getChild(ROOT_SETTINGS_DOCUMENT);
    if (rootSettings != null && rootSettings.getType().getName().equals(TYPE_SETTINGS)) {
      if (!rootSettings.isCheckedOut()) {
        rootSettings.checkOut();
      }

      Struct settings = rootSettings.getStruct(SETTINGS_STRUCT);
      settings = settings.builder().set(ROOTS_LIST, topNodes).build();
      rootSettings.set(SETTINGS_STRUCT, settings);
      rootSettings.checkIn();
    }
  }

  /**
   * Returns true if the linked content is linked has weak link inside the linking content
   */
  private boolean isWeakLinked(@NonNull Content linkedContent,
                               @NonNull Content linkingContent) {
    //check all link descriptors....
    List<CapPropertyDescriptor> descriptors = linkingContent.getType().getDescriptors();
    for (CapPropertyDescriptor descriptor : descriptors) {
      //..if they link the content and if they are not weak link
      if (descriptor.getType().equals(CapPropertyDescriptorType.LINK)) {
        LinkPropertyDescriptor linkPropertyDescriptor = (LinkPropertyDescriptor) descriptor;
        List<Content> links = linkingContent.getLinks(descriptor.getName());
        if (links.contains(linkedContent) && !linkPropertyDescriptor.isWeakLink()) {
          return false;
        }
      }
      else if (descriptor.getType().equals(CapPropertyDescriptorType.STRUCT)) {
        Struct struct = linkingContent.getStruct(descriptor.getName());
        if (struct != null && containsLink(struct, linkedContent)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Checks if the content is linked inside the struct
   *
   * @param struct        the struct property to check
   * @param linkedContent the content to search for
   */
  private boolean containsLink(@NonNull Struct struct, Content linkedContent) {
    Map<String, Object> properties = struct.toNestedMaps();
    List<Content> result = new ArrayList<>();
    collectStructReferences(properties, linkedContent, result);
    return !result.isEmpty();
  }

  /**
   * Helper for struct search
   */
  private void collectStructReferences(@NonNull Map<String, Object> properties,
                                       @NonNull Content linkedContent,
                                       @NonNull List<Content> result) {
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof Map) {
        Map<String, Object> nestedMap = (Map<String, Object>) value;
        collectStructReferences(nestedMap, linkedContent, result);
      }
      else if (value instanceof List) {
        List<Object> list = (List<Object>) value;
        for (Object listItem : list) {
          if (listItem instanceof Map) {
            collectStructReferences((Map<String, Object>) listItem, linkedContent, result);
          }
          else if (listItem instanceof Content) {
            if (listItem.equals(linkedContent)) {
              result.add((Content) listItem);
            }
          }
        }
      }
    }
  }


  private void updateContentName(@NonNull Content content, @NonNull String defaultName) {
    int index = 1;
    String updatedName = defaultName;
    Content parentContent = content.getParent();
    while (parentContent != null && parentContent.getChild(updatedName) != null) {
      updatedName = defaultName + " (" + index + ")";
      index++;
    }

    try {
      if (!content.getName().equals(defaultName)) {
        content.rename(updatedName);
      }
    } catch (Exception e) {
      LOG.warn("Failed to rename new taxonomy node, keeping default name (" + e.getMessage() + ")");
    }
  }

  /**
   * Content should be publish after each change.
   *
   * @param content
   */
  private void publish(@NonNull Content content) {
    try {
      if (content.isCheckedOutByCurrentSession()) {
        content.checkIn();
        approveAndPublish(content);
      }
    } catch (Exception e) {
      LOG.error("Error publishing " + content + ": " + e.getMessage(), e);
    }
  }

  /**
   * Returns the name that is used after a taxonomy has been renamed.
   * The new value of the "value" field will be used as document name too.
   *
   * @param content The content to rename.
   * @return The new document name or the original one if the "value" field is empty.
   */
  @NonNull
  private String getTaxonomyDocumentName(@NonNull Content content) {
    // rename content
    String name = content.getString(VALUE);
    if (!StringUtils.isEmpty(name)) {
      name = name.replace('/', '_');
      String formattingName = name;
      int renamingIndex = 0;
      while (content.getParent() != null && content.getParent().getChildDocumentsByName().containsKey(formattingName)) {
        renamingIndex++;
        formattingName = name + "(" + renamingIndex + ")";
      }
      return formattingName;
    }
    return content.getName();
  }

  /**
   * Returns the content for the given reference.
   *
   * @param ref given reference
   * @return content
   */
  @Nullable
  private Content getContent(@NonNull String ref) {
    return contentRepository.getContent(TaxonomyUtil.asContentId(ref));
  }

  /**
   * Approves and publishes the given content, used
   * when a taxonomy content has been changed.
   *
   * @param content The content to approve and publish.
   */
  private void approveAndPublish(@NonNull Content content) {
    try {
      if (content.isFolder()) {
        return;
      }

      LOG.info("Publishing taxonomy node {}", content);
      PublicationService publisher = contentRepository.getPublicationService();
      Version checkedInVersion = content.getCheckedInVersion();
      publisher.approve(checkedInVersion);
      publisher.approvePlace(content);
      //publish the folder containing the content
      Content parentFolder = content.getParent();
      if (!publisher.isPublished(parentFolder)) {
        publisher.approvePlace(parentFolder);
        publisher.publish(parentFolder);
      }
      publisher.publish(content);
    } catch (Exception e) {
      LOG.error("Publication of taxonomy node '" + content + "' failed.", e);
    }
  }

  private void buildPathRecursively(@NonNull Content content, @NonNull List<Content> path) {
    path.add(0, content);
    Content parent = getParent(content);
    if (parent != null && !path.contains(parent)) {
      buildPathRecursively(parent, path);
    }
  }

  /**
   * Returns the first referrer of the given content to determine the path
   * of a taxonomy node.
   *
   * @param content The taxonomy content to search the referrer for.
   * @return parent content
   */
  @Nullable
  private Content getParent(@NonNull Content content) {
    return content.getReferrerWithDescriptorFulfilling(taxonomyContentType.getName(), CHILDREN, "isInProduction");
  }


  /**
   * Converts the given list of nodes to a taxonomy node list representation.
   *
   * @param contents The contents to create the list for.
   * @param offset   The offset value if used or -1.
   * @param count    The count of the items if used or -1.
   * @param addRoot  If true, the root is added to the node list, used when a path is build as list.
   * @return The taxonomy node list representation.
   */
  @NonNull
  protected TaxonomyNodeList asNodeList(@NonNull List<Content> contents, int offset, int count, boolean addRoot) {
    List<TaxonomyNode> nodes = new ArrayList<>();
    //used for path info
    if (addRoot) {
      nodes.add(getRoot());
    }

    int totalSize = contents.size();
    List<Content> contentList = new ArrayList<>(contents);
    if (offset > -1 && count > -1) {
      int lastIndex = offset + count;
      if (lastIndex > totalSize) {
        lastIndex = totalSize;
      }
      contentList = contents.subList(offset, lastIndex);
    }

    for (Content c : contentList) {
      TaxonomyNode n = asNode(c);
      nodes.add(n);
    }
    return new TaxonomyNodeList(nodes);
  }

  /**
   * Converts a content object to a taxonomy node instance.
   *
   * @param content The content object to convert.
   * @return The taxonomy node representation.
   */
  @NonNull
  protected TaxonomyNode asNode(@NonNull Content content) {
    TaxonomyNode node = createEmptyNode();
    node.setName(content.getString(VALUE));
    if (Strings.isNullOrEmpty(node.getName())) {
      node.setName(content.getName());
    }
    node.setRef(TaxonomyUtil.asNodeRef(content.getId()));
    node.setExtendable(true);
    node.setSiteId(getSiteId());
    node.setType(taxonomyContentType.getName());
    node.setLeaf(getValidChildren(content, 1).isEmpty());
    List<Content> path = new ArrayList<>();
    buildPathRecursively(content, path);
    node.setLevel(path.size());
    return node;
  }

  @NonNull
  protected Content asContent(@NonNull TaxonomyNode node) {
    Content content = contentRepository.getContent(TaxonomyUtil.asContentId(node.getRef()));
    if (content == null) {
      throw new IllegalStateException(String.format("Given TaxonomyNode with id %s can not be converted to a Content", node.getTaxonomyId()));
    }
    return content;
  }

  /**
   * Filters deleted or destroyed children of the taxonomy.
   *
   * @param content given content
   * @return valid children
   */
  @NonNull
  private List<Content> getValidChildren(@NonNull Content content) {
    return getValidChildren(content, 0);
  }

  @NonNull
  private List<Content> getValidChildren(@NonNull Content content, int limit) {
    String query = "TYPE " + taxonomyContentType.getName() + " :isInProduction";
    if (limit > 0) {
      query = query + " LIMIT " + limit;
    }

    return content.getLinksFulfilling(CHILDREN, query);
  }

  /**
   * Creates a list of top level nodes.
   *
   * @return list of top level nodes
   */
  @NonNull
  private TaxonomyNodeList getTopLevel() {
    List<TaxonomyNode> list = new ArrayList<>();
    List<Content> topLevelContent = findRootNodes(rootFolder);
    for (Content c : topLevelContent) {
      if (TaxonomyUtil.isTaxonomy(c, taxonomyContentType)) {
        list.add(asNode(c));
      }
    }
    return new TaxonomyNodeList(list);
  }

  /**
   * Recursively collects the nodes from the taxonomy that have no parent
   *
   * @param folder  The folder to lookup keywords in.
   * @return List<Content>
   */
  @NonNull
  private List<Content> findRootNodes(@NonNull Content folder) {
    List<Content> rootNodesFromSettings = getRootNodesFromSettings();
    if (rootNodesFromSettings != null) {
      return rootNodesFromSettings;
    }

    String query = "TYPE " + taxonomyContentType.getName() + " :isInProduction";
    Collection<Content> nodes = folder.getChildrenFulfilling(query);
    List<Content> matches = new ArrayList<>();
    for (Content child : nodes) {
      if (getParent(child) == null
          && !contentRepository.getPublicationService().isToBeDeleted(child)) { //NOSONAR
        matches.add(child);
      }
    }
    return matches;
  }

  /**
   * Recursively collects the nodes from the taxonomy that have no parent
   *
   * @param folder  The folder to lookup keywords in.
   * @param matches
   */
  private void findAll(@NonNull Content folder, @NonNull List<Content> matches) {
    Collection<Content> nodes = folder.getChildren();
    for (Content child : nodes) {
      if (child.isFolder()) {
        findAll(child, matches);
      }
      else if (!child.isDeleted()
              && !child.isDestroyed()
              && !taxonomyCycleValidator.isCyclic(child, taxonomyContentType)
              && !contentRepository.getPublicationService().isToBeDeleted(child)) {
        matches.add(child);
      }
    }
  }
}
