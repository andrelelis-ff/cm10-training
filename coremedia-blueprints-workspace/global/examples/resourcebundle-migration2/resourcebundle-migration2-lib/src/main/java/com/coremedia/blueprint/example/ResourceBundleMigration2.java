package com.coremedia.blueprint.example;

import com.coremedia.cap.Cap;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.publication.PublicationService;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ResourceBundleMigration2 extends AbstractUAPIClient {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceBundleMigration2.class);

  private static final String SIMULATE_PARAMETER_SHORT = "s";
  private static final String SIMULATE_PARAMETER_LONG = "simulate";
  private static final String NEW_NAME_POSTFIX = "AsResourceBundle";

  private boolean simulate = false;

  private Collection<Content> allOldBundles = new HashSet<>();
  private Collection<Content> allNewBundles = new HashSet<>();
  private Collection<Content> allContentsWithBundles = new HashSet<>();

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
    return "usage: cm resourcebundle-migration2 -u admin -p admin [--simulate]";
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
    main(new ResourceBundleMigration2(), args);
  }

  @Override
  protected void run() {
    LOG.info("Change resource bundles from CMSettings to CMResourceBundle... {}", simulate ? "(simulation mode)" : "");
    allContentsWithBundles = fetchContentsWithBundles();
    for (Content content : allContentsWithBundles) {
      migrate(content);
    }
    if (!simulate) {
      publishAndCleanup();
    }
    report();
    LOG.info("Changed resource bundles from CMSettings to CMResourceBundle! {}", simulate ? "(simulation mode)" : "");
  }

  private void report() {
    LOG.info("Migration concerns the resource bundle referencing contents:");
    for (Content content : allContentsWithBundles) {
      LOG.info("  {}", content);
    }
    LOG.info("... and the resource bundles:");
    for (Content content : allOldBundles) {
      LOG.info("  {}", content);
    }
  }

  private Collection<Content> fetchContentsWithBundles() {
    QueryService queryService = getContentRepository().getQueryService();
    Collection<Content> docs = queryService.poseContentQuery("TYPE CMLinkable: BELOW PATH '/Sites' AND resourceBundles IS NOT NULL");
    LOG.info("Found {} documents with resource bundles.", docs.size());
    return docs;
  }

  private void migrate(Content content) {
    LOG.info("Migrate content {}...", content.getName());
    List<Content> newBundles = new ArrayList<>();
    List<Content> oldBundles = content.getLinks("resourceBundles");
    allOldBundles.addAll(oldBundles);
    if (!simulate) {
      for (Content oldBundle : oldBundles) {
        newBundles.add(asCMResourceBundle(oldBundle));
      }
      ensureCheckedOutByMe(content);
      content.set("resourceBundles2", newBundles);
      content.set("resourceBundles", Collections.emptyList());
      content.checkIn();
    }
  }

  private Content asCMResourceBundle(Content oldBundle) {
    Content bundle = getContentRepository().getChild(oldBundle.getPath()+NEW_NAME_POSTFIX);
    return bundle!=null ? bundle : createResourceBundle(oldBundle);
  }

  private Content createResourceBundle(Content oldBundle) {
    ContentRepository repo = getContentRepository();
    Map<String, ?> propertyValues = Collections.singletonMap("localizations", oldBundle.getStruct("settings"));
    Content folder = oldBundle.getParent();
    String newName = oldBundle.getName()+NEW_NAME_POSTFIX;
    ContentType newType = repo.getContentType("CMResourceBundle");
    Content bundle = repo.createChild(folder, newName, newType, propertyValues);
    Content oldMaster = oldBundle.getLink("master");
    if (oldMaster!=null) {
      bundle.set("master", asCMResourceBundle(oldMaster));
    }
    allNewBundles.add(bundle);
    return bundle;
  }

  private void publishAndCleanup() {
    PublicationService publicationService = getContentRepository().getPublicationService();
    publicationService.publish(publicationService.createPublicationSet(allNewBundles));
    publicationService.publish(publicationService.createPublicationSet(allContentsWithBundles));
    for (Content oldBundle : allOldBundles) {
      publicationService.toBeWithdrawn(oldBundle);
      publicationService.approvePlace(oldBundle);
    }
    publicationService.publish(publicationService.createPublicationSet(allOldBundles));
    for (Content oldBundle : allOldBundles) {
      oldBundle.delete();
    }
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
