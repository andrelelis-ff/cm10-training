package com.coremedia.livecontext.asset.impl;

import com.coremedia.blueprint.base.livecontext.util.CommerceReferenceHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.ContentSiteAspect;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Collections.emptyList;

/**
 * In-Memory-Changelog of Commerce Assets.
 * This assures that current editorial changes (e.g. Adding externalId reference from Content to a catalog object, or
 * removal of the reference) are instantly available
 */
class AssetChanges implements RemovalListener<Content, String>, InitializingBean {

  private final ReadWriteLock rwl = new ReentrantReadWriteLock();
  /**
   * Read-lock for the maps.
   */
  private final Lock r = rwl.readLock();
  /**
   * Write-lock for the maps.
   */
  private final Lock w = rwl.writeLock();

  private Map<Site,Multimap<Content, String>> siteToMultimap = new HashMap<>();
  private Map<Site,Multimap<String, Content>> siteToInverseMultimap = new HashMap<>();

  //we are using the google cache to make the maps LRU
  private Cache<Content, String> cacheForEvict;

  private SitesService sitesService;
  private int maximumSize = 1000;
  private int expireAfterWriteInSeconds = 60*60;

  @Override
  public void afterPropertiesSet() throws Exception {
    cacheForEvict = CacheBuilder.newBuilder()
            .maximumSize(maximumSize)
            .expireAfterWrite(expireAfterWriteInSeconds, TimeUnit.SECONDS)
            .removalListener(this)
            .build();
  }

  public void update(@NonNull Content content) {
    w.lock();
    try {
      //reset the map for the given content for all sites
      remove(content);
      insert(content);
      markEmptyContent(content);
      cacheForEvict.put(content, "");

    }finally {
      w.unlock();
    }
  }

  /**
   * remove all entries from the multimap and inverseMultimap for all sites
   * because the content could have been e.g. moved
   * @param content
   */
  private void remove(Content content) {
    for (Map.Entry<Site, Multimap<Content, String>> entry : siteToMultimap.entrySet()) {
      Site site = entry.getKey();
      Multimap<Content, String> multimap = entry.getValue();
      Collection<String> ids = multimap.removeAll(content);
      Multimap<String, Content> inverseMultimap = siteToInverseMultimap.get(site);
      for (String id : ids) {
        inverseMultimap.remove(id, content);
      }
    }
  }

  private void insert(Content content) {
    ContentSiteAspect contentSiteAspect = sitesService.getContentSiteAspect(content);
    Site site = contentSiteAspect.getSite();

    if (site == null) {
      return;
    }
    //initialize the multimap if the site is non-null
    if (!siteToMultimap.keySet().contains(site)) {
      siteToMultimap.put(site, HashMultimap.<Content, String>create());
      siteToInverseMultimap.put(site, HashMultimap.<String, Content>create());
    }

    List<String> externalIds = getExternalIds(content);

    if (externalIds != null) {
      Multimap<Content, String> multimap = siteToMultimap.get(site);
      Multimap<String, Content> inverseMultimap = siteToInverseMultimap.get(site);
      multimap.putAll(content, externalIds);
      for (String externalId : externalIds) {
        inverseMultimap.put(externalId, content);
      }
    }
  }

  /**
   * once content is processed we need mark contents without asset references
   * E.g. when content is deleted we need to know about this change
   * @param content given content
   */
  private void markEmptyContent(Content content) {
    for (Map.Entry<Site, Multimap<Content, String>> entry : siteToMultimap.entrySet()) {
      Site site = entry.getKey();
      Multimap<Content, String> multimap = entry.getValue();
      Multimap<String, Content> inverseMultimap = siteToInverseMultimap.get(site);
      if (!multimap.containsKey(content)) {
        multimap.put(content, null);
      }
      if (!inverseMultimap.containsValue(content)) {
        inverseMultimap.put(null, content);
      }
    }
  }

  @VisibleForTesting
  List<String> getExternalIds(Content content) {
    return CommerceReferenceHelper.getExternalIds(content);
  }

  @NonNull
  public Collection<Content> get(String externalId, Site site) {
    if (site == null) {
      return emptyList();
    }
    r.lock();
    try {
      Multimap<String, Content> inverseMultimap = siteToInverseMultimap.get(site);
      if (inverseMultimap != null) {
        return inverseMultimap.get(externalId);
      }
    } finally {
      r.unlock();
    }
    return emptyList();
  }

  /**
   * Check if an external id is mapped for the given site and content.
   *
   * @param content content which must be checked for given external ids
   * @param externalId external id which must be checked
   * @param site the site which contains the given content
   * @return true if no mapping for site and content exist.
   *         true if the external id is mapped to the site and content, otherwise false.
   */
  public boolean isUpToDate(Content content, String externalId, Site site) {
    r.lock();
    try {
      Multimap<Content, String> multimap = siteToMultimap.get(site);
      if (multimap == null) {
        //no content is processed for this site. hence up-to-date
        return true;
      }

      if (!multimap.containsKey(content)) {
        return true;
      }

      return multimap.containsEntry(content, externalId);
    } finally {
      r.unlock();
    }
  }

  public void remove(String externalId, Site site) {
    w.lock();
    try {
      Multimap<String, Content> inversemultimap = siteToInverseMultimap.get(site);
      if (inversemultimap != null) {
        Collection<Content> contents = inversemultimap.removeAll(externalId);
        Multimap<Content, String> multimap = siteToMultimap.get(site);
        for (Content content : contents) {
          multimap.remove(content, externalId);
        }
      }
    } finally {
      w.unlock();
    }
  }

  @Override
  public void onRemoval(RemovalNotification<Content, String> notification) {
    if (notification != null && !notification.getCause().equals(RemovalCause.REPLACED)) {
      Content content = notification.getKey();
      if (content != null) {
        w.lock();
        try {
          remove(content);
        } finally {
          w.unlock();
        }
      }
    }
  }

  @Autowired
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Value("${livecontext.assetService.changeEvents.maximumSize}")
  public void setMaximumSize(int maximumSize) {
    this.maximumSize = maximumSize;
  }

  @Value("${livecontext.assetService.changeEvents.expireAfterWriteInSeconds}")
  public void setExpireAfterWriteInSeconds(int expireAfterWriteInSeconds) {
    this.expireAfterWriteInSeconds = expireAfterWriteInSeconds;
  }
}
