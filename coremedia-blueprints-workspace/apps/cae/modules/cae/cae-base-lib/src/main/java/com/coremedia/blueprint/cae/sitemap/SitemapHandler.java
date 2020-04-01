package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.objectserver.web.HandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_SEGMENTS;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;

@RequestMapping
public class SitemapHandler {
  static final String ACTION_NAME = "sitemap";
  private static final String SITEMAP_PATH = "sitemapPath";
  private static final String URI_PATTERN = "/" + PREFIX_SERVICE + "/" + ACTION_NAME + "/{" + SITEMAP_PATH + ":" + PATTERN_SEGMENTS + "}";

  private static final Logger LOG = LoggerFactory.getLogger(SitemapHandler.class);
  private String sitemapDirectory;
  private CapConnection capConnection;


  // --- Construct and configure ------------------------------------

  @Required
  public void setSitemapDirectory(String sitemapDirectory) {
    this.sitemapDirectory = sitemapDirectory;
  }

  @Required
  public void setCapConnection(CapConnection capConnection) {
    this.capConnection = capConnection;
  }


  // --- Handler ----------------------------------------------------

  @GetMapping({URI_PATTERN})
  public ModelAndView handleRequest(@PathVariable(SITEMAP_PATH) String sitemapPath) {
    try {
      File sitemapFile = new File(sitemapDirectory);
      sitemapFile = new File(sitemapFile, sitemapPath);
      if (!sitemapFile.canRead() || !sitemapFile.isFile()) {
        LOG.debug("Sitemap file " + sitemapFile.getAbsolutePath() + " has been requested but is not available.");
        return HandlerHelper.notFound();
      }
      Blob blob = capConnection.getBlobService().fromURL(urlOf(sitemapFile));
      return HandlerHelper.createModel(blob);
    } catch (Exception e) {
      LOG.error("Cannot handle sitemap request for " + sitemapPath, e);
      return HandlerHelper.notFound();
    }
  }


  // --- internal ---------------------------------------------------

  private static URL urlOf(File sitemapFile) {
    try {
      return sitemapFile.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException("Cannot create URL of file " + sitemapFile.getAbsolutePath(), e);
    }
  }

}
