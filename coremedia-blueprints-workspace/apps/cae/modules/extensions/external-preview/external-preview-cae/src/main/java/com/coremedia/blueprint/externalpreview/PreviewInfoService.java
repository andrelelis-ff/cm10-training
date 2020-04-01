package com.coremedia.blueprint.externalpreview;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the tokens and their data to preview.
 */
public final class PreviewInfoService {
  //the max amount of tokens to be stored as preview data.
  private static final int MAX_TOKENS = 1000;

  //linked hash map to reduce the amount of tokens stored in the CAE.
  private Map<String, PreviewInfoItem> token2PreviewData = new RemoveOldestLinkHashMap<>();

  private static final PreviewInfoService INSTANCE = new PreviewInfoService();

  public static PreviewInfoService getInstance() {
    return INSTANCE;
  }

  private PreviewInfoService() {
    //force singleton.
  }

  /**
   * Stores the preview information for the given token.
   * @param token The token to store the preview data for.
   * @param data The preview information.
   */
  public void applyPreview(String token, String data) {
    PreviewInfoItem info = new PreviewInfoItem(token, data);
    token2PreviewData.put(token, info);
  }

  /**
   * Returns the preview data for the given token.
   * @param token The token to retrieve the preview data for.
   * @return The info item that contains the preview data.
   */
  public PreviewInfoItem getPreviewInfo(String token) {
    cleanupCheck();
    if(token == null) {
      return null;
    }
    if (token2PreviewData.containsKey(token)) {
      return token2PreviewData.get(token);
    }
    return null;
  }

  /**
   * Removes invalid entries from the service registry.
   */
  private void cleanupCheck() {
    Iterator<PreviewInfoItem> it = token2PreviewData.values().iterator();
    while (it.hasNext()) {
      PreviewInfoItem item = it.next();
      if (!item.isValid()) {
        token2PreviewData.remove(item.getToken());
      }
    }
  }

  /**
   * Removes the preview info from the preview data store.
   * @param token The token to remove.
   */
  public void removePreview(String token) {
    if(token != null && token2PreviewData.containsKey(token)) {
      token2PreviewData.remove(token);
    }
  }

  /**
   * Static inner class for handling the overwritten "removeEldestEntry" method.
   * @param <String>
   * @param <PreviewInfoItem>
   */
  static class RemoveOldestLinkHashMap<String, PreviewInfoItem> extends LinkedHashMap<String, PreviewInfoItem> {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, PreviewInfoItem> eldest) {
      return size() > MAX_TOKENS;
    }
  }
}
