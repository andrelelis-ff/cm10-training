package com.coremedia.blueprint.studio.topicpages.rest;

import com.coremedia.blueprint.base.rest.config.ConfigurationService;
import com.coremedia.blueprint.base.rest.config.StructConfiguration;
import com.coremedia.blueprint.taxonomies.Taxonomy;
import com.coremedia.blueprint.taxonomies.TaxonomyNode;
import com.coremedia.blueprint.taxonomies.TaxonomyResolver;
import com.coremedia.blueprint.taxonomies.TaxonomyUtil;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.CapObjectDestroyedException;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.BulkOperationFailedException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.ContentSiteAspect;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteDestroyedException;
import com.coremedia.cap.multisite.SitesService;
import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * A REST service for resolving mime types and handling
 * uploads.
 */
@RestController
@RequestMapping(value = "topicpages", produces = MediaType.APPLICATION_JSON_VALUE)
public class TopicPagesResource {
  private static final Logger LOG = LoggerFactory.getLogger(TopicPagesResource.class);

  private static final String TOPIC_PAGE_CONTENT_TYPE = "CMTaxonomy";
  private static final String TOPIC_PAGE_CUSTOM_CONTENT_TYPE = "CMChannel";
  private static final String TOPIC_PAGE_CONTEXTS = "contexts";
  private static final String TOPIC_PAGE_SITE = "site";
  private static final String CHANNEL_CHILDREN = "children";

  private static final String TOPIC_PAGES_SETTINGS = "TopicPages";
  private static final String STRUCT_PROPERTY_TOPIC_CHANNEL = "TopicPagePage";
  private static final String STRUCT_PROPERTY_GROUPS = "administrationGroups";
  private static final String STRUCT_PROPERTY_FOLDER = "folder";

  private static final String TAXONOMY_FOLDER_NAME = "Taxonomies";

  private static final int MAX_VISIBLE_ITEMS = 100;

  // This path is used, if no folder for custom topic pages is specified in the related settings
  private static final String DEFAULT_SITESPECIFIC_CUSTOM_TOPIC_PAGES_FOLDER = "Navigation/Topic Pages";

  //  This are the default groups which are enabled to use the custom topic pages administration
  private static final List<String> DEFAULT_GROUPS_ADMIN_CUSTOM_TOPIC_PAGES = new ArrayList<>();

  static {
    DEFAULT_GROUPS_ADMIN_CUSTOM_TOPIC_PAGES.add("media-administrator");
  }

  private CapConnection capConnection;
  private ConfigurationService configurationService;
  private TaxonomyResolver strategyResolver;
  private SitesService sitesService;
  private String siteConfigPath;
  private String globalConfigPath;
  private String ignoredTaxonomies;

  // --- construction -----------------------------------------------


  public TopicPagesResource(CapConnection capConnection,
                            ConfigurationService configurationService,
                            TaxonomyResolver strategyResolver,
                            SitesService sitesService,
                            String siteConfigPath,
                            String globalConfigPath) {
    this.capConnection = capConnection;
    this.configurationService = configurationService;
    this.strategyResolver = strategyResolver;
    this.sitesService = sitesService;
    this.siteConfigPath = siteConfigPath;
    this.globalConfigPath = globalConfigPath;
  }

  public void setIgnoredTaxonomies(String ignoredTaxonomies) {
    this.ignoredTaxonomies = ignoredTaxonomies;
  }

