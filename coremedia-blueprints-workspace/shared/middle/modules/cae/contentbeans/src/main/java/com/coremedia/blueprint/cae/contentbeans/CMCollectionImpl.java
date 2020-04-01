package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.feeds.FeedFormat;
import com.coremedia.blueprint.common.feeds.FeedSource;
import com.coremedia.blueprint.common.util.ContainerFlattener;
import com.coremedia.xml.MarkupUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Generated extension class for immutable beans of document type "CMCollection".
 */
public class CMCollectionImpl<T> extends CMCollectionBase<T> {
  /**
   * implements {@link FeedSource#getItemsFlattened()}
   * <p>
   * Unlike the name suggests, this method does NOT work transitively or
   * recursively, since this is not expressible with generics: We cannot cast
   * or check our T against the nested CMCollection's T.  Therefore it is up
   * to subclasses with concrete types to refine this method.
   * <p>
   * Making this method abstract or throwing an UnsupportedOperationException
   * would be more appropriate, but for compatibility reasons we preserve this
   * lame implementation.
   *
   * @return the same as {@link #getItems()}
   */
  @Override
  public List<T> getItemsFlattened() {
    return getItems();
  }

  @Override
  public FeedFormat getFeedFormat() {
    // determine the target feed format
    FeedFormat configuredFeedFormat = FeedFormat.Rss_2_0; // RSS is the default format
    String formatSetting = getSettingsService().setting("site.rss.format", String.class, this);
    if (FeedFormat.Atom_1_0.toString().equals(formatSetting)) {
      configuredFeedFormat = FeedFormat.Atom_1_0;
    }
    return configuredFeedFormat;
  }

  @Override
  public List<T> getFeedItems() {
    return getItems();
  }

  @Override
  public String getFeedTitle() {
    return StringUtils.isNotBlank(getTeaserTitle()) ? getTeaserTitle() : StringUtils.EMPTY;
  }

  @Override
  public String getFeedDescription() {
    String description = null;
    if(getTeaserText() != null) {
      description = MarkupUtil.asPlainText(getTeaserText());
    }
    else if(getDetailText() != null) {
      description = MarkupUtil.asPlainText(getDetailText());
    }

    return description;
  }

  /**
   * implements Container#getFlattenedItems()
   * <p>
   * Returns the flattened items of the items property and possible CMCollection
   * entries.
   *
   * @return the flattened items
   */
  @Override
  public List<?> getFlattenedItems() {
    return ContainerFlattener.flatten(this, Object.class);
  }
}
