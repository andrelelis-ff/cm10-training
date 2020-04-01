package com.coremedia.livecontext.asset.impl;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.events.ContentEvent;
import com.coremedia.cap.content.events.ContentRepositoryListenerBase;

import javax.inject.Inject;

/**
 * A {@link com.coremedia.cap.content.events.ContentRepositoryListener} that reacts on changes
 * of CMPicture documents and stores them in {@link AssetChanges}
 */
class AssetChangesRepositoryListener extends ContentRepositoryListenerBase {

  private static final String CMVISUAL_TYPE = "CMVisual";
  private static final String CMDOWNLOAD_TYPE = "CMDownload";

  private ContentRepository repository;
  private AssetChanges assetChanges;

  @Override
  protected void handleContentEvent(ContentEvent event) {
    Content content = event.getContent();
    if (!content.isDestroyed() &&
            (content.getType().isSubtypeOf(CMVISUAL_TYPE) ||
                    content.getType().isSubtypeOf(CMDOWNLOAD_TYPE))) {
      assetChanges.update(content);
    }
  }

  public void start() {
    repository.addContentRepositoryListener(this);
  }

  public void stop() {
    repository.removeContentRepositoryListener(this);
  }

  @Inject
  public void setRepository(ContentRepository repository) {
    this.repository = repository;
  }

  @Inject
  public void setAssetChanges(AssetChanges assetChanges) {
    this.assetChanges = assetChanges;
  }

}