  @GetMapping("settings")
  public TopicPagesSettingsRepresentation getSettings(@RequestParam(value = TOPIC_PAGE_SITE, required = false) String siteId) { // NOSONAR  cyclomatic complexity
    TopicPagesSettingsRepresentation representation = new TopicPagesSettingsRepresentation();
    StructConfiguration config = configurationService.getStructMaps(siteId, TOPIC_PAGES_SETTINGS, "settings");
    representation.getAdminGroups().addAll(getGroupsFromStruct(config.getGlobalStructs()));

    for (String group : getGroupsFromStruct(config.getLocalStructs())) {
      if (!representation.getAdminGroups().contains(group)) {
        representation.getAdminGroups().add(group);
      }
    }

    if (representation.getAdminGroups().isEmpty()) {
      representation.getAdminGroups().addAll(DEFAULT_GROUPS_ADMIN_CUSTOM_TOPIC_PAGES);
    }

    String folder = getFolderFromStruct(config.getLocalStructs());
    if (Strings.isNullOrEmpty(folder)) {
      folder = getFolderFromStruct(config.getGlobalStructs());
    }
    if (Strings.isNullOrEmpty(folder)) {
      LOG.debug("No default topic page folder found in local or global TopicPage settings. Use default path: {}", DEFAULT_SITESPECIFIC_CUSTOM_TOPIC_PAGES_FOLDER);
      folder = DEFAULT_SITESPECIFIC_CUSTOM_TOPIC_PAGES_FOLDER;
    }

    Content topicPageChannel = getTopicChanneFromStruct(config.getLocalStructs());
    if (topicPageChannel == null) {
      topicPageChannel = getTopicChanneFromStruct(config.getGlobalStructs());
    }
    if (topicPageChannel == null) {
      String siteName = siteId;
      if (!Strings.isNullOrEmpty(siteId)) {
        Site site = sitesService.getSite(siteId);
        if (site != null) {
          siteName = site.getName() + "/" + site.getLocale();
        }
      }

      if (!Strings.isNullOrEmpty(siteId)) {
        LOG.debug("No topic page channel found in local or global TopicPage settings for site " + siteName + ". It is recommended to " +
                        "create a site specific '" + TOPIC_PAGES_SETTINGS + "' settings document with a custom topic page channel.",
                siteName, TOPIC_PAGES_SETTINGS);
      }
    }
    representation.setTopicPageChannel(topicPageChannel);

    Content siteConfigFolder = resolveSiteConfigurationFolder(siteId, folder);
    if (siteConfigFolder != null) {
      folder = siteConfigFolder.getPath();
      Content topicPagesFolder = capConnection.getContentRepository().getChild(folder);
      if (topicPagesFolder != null) {
        representation.setFolder(topicPagesFolder);
      }
    }

    return representation;
  }

  /**
   * Returns a collection of topics to display in the topic pages editor.
   *
   * @param term The search term the user has entered or null.
   * @return The representation of the search result.
   */
  @GetMapping("topics")
  public TopicsRepresentation getTopics(@RequestParam("taxonomy") String taxonomy,
                                        @RequestParam("site") String siteId,
                                        @RequestParam("term") String term) {
    TopicsRepresentation result = new TopicsRepresentation();

    String taxonomyContentId = IdHelper.formatContentId(taxonomy);
    Content taxonomyFolder = capConnection.getContentRepository().getContent(taxonomyContentId);
    Taxonomy taxonomyStrategy = findTaxonomy(siteId, taxonomyFolder.getName());

    List<TaxonomyNode> keywords;
    if (taxonomyStrategy == null) {
      keywords = Collections.emptyList();
    } else if (Strings.isNullOrEmpty(term)) {
      keywords = taxonomyStrategy.getAllChildren();
    } else {
      keywords = taxonomyStrategy.find(term).getNodes();
    }

    if (keywords.size() > MAX_VISIBLE_ITEMS) {
      keywords = keywords.subList(0, MAX_VISIBLE_ITEMS - 1);
      result.setFiltered(true);
    }

    for (TaxonomyNode node : keywords) {
      String contentId = TaxonomyUtil.asContentId(node.getRef());
      Content topic = capConnection.getContentRepository().getContent(contentId);
      if (topic.isInProduction()) {
        TopicRepresentation topicRepresentation = new TopicRepresentation(topic);
        topicRepresentation.setPage(getContextForTopic(topic, siteId));
        result.getItems().add(topicRepresentation);
      }
    }

    //sorting the result
    Collections.sort(result.getItems(), new Comparator<TopicRepresentation>() {
      @Override
      public int compare(TopicRepresentation o1, TopicRepresentation o2) {
        return o1.getName().toLowerCase(Locale.ROOT).compareTo(o2.getName().toLowerCase(Locale.ROOT));
      }
    });

    return result;
  }

