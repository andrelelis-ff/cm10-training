package com.coremedia.livecontext.studio.asset;

import com.coremedia.blueprint.base.livecontext.util.CommerceReferenceHelper;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.events.ContentEvent;
import com.coremedia.ecommerce.studio.rest.cache.CommerceCacheInvalidationSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CommerceReferenceHelper.class)
public class AssetInvalidationRepositoryListenerTest {

  @Mock
  private CommerceCacheInvalidationSource invalidationSource;

  @Mock
  private ContentType cmPictureType;

  @Mock
  private Content content;

  @Mock
  private ContentEvent event;

  @Mock
  private ContentRepository repository;

  private AssetInvalidationRepositoryListener testling;

  @Before
  public void setUp() {
    testling = new AssetInvalidationRepositoryListener(invalidationSource, repository);

    mockStatic(CommerceReferenceHelper.class);

    testling.start();

    when(event.getType()).thenReturn(ContentEvent.CONTENT_CREATED);
    when(event.getContent()).thenReturn(content);
    when(content.getRepository()).thenReturn(repository);
    when(content.getType()).thenReturn(cmPictureType);
    when(cmPictureType.isSubtypeOf(CMPicture.NAME)).thenReturn(true);
  }

  @Test
  public void testHandleContentEvent() {
    // content has any external references
    List<String> externalReferences = newArrayList("vendor:///catalog/product/what", "vendor:///catalog/product/ever");
    when(CommerceReferenceHelper.getExternalReferences(content)).thenReturn(externalReferences);

    testling.handleContentEvent(event);

    // then all products and product variants should be invalidated.
    verify(invalidationSource).invalidateReferences(newHashSet(externalReferences));
  }
}
