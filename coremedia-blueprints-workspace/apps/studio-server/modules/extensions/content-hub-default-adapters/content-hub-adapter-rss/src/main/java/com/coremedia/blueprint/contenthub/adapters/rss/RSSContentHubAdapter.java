package com.coremedia.blueprint.contenthub.adapters.rss;

import com.coremedia.contenthub.api.ContentHubAdapter;
import com.coremedia.contenthub.api.ContentHubContext;
import com.coremedia.contenthub.api.ContentHubObject;
import com.coremedia.contenthub.api.ContentHubObjectId;
import com.coremedia.contenthub.api.ContentHubTransformer;
import com.coremedia.contenthub.api.Folder;
import com.coremedia.contenthub.api.Item;
import com.coremedia.contenthub.api.column.ColumnProvider;
import com.coremedia.contenthub.api.exception.ContentHubException;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


class RSSContentHubAdapter implements ContentHubAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(RSSContentHubAdapter.class);

  private final RSSContentHubSettings settings;
  private final String connectionId;
  private final SyndFeed feed;
  private final RSSColumnProvider columnProvider;

  RSSContentHubAdapter(RSSContentHubSettings settings, String connectionId) {
    this.settings = settings;
    this.connectionId = connectionId;
    columnProvider = new RSSColumnProvider();
    String url = settings!=null ? settings.getUrl() : null;
    if (url == null) {
      String msg = "Error reading url for RSS stream with id:" + connectionId;
      LOGGER.error(msg);
      throw new ContentHubException(msg);
    }

    try {
      String proxyHost = settings.getProxyHost();
      String proxyType = settings.getProxyType();
      Integer proxyPort = settings.getProxyPort();

      SyndFeedInput input = new SyndFeedInput();

      URL feedSource = new URL(url);

      if (proxyType != null && proxyHost != null && proxyPort != null) {
        Proxy proxy = new Proxy(Proxy.Type.valueOf(proxyType.toUpperCase()), new InetSocketAddress(proxyHost, proxyPort));
        URLConnection urlConnection = feedSource.openConnection(proxy);
        XmlReader.setDefaultEncoding("utf8");
        XmlReader reader = new XmlReader(urlConnection);
        feed = input.build(reader);
      } else {
        XmlReader.setDefaultEncoding("utf8");
        XmlReader reader = new XmlReader(feedSource);
        feed = input.build(reader);
      }
    } catch (Exception e) {
      String plainXML = getFeedXML(url);
      String msg = "Error reading RSS stream '" + url + "': " + e.getMessage() + ", feed XML:\n" + plainXML;
      LOGGER.error(msg);
      throw new ContentHubException(msg);
    }
  }

  @NonNull
  @Override
  public Folder getRootFolder(@NonNull ContentHubContext context) throws ContentHubException {
    String displayName = settings.getDisplayName();
    if (StringUtils.isEmpty(displayName)) {
      displayName = settings.getUrl();
    }
    ContentHubObjectId rootId = new ContentHubObjectId(connectionId, connectionId);
    return new RSSFolder(rootId, feed, displayName);
  }

  @Nullable
  @Override
  public Item getItem(@NonNull ContentHubContext context, @NonNull ContentHubObjectId id) throws ContentHubException {
    List<SyndEntry> entries = feed.getEntries();
    String externalId = id.getExternalId();
    for (SyndEntry entry : entries) {
      if (entry.getUri().equals(externalId)) {
        return new RSSItem(id, feed, entry);
      }
    }
    return null;
  }

  @Nullable
  @Override
  public Folder getFolder(@NonNull ContentHubContext context, @NonNull ContentHubObjectId id) throws ContentHubException {
    return getRootFolder(context);
  }

  @NonNull
  @Override
  public List<Item> getItems(@NonNull ContentHubContext context, @NonNull Folder folder) throws ContentHubException {
    List<Item> result = new ArrayList<>();
    List<SyndEntry> entries = feed.getEntries();
    for (SyndEntry entry : entries) {
      ContentHubObjectId id = new ContentHubObjectId(connectionId, entry.getUri());
      result.add(new RSSItem(id, feed, entry));
    }
    return result;
  }

  @NonNull
  @Override
  public List<Folder> getSubFolders(@NonNull ContentHubContext context, @NonNull Folder folder) throws ContentHubException {
    return Collections.emptyList();
  }

  @Nullable
  @Override
  public Folder getParent(@NonNull ContentHubContext context, @NonNull ContentHubObject contentHubObject) throws ContentHubException {
    if (!contentHubObject.getId().equals(getRootFolder(context).getId())) {
      return getRootFolder(context);
    }
    return null;
  }

  @Override
  @NonNull
  public ContentHubTransformer transformer() {
    return new RSSContentHubTransformer();
  }


  //------------------------ Helper ------------------------------------------------------------------------------------

  /**
   * Just a helper to detect what went wrong when parsing RSS feed.
   * E.g. the result may be a 301 moved permanently or contain a special character
   * that is not parsable, even with utf8.
   */
  private String getFeedXML(String url) {
    StringBuilder response = new StringBuilder();
    try {
      URLConnection connection = new URL(url).openConnection();
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
        response.append(System.getProperty("line.separator"));
      }
      in.close();
    } catch (Exception e) {
      //ignore
    }
    return response.toString();
  }

  @NonNull
  @Override
  public ColumnProvider columnProvider() {
    return columnProvider;
  }
}