  /**
   * Returns a list of all taxonomies available.
   */
  @GetMapping("taxonomies")
  public List<Content> getTaxonomies() {
    List<Content> taxonomies = new ArrayList<>();

    //gather all site depending taxonomies
    Set<Site> sites = sitesService.getSites();
    for (Site site : sites) {
      try {
        Content siteConfigFolder = site.getSiteRootFolder().getChild(siteConfigPath);
        if (siteConfigFolder != null) {
          Content taxonomyFolder = siteConfigFolder.getChild(TAXONOMY_FOLDER_NAME);
          if (taxonomyFolder != null) {
            String taxonomiesFolderPath = taxonomyFolder.getPath();
            addTaxonomy(taxonomiesFolderPath, taxonomies);
          }
        }
      } catch (CapObjectDestroyedException | SiteDestroyedException e) {
        LOG.debug("ignoring destroyed site '{}'", site.getId(), e);
      }
    }

    //gather global taxonomies
    String globalPath = globalConfigPath;
    if (!globalPath.endsWith("/")) {
      globalPath = globalPath + "/";
    }
    globalPath = globalPath + TAXONOMY_FOLDER_NAME;
    addTaxonomy(globalPath, taxonomies);
    return taxonomies;
  }

  /**
   * Finds the taxonomy strategy for the given taxonomy id and site.
   *
   * @param taxonomyId The taxonomy id
   * @param siteId     The id of the site
   * @return The taxonomy strategy for the given taxonomy id and site
   */
  @Nullable
  private Taxonomy findTaxonomy(String siteId, String taxonomyId) {
    Taxonomy taxonomyStrategy = strategyResolver.getTaxonomy(siteId, taxonomyId);
    if (taxonomyStrategy == null) {
      LOG.debug("No taxonomy strategy found for site '" + siteId + "' and taxonomy id '" + taxonomyId + "', " +
              "or taxonomy is not readable.");
    }
    return taxonomyStrategy;
  }

  /**
   * Checks the taxonomies folder for subfolders and checks each
   * subfolder if it contains a valid taxonomy keyword.
   */
  private void addTaxonomy(String path, List<Content> taxonomies) {
    Content taxonomiesFolder = this.capConnection.getContentRepository().getChild(path);
    if (taxonomiesFolder != null) {
      Set<Content> taxonomyFolders = taxonomiesFolder.getSubfolders();
      for (Content taxonomyFolder : taxonomyFolders) {
        Set<Content> keywords = taxonomyFolder.getChildrenWithType(TOPIC_PAGE_CONTENT_TYPE);
        //only non empty folder are valid
        if (!keywords.isEmpty() && isValid(taxonomyFolder)) {
          taxonomies.add(taxonomyFolder);
        }
      }
    }
  }

  /**
   * Checks if the given taxonomy folder is a valid topic page taxonomy.
   *
   * @param taxonomiesFolder the folder to check
   * @return true if the folder should be shown in the list of taxonomies
   */
  private boolean isValid(Content taxonomiesFolder) {
    if (ignoredTaxonomies != null) {
      String[] taxonomyNames = ignoredTaxonomies.split(",");
      List<String> taxonomyNameList = Arrays.asList(taxonomyNames);
      return !taxonomyNameList.contains(taxonomiesFolder.getName());
    }
    return true;
  }

  /**
   * Creates or deletes the custom page for the given content.
   *
   * @param id     The id of the keyword to create or delete the custom page for
   * @param create True, if a custom page should be generated.
   * @param site   The default site to apply the custom page for.
   * @return The updated topic representation.
   */
  @PostMapping("page")
  public TopicRepresentation updatePage(@RequestParam("id") String id,
                                        @RequestParam("create") Boolean create,
                                        @RequestParam("site") String site) {
    TopicPagesSettingsRepresentation settings = getSettings(site);
    String capId = IdHelper.formatContentId(id);
    Content topic = capConnection.getContentRepository().getContent(capId);
    String topicSiteId = resolveSite(topic, site);
    Content topicPagesFolder = getSettings(site).getFolder();
    Content context = getContextForTopic(topic, topicSiteId);
    Content siteChannel = settings.getTopicPageChannel();
    TopicRepresentation rep = new TopicRepresentation(topic, context, siteChannel, topicPagesFolder);

    if (!topic.isCheckedOut()) {
      topic.checkOut();
    }

    if (create) {
      createCustomTopicPage(rep, topicSiteId);
    } else {
      deleteCustomTopicPage(rep, topicSiteId);
    }

    if (topic.isCheckedOut()) {
      topic.checkIn();
    }

    return rep;
  }

