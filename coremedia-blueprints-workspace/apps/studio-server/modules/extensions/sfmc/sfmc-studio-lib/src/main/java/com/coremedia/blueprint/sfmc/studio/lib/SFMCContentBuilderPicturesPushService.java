package com.coremedia.blueprint.sfmc.studio.lib;

import com.coremedia.blueprint.base.sfmc.contentlib.contentbuilder.push.ContentBuilderPushStateListener;
import com.coremedia.blueprint.base.sfmc.contentlib.contentbuilder.push.SFMCContentBuilderPushService;
import com.coremedia.blueprint.base.sfmc.libservices.contentbuilder.documents.SFMCCategory;
import com.coremedia.blueprint.base.sfmc.libservices.context.SFMCContext;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
import com.coremedia.cap.content.Content;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

/**
 * {@link SFMCContentBuilderPushService} responsible for pushing lists of Content#pictures into SFMC <br/>
 *
 */
public class SFMCContentBuilderPicturesPushService implements SFMCContentBuilderPushService {

  private static final String PICTURES_PROPERTY_NAME = "pictures";
  private static final String DATA_PROPERTY_NAME = "data";

  @Autowired
  @Qualifier("sfmcContentBuilderImagePushService")
  SFMCContentBuilderPushService sfmcContentBuilderImagePushService;

  @Override
  public void push(@NonNull SFMCContext context,
                   @NonNull SFMCCategory category,
                   @NonNull Content content,
                   @NonNull String propertyName,
                   @Nullable ContentBuilderPushStateListener contentBuilderPushStateListener) {

    if (!isPushable(content, propertyName) || sfmcContentBuilderImagePushService == null) {
      return;
    }

    List<Content> pictures = content.getLinks(PICTURES_PROPERTY_NAME);
    for (Content picture : pictures) {
      sfmcContentBuilderImagePushService.push(context, category, picture, DATA_PROPERTY_NAME, contentBuilderPushStateListener);
    }

    notifyItemPushed(contentBuilderPushStateListener);
  }

  @Override
  public boolean isPushable(@NonNull Content content, @NonNull String propertyName) {
    if (sfmcContentBuilderImagePushService == null || !propertyName.equals(PICTURES_PROPERTY_NAME)) {
      return false;
    }

    CapPropertyDescriptor descriptor = content.getType().getDescriptor(propertyName);
    return descriptor != null && descriptor.getType().equals(CapPropertyDescriptorType.LINK);
  }

  private void notifyItemPushed(@Nullable ContentBuilderPushStateListener contentBuilderPushStateListener) {
    if (contentBuilderPushStateListener == null) {
      return;
    }

    contentBuilderPushStateListener.itemPushed();
  }

  @Override
  public int calculateAdditionalPartsCount(@NonNull Content content, @NonNull String propertyName) {
    List<Content> pictures = content.getLinks(PICTURES_PROPERTY_NAME);
    int additionalPartsCount = 0;
    for (Content picture : pictures) {
      additionalPartsCount += sfmcContentBuilderImagePushService.calculateAdditionalPartsCount(picture, DATA_PROPERTY_NAME);
    }
    return additionalPartsCount;
  }
}
