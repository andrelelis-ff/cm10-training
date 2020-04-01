package com.coremedia.blueprint.feeder.populate;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.feeder.MutableFeedable;
import com.coremedia.cap.multisite.ContentSiteAspect;
import com.coremedia.cap.multisite.SitesService;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

public class LanguageFeedablePopulatorTest {

  @Test
  public void testLocale() {
    final boolean[] covered = new boolean[] {false};

    MutableFeedable mutableFeedable = new MutableFeedableImpl() {
      @Override
      public void setStringElement(String s, String s1, Map<String, ?> stringMap) {
        assertEquals("unexpected field", LanguageFeedablePopulator.SOLR_LANGUAGE_FIELD_NAME, s);
        assertEquals("unexpected language", "fr", s1);
        covered[0] = true;
      }
    };

    ContentType contentType = Mockito.mock(ContentType.class);
    Mockito.when(contentType.isSubtypeOf("CMLocalized")).thenReturn(true);

    Content content = Mockito.mock(Content.class);
    Mockito.when(content.getType()).thenReturn(contentType);

    SitesService sitesService = Mockito.mock(SitesService.class);
    ContentSiteAspect contentSiteAspect = Mockito.mock(ContentSiteAspect.class);
    Mockito.when(sitesService.getContentSiteAspect(content)).thenReturn(contentSiteAspect);
    Mockito.when(contentSiteAspect.getLocale()).thenReturn(Locale.FRANCE);

    LanguageFeedablePopulator lfp = new LanguageFeedablePopulator();
    lfp.setSitesService(sitesService);
    lfp.populate(mutableFeedable, content);

    assertTrue(covered[0]);
  }

  @Test
  public void testIrrelevantDoctype() {
    MutableFeedable mutableFeedable = new MutableFeedableImpl() {
      @Override
      public void setStringElement(String s, String s1, Map<String, ?> stringMap) {
        fail("Not supposed to be invoked for non-CMLocalized content");
      }
    };

    ContentType contentType = Mockito.mock(ContentType.class);
    Mockito.when(contentType.isSubtypeOf("CMLocalized")).thenReturn(false);

    Content content = Mockito.mock(Content.class);
    Mockito.when(content.getType()).thenReturn(contentType);

    LanguageFeedablePopulator lfp = new LanguageFeedablePopulator();
    lfp.populate(mutableFeedable, content);
  }

  @Test
  public void testNull() {
    LanguageFeedablePopulator lfp = new LanguageFeedablePopulator();
    try {
      lfp.populate(null, null);
      fail("Illegal arguments are ignored silently");
    } catch (IllegalArgumentException e) {
      // (/)
    } catch (Exception e) {
      fail("Wrong exception " + e + ", expected IllegalArgumentException.");
    }
  }

}
