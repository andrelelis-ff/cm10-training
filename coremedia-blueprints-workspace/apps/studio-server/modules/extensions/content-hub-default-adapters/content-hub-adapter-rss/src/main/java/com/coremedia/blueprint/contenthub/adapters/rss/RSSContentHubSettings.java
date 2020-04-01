package com.coremedia.blueprint.contenthub.adapters.rss;


/**
 * Interface that marks the settings that are needed for a connection to RSS
 */
interface RSSContentHubSettings {
  /**
   * @return the url to the Rss Post. In order to receive the pictures for a Rss Post
   * the URL will be accessed with the {@link java.net.URLConnection}.
   */
  String getUrl();

  /**
   * @return the name
   */
  String getDisplayName();

  /**
   * @return the Proxy Host for your Rss Connection
   */
  String getProxyHost();

  /**
   * @return a value of {@link java.net.Proxy}#type
   */
  String getProxyType();

  /**
   * @return a proxy port for your Rss Connection
   */
  Integer getProxyPort();
}
