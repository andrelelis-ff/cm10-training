package com.coremedia.blueprint.contenthub.adapters.rss;

import com.coremedia.contenthub.api.ContentCreationUtil;
import com.coremedia.contenthub.api.ContentHubAdapter;
import com.coremedia.contenthub.api.ContentHubContext;
import com.coremedia.contenthub.api.ContentHubObject;
import com.coremedia.contenthub.api.ContentHubTransformer;
import com.coremedia.contenthub.api.ContentModel;
import com.coremedia.contenthub.api.ContentModelReference;
import com.coremedia.contenthub.api.Item;
import com.coremedia.contenthub.api.UrlBlobBuilder;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class RSSContentHubTransformer implements ContentHubTransformer {
  @Override
  @NonNull
  public ContentModel transform(Item item, ContentHubAdapter contentHubAdapter, ContentHubContext contentHubContext) {
    if (!(item instanceof RSSItem)) {
      throw new IllegalArgumentException("Not my item: " + item);
    }
    return transformRssItem((RSSItem)item);
  }

  @Override
  @Nullable
  public ContentModel resolveReference(ContentHubObject owner, ContentModelReference reference, ContentHubAdapter contentHubAdapter, ContentHubContext contentHubContext) {
    String imageUrl = (String) reference.getData();
    String imageName = ContentCreationUtil.extractNameFromUrl(imageUrl);
    if (imageName == null) {
      return null;
    }
    ContentModel referenceModel = ContentModel.createReferenceModel(imageName, reference.getCoreMediaContentType());
    referenceModel.put("data", new UrlBlobBuilder(owner, "rssPicture").withUrl(imageUrl).withEtag().build());
    referenceModel.put("title", "Image " + imageName);

    return referenceModel;
  }


  // --- internal ---------------------------------------------------

  @NonNull
  private ContentModel transformRssItem(RSSItem item) {
    ContentModel contentModel = ContentModel.createContentModel(item.getRssEntry().getTitle(), item.getId(), item.getCoreMediaContentType());
    contentModel.put("title", item.getName());
    String description = extractDescription(item);
    if (description != null) {
      contentModel.put("detailText", ContentCreationUtil.convertStringToRichtext(description));
    }

    SyndEntry rssEntry = item.getRssEntry();
    List<String> imageUrls = FeedImageExtractor.extractImageUrls(rssEntry);
    List<ContentModelReference> refs = new ArrayList<>();
    for (String imageUrl : imageUrls) {
      ContentModelReference contentModelRef = ContentModelReference.create(contentModel, "CMPicture", imageUrl);
      refs.add(contentModelRef);
    }
    contentModel.put("pictures", refs);

    return contentModel;
  }

  @Nullable
  private String extractDescription(@Nullable RSSItem rssItem) {
    SyndEntry rssEntry = rssItem==null ? null : rssItem.getRssEntry();
    SyndContent syndContent = rssEntry==null ? null : rssEntry.getDescription();
    return syndContent==null ? null : syndContent.getValue();
  }
}
