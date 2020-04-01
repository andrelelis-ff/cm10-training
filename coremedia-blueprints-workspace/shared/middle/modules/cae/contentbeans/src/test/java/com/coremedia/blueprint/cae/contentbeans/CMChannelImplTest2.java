package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.common.layout.PageGridService;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CMChannelImplTest2 {
  @Mock
  private PageGridService pageGridService;

  @Mock
  private PageGrid pageGrid;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private ValidationService validationService;

  @Mock
  private Content channelContent;

  private CMChannelImpl testling;

  @Before
  public void setup() {
    testling = new PartiallyMockedCMChannelImpl(channelContent, contentBeanFactory);
    testling.setPageGridService(pageGridService);
    testling.setValidationService(validationService);

    when(validationService.filterList(any(List.class))).thenReturn(Collections.emptyList());

    when(pageGridService.getContentBackedPageGrid(any(CMChannelImpl.class))).thenReturn(pageGrid);
    List contents = Collections.singletonList(channelContent);
    when(pageGrid.getMainItems()).thenReturn(contents);

    when(channelContent.isInstanceOf(CMTeasable.NAME)).thenReturn(true);
    when(contentBeanFactory.createBeanFor(channelContent, CMTeasable.class)).thenReturn(testling);
  }

  @Test
  public void testGetPictureDetectRecursion() {
    CMPicture picture = testling.getPicture();
    assertNull(picture);
  }


  // --- internal ---------------------------------------------------

  private class PartiallyMockedCMChannelImpl extends CMChannelImpl {
    private ContentBeanFactory contentBeanFactory;
    private Content content;

    public PartiallyMockedCMChannelImpl(Content content, ContentBeanFactory contentBeanFactory) {
      this.content = content;
      this.contentBeanFactory = contentBeanFactory;
    }

    @Override
    public Content getContent() {
      return content;
    }

    @Override
    public ContentBeanFactory getContentBeanFactory() {
      return contentBeanFactory;
    }

    @Override
    public String toString() {
      return "An almost realistic CMChannelImpl";
    }
  }
}