  /**
   * Deletes the custom topic page for the given topic.
   *
   * @param rep       The representation of the topic.
   * @param topicSite The site to delete the topic for.
   */
  private void deleteCustomTopicPage(TopicRepresentation rep, String topicSite) {
    Content topic = rep.getTopic();
    Content topicPage = getContextForTopic(topic, topicSite);
    if (topicPage != null) {
      //get the context link list from the taxonomy...
      List<Content> contexts = topic.getLinks(TOPIC_PAGE_CONTEXTS);
      List<Content> updatedContexts = new ArrayList<>();
      //..remove the site context children linklist of the root channel
      updateRootChannel(rep, topicPage, false);

      //also remove the topic page from the context link list of the taxonomy
      updatedContexts.addAll(contexts);
      updatedContexts.remove(topicPage);
      topic.set(TOPIC_PAGE_CONTEXTS, updatedContexts);
      //do flush connection! otherwise the checkin/out state is not detected correctly! UAPI bug?
      topicPage.getRepository().getConnection().flush();
      if (topicPage.isCheckedOut()) {
        topicPage.checkIn();
      }

      try {
        topicPage.delete();
      } catch (BulkOperationFailedException e) {
        rep.setStatus(TopicRepresentation.STATUS_ERROR_DELETION_FAILED);
        LOG.error("Failed to delete topic page " + topicPage + ": " + e.getMessage(), e);
      }
      rep.setPage(null);
    }
  }

  /**
   * Creates a new custom topic page for the given topic.
   *
   * @param rep The topic to create the custom page for.
   */
  private void createCustomTopicPage(TopicRepresentation rep, String topicSite) {
    Content topic = rep.getTopic();
    if (rep.getPage() == null) {
      Content topicPage = findOrCreateCustomPage(rep);
      if (topicPage.isCheckedOut()) {
        topicPage.checkIn();
      }
      rep.setPage(topicPage);

      //apply the context property so that the page can be resolved
      List<Content> contexts = topic.getLinks(TOPIC_PAGE_CONTEXTS);
      List<Content> updatedContexts = new ArrayList<>();
      updatedContexts.addAll(contexts);
      updatedContexts.add(topicPage);
      topic.set(TOPIC_PAGE_CONTEXTS, updatedContexts);

      updateRootChannel(rep, topicPage, true);
    } else {
      LOG.warn("Topic '" + topic.getName() + "' already has a customized page for site '" + topicSite + "'");
      rep.setStatus(TopicRepresentation.STATUS_ERROR_CUSTOM_PAGE_EXISTS);
    }
  }

  /**
   * Updates the root channel.
   *
   * @param add If true, the topic page will be added to the root channel, removed otherwise.
   */
  private void updateRootChannel(TopicRepresentation rep, Content topicPage, boolean add) {
    Content rootSiteChannel = rep.getRootChannel();
    if (rootSiteChannel != null && (rootSiteChannel.isCheckedIn() || rootSiteChannel.isCheckedOutByCurrentSession())) {
      if (rootSiteChannel.isCheckedIn()) {
        rootSiteChannel.checkOut();
      }
      List<Content> newChildrenList = new ArrayList<>();
      List<Content> children = rootSiteChannel.getLinks(CHANNEL_CHILDREN);
      for (Content child : children) {
        if (child.isInProduction()) {
          newChildrenList.add(child);
        }
      }
      if (add) {
        newChildrenList.add(topicPage);
      } else {
        newChildrenList.remove(topicPage);
      }

      //update children link list of the root channel
      rootSiteChannel.set(CHANNEL_CHILDREN, newChildrenList);
      rootSiteChannel.getRepository().getConnection().flush();
      rootSiteChannel.checkIn();
    } else {
      rep.setStatus(TopicRepresentation.STATUS_ERROR_COULD_NOT_RESOLVE_ROOT_CHANNEL);
    }
  }

