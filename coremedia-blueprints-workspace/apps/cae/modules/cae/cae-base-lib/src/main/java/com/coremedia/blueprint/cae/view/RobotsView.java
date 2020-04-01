package com.coremedia.blueprint.cae.view;

import com.coremedia.blueprint.base.links.UriConstants;
import com.coremedia.blueprint.cae.handlers.NavigationSegmentsUriHelper;
import com.coremedia.blueprint.cae.sitemap.SitemapHelper;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.robots.RobotsBean;
import com.coremedia.blueprint.common.robots.RobotsEntry;
import com.coremedia.cap.multisite.Site;
import com.coremedia.objectserver.view.TextView;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Programmed view that renders a given {@link RobotsBean} as Robots.txt
 */
public class RobotsView implements TextView {

  private static final String COLON_SPACE = ": ";
  private static final String NEWLINE = System.getProperty("line.separator");

  private static final Logger LOG = LoggerFactory.getLogger(RobotsView.class);

  private LinkFormatter linkFormatter;
  private SitemapHelper sitemapHelper;

  @Required
  public void setLinkFormatter(LinkFormatter linkFormatter) {
    this.linkFormatter = linkFormatter;
  }

  @Required
  public void setSitemapHelper(SitemapHelper sitemapHelper) {
    this.sitemapHelper = sitemapHelper;
  }

  @Override
  public void render(Object self, String view, Writer writer, @NonNull HttpServletRequest request,
                     @NonNull HttpServletResponse response) {
    if (!(self instanceof RobotsBean)) {
      throw new IllegalArgumentException("Not of type [" + RobotsBean.class + "]");
    }

    RobotsBean robotsBean = (RobotsBean) self;

    LOG.debug("Generating robots.txt for [{}]", robotsBean.getRootChannel().getContent().getName());

    try {
      renderRobotsNode(robotsBean, writer, request, response);
      response.setContentType(UriConstants.ContentTypes.CONTENT_TYPE_TEXT);

      writer.flush();

    } catch (IOException ex) {
      LOG.error("writing failed due to [{}]",ex.getMessage());
    }
  }

  private void renderRobotsNode(RobotsBean robotsBean, Writer writer, @NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response) throws IOException {
    for (RobotsEntry robotsEntry : robotsBean.getRobotsEntries()) {
      if (!Strings.isNullOrEmpty(robotsEntry.getUserAgent())) {
        LOG.debug("Generating robots.txt user agent node for [{}]", robotsEntry.getUserAgent());
        writeUserAgentNode(writer, robotsEntry, request, response);
      }
    }

    // The Blueprint features one robots.txt per web presence
    // (s. Apache rewrite rules).
    // So we add sitemap entries for all sites of the web presence.
    writeSitemapEntriesRecursively(writer, rootSite(robotsBean.getSite()));
  }

  private void writeUserAgentNode(Writer writer, RobotsEntry robotsEntry, @NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response) throws IOException {
    // writing user agent line:
    StringBuilder sb = new StringBuilder();
    sb.append(RobotsEntry.USER_AGENT_TAG).append(COLON_SPACE).append(robotsEntry.getUserAgent()).append(NEWLINE);
    writer.write(sb.toString());

    // writing disallow lines:
    for (CMLinkable link : robotsEntry.getDisallowed()) {

      if (link != null) {
        String entry = generateLinkEntries(RobotsEntry.DISALLOW_TAG, link, request, response);
        writer.write(entry);
      }
    }

    // writing allow lines:
    for (CMLinkable link : robotsEntry.getAllowed()) {

      if (link != null) {
        String entry = generateLinkEntries(RobotsEntry.ALLOW_TAG, link, request, response);
        writer.write(entry);
      }
    }

    // writing custom lines:
    for (String custom : robotsEntry.getCustom()) {

      if (!Strings.isNullOrEmpty(custom)) {
        sb = new StringBuilder().append(custom).append(NEWLINE);
        writer.write(sb.toString());
      }
    }

    writer.write(NEWLINE);
  }

  private void writeSitemapEntriesRecursively(Writer writer, Site master) throws IOException {
    if (master !=null) {
      writeSitemapEntry(writer, master);
      for (Site site : master.getDerivedSites()) {
        writeSitemapEntriesRecursively(writer, site);
      }
    }
  }

  private void writeSitemapEntry(Writer writer, Site site) throws IOException {
    if (sitemapHelper.isSitemapEnabled(site)) {
      writer.write(RobotsEntry.SITEMAP_TAG);
      writer.write(COLON_SPACE);
      writer.write(sitemapHelper.sitemapIndexUrl(site));
      writer.write(NEWLINE);
    }
  }

  private static Site rootSite(Site site) {
    Site master = site==null ? null : site.getMasterSite();
    return master==null ? site : rootSite(master);
  }

  private String generateLinkEntries(String tag, CMLinkable link, @NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response) {
    StringBuilder sb = new StringBuilder();
    String url = linkFormatter.formatLink(link, null, request, response, false);

    // bots need to have a trailing '/' to recognize a path, thus channels have to be added as their direct link
    // as well as with a trailing '/':
    if (link instanceof CMNavigation) {
      sb.append(tag).append(COLON_SPACE).append(url).append(NEWLINE);                                   // link to self
      sb.append(tag).append(COLON_SPACE).append(url).append(NavigationSegmentsUriHelper.SEGMENT_DELIM); // trailed
    } else {
      sb.append(tag).append(COLON_SPACE).append(url);
    }

    sb.append(NEWLINE);
    return sb.toString();
  }
}
