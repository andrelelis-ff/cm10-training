package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.common.contentbeans.CMExternalLink;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;

/**
 * Test for {@link ExternalLinkHandler}
 */

public class ExternalLinkHandlerTest {

  private static final String SOME_VIEW = "somePreview";
  private static final String FRAGMENT_VIEW = "fragmentPreview";

  private ExternalLinkHandler externalLinkHandler = new ExternalLinkHandler();

  @Mock
  private CMExternalLink cmExternalLink;

  @Before
  public void setUp(){
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void createLinkWithSomeView(){
    Mockito.when(cmExternalLink.getUrl()).thenReturn("someLink");
    Assert.assertNotNull(externalLinkHandler.buildLinkForExternalLink(cmExternalLink, SOME_VIEW));
  }

  @Test
  public void createLinkWithFragmentView(){
    Assert.assertNull(externalLinkHandler.buildLinkForExternalLink(cmExternalLink, FRAGMENT_VIEW));
  }

  @Test
  public void createLinkWithEmptyLink(){
    Assert.assertNull(externalLinkHandler.buildLinkForExternalLink(cmExternalLink, SOME_VIEW));
  }

}