  /**
   * Well, lets just assume there is an existing topic page that hasn't been linked successfully.
   * Or the page has been unlinked. In this case try to use the existing custom page.
   * Otherwise create a new one.
   *
   * @param rep The current topic page representation.
   */
  private Content findOrCreateCustomPage(TopicRepresentation rep) {
    Content topic = rep.getTopic();
    //try to apply name matching
    Content existingCustomTopicPage = rep.getTopicPagesFolder().getChild(formatTopicPageName(topic));
    if (existingCustomTopicPage != null && existingCustomTopicPage.isInProduction()) {
      return existingCustomTopicPage;
    }

    //try to apply title matching
    for (Content child : rep.getTopicPagesFolder().getChildDocuments()) {
      String title = child.getString("title");
      if (child.getName().contains(topic.getName()) ||
              (!Strings.isNullOrEmpty(title) && title.equals(topic.getName()))) {
        return child;
      }
    }

    Map<String, Object> properties = new HashMap<>();
    // segment format: id-defaultTopicpageSegment
    int numId = IdHelper.parseContentId(topic.getId());
    properties.put("segment", numId + "-" + rep.getRootChannel().getString("segment"));
    properties.put("title", topic.getName());

    //apply the hidden flags since the topic pages are not part of the navigation
    properties.put("hidden", 1);
    properties.put("hiddenInSitemap", 1);
    ContentType type = requireNonNull(capConnection.getContentRepository().getContentType(TOPIC_PAGE_CUSTOM_CONTENT_TYPE));

    Content newTopicPage = type.createByTemplate(rep.getTopicPagesFolder(), formatTopicPageName(topic), "{3} ({1})", properties);

    //apply locale afterwards, since we now have a content site aspect
    ContentSiteAspect contentSiteAspect = sitesService.getContentSiteAspect(newTopicPage);
    Optional<Site> site = contentSiteAspect.findSite();
    Locale locale = site.map(Site::getLocale).orElse(null);
    if (locale != null) {
      contentSiteAspect.setLocale(locale);
    }

    return newTopicPage;
  }

  /**
   * Returns the name for the newly generated custom topic page.
   *
   * @param topic The taxonomy content to creat the custom topic page for.
   */
  private String formatTopicPageName(Content topic) {
    return topic.getName() + " [Topic]";
  }

  /**
   * Resolves the name of the site, depending on the location of the topic
   * and which default site setting is passed.
   */
  private String resolveSite(Content topic, String defaultSiteId) {
    Optional<Site> site = sitesService.getContentSiteAspect(topic).findSite();
    return site.map(Site::getId).orElse(defaultSiteId);
  }

  /**
   * Checks to which site each linked context of the topic belongs to.
   *
   * @param topic  The topic to lookup the site depending context for.
   * @param siteId The id of the site to context lookup is made for.
   */
  private Content getContextForTopic(Content topic, String siteId) {
    List<Content> contexts = topic.getLinks(TOPIC_PAGE_CONTEXTS);
    for (Content context : contexts) {
      if (context.isInProduction()) {
        Optional<Site> topicSite = sitesService.getContentSiteAspect(context).findSite();
        String topicSiteId = topicSite.map(Site::getId).orElse(null);
        if (topicSiteId != null && topicSiteId.equals(siteId)) {
          return context;
        }
      }
    }
    return null;
  }

  private List<String> getGroupsFromStruct(Map<String, Object> struct) {
    if (struct != null) {
      List<String> groups = (List<String>) struct.get(STRUCT_PROPERTY_GROUPS); //NOSONAR
      if (groups != null) {
        return groups;
      }
    }
    return Collections.emptyList();
  }

  private String getFolderFromStruct(Map<String, Object> struct) {
    if (struct != null) {
      return (String) struct.get(STRUCT_PROPERTY_FOLDER); //NOSONAR
    }
    return null;
  }

  private Content getTopicChanneFromStruct(Map<String, Object> struct) {
    if (struct != null) {
      return (Content) struct.get(STRUCT_PROPERTY_TOPIC_CHANNEL);
    }
    return null;
  }

  private Content resolveSiteConfigurationFolder(String siteId, String siteConfigurationPath) {
    if (!Strings.isNullOrEmpty(siteId)) {
      Site site = sitesService.findSite(siteId)
              .orElseThrow(() -> new IllegalArgumentException(format("Site '%s' does not exist.", siteId)));

      Content root = site.getSiteRootFolder();
      Content configPath = root.getChild(siteConfigurationPath);
      if (configPath == null) {
        LOG.debug("Could not resolve relative site config path '" + siteConfigurationPath + "' for site " + site.getName());
        return null;
      }
      return configPath;
    }
    return null;
  }
}
