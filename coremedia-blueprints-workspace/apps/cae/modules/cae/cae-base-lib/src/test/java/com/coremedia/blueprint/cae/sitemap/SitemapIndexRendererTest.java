package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.blueprint.base.links.UrlPrefixResolver;
import com.coremedia.cap.multisite.Site;
import com.coremedia.xml.DelegatingSaxHandler;
import com.coremedia.xml.XmlUtil5;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import javax.xml.XMLConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SitemapIndexRendererTest {
  private static final String URL_PREFIX = "http://www.acme.com/";
  private static final String SITEMAP_ENTRY_FILENAME_1 = "sitemap1.xml.gz";
  private static final String SITEMAP_ENTRY_FILENAME_2 = "sitemap2.xml.gz";
  private static final String SITEMAP_ENTRY_FILENAME_3 = "sitemap3.xml.gz";

  private static final String SITEMAP_ENTRY_URL_1 = URL_PREFIX + "service/sitemap/thesiteid/" + SITEMAP_ENTRY_FILENAME_1;
  private static final String SITEMAP_ENTRY_URL_2 = URL_PREFIX + "service/sitemap/thesiteid/" + SITEMAP_ENTRY_FILENAME_2;
  private static final String SITEMAP_ENTRY_URL_3 = URL_PREFIX + "service/sitemap/thesiteid/" + SITEMAP_ENTRY_FILENAME_3;

  private static final String A_URL = URL_PREFIX + "path?foo=bar&bla=blub";
  private static final String XML_ESCAPED_URL = StringEscapeUtils.escapeXml(A_URL);

  private static final String FILENAME = "sitemap";
  private static final String SITEMAP_INDEX_FILENAME = "sitemap_index.xml";
  private static final String THE_SITE_ID = "thesiteid";

  private String uniqueTmpdirPostfix;

  @Mock
  Site site;

  @Mock
  UrlPrefixResolver urlPrefixResolver;

  @Mock
  SitemapHelper sitemapHelper;

  @Mock
  ServletContext servletContext;

  private SitemapIndexRenderer testling;

  @Before
  public void initTargetDirName() {
    uniqueTmpdirPostfix = System.currentTimeMillis() + "-" + Math.round(Math.random()*100000000);
    when(site.getId()).thenReturn(THE_SITE_ID);
    when(urlPrefixResolver.getUrlPrefix(THE_SITE_ID, null, null)).thenReturn("//www.acme.com");
    when(sitemapHelper.sitemapProtocol(site)).thenReturn("http");

    testling = new SitemapIndexRenderer();
    testling.setTargetDirectory(getTargetDir().getAbsolutePath());
    testling.setUrlPrefixResolver(urlPrefixResolver);
    testling.setSitemapHelper(sitemapHelper);
    testling.setPrependBaseUri(false);  // test short apache urls, the relevant case
    testling.setSite(site);
  }
/*
  @After
  public void deleteTargetDir() {
    File targetDir = getTargetDir();
    if (targetDir.exists()) {
      try {
        FileUtils.forceDelete(targetDir);
      } catch (IOException e) {
        throw new IOError(e);
      }
    }
  }*/


  @Test
  public void testSimpleSitemapIndex() {
    when(sitemapHelper.sitemapIndexEntryUrl(site, SITEMAP_ENTRY_FILENAME_1)).thenReturn(SITEMAP_ENTRY_URL_1);

    simpleSitemapIndex();
    checkSitemapIndexIn(getOutputDir());
  }

  @Test
  public void testBackup() {
    when(sitemapHelper.sitemapIndexEntryUrl(site, SITEMAP_ENTRY_FILENAME_1)).thenReturn(SITEMAP_ENTRY_URL_1);

    File backupDir = new File(getOutputDir(), FILENAME + ".bak");
    simpleSitemapIndex();
    assertFalse("Unexpected backup", backupDir.exists());
    simpleSitemapIndex();
    checkSitemapIndexIn(backupDir);
  }

  @Test
  public void testSplitSitemap() throws FileNotFoundException{
    when(sitemapHelper.sitemapIndexEntryUrl(site, SITEMAP_ENTRY_FILENAME_1)).thenReturn(SITEMAP_ENTRY_URL_1);
    when(sitemapHelper.sitemapIndexEntryUrl(site, SITEMAP_ENTRY_FILENAME_2)).thenReturn(SITEMAP_ENTRY_URL_2);
    when(sitemapHelper.sitemapIndexEntryUrl(site, SITEMAP_ENTRY_FILENAME_3)).thenReturn(SITEMAP_ENTRY_URL_3);

    testling.startUrlList();
    for (int i = 0; i < 50001; ++i) {
      testling.appendUrl(A_URL + i);
    }
    testling.endUrlList();
    testling.getResponse();

    checkSitemapIndexIn(getOutputDir());

    String sitemapIndex = fileToString(new File(getOutputDir(), SITEMAP_INDEX_FILENAME));
    assertTrue("Unexpected index content", sitemapIndex.contains("<loc>" + SITEMAP_ENTRY_URL_1 +"</loc>"));
    assertTrue("Unexpected index content", sitemapIndex.contains("<loc>" + SITEMAP_ENTRY_URL_2 +"</loc>"));
    assertFalse("Unexpected index content", sitemapIndex.contains("<loc>" + SITEMAP_ENTRY_URL_3 +"</loc>"));

    String sitemap = gzipToString(new File(getOutputDir(), FILENAME + "1.xml.gz"));
    assertTrue("Unexpected sitemap content", sitemap.contains("<url><loc>" + XML_ESCAPED_URL + "0</loc></url>"));
    assertTrue("Unexpected sitemap content", sitemap.contains("<url><loc>" + XML_ESCAPED_URL + "49999</loc></url>"));
    assertFalse("Unexpected sitemap content", sitemap.contains("<url><loc>" + XML_ESCAPED_URL + "50000</loc></url>"));

    sitemap = gzipToString(new File(getOutputDir(), FILENAME + "2.xml.gz"));
    assertTrue("Unexpected sitemap content", sitemap.contains("<url><loc>" + XML_ESCAPED_URL + "50000</loc></url>"));
    assertFalse("Unexpected sitemap content", sitemap.contains("<url><loc>" + XML_ESCAPED_URL + "49999</loc></url>"));
  }


  // --- internal ---------------------------------------------------

  private void simpleSitemapIndex() {
    testling.startUrlList();
    testling.appendUrl(A_URL);
    testling.endUrlList();
    testling.getResponse();
  }

  private void checkSitemapIndexIn(File targetDir) {
    assertTrue("No directory " + targetDir.getAbsolutePath(), targetDir.isDirectory());
    File sitemapIndexFile = new File(targetDir, SITEMAP_INDEX_FILENAME);
    assertTrue("No index file in " + targetDir.getAbsolutePath(), sitemapIndexFile.exists());
    validate(sitemapIndexFile);
    String sitemapIndex = fileToString(sitemapIndexFile);
    assertTrue("Unexpected content", sitemapIndex.contains("<loc>" + SITEMAP_ENTRY_URL_1 +"</loc>"));
    assertTrue("No sitemap file in " + targetDir.getAbsolutePath(), new File(targetDir, SITEMAP_ENTRY_FILENAME_1).exists());
  }

  private String fileToString(File file) {
    try {
      return IOUtils.toString(file.toURI().toURL(), "UTF-8");
    } catch (IOException e) {
      // Must not happen, escalate
      throw new IOError(e);
    }
  }

  private String gzipToString(File file) throws FileNotFoundException {
    FileInputStream fileInputStream = new FileInputStream(file);
    GZIPInputStream gzipInputStream = null;
    try {
      gzipInputStream = new GZIPInputStream(fileInputStream);
      return IOUtils.toString(gzipInputStream, "UTF-8");
    } catch (IOException e) {
      // Must not happen, escalate
      throw new IOError(e);
    } finally {
      IOUtils.closeQuietly(gzipInputStream);
      IOUtils.closeQuietly(fileInputStream);
    }
  }

  private File getTargetDir() {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    return new File(tmpDir, "coremedia-" + uniqueTmpdirPostfix);
  }

  private File getOutputDir() {
    return new File(getTargetDir(), THE_SITE_ID);
  }

  private void validate(File sitemapIndex) {
    try {
      XmlUtil5 xmlutil = new XmlUtil5();
      xmlutil.registerSchema("http://www.sitemaps.org/schemas/sitemap/0.9",
              XMLConstants.W3C_XML_SCHEMA_NS_URI,
              new String[]{"http://www.sitemaps.org/schemas/sitemap/0.9/siteindex.xsd"});
      InputStream inputStream = new FileInputStream(sitemapIndex);
      try {
        // DelegatingSaxHandler for strict validation
        xmlutil.saxParse(inputStream, new DelegatingSaxHandler(null, null, null), null, "http://www.sitemaps.org/schemas/sitemap/0.9");
      } finally {
        IOUtils.closeQuietly(inputStream);
      }
    } catch (Exception e) {
      throw new RuntimeException("Sitemap validation failed", e);
    }
  }
}
