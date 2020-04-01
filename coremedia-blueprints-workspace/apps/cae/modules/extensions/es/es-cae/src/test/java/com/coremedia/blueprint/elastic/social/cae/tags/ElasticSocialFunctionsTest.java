package com.coremedia.blueprint.elastic.social.cae.tags;

import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.elastic.social.cae.guid.GuidCookieHandler;
import com.coremedia.cap.content.Content;
import com.coremedia.elastic.social.api.users.CommunityUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElasticSocialFunctionsTest {

  @Mock
  private CommunityUser communityUser;

  @Mock
  private CMTeasable cmTeasable;

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private Content content;

  @Test
  public void isAnonymous() {
    when(communityUser.isAnonymous()).thenReturn(true);
    assertTrue(ElasticSocialFunctions.isAnonymous(communityUser));
  }

  @Test
  public void isActivated() {
    when(communityUser.isActivated()).thenReturn(true);
    assertTrue(ElasticSocialFunctions.isActivated(communityUser));
  }

  @Test
  public void isActivatedRequiresModeration() {
    when(communityUser.isActivated()).thenReturn(false);
    when(communityUser.isActivatedAndRequiresModeration()).thenReturn(true);
    assertTrue(ElasticSocialFunctions.isActivated(communityUser));
  }

  @Test
  public void getCurrentGuid() {
    GuidCookieHandler.setCurrentGuid("1234+5");
    String guid = GuidCookieHandler.getCurrentGuid();
    assertEquals("1234+5", guid);
  }

  @Test
  public void escapeJavaScript() {
    String escapedString = ElasticSocialFunctions.escapeJavaScript("a\nb");
    assertEquals("a\\nb", escapedString);
  }
}
