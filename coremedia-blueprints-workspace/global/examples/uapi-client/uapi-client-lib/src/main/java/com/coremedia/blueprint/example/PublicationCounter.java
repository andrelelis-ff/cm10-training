package com.coremedia.blueprint.example;

import com.coremedia.cap.Cap;
import com.coremedia.cap.common.DateConverter;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.publication.PublicationService;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cmdline.AbstractUAPIClient;
import org.apache.commons.cli.*;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.*;

/**
 * This client counts all publications for a given content type and a given date in the past up to today. Default
 * interval is set to the last 24 hours.
 */
public class PublicationCounter extends AbstractUAPIClient {

  private static final String AFTER_PARAMETER_SHORT = "a";
  private static final String AFTER_PARAMETER_LONG = "after";
  private static final String CONTENT_TYPE_PARAMETER_SHORT = "t";
  private static final String CONTENT_TYPE_PARAMETER_LONG = "content-type";
  private static final int AFTER_VALUE_DEFAULTOFFSETDAYS = 1;
  private static final int MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;

  private String contentType;
  private Calendar now;
  private Calendar after;

  public PublicationCounter() {
    now = Calendar.getInstance();
    now.setTime(new Date());
  }

  @Override
  protected void fillInOptions(Options options) {
    options.addOption(OptionBuilder.hasArg()
            .withDescription("Content type to be counted (required)")
            .withLongOpt(CONTENT_TYPE_PARAMETER_LONG)
            .create(CONTENT_TYPE_PARAMETER_SHORT));
    options.addOption(OptionBuilder.hasArg()
            .withDescription("starting date of publications to be counted. Format is "
                    + AbstractUAPIClient.DATEFORMAT_DEFAULT)
            .withLongOpt(AFTER_PARAMETER_LONG)
            .create(AFTER_PARAMETER_SHORT));

  }

  @NonNull
  @Override
  protected String getUsage() {
    return "cm count-publications -u <user> [other options] --" + CONTENT_TYPE_PARAMETER_LONG
            + " <content type> --" + AFTER_PARAMETER_LONG + " <startdate> (" + AbstractUAPIClient.DATEFORMAT_DEFAULT + ")";
  }

  @Override
  protected boolean parseCommandLine(CommandLine commandLine) {
    contentType = commandLine.getOptionValue(CONTENT_TYPE_PARAMETER_SHORT);
    after = Calendar.getInstance();
    after.setTime(parseDateParameter(commandLine, AFTER_PARAMETER_SHORT, DATEFORMAT_DEFAULT,
            new Date(System.currentTimeMillis() - (AFTER_VALUE_DEFAULTOFFSETDAYS * MILLISECONDS_PER_DAY))));
    if (!after.before(now)) {
      getLogger().error("Start date of search interval must not lie ahead");
      return false;
    }
    getLogger().info("Counting publications for content type {} published after {}", contentType, DateConverter.convertToString(after.getTimeInMillis()));
    return true;
  }

  @Override
  protected void fillInConnectionParameters(Map<String,Object> params) {
    super.fillInConnectionParameters(params);
    params.put(Cap.USE_WORKFLOW, "false");
  }

  @Override
  protected void run() {
    Map<String, ContentType> contentTypesByName = getContentRepository().getContentTypesByName();
    if (contentTypesByName.containsKey(contentType)) {

      QueryService queryService = getContentRepository().getQueryService();
      PublicationService publicationService = getContentRepository().getPublicationService();

      Collection<Content> result = queryService.poseContentQuery("TYPE " + contentType
              + ": ?0 <= publicationDate AND publicationDate <= ?1",
              after, now);

      if (!result.isEmpty()) {
        if (verbose) {
          for (Content content : result) {
            Calendar publicationDate = publicationService.getPublicationDate(content);
            getLogger().info("Counting: ContentType = {} - Id = {} - Name = {} - Path = {} - Publication Date = {}",
                    content.getType(), content.getId(), content.getName(), content.getPath(),
                    DateConverter.convertToString(publicationDate.getTimeInMillis()));
          }
        }
        getLogger().info("Counted {} Documents, published after {}", result.size(), DateConverter.convertToString(after.getTimeInMillis()));
      } else {
        getLogger().info("No matching content found");
      }
    } else {
      getLogger().error("Repository does not contain a content type {}", contentType);
    }

  }

  public static void main(String[] args) {
    main(new PublicationCounter(), args);
  }
}
