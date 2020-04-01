package com.coremedia.blueprint.example;

import com.coremedia.cap.Cap;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cmdline.AbstractUAPIClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ResourceBundleMigration extends AbstractUAPIClient {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceBundleMigration.class);

  private static final String SIMULATE_PARAMETER_SHORT = "s";
  private static final String SIMULATE_PARAMETER_LONG = "simulate";

  private boolean simulate = false;

  @Override
  protected void fillInOptions(Options options) {
    options.addOption(OptionBuilder
            .withDescription("Only log what would be done, do not change anything.")
            .withLongOpt(SIMULATE_PARAMETER_LONG)
            .create(SIMULATE_PARAMETER_SHORT));
  }

  @NonNull
  @Override
  protected String getUsage() {
    return "usage: cm resourcebundle-migration -u admin -p admin [--simulate]";
  }

  @Override
  protected boolean parseCommandLine(CommandLine commandLine) {
    simulate = commandLine.hasOption(SIMULATE_PARAMETER_SHORT);
    return true;
  }

  @Override
  protected void fillInConnectionParameters(Map<String,Object> params) {
    super.fillInConnectionParameters(params);
    params.put(Cap.USE_WORKFLOW, "false");
  }

  public static void main(String[] args) {
    main(new ResourceBundleMigration(), args);
  }

  @Override
  protected void run() {
    LOG.info("Migrate resource bundles... {}", simulate ? "(simulation mode)" : "");
    Collection<Content> sites = fetchSites();
    for (Content site : sites) {
      migrate(site);
    }
    LOG.info("Migrated resource bundles! {}", simulate ? "(simulation mode)" : "");
  }

  /**
   * Fetch the sites to migrate.
   */
  private Collection<Content> fetchSites() {
    QueryService queryService = getContentRepository().getQueryService();
    Collection<Content> sites = queryService.poseContentQuery("TYPE CMSite: BELOW PATH '/Sites'");
    LOG.info("Found {} sites.", sites.size());
    return sites;
  }

  /**
   * Migrate the site.
   */
  private void migrate(Content site) {
    LOG.info("Migrate site {}...", site.getName());
    Content rootChannel = site.getLink("root");
    List<Content> reallySettings = new ArrayList<>();
    List<Content> resourceBundles = new ArrayList<>();
    splitLinkedSettings(rootChannel, reallySettings, resourceBundles);
    if (!resourceBundles.isEmpty()) {
      if (!simulate) {
        update(rootChannel, reallySettings, resourceBundles);
      }
      LOG.info("Migrated site {}, root channel {}.", site.getName(), rootChannel.getId());
    } else {
      LOG.info("No changes for site {}.", site.getName());
    }
  }

  /**
   * Split the rootChannel's linkedSettings into actual settings and resource bundles
   */
  private void splitLinkedSettings(Content rootChannel, List<Content> reallySettings, List<Content> resourceBundles) {
    List<Content> linkedSettings = rootChannel.getLinks("linkedSettings");
    for (Content settings : linkedSettings) {
      if (isResourceBundle(settings)) {
        LOG.info("Found resource bundle {}", settings.getName());
        resourceBundles.add(settings);
      } else {
        reallySettings.add(settings);
      }
    }
  }

  /**
   * Update the root channel with the splitted linkedSettings and resourceBundles.
   */
  private void update(Content rootChannel, List<Content> reallySettings, List<Content> resourceBundles) {
    ensureCheckedOutByMe(rootChannel);
    rootChannel.set("linkedSettings", reallySettings);
    rootChannel.set("resourceBundles", resourceBundles);
    rootChannel.checkIn();
  }

  /**
   * Decide whether the settings document is a resource bundle.
   */
  private boolean isResourceBundle(Content content) {
    String name = content.getName();
    return name.length()>3 && name.charAt(name.length()-3)=='_';
  }

  private void ensureCheckedOutByMe(Content content) {
    if (!content.isCheckedOutByCurrentSession()) {
      if (content.isCheckedOut()) {
        content.checkIn();
      }
      content.checkOut();
    }
  }
}
