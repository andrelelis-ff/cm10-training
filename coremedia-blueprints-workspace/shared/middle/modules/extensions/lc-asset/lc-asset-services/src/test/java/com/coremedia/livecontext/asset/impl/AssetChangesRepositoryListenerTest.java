package com.coremedia.livecontext.asset.impl;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.events.ContentEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetChangesRepositoryListenerTest {

  private static final String CMVISUAL_DOCTYPE_NAME = "CMVisual";

  @InjectMocks
  private AssetChangesRepositoryListener testling = new AssetChangesRepositoryListener();

  @Mock
  private ContentRepository repository;
  @Mock
  private AssetChanges assetChanges;
  @Mock
  private ContentEvent event;
  @Mock
  private Content content;
  @Mock
  private ContentType cmPictureType;

  @Before
  public void setUp() throws Exception {
    testling.setRepository(repository);
    testling.setAssetChanges(assetChanges);
    testling.start();
    when(event.getContent()).thenReturn(content);
    when(content.getType()).thenReturn(cmPictureType);
    when(cmPictureType.isSubtypeOf(CMVISUAL_DOCTYPE_NAME)).thenReturn(true);
  }

  @Test
  public void testHandleContentEvent() throws Exception {
    testling.handleContentEvent(event);
    verify(assetChanges).update(content);
  }
}